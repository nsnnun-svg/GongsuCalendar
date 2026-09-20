package com.gongsu.calendar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gongsu.calendar.R
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.util.DateUtils
import com.gongsu.calendar.util.TaxCalculator
import com.gongsu.calendar.util.TaxType
import kotlinx.coroutines.launch

class SettlementActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
    private lateinit var container: LinearLayout
    private var gross: Long = 0L
    private var startDate = ""
    private var endDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        prefs = AppPrefs(this)

        startDate = intent.getStringExtra(EXTRA_START) ?: ""
        endDate = intent.getStringExtra(EXTRA_END) ?: ""

        findViewById<TextView>(R.id.tvTitle).text = "정산/세금"
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        container = findViewById(R.id.container)

        loadGrossAndBuild()
    }

    private fun loadGrossAndBuild() {
        lifecycleScope.launch {
            val entries = AppDatabase.getInstance(this@SettlementActivity)
                .workEntryDao().getRange(startDate, endDate)
            gross = entries.sumOf { it.totalAmount }
            build()
        }
    }

    private fun build() {
        container.removeAllViews()

        val period = TextView(this).apply {
            text = "기간: $startDate ~ $endDate"
            setPadding(40, 30, 40, 10)
            textSize = 14f
            setTextColor(0xFF666666.toInt())
        }
        container.addView(period)

        val grossRow = TextView(this).apply {
            text = "합계(세전): ${DateUtils.formatMoney(gross)}원"
            setPadding(40, 10, 40, 20)
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        container.addView(grossRow)

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            setPadding(30, 0, 30, 20)
        }
        val options = listOf(
            "없음" to TaxType.NONE,
            "일용근무" to TaxType.DAILY_WORK,
            "4대보험" to TaxType.FOUR_INSURANCE,
            "사업소득" to TaxType.BUSINESS_INCOME
        )
        val resultContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        fun renderResult(type: TaxType) {
            resultContainer.removeAllViews()
            val result = TaxCalculator.calculate(gross, type, prefs)
            result.lines.forEach { line ->
                resultContainer.addView(TextView(this).apply {
                    text = "${line.label}: -${DateUtils.formatMoney(line.amount)}원"
                    setPadding(40, 8, 40, 8)
                    textSize = 15f
                })
            }
            resultContainer.addView(TextView(this).apply {
                text = "공제 합계: -${DateUtils.formatMoney(result.totalDeduction)}원"
                setPadding(40, 12, 40, 4)
                textSize = 15f
                setTextColor(0xFFD32F2F.toInt())
            })
            resultContainer.addView(TextView(this).apply {
                text = "실수령액: ${DateUtils.formatMoney(result.net)}원"
                setPadding(40, 10, 40, 30)
                textSize = 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(0xFF1B5E20.toInt())
            })
        }

        options.forEachIndexed { idx, (label, type) ->
            val rb = RadioButton(this).apply {
                text = label
                id = idx
                setPadding(20, 10, 20, 10)
            }
            radioGroup.addView(rb)
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            renderResult(options[checkedId].second)
        }
        container.addView(radioGroup)
        container.addView(resultContainer)

        radioGroup.check(0)
        renderResult(TaxType.NONE)
    }

    companion object {
        private const val EXTRA_START = "extra_start"
        private const val EXTRA_END = "extra_end"

        fun start(context: Context, start: String, end: String) {
            val intent = Intent(context, SettlementActivity::class.java)
            intent.putExtra(EXTRA_START, start)
            intent.putExtra(EXTRA_END, end)
            context.startActivity(intent)
        }
    }
}
