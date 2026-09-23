package com.gongsu.calendar.prefs

import android.content.Context

class AppPrefs(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("gongsu_prefs", Context.MODE_PRIVATE)

    /** -1 = 전체 보기, 0 = 그룹없음, 그 외 = 그룹 id */
    var activeGroupId: Long
        get() = sp.getLong("active_group", -1L)
        set(value) { sp.edit().putLong("active_group", value).apply() }

    // ---------- 기본값 수정 ----------
    var defaultUnitPrice: Long
        get() = sp.getLong("default_price", 147000L)
        set(value) { sp.edit().putLong("default_price", value).apply() }

    var defaultGongsu: Float
        get() = sp.getFloat("default_gongsu", 1.0f)
        set(value) { sp.edit().putFloat("default_gongsu", value).apply() }

    /** 단가 +/- 버튼을 누를 때 바뀌는 금액 */
    var priceStep: Long
        get() = sp.getLong("price_step", 16300L)
        set(value) { sp.edit().putLong("price_step", value).apply() }

    /** 공수 +/- 버튼을 누를 때 바뀌는 값 */
    var gongsuStep: Float
        get() = sp.getFloat("gongsu_step", 0.1f)
        set(value) { sp.edit().putFloat("gongsu_step", value).apply() }

    // ---------- UI 변경 ----------
    /** true = 연간달력에 금액 표시, false = 공수 표시 (연간달력 화면은 추후 지원) */
    var yearlyShowAmount: Boolean
        get() = sp.getBoolean("yearly_show_amount", true)
        set(value) { sp.edit().putBoolean("yearly_show_amount", value).apply() }

    var showAdjacentMonthGongsu: Boolean
        get() = sp.getBoolean("show_adjacent", true)
        set(value) { sp.edit().putBoolean("show_adjacent", value).apply() }

    var showBottomSummary: Boolean
        get() = sp.getBoolean("show_bottom_summary", true)
        set(value) { sp.edit().putBoolean("show_bottom_summary", value).apply() }

    var showMonthGroupName: Boolean
        get() = sp.getBoolean("show_month_group", true)
        set(value) { sp.edit().putBoolean("show_month_group", value).apply() }

    var showAmountInMonth: Boolean
        get() = sp.getBoolean("show_amount", true)
        set(value) { sp.edit().putBoolean("show_amount", value).apply() }

    var showGongsuInMonth: Boolean
        get() = sp.getBoolean("show_gongsu", true)
        set(value) { sp.edit().putBoolean("show_gongsu", value).apply() }

    var showMemoInMonth: Boolean
        get() = sp.getBoolean("show_memo", true)
        set(value) { sp.edit().putBoolean("show_memo", value).apply() }

    // ---------- 공수 단축키 값 ----------
    var preset1: Float
        get() = sp.getFloat("preset1", 0.5f)
        set(value) { sp.edit().putFloat("preset1", value).apply() }

    var preset2: Float
        get() = sp.getFloat("preset2", 1.0f)
        set(value) { sp.edit().putFloat("preset2", value).apply() }

    var preset3: Float
        get() = sp.getFloat("preset3", 1.5f)
        set(value) { sp.edit().putFloat("preset3", value).apply() }

    var preset4: Float
        get() = sp.getFloat("preset4", 2.0f)
        set(value) { sp.edit().putFloat("preset4", value).apply() }

    // ---------- 세율조정 : 적용 방식 ----------
    /** "daily" | "insurance" | "business" */
    var taxMode: String
        get() = sp.getString("tax_mode", "daily") ?: "daily"
        set(value) { sp.edit().putString("tax_mode", value).apply() }

    // 일용근무 세율
    var dailyIncomeTax: Float
        get() = sp.getFloat("daily_income_tax", 2.7f)
        set(value) { sp.edit().putFloat("daily_income_tax", value).apply() }

    /** 지방소득세 = 소득세의 몇 % 인지 (기본 10%) */
    var dailyLocalTaxRatio: Float
        get() = sp.getFloat("daily_local_tax_ratio", 10.0f)
        set(value) { sp.edit().putFloat("daily_local_tax_ratio", value).apply() }

    var dailyEmploymentInsurance: Float
        get() = sp.getFloat("daily_employment_insurance", 0.9f)
        set(value) { sp.edit().putFloat("daily_employment_insurance", value).apply() }

    // 4대보험 세율
    var insurancePension: Float
        get() = sp.getFloat("insurance_pension", 4.75f)
        set(value) { sp.edit().putFloat("insurance_pension", value).apply() }

    var insuranceHealth: Float
        get() = sp.getFloat("insurance_health", 3.595f)
        set(value) { sp.edit().putFloat("insurance_health", value).apply() }

    /** 노인장기요양보험 = 건강보험의 몇 % 인지 (기본 13.14%) */
    var insuranceLongTermCareRatio: Float
        get() = sp.getFloat("insurance_ltc_ratio", 13.14f)
        set(value) { sp.edit().putFloat("insurance_ltc_ratio", value).apply() }

    var insuranceEmployment: Float
        get() = sp.getFloat("insurance_employment", 0.9f)
        set(value) { sp.edit().putFloat("insurance_employment", value).apply() }

    var insuranceIndustrial: Float
        get() = sp.getFloat("insurance_industrial", 3.56f)
        set(value) { sp.edit().putFloat("insurance_industrial", value).apply() }

    // 사업소득세율
    var businessIncomeTax: Float
        get() = sp.getFloat("business_income_tax", 3.0f)
        set(value) { sp.edit().putFloat("business_income_tax", value).apply() }

    /** 지방소득세 = 사업소득세의 몇 % 인지 (기본 10%) */
    var businessLocalTaxRatio: Float
        get() = sp.getFloat("business_local_tax_ratio", 10.0f)
        set(value) { sp.edit().putFloat("business_local_tax_ratio", value).apply() }

    /** 현재 선택된 방식으로 계산한 최종 공제율(%) */
    fun effectiveTaxRate(): Float {
        return when (taxMode) {
            "insurance" -> insurancePension + insuranceHealth +
                insuranceHealth * insuranceLongTermCareRatio / 100f +
                insuranceEmployment + insuranceIndustrial
            "business" -> businessIncomeTax + businessIncomeTax * businessLocalTaxRatio / 100f
            else -> dailyIncomeTax + dailyIncomeTax * dailyLocalTaxRatio / 100f +
                dailyEmploymentInsurance
        }
    }

    fun resetUiDefaults() {
        sp.edit()
            .putLong("default_price", 147000L)
            .putFloat("default_gongsu", 1.0f)
            .putLong("price_step", 16300L)
            .putFloat("gongsu_step", 0.1f)
            .putBoolean("yearly_show_amount", true)
            .putBoolean("show_adjacent", true)
            .putBoolean("show_bottom_summary", true)
            .putBoolean("show_month_group", true)
            .putBoolean("show_amount", true)
            .putBoolean("show_gongsu", true)
            .putBoolean("show_memo", true)
            .putFloat("preset1", 0.5f)
            .putFloat("preset2", 1.0f)
            .putFloat("preset3", 1.5f)
            .putFloat("preset4", 2.0f)
            .putString("tax_mode", "daily")
            .putFloat("daily_income_tax", 2.7f)
            .putFloat("daily_local_tax_ratio", 10.0f)
            .putFloat("daily_employment_insurance", 0.9f)
            .putFloat("insurance_pension", 4.75f)
            .putFloat("insurance_health", 3.595f)
            .putFloat("insurance_ltc_ratio", 13.14f)
            .putFloat("insurance_employment", 0.9f)
            .putFloat("insurance_industrial", 3.56f)
            .putFloat("business_income_tax", 3.0f)
            .putFloat("business_local_tax_ratio", 10.0f)
            .apply()
    }
}
