package com.gongsu.calendar.ui

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gongsu.calendar.prefs.AppPrefs

/** 예전 앱의 "세율변경" 화면과 같은 구성: 일용근무 세율 / 4대보험 세율 / 사업소득세율 */
class TaxSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
    private lateinit var body: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPrefs(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        UiHelper.applyInsets(root)
        root.addView(UiHelper.header(this, "세율변경") { finish() })

        val scroll = ScrollView(this)
        body = LinearLayout(this)
        body.orientation = LinearLayout.VERTICAL
        body.setPadding(UiHelper.dp(this, 20), UiHelper.dp(this, 4), UiHelper.dp(this, 20), UiHelper.dp(this, 32))
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)

        render()
    }

    private fun render() {
        body.removeAllViews()

        applyModeRow()

        section("일용근무 세율")
        row("소득세", prefs.dailyIncomeTax) { editPercent("소득세", prefs.dailyIncomeTax) { prefs.dailyIncomeTax = it } }
        row("지방소득세", prefs.dailyLocalTaxRatio, "(소득세의 %)") { editPercent("지방소득세 (소득세의 %)", prefs.dailyLocalTaxRatio) { prefs.dailyLocalTaxRatio = it } }
        row("고용보험", prefs.dailyEmploymentInsurance) { editPercent("고용보험", prefs.dailyEmploymentInsurance) { prefs.dailyEmploymentInsurance = it } }

        section("4대보험 세율")
        row("지역보험", prefs.insurancePension) { editPercent("지역보험", prefs.insurancePension) { prefs.insurancePension = it } }
        row("건강보험", prefs.insuranceHealth) { editPercent("건강보험", prefs.insuranceHealth) { prefs.insuranceHealth = it } }
        row("노인장기요양보험", prefs.insuranceLongTermCareRatio, "(건강보험의 %)") { editPercent("노인장기요양보험 (건강보험의 %)", prefs.insuranceLongTermCareRatio) { prefs.insuranceLongTermCareRatio = it } }
        row("고용보험", prefs.insuranceEmployment) { editPercent("고용보험", prefs.insuranceEmployment) { prefs.insuranceEmployment = it } }
        row("산재보험", prefs.insuranceIndustrial) { editPercent("산재보험", prefs.insuranceIndustrial) { prefs.insuranceIndustrial = it } }

        section("사업소득세율")
        row("사업소득세", prefs.businessIncomeTax) { editPercent("사업소득세", prefs.businessIncomeTax) { prefs.businessIncomeTax = it } }
        row("지방소득세", prefs.businessLocalTaxRatio, "(사업소득세의 %)") { editPercent("지방소득세 (사업소득세의 %)", prefs.businessLocalTaxRatio) { prefs.businessLocalTaxRatio = it } }

        val summary = TextView(this)
        summary.text = "현재 적용 방식 최종 공제율: ${"%.2f".format(prefs.effectiveTaxRate())}%"
        summary.textSize = 15f
        summary.setTextColor(0xFF2E7D32.toInt())
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = UiHelper.dp(this, 20)
        body.addView(summary, lp)
    }

    /** 정산/세금 화면에 어떤 방식을 적용할지 고르는 라디오 버튼 */
    private fun applyModeRow() {
        val label = TextView(this)
        label.text = "정산에 적용할 방식"
        label.textSize = 15f
        label.setTextColor(0xFF555555.toInt())
        body.addView(label)

        val group = RadioGroup(this)
        group.orientation = RadioGroup.HORIZONTAL

        val modes = listOf("daily" to "일용근무", "insurance" to "4대보험", "business" to "사업소득")
        val buttons = modes.map { (key, text) ->
            val rb = RadioButton(this)
            rb.text = text
            rb.textSize = 14f
            rb.isChecked = prefs.taxMode == key
            group.addView(rb)
            key to rb
        }
        buttons.forEach { (key, rb) ->
            rb.setOnClickListener {
                prefs.taxMode = key
                render()
            }
        }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = UiHelper.dp(this, 6)
        lp.bottomMargin = UiHelper.dp(this, 8)
        body.addView(group, lp)
        body.addView(UiHelper.divider(this))
    }

    private fun section(title: String) {
        val t = TextView(this)
        t.text = title
        t.textSize = 15f
        t.setTextColor(0xFF555555.toInt())
        t.setBackgroundColor(0xFFF0F0F0.toInt())
        t.setPadding(UiHelper.dp(this, 4), UiHelper.dp(this, 14), UiHelper.dp(this, 4), UiHelper.dp(this, 10))
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = UiHelper.dp(this, 12)
        body.addView(t, lp)
    }

    private fun row(label: String, percent: Float, note: String = "", onChange: () -> Unit) {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(0, UiHelper.dp(this, 10), 0, UiHelper.dp(this, 10))

        val l = TextView(this)
        l.text = if (note.isEmpty()) label else "$label $note"
        l.textSize = 16f
        l.setTextColor(0xFF222222.toInt())
        l.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val v = TextView(this)
        v.text = "${trimPercent(percent)}%"
        v.textSize = 16f
        v.gravity = Gravity.END
        v.setTextColor(0xFF111111.toInt())
        v.setPadding(0, 0, UiHelper.dp(this, 12), 0)

        val btn = Button(this)
        btn.text = "변경"
        btn.textSize = 13f
        btn.minWidth = UiHelper.dp(this, 72)
        btn.minHeight = UiHelper.dp(this, 40)
        btn.setTextColor(0xFF2E7D32.toInt())
        btn.setBackgroundColor(0x00000000)
        btn.setOnClickListener { onChange() }

        r.addView(l)
        r.addView(v)
        r.addView(btn)
        body.addView(r)
        body.addView(UiHelper.divider(this))
    }

    private fun trimPercent(v: Float): String {
        return if (v == v.toLong().toFloat()) v.toLong().toString() else v.toString()
    }

    private fun editPercent(title: String, current: Float, onSave: (Float) -> Unit) {
        val et = EditText(this)
        et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        et.setText(current.toString())
        et.setSelection(et.text.length)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(UiHelper.padded(this, et))
            .setPositiveButton("확인") { _, _ ->
                val v = et.text.toString().toFloatOrNull()
                if (v == null || v < 0f || v > 100f) {
                    Toast.makeText(this, "0~100 사이 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    onSave(v)
                    render()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
