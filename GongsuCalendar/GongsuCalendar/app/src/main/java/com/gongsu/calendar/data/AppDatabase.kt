package com.gongsu.calendar.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** 하루 기록. 날짜(yyyy-MM-dd)당 1건. groupId 0 = 그룹없음 */
data class WorkEntry(
    val date: String,
    val gongsu: Double,
    val unitPrice: Long,
    val isHoliday: Boolean,
    val isSettled: Boolean,
    val groupId: Long,
    val memo: String
) {
    val totalAmount: Long
        get() = if (isHoliday) 0L else Math.round(gongsu * unitPrice)
}

data class WorkGroup(val id: Long, val name: String)

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "gongsu.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE work_entries (" +
                "date TEXT PRIMARY KEY, " +
                "gongsu REAL NOT NULL, " +
                "unit_price INTEGER NOT NULL, " +
                "holiday INTEGER NOT NULL, " +
                "settled INTEGER NOT NULL, " +
                "group_id INTEGER NOT NULL, " +
                "memo TEXT NOT NULL)"
        )
        db.execSQL("CREATE TABLE work_groups (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun toEntry(c: Cursor): WorkEntry = WorkEntry(
        date = c.getString(0),
        gongsu = c.getDouble(1),
        unitPrice = c.getLong(2),
        isHoliday = c.getInt(3) == 1,
        isSettled = c.getInt(4) == 1,
        groupId = c.getLong(5),
        memo = c.getString(6)
    )

    private fun toValues(e: WorkEntry): ContentValues {
        val cv = ContentValues()
        cv.put("date", e.date)
        cv.put("gongsu", e.gongsu)
        cv.put("unit_price", e.unitPrice)
        cv.put("holiday", if (e.isHoliday) 1 else 0)
        cv.put("settled", if (e.isSettled) 1 else 0)
        cv.put("group_id", e.groupId)
        cv.put("memo", e.memo)
        return cv
    }

    // ---------- 기록 ----------
    fun getRange(start: String, end: String): List<WorkEntry> {
        val list = ArrayList<WorkEntry>()
        readableDatabase.rawQuery(
            "SELECT $COLS FROM work_entries WHERE date BETWEEN ? AND ? ORDER BY date",
            arrayOf(start, end)
        ).use { c ->
            while (c.moveToNext()) list.add(toEntry(c))
        }
        return list
    }

    fun getAllEntries(): List<WorkEntry> = getRange("0000-01-01", "9999-12-31")

    fun getEntry(date: String): WorkEntry? {
        var result: WorkEntry? = null
        readableDatabase.rawQuery(
            "SELECT $COLS FROM work_entries WHERE date = ?",
            arrayOf(date)
        ).use { c ->
            if (c.moveToFirst()) result = toEntry(c)
        }
        return result
    }

    fun upsert(e: WorkEntry) {
        writableDatabase.insertWithOnConflict(
            "work_entries", null, toValues(e), SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun deleteEntry(date: String) {
        writableDatabase.delete("work_entries", "date = ?", arrayOf(date))
    }

    // ---------- 일괄변경 (기록된 날짜만 적용) ----------
    private fun bulk(start: String, end: String, cv: ContentValues) {
        writableDatabase.update("work_entries", cv, "date BETWEEN ? AND ?", arrayOf(start, end))
    }

    fun bulkUpdateMemo(start: String, end: String, memo: String) {
        val cv = ContentValues()
        cv.put("memo", memo)
        bulk(start, end, cv)
    }

    fun bulkUpdateGongsu(start: String, end: String, gongsu: Double) {
        val cv = ContentValues()
        cv.put("gongsu", gongsu)
        bulk(start, end, cv)
    }

    fun bulkUpdateUnitPrice(start: String, end: String, price: Long) {
        val cv = ContentValues()
        cv.put("unit_price", price)
        bulk(start, end, cv)
    }

    fun bulkUpdateSettled(start: String, end: String, settled: Boolean) {
        val cv = ContentValues()
        cv.put("settled", if (settled) 1 else 0)
        bulk(start, end, cv)
    }

    // ---------- 그룹 ----------
    fun getGroups(): List<WorkGroup> {
        val list = ArrayList<WorkGroup>()
        readableDatabase.rawQuery("SELECT id, name FROM work_groups ORDER BY id", null).use { c ->
            while (c.moveToNext()) list.add(WorkGroup(c.getLong(0), c.getString(1)))
        }
        return list
    }

    fun addGroup(name: String): Long {
        val cv = ContentValues()
        cv.put("name", name)
        return writableDatabase.insert("work_groups", null, cv)
    }

    fun deleteGroup(id: Long) {
        val db = writableDatabase
        db.delete("work_groups", "id = ?", arrayOf(id.toString()))
        val cv = ContentValues()
        cv.put("group_id", 0L)
        db.update("work_entries", cv, "group_id = ?", arrayOf(id.toString()))
    }

    // ---------- 백업 복구 ----------
    fun replaceAll(entries: List<WorkEntry>, groups: List<WorkGroup>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("work_entries", null, null)
            db.delete("work_groups", null, null)
            for (g in groups) {
                val cv = ContentValues()
                cv.put("id", g.id)
                cv.put("name", g.name)
                db.insert("work_groups", null, cv)
            }
            for (e in entries) {
                db.insertWithOnConflict("work_entries", null, toValues(e), SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val COLS = "date, gongsu, unit_price, holiday, settled, group_id, memo"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: AppDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}
