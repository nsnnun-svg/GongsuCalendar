package com.gongsu.calendar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkEntryDao {

    @Query("SELECT * FROM work_entry WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun observeRange(start: String, end: String): Flow<List<WorkEntry>>

    @Query("SELECT * FROM work_entry WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getRange(start: String, end: String): List<WorkEntry>

    @Query("SELECT * FROM work_entry WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): WorkEntry?

    @Query("SELECT * FROM work_entry ORDER BY date ASC")
    suspend fun getAll(): List<WorkEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WorkEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<WorkEntry>)

    @Delete
    suspend fun delete(entry: WorkEntry)

    @Query("DELETE FROM work_entry WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM work_entry")
    suspend fun deleteAll()

    @Query("UPDATE work_entry SET memo = :memo WHERE date BETWEEN :start AND :end")
    suspend fun bulkUpdateMemo(start: String, end: String, memo: String)

    @Query("UPDATE work_entry SET gongsu = :gongsu, isHoliday = 0 WHERE date BETWEEN :start AND :end")
    suspend fun bulkUpdateGongsu(start: String, end: String, gongsu: Double)

    @Query("UPDATE work_entry SET unitPrice = :unitPrice WHERE date BETWEEN :start AND :end")
    suspend fun bulkUpdateUnitPrice(start: String, end: String, unitPrice: Long)

    @Query("UPDATE work_entry SET isSettled = :settled WHERE date BETWEEN :start AND :end")
    suspend fun bulkUpdateSettled(start: String, end: String, settled: Boolean)
}
