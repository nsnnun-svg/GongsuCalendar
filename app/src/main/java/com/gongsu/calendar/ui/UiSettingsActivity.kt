package com.gongsu.calendar.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.gongsu.calendar.R
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.util.DateUtils

class UiSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        prefs = AppPrefs(this)

        findViewById<TextView>(R.id.tvTitle).text = "UI설정"
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        container = findViewById(R.id.container)

        rebuild()
    }

    private fun rebuild() {
        container.removeAllViews()
        addSection("기본값 수정")
        addValueRow("기본입력일당", DateUtils.formatMoney(prefs.defaultUnitPrice)) {
            promptLong("기본입력일당", prefs.defaultUnitPrice) { prefs.defaultUnitPrice = it; rebuild() }
        }
        addValueRow("기본입력공수", DateUtils.formatGongsu(prefs.defaultGongsu)) {
            promptDouble("기본입력공수", prefs.defaultGongsu) { prefs.defaultGongsu = it; rebuild() }
        }
        addValueRow("일당 버튼 증감값", DateUtils.formatMoney(prefs.priceStep)) {
            promptLong("일당 버튼 증감값", prefs.priceStep) { prefs.priceStep = it; rebuild() }
        }
        addValueRow("공수 버튼 증감값", DateUtils.formatGongsu(prefs.gongsuStep)) {
            promptDouble("공수 버튼 증감값", prefs.gongsuStep) { prefs.gongsuStep = it; rebuild() }
        }

        addSection("UI 변경")
        addSwitchRow("연간달력 금액표시", prefs.showAmountInYearCalendar) { prefs.showAmountInYearCalendar = it }
        addSwitchRow("이전/다음 달 공수 표기", prefs.showAdjacentMonthGongsu) { prefs.showAdjacentMonthGongsu = it }
        addSwitchRow("하단 금액/공수 표시", prefs.showBottomSummary) { prefs.showBottomSummary = it }
        addSwitchRow("월간달력 그룹명 표시", prefs.showGroupNameInMonth) { prefs.showGroupNameInMonth = it }
        addSwitchRow("월간달력 금액 표시", prefs.showAmountInMonth) { prefs.showAmountInMonth = it }
    }

    private fun addSection(title: String) {
        val v = LayoutInflater.from(this).inflate(R.layout.item_settings_section, container, false) as TextView
        v.text = title
        container.addView(v)
    }

    private fun addValueRow(label: String, value: String, onChange: () -> Unit) {
        val row = LayoutInflater.from(this).inflate(R.layout.item_settings_row, container, false)
        row.findViewById<TextView>(R.id.tvLabel).text = label
        row.findViewById<TextView>(R.id.tvValue).text = value
        row.findViewById<android.widget.Button>(R.id.btnChange).setOnClickListener { onChange() }
        container.addView(row)
    }

    private fun addSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
        val row = LayoutInflater.from(this).inflate(R.layout.item_settings_switch_row, container, false)
        row.findViewById<TextView>(R.id.tvLabel).text = label
        val sw = row.findViewById<SwitchCompat>(R.id.switchValue)
        sw.isChecked = checked
        sw.setOnCheckedChangeListener { _, isChecked -> onChange(isChecked) }
        container.addView(row)
    }

    private fun promptLong(title: String, current: Long, onOk: (Long) -> Unit) {
        val et = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(current.toString())
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(et)
            .setPositiveButton("확인") { _, _ -> onOk(et.text.toString().toLongOrNull() ?: current) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun promptDouble(title: String, current: Double, onOk: (Double) -> Unit) {
        val et = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(current.toString())
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(et)
            .setPositiveButton("확인") { _, _ -> onOk(et.text.toString().toDoubleOrNull() ?: current) }
            .setNegativeButton("취소", null)
            .show()
    }
}
