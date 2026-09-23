package com.gongsu.calendar.backup

import android.content.Context
import android.net.Uri
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.data.WorkEntry
import com.gongsu.calendar.data.WorkGroup
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(private val context: Context) {

    fun exportToJson(): String {
        val db = AppDatabase.getInstance(context)
        val root = JSONObject()
        root.put("version", 1)

        val groups = JSONArray()
        for (g in db.getGroups()) {
            val o = JSONObject()
            o.put("id", g.id)
            o.put("name", g.name)
            groups.put(o)
        }
        root.put("groups", groups)

        val entries = JSONArray()
        for (e in db.getAllEntries()) {
            val o = JSONObject()
            o.put("date", e.date)
            o.put("gongsu", e.gongsu)
            o.put("unitPrice", e.unitPrice)
            o.put("holiday", e.isHoliday)
            o.put("settled", e.isSettled)
            o.put("groupId", e.groupId)
            o.put("memo", e.memo)
            entries.put(o)
        }
        root.put("entries", entries)
        return root.toString(2)
    }

    /** 파일 내용을 전부 읽어 검사한 뒤에만 기존 데이터를 교체한다 */
    fun importFromJson(json: String) {
        val root = JSONObject(json)

        val groups = ArrayList<WorkGroup>()
        val ga = root.optJSONArray("groups")
        if (ga != null) {
            for (i in 0 until ga.length()) {
                val o = ga.getJSONObject(i)
                groups.add(WorkGroup(o.getLong("id"), o.getString("name")))
            }
        }

        val entries = ArrayList<WorkEntry>()
        val ea = root.getJSONArray("entries")
        for (i in 0 until ea.length()) {
            val o = ea.getJSONObject(i)
            entries.add(
                WorkEntry(
                    date = o.getString("date"),
                    gongsu = o.getDouble("gongsu"),
                    unitPrice = o.getLong("unitPrice"),
                    isHoliday = o.optBoolean("holiday", false),
                    isSettled = o.optBoolean("settled", false),
                    groupId = o.optLong("groupId", 0L),
                    memo = o.optString("memo", "")
                )
            )
        }
        AppDatabase.getInstance(context).replaceAll(entries, groups)
    }

    fun writeToUri(uri: Uri, text: String) {
        context.contentResolver.openOutputStream(uri)?.use {
            it.write(text.toByteArray(Charsets.UTF_8))
        }
    }

    fun readFromUri(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use {
            String(it.readBytes(), Charsets.UTF_8)
        } ?: ""
    }
}
