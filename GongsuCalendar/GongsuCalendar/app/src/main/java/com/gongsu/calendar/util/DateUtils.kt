package com.gongsu.calendar.util

import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

object DateUtils {
    fun toKey(d: LocalDate): String = d.toString()

    fun fromKey(k: String): LocalDate = LocalDate.parse(k)

    /** 일요일 시작, 주 단위로 채운 달력 칸 목록 */
    fun buildMonthGrid(month: YearMonth): List<LocalDate> {
        val first = month.atDay(1)
        val offset = first.dayOfWeek.value % 7 // 일=0, 월=1 ... 토=6
        val start = first.minusDays(offset.toLong())
        val cells = ((offset + month.lengthOfMonth() + 6) / 7) * 7
        val list = ArrayList<LocalDate>()
        for (i in 0 until cells) list.add(start.plusDays(i.toLong()))
        return list
    }

    fun formatMoney(v: Long): String = String.format(Locale.US, "%,d", v)

    fun formatGongsu(g: Double): String {
        val r = Math.round(g * 100) / 100.0
        return r.toString()
    }
}
