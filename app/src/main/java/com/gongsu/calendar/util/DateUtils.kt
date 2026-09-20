package com.gongsu.calendar.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toKey(date: LocalDate): String = date.format(ISO)

    fun fromKey(key: String): LocalDate = LocalDate.parse(key, ISO)

    /** 해당 월 캘린더 그리드에 표시할 날짜 목록 (일요일 시작, 6주 x 7일 = 42칸). */
    fun buildMonthGrid(yearMonth: YearMonth): List<LocalDate> {
        val first = yearMonth.atDay(1)
        // DayOfWeek.SUNDAY = 7 in java.time; we want offset from Sunday
        val firstDowValue = first.dayOfWeek.value % 7 // Sunday(7)->0, Monday(1)->1 ... Saturday(6)->6
        val start = first.minusDays(firstDowValue.toLong())
        return (0 until 42).map { start.plusDays(it.toLong()) }
    }

    fun formatMoney(amount: Long): String =
        NumberFormat.getNumberInstance(Locale.KOREA).format(amount)

    fun formatGongsu(g: Double): String {
        return if (g == g.toLong().toDouble()) String.format(Locale.KOREA, "%.1f", g)
        else String.format(Locale.KOREA, "%.1f", g)
    }
}
