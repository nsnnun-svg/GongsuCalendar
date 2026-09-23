package com.gongsu.calendar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.gongsu.calendar.R
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

class SettlementActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_START = "start"
        private const val EXTRA_END = "end"

        fun start(context: Context, startKey: String, endKey: String) {
            val i = Intent(context, SettlementActivity::class.java)
            i.putExtra(EXTRA_START, startKey)
            i.putExtra(EXTRA_END, endKey)
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startKey = intent.getStringExtra(EXTRA_START)
            ?: DateUtils.toKey(LocalDate.now().withDayOfMonth(1))
        val endKey = intent.getStringExtra(EXTRA_END)
            ?: DateUtils.toKey(YearMonth.now().atEndOfMonth())

        val prefs = AppPrefs(this)
        val db = AppDatabase.getInstance(this)

        var entries = db.getRange(startKey, endKey)
        val gid = prefs.activeGroupId
        if (gid != -1L) entries = entries.filter { it.groupId == gid }

        val filterName = when (gid) {
            -1L -> "전체"
            0L -> "그룹없음"
            else -> db.getGroups().firstOrNull { it.id == gid }?.name ?: "전체"
        }

        val workEntries = entries.filter { !it.isHoliday }
        val holidayCount = entries.count { it.isHoliday }
        val totalGongsu = workEntries.sumOf { it.gongsu }
        val total = entries.sumOf { it.totalAmount }
        val settledAmount = entries.filter { it.isSettled }.sumOf { it.totalAmount }
        val unsettledAmount = total - settledAmount
        val rate = prefs.effectiveTaxRate()
        val modeLabel = when (prefs.taxMode) {
            "insurance" -> "4대보험"
            "business" -> "사업소득"
            else -> "일용근무"
        }
        val tax = Math.round(total * rate / 100.0)
        val net = total - tax

        val scroll = ScrollView(this)
        UiHelper.applyInsets(scroll)

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.addView(UiHelper.header(this, getString(R.string.settlement_tax)) { finish() })

        val body = LinearLayout(this)
        body.orientation = LinearLayout.VERTICAL
        body.setPadding(
            UiHelper.dp(this, 20), UiHelper.dp(this, 8),
            UiHelper.dp(this, 20), UiHelper.dp(this, 24)
        )

        body.addView(UiHelper.label(this, "기간"))
        body.addView(UiHelper.row(this, "$startKey ~ $endKey", ""))
        body.addView(UiHelper.row(this, "그룹 필터", filterName))
        body.addView(UiHelper.divider(this))

        body.addView(UiHelper.row(this, "근무일수", "${workEntries.size}일"))
        body.addView(UiHelper.row(this, "휴일", "${holidayCount}일"))
        body.addView(UiHelper.row(this, "총 공수", DateUtils.formatGongsu(totalGongsu)))
        body.addView(UiHelper.divider(this))

        body.addView(UiHelper.row(this, "총액", DateUtils.formatMoney(total) + "원", true))
        body.addView(UiHelper.row(this, "적용 방식", modeLabel))
        body.addView(UiHelper.row(this, "세율", "%.2f".format(rate) + "%"))
        body.addView(UiHelper.row(this, "세금(공제)", "-" + DateUtils.formatMoney(tax) + "원"))
        body.addView(UiHelper.row(this, "실수령액", DateUtils.formatMoney(net) + "원", true))
        body.addView(UiHelper.divider(this))

        body.addView(UiHelper.row(this, "정산완료", DateUtils.formatMoney(settledAmount) + "원"))
        body.addView(UiHelper.row(this, "미정산", DateUtils.formatMoney(unsettledAmount) + "원"))

        col.addView(body)
        scroll.addView(col)
        setContentView(scroll)
    }
}
