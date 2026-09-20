package com.gongsu.calendar.util

import com.gongsu.calendar.prefs.AppPrefs
import kotlin.math.roundToLong

enum class TaxType { NONE, DAILY_WORK, FOUR_INSURANCE, BUSINESS_INCOME }

data class TaxLine(val label: String, val amount: Long)

data class TaxResult(
    val gross: Long,
    val lines: List<TaxLine>,
    val totalDeduction: Long,
    val net: Long
)

object TaxCalculator {

    fun calculate(gross: Long, type: TaxType, prefs: AppPrefs): TaxResult {
        val lines = mutableListOf<TaxLine>()

        when (type) {
            TaxType.NONE -> { /* 공제 없음 */ }

            TaxType.DAILY_WORK -> {
                val incomeTax = pct(gross, prefs.dailyIncomeTaxRate)
                val localTax = pct(incomeTax, prefs.dailyLocalIncomeTaxRate)
                val empIns = pct(gross, prefs.dailyEmploymentInsuranceRate)
                lines += TaxLine("소득세", incomeTax)
                lines += TaxLine("지방소득세", localTax)
                lines += TaxLine("고용보험", empIns)
            }

            TaxType.FOUR_INSURANCE -> {
                val pension = pct(gross, prefs.nationalPensionRate)
                val health = pct(gross, prefs.healthInsuranceRate)
                val longTermCare = pct(health, prefs.longTermCareRate)
                val emp = pct(gross, prefs.fourInsEmploymentRate)
                val industrial = pct(gross, prefs.industrialAccidentRate)
                lines += TaxLine("지역(국민)보험", pension)
                lines += TaxLine("건강보험", health)
                lines += TaxLine("노인장기요양보험", longTermCare)
                lines += TaxLine("고용보험", emp)
                lines += TaxLine("산재보험", industrial)
            }

            TaxType.BUSINESS_INCOME -> {
                val tax = pct(gross, prefs.businessIncomeTaxRate)
                lines += TaxLine("사업소득세(3.3% 등)", tax)
            }
        }

        val totalDeduction = lines.sumOf { it.amount }
        return TaxResult(gross, lines, totalDeduction, gross - totalDeduction)
    }

    private fun pct(base: Long, rate: Double): Long = (base * rate / 100.0).roundToLong()
}
