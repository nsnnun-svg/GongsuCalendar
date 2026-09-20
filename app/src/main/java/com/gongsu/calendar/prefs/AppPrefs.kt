package com.gongsu.calendar.prefs

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(context: Context) {
    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("gongsu_prefs", Context.MODE_PRIVATE)

    // ---------- 기본값 ----------
    var defaultUnitPrice: Long
        get() = sp.getLong(KEY_DEFAULT_UNIT_PRICE, 144_000L)
        set(v) = sp.edit().putLong(KEY_DEFAULT_UNIT_PRICE, v).apply()

    var defaultGongsu: Double
        get() = sp.getFloat(KEY_DEFAULT_GONGSU, 1.0f).toDouble()
        set(v) = sp.edit().putFloat(KEY_DEFAULT_GONGSU, v.toFloat()).apply()

    var priceStep: Long
        get() = sp.getLong(KEY_PRICE_STEP, 16_100L)
        set(v) = sp.edit().putLong(KEY_PRICE_STEP, v).apply()

    var gongsuStep: Double
        get() = sp.getFloat(KEY_GONGSU_STEP, 0.1f).toDouble()
        set(v) = sp.edit().putFloat(KEY_GONGSU_STEP, v.toFloat()).apply()

    // ---------- UI 변경 ----------
    var showAmountInYearCalendar: Boolean
        get() = sp.getBoolean(KEY_SHOW_AMOUNT_YEAR, true)
        set(v) = sp.edit().putBoolean(KEY_SHOW_AMOUNT_YEAR, v).apply()

    var showAdjacentMonthGongsu: Boolean
        get() = sp.getBoolean(KEY_SHOW_ADJACENT, true)
        set(v) = sp.edit().putBoolean(KEY_SHOW_ADJACENT, v).apply()

    var showBottomSummary: Boolean
        get() = sp.getBoolean(KEY_SHOW_BOTTOM, true)
        set(v) = sp.edit().putBoolean(KEY_SHOW_BOTTOM, v).apply()

    var showGroupNameInMonth: Boolean
        get() = sp.getBoolean(KEY_SHOW_GROUP_NAME, true)
        set(v) = sp.edit().putBoolean(KEY_SHOW_GROUP_NAME, v).apply()

    var showAmountInMonth: Boolean
        get() = sp.getBoolean(KEY_SHOW_AMOUNT_MONTH, true)
        set(v) = sp.edit().putBoolean(KEY_SHOW_AMOUNT_MONTH, v).apply()

    // ---------- 세율: 일용근무 ----------
    var dailyIncomeTaxRate: Double        // 소득세 %
        get() = sp.getFloat(KEY_DAILY_INCOME_TAX, 2.7f).toDouble()
        set(v) = sp.edit().putFloat(KEY_DAILY_INCOME_TAX, v.toFloat()).apply()

    var dailyLocalIncomeTaxRate: Double   // 지방소득세 % (소득세의 %)
        get() = sp.getFloat(KEY_DAILY_LOCAL_TAX, 10.0f).toDouble()
        set(v) = sp.edit().putFloat(KEY_DAILY_LOCAL_TAX, v.toFloat()).apply()

    var dailyEmploymentInsuranceRate: Double  // 고용보험 %
        get() = sp.getFloat(KEY_DAILY_EMP_INS, 0.9f).toDouble()
        set(v) = sp.edit().putFloat(KEY_DAILY_EMP_INS, v.toFloat()).apply()

    // ---------- 세율: 4대보험 ----------
    var nationalPensionRate: Double   // 지역(국민)보험 %
        get() = sp.getFloat(KEY_NATIONAL_PENSION, 4.5f).toDouble()
        set(v) = sp.edit().putFloat(KEY_NATIONAL_PENSION, v.toFloat()).apply()

    var healthInsuranceRate: Double  // 건강보험 %
        get() = sp.getFloat(KEY_HEALTH_INS, 3.545f).toDouble()
        set(v) = sp.edit().putFloat(KEY_HEALTH_INS, v.toFloat()).apply()

    var longTermCareRate: Double     // 노인장기요양보험 % (건강보험의 %)
        get() = sp.getFloat(KEY_LONG_TERM_CARE, 12.81f).toDouble()
        set(v) = sp.edit().putFloat(KEY_LONG_TERM_CARE, v.toFloat()).apply()

    var fourInsEmploymentRate: Double  // 고용보험 %
        get() = sp.getFloat(KEY_FOUR_EMP_INS, 0.9f).toDouble()
        set(v) = sp.edit().putFloat(KEY_FOUR_EMP_INS, v.toFloat()).apply()

    var industrialAccidentRate: Double  // 산재보험 %
        get() = sp.getFloat(KEY_INDUSTRIAL_ACCIDENT, 3.7f).toDouble()
        set(v) = sp.edit().putFloat(KEY_INDUSTRIAL_ACCIDENT, v.toFloat()).apply()

    // ---------- 세율: 사업소득 ----------
    var businessIncomeTaxRate: Double  // 사업소득 %
        get() = sp.getFloat(KEY_BUSINESS_INCOME_TAX, 3.3f).toDouble()
        set(v) = sp.edit().putFloat(KEY_BUSINESS_INCOME_TAX, v.toFloat()).apply()

    // ---------- 그룹 필터 ----------
    var activeGroupId: Long
        get() = sp.getLong(KEY_ACTIVE_GROUP, -1L) // -1 = 전체
        set(v) = sp.edit().putLong(KEY_ACTIVE_GROUP, v).apply()

    companion object {
        private const val KEY_DEFAULT_UNIT_PRICE = "default_unit_price"
        private const val KEY_DEFAULT_GONGSU = "default_gongsu"
        private const val KEY_PRICE_STEP = "price_step"
        private const val KEY_GONGSU_STEP = "gongsu_step"

        private const val KEY_SHOW_AMOUNT_YEAR = "show_amount_year"
        private const val KEY_SHOW_ADJACENT = "show_adjacent"
        private const val KEY_SHOW_BOTTOM = "show_bottom"
        private const val KEY_SHOW_GROUP_NAME = "show_group_name"
        private const val KEY_SHOW_AMOUNT_MONTH = "show_amount_month"

        private const val KEY_DAILY_INCOME_TAX = "daily_income_tax"
        private const val KEY_DAILY_LOCAL_TAX = "daily_local_tax"
        private const val KEY_DAILY_EMP_INS = "daily_emp_ins"

        private const val KEY_NATIONAL_PENSION = "national_pension"
        private const val KEY_HEALTH_INS = "health_ins"
        private const val KEY_LONG_TERM_CARE = "long_term_care"
        private const val KEY_FOUR_EMP_INS = "four_emp_ins"
        private const val KEY_INDUSTRIAL_ACCIDENT = "industrial_accident"

        private const val KEY_BUSINESS_INCOME_TAX = "business_income_tax"

        private const val KEY_ACTIVE_GROUP = "active_group"
    }
}
