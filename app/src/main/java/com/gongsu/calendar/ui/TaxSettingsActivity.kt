package com.gongsu.calendar.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gongsu.calendar.R
import com.gongsu.calendar.prefs.AppPrefs
import java.util.Locale

class TaxSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        prefs = AppPrefs(this)

        findViewById<TextView>(R.id.tvTitle).text = "세율변경"
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        container = findViewById(R.id.container)

        rebuild()
    }

    private fun pctStr(v: Double) = String.format(Locale.KOREA, "%.3f", v).trimEnd('0').trimEnd('.') + "%"

    private fun rebuild() {
        container.removeAllViews()

        addSection("일용근무 세율")
        addRow("소득세", prefs.dailyIncomeTaxRate) { prefs.dailyIncomeTaxRate = it; rebuild() }
        addRow("지방소득세 (소득세의 %)", prefs.dailyLocalIncomeTaxRate) { prefs.dailyLocalIncomeTaxRate = it; rebuild() }
        addRow("고용보험", prefs.dailyEmploymentInsuranceRate) { prefs.dailyEmploymentInsuranceRate = it; rebuild() }

        addSection("4대보험 세율")
        addRow("지역보험", prefs.nationalPensionRate) { prefs.nationalPensionRate = it; rebuild() }
        addRow("건강보험", prefs.healthInsuranceRate) { prefs.healthInsuranceRate = it; rebuild() }
        addRow("노인장기요양보험 (건강보험의 %)", prefs.longTermCareRate) { prefs.longTermCareRate = it; rebuild() }
        addRow("고용보험", prefs.fourInsEmploymentRate) { prefs.fourInsEmploymentRate = it; rebuild() }
        addRow("산재보험", prefs.industrialAccidentRate) { prefs.industrialAccidentRate = it; rebuild() }

        addSection("사업소득세율")
        addRow("사업소득", prefs.businessIncomeTaxRate) { prefs.businessIncomeTaxRate = it; rebuild() }
    }

    private fun addSection(title: String) {
        val v = LayoutInflater.from(this).inflate(R.layout.item_settings_section, container, false) as TextView
        v.text = title
        container.addView(v)
    }

    private fun addRow(label: String, value: Double, onChange: (Double) -> Unit) {
        val row = LayoutInflater.from(this).inflate(R.layout.item_settings_row, container, false)
        row.findViewById<TextView>(R.id.tvLabel).text = label
        row.findViewById<TextView>(R.id.tvValue).text = pctStr(value)
        row.findViewById<android.widget.Button>(R.id.btnChange).setOnClickListener {
            val et = EditText(this).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(value.toString())
            }
            AlertDialog.Builder(this)
                .setTitle("$label (%)")
                .setView(et)
                .setPositiveButton("확인") { _, _ ->
                    onChange(et.text.toString().toDoubleOrNull() ?: value)
                }
                .setNegativeButton("취소", null)
                .show()
        }
        container.addView(row)
    }
}
