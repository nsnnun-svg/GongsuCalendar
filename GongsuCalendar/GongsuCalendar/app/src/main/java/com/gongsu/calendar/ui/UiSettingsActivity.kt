package com.gongsu.calendar.ui

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gongsu.calendar.R
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.util.DateUtils

/** 예전 앱의 "UI설정" 화면과 같은 구성: 기본값 수정 / UI 변경 / 공수 단축키 값 */
class UiSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
    private lateinit var body: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPrefs(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        UiHelper.applyInsets(root)
        root.addView(UiHelper.header(this, "UI설정") { finish() })

        val scroll = android.widget.ScrollView(this)
        body = LinearLayout(this)
        body.orientation = LinearLayout.VERTICAL
        body.setPadding(UiHelper.dp(this, 20), UiHelper.dp(this, 4), UiHelper.dp(this, 20), UiHelper.dp(this, 32))
        scroll.addView(body)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        setContentView(root)

        render()
    }

    private fun render() {
        body.removeAllViews()

        section("기본값 수정")
        valueRow("기본입력일당", DateUtils.formatMoney(prefs.defaultUnitPrice)) { editLong("기본입력일당", prefs.defaultUnitPrice) { prefs.defaultUnitPrice = it } }
        valueRow("기본입력공수", DateUtils.formatGongsu(prefs.defaultGongsu.toDouble())) { editFloat("기본입력공수", prefs.defaultGongsu) { prefs.defaultGongsu = it } }
        valueRow("일당 버튼 증감값", DateUtils.formatMoney(prefs.priceStep)) { editLong("일당 버튼 증감값", prefs.priceStep) { prefs.priceStep = it } }
        valueRow("공수 버튼 증감값", DateUtils.formatGongsu(prefs.gongsuStep.toDouble())) { editFloat("공수 버튼 증감값", prefs.gongsuStep) { prefs.gongsuStep = it } }

        section("UI 변경")
        valueRow("연간달력 표시 방식", if (prefs.yearlyShowAmount) "금액표시" else "공수표시") { pickYearlyMode() }
        toggleRow("이전/다음 달 공수 표기", prefs.showAdjacentMonthGongsu) { prefs.showAdjacentMonthGongsu = it }
        toggleRow("하단 금액/공수 표시", prefs.showBottomSummary) { prefs.showBottomSummary = it }
        toggleRow("월간달력 그룹명 표시", prefs.showMonthGroupName) { prefs.showMonthGroupName = it }
        toggleRow("월간달력 금액 표시", prefs.showAmountInMonth) { prefs.showAmountInMonth = it }
        toggleRow("월간달력 공수 표시", prefs.showGongsuInMonth) { prefs.showGongsuInMonth = it }
        toggleRow("월간달력 메모 표시", prefs.showMemoInMonth) { prefs.showMemoInMonth = it }

        section("공수 단축키 값")
        valueRow("첫번째 버튼값", DateUtils.formatGongsu(prefs.preset1.toDouble())) { editFloat("첫번째 버튼값", prefs.preset1) { prefs.preset1 = it } }
        valueRow("두번째 버튼값", DateUtils.formatGongsu(prefs.preset2.toDouble())) { editFloat("두번째 버튼값", prefs.preset2) { prefs.preset2 = it } }
        valueRow("세번째 버튼값", DateUtils.formatGongsu(prefs.preset3.toDouble())) { editFloat("세번째 버튼값", prefs.preset3) { prefs.preset3 = it } }
        valueRow("네번째 버튼값", DateUtils.formatGongsu(prefs.preset4.toDouble())) { editFloat("네번째 버튼값", prefs.preset4) { prefs.preset4 = it } }

        val resetBtn = Button(this)
        resetBtn.text = "기본값 복구"
        resetBtn.setTextColor(0xFFFFFFFF.toInt())
        resetBtn.setBackgroundColor(0xFF2E7D32.toInt())
        resetBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("기본값 복구")
                .setMessage("모든 설정을 기본값으로 되돌릴까요? (기록된 날짜 데이터는 지워지지 않습니다)")
                .setPositiveButton("복구") { _, _ ->
                    prefs.resetUiDefaults()
                    render()
                    Toast.makeText(this, "기본값으로 복구되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소", null)
                .show()
        }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UiHelper.dp(this, 52))
        lp.topMargin = UiHelper.dp(this, 24)
        body.addView(resetBtn, lp)
    }

    // ---------------- 화면 구성 도우미 ----------------

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

    private fun valueRow(label: String, value: String, onChange: () -> Unit) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(0, UiHelper.dp(this, 10), 0, UiHelper.dp(this, 10))

        val l = TextView(this)
        l.text = label
        l.textSize = 16f
        l.setTextColor(0xFF222222.toInt())
        l.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val v = TextView(this)
        v.text = value
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

        row.addView(l)
        row.addView(v)
        row.addView(btn)
        body.addView(row)
        body.addView(UiHelper.divider(this))
    }

    private fun toggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(0, UiHelper.dp(this, 10), 0, UiHelper.dp(this, 10))

        val l = TextView(this)
        l.text = label
        l.textSize = 16f
        l.setTextColor(0xFF222222.toInt())
        l.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val sw = Switch(this)
        sw.isChecked = checked
        sw.setOnCheckedChangeListener { _, isChecked -> onChange(isChecked) }

        row.addView(l)
        row.addView(sw)
        body.addView(row)
        body.addView(UiHelper.divider(this))
    }

    private fun editLong(title: String, current: Long, onSave: (Long) -> Unit) {
        val et = EditText(this)
        et.inputType = InputType.TYPE_CLASS_NUMBER
        et.setText(current.toString())
        et.setSelection(et.text.length)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(UiHelper.padded(this, et))
            .setPositiveButton("확인") { _, _ ->
                val v = et.text.toString().toLongOrNull()
                if (v == null) {
                    Toast.makeText(this, "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    onSave(v)
                    render()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun editFloat(title: String, current: Float, onSave: (Float) -> Unit) {
        val et = EditText(this)
        et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        et.setText(current.toString())
        et.setSelection(et.text.length)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(UiHelper.padded(this, et))
            .setPositiveButton("확인") { _, _ ->
                val v = et.text.toString().toFloatOrNull()
                if (v == null) {
                    Toast.makeText(this, "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    onSave(v)
                    render()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun pickYearlyMode() {
        val options = arrayOf("금액표시", "공수표시")
        val current = if (prefs.yearlyShowAmount) 0 else 1
        AlertDialog.Builder(this)
            .setTitle("연간달력 표시 방식")
            .setSingleChoiceItems(options, current) { d, which ->
                prefs.yearlyShowAmount = which == 0
                d.dismiss()
                render()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
