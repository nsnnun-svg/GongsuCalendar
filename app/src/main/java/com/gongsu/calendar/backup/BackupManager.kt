package com.gongsu.calendar.backup

import android.content.Context
import android.net.Uri
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.data.GroupEntity
import com.gongsu.calendar.data.WorkEntry
import org.json.JSONArray
import org.json.JSONObject

/**
 * 전체 데이터(근무기록 + 그룹)를 JSON 문자열로 내보내거나, JSON으로부터 복구한다.
 * 파일 저장/열기는 Storage Access Framework(ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT)를
 * 호출하는 Activity 쪽에서 처리하고, 이 클래스는 문자열 <-> DB 변환만 담당한다.
 */
class BackupManager(private val context: Context) {

    private val db = AppDatabase.getInstance(context)

    suspend fun exportToJson(): String {
        val entries = db.workEntryDao().getAll()
        val groups = db.groupDao().getAll()

        val entriesArray = JSONArray()
        entries.forEach { e ->
            entriesArray.put(
                JSONObject().apply {
                    put("date", e.date)
                    put("gongsu", e.gongsu)
                    put("isHoliday", e.isHoliday)
                    put("unitPrice", e.unitPrice)
                    put("memo", e.memo)
                    put("groupId", e.groupId ?: JSONObject.NULL)
                    put("isSettled", e.isSettled)
                }
            )
        }

        val groupsArray = JSONArray()
        groups.forEach { g ->
            groupsArray.put(
                JSONObject().apply {
                    put("id", g.id)
                    put("name", g.name)
                    put("colorHex", g.colorHex)
                }
            )
        }

        return JSONObject().apply {
            put("version", 1)
            put("entries", entriesArray)
            put("groups", groupsArray)
        }.toString()
    }

    suspend fun importFromJson(json: String) {
        val root = JSONObject(json)
        val groupsArray = root.optJSONArray("groups") ?: JSONArray()
        val newGroups = mutableListOf<GroupEntity>()
        for (i in 0 until groupsArray.length()) {
            val o = groupsArray.getJSONObject(i)
            newGroups += GroupEntity(
                id = o.getLong("id"),
                name = o.getString("name"),
                colorHex = o.optString("colorHex", "#2E7D32")
            )
        }
        newGroups.forEach { db.groupDao().upsert(it) }

        val entriesArray = root.optJSONArray("entries") ?: JSONArray()
        val newEntries = mutableListOf<WorkEntry>()
        for (i in 0 until entriesArray.length()) {
            val o = entriesArray.getJSONObject(i)
            newEntries += WorkEntry(
                date = o.getString("date"),
                gongsu = o.optDouble("gongsu", 0.0),
                isHoliday = o.optBoolean("isHoliday", false),
                unitPrice = o.optLong("unitPrice", 0L),
                memo = o.optString("memo", ""),
                groupId = if (o.isNull("groupId")) null else o.optLong("groupId"),
                isSettled = o.optBoolean("isSettled", false)
            )
        }
        db.workEntryDao().upsertAll(newEntries)
    }

    fun writeToUri(uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    fun readFromUri(uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use { input ->
            return input.readBytes().toString(Charsets.UTF_8)
        }
        return "{}"
    }
}
