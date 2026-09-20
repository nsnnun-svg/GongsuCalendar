package com.gongsu.calendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 하루 단위 근무 기록.
 * date 는 "yyyy-MM-dd" 형식의 문자열이며 기본 키로 사용한다 (하루 1건).
 */
@Entity(tableName = "work_entry")
data class WorkEntry(
    @PrimaryKey val date: String,
    val gongsu: Double = 0.0,      // 공수 (0.5, 1.0, 1.5, 2.0 ...)
    val isHoliday: Boolean = false, // 휴 여부
    val unitPrice: Long = 0L,       // 단가
    val memo: String = "",
    val groupId: Long? = null,
    val isSettled: Boolean = false
) {
    val totalAmount: Long
        get() = if (isHoliday) 0L else (gongsu * unitPrice).toLong()
}
