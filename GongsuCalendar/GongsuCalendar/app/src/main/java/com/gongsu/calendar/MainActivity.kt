package com.gongsu.calendar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gongsu.calendar.backup.BackupManager
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.ui.CalendarAdapter
import com.gongsu.calendar.ui.EntryDialog
import com.gongsu.calendar.ui.GroupFilterDialog
import com.gongsu.calendar.ui.SettlementActivity
import com.gongsu.calendar.ui.TaxSettingsActivity
import com.gongsu.calendar.ui.UiHelper
import com.gongsu.calendar.ui.UiSettingsActivity
import com.gongsu.calendar.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
    private lateinit var db: AppDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvCalendar: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var tvMonthTitle: TextView
    private lateinit var tvTotalAmountSummary: TextView
    private lateinit var tvGongsuCountSummary: TextView

    private var currentMonth: YearMonth = YearMonth.now()

    private val backupManager by lazy { BackupManager(this) }

    private val createDocLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                backupManager.writeToUri(uri, backupManager.exportToJson())
                toast("백업이 저장되었습니다.")
            } catch (e: Exception) {
                toast("백업 실패: ${e.message}")
            }
        }
    }

    private val openDocLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                backupManager.importFromJson(backupManager.readFromUri(uri))
                toast("복구가 완료되었습니다.")
                loadMonth()
            } catch (e: Exception) {
                toast("복구 실패: 올바른 백업 파일이 아닙니다.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = AppPrefs(this)
        db = AppDatabase.getInstance(this)
        drawerLayout = findViewById(R.id.drawerLayout)
        rvCalendar = findViewById(R.id.rvCalendar)
        tvMonthTitle = findViewById(R.id.tvMonthTitle)
        tvTotalAmountSummary = findViewById(R.id.tvTotalAmountSummary)
        tvGongsuCountSummary = findViewById(R.id.tvGongsuCountSummary)

        // 상단/하단 시스템 영역과 겹치지 않게 여백
        UiHelper.applyInsets(findViewById(R.id.contentRoot))
        UiHelper.applyInsets(findViewById(R.id.drawerScroll))

        adapter = CalendarAdapter { date -> openEntryDialog(date) }
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = adapter

        findViewById<View>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnFolder).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnPrevMonth).setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            loadMonth()
        }
        findViewById<View>(R.id.btnNextMonth).setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            loadMonth()
        }
        findViewById<View>(R.id.btnCalendarJump).setOnClickListener {
            currentMonth = YearMonth.now()
            loadMonth()
        }
        findViewById<View>(R.id.btnListView).setOnClickListener {
            toast("목록 보기 (준비중)")
        }
        findViewById<View>(R.id.btnSettlement).setOnClickListener {
            val (s, e) = monthRange()
            SettlementActivity.start(this, s, e)
        }
        findViewById<View>(R.id.btnGroupFilter).setOnClickListener {
            GroupFilterDialog(this) { loadMonth() }.show()
        }

        buildDrawerContent()
    }

    override fun onResume() {
        super.onResume()
        loadMonth()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun openEntryDialog(date: LocalDate) {
        EntryDialog(this, date) { loadMonth() }.show()
    }

    private fun monthRange(): Pair<String, String> {
        val start = currentMonth.atDay(1)
        val end = currentMonth.atEndOfMonth()
        return Pair(DateUtils.toKey(start), DateUtils.toKey(end))
    }

    private fun loadMonth() {
        tvMonthTitle.text = "${currentMonth.year}년 ${currentMonth.monthValue}월"
        val days = DateUtils.buildMonthGrid(currentMonth)

        var entries = db.getRange(DateUtils.toKey(days.first()), DateUtils.toKey(days.last()))
        val g = prefs.activeGroupId
        if (g != -1L) entries = entries.filter { it.groupId == g }

        val groupNames = db.getGroups().associate { it.id to it.name }

        adapter.submit(
            days = days,
            currentMonth = currentMonth,
            entries = entries,
            groupNames = groupNames,
            showAmount = prefs.showAmountInMonth,
            showGongsu = prefs.showGongsuInMonth,
            showMemo = prefs.showMemoInMonth,
            showGroupName = prefs.showMonthGroupName,
            showAdjacentGongsu = prefs.showAdjacentMonthGongsu
        )

        val bottomBar = findViewById<View>(R.id.bottomSummaryBar)
        if (prefs.showBottomSummary) {
            bottomBar.visibility = View.VISIBLE

            // 하단 합계는 "현재 월"의 날짜만 집계
            val monthEntries = entries.filter {
                YearMonth.from(DateUtils.fromKey(it.date)) == currentMonth
            }
            val total = monthEntries.sumOf { it.totalAmount }
            val holidays = monthEntries.count { it.isHoliday }
            val workDays = monthEntries.size - holidays
            val gongsuSum = monthEntries.filter { !it.isHoliday }.sumOf { it.gongsu }

            tvTotalAmountSummary.text = DateUtils.formatMoney(total) + "원"
            tvGongsuCountSummary.text =
                "공수 ${DateUtils.formatGongsu(gongsuSum)} · 근무 ${workDays}일 · 휴 ${holidays}일"
        } else {
            bottomBar.visibility = View.GONE
        }
    }

    // ---------------------------------------------------------------
    // 왼쪽 메뉴
    // ---------------------------------------------------------------
    private fun buildDrawerContent() {
        val container = findViewById<LinearLayout>(R.id.drawerContent)
        container.removeAllViews()

        val tv = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
        val selectableBg = tv.resourceId

        fun section(title: String) {
            val v = TextView(this)
            v.text = title
            v.textSize = 13f
            v.setTextColor(0xFF888888.toInt())
            v.setPadding(UiHelper.dp(this, 20), UiHelper.dp(this, 18), UiHelper.dp(this, 16), UiHelper.dp(this, 6))
            container.addView(v)
        }

        fun item(title: String, action: () -> Unit) {
            val v = TextView(this)
            v.text = title
            v.textSize = 16f
            v.setTextColor(0xFF222222.toInt())
            v.setPadding(UiHelper.dp(this, 28), UiHelper.dp(this, 14), UiHelper.dp(this, 16), UiHelper.dp(this, 14))
            if (selectableBg != 0) v.setBackgroundResource(selectableBg)
            v.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                action()
            }
            container.addView(v)
        }

        section(getString(R.string.menu_data_management))
        item(getString(R.string.menu_backup_restore)) { showBackupRestoreDialog() }
        container.addView(UiHelper.divider(this))

        section(getString(R.string.menu_settings))
        item(getString(R.string.menu_default_ui)) {
            startActivity(Intent(this, UiSettingsActivity::class.java))
        }
        item(getString(R.string.menu_tax_rate)) {
            startActivity(Intent(this, TaxSettingsActivity::class.java))
        }
        item(getString(R.string.menu_notification)) { toast("준비중인 기능입니다.") }
        container.addView(UiHelper.divider(this))

        section(getString(R.string.menu_bulk))
        item(getString(R.string.menu_bulk_memo)) { showBulkMemoDialog() }
        item(getString(R.string.menu_bulk_gongsu)) { showBulkGongsuDialog() }
        item(getString(R.string.menu_bulk_settled)) { showBulkSettledDialog() }
        item(getString(R.string.menu_bulk_price)) { showBulkPriceDialog() }
        container.addView(UiHelper.divider(this))

        val shareBar = TextView(this)
        shareBar.text = getString(R.string.menu_share)
        shareBar.textSize = 16f
        shareBar.setTextColor(0xFFFFFFFF.toInt())
        shareBar.setBackgroundColor(0xFF333333.toInt())
        shareBar.setPadding(UiHelper.dp(this, 28), UiHelper.dp(this, 16), UiHelper.dp(this, 16), UiHelper.dp(this, 16))
        shareBar.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            shareSummary()
        }
        container.addView(shareBar)
    }

    private fun shareSummary() {
        val text = "${currentMonth.year}년 ${currentMonth.monthValue}월 합계: " +
            "${tvTotalAmountSummary.text} (${tvGongsuCountSummary.text})"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "공유하기"))
    }

    private fun showBackupRestoreDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_backup_restore))
            .setItems(arrayOf("백업 파일로 저장", "백업 파일에서 복구")) { _, which ->
                if (which == 0) {
                    createDocLauncher.launch("gongsu_backup.json")
                } else {
                    openDocLauncher.launch(arrayOf("*/*"))
                }
            }
            .show()
    }

    // ---------------------------------------------------------------
    // 일괄변경 (현재 보고 있는 달, 이미 기록된 날짜에만 적용)
    // ---------------------------------------------------------------
    private fun bulkTitle(name: String): String =
        "$name (${currentMonth.monthValue}월, 기록된 날짜만)"

    private fun showBulkMemoDialog() {
        val et = EditText(this)
        et.hint = "메모 내용"
        AlertDialog.Builder(this)
            .setTitle(bulkTitle(getString(R.string.menu_bulk_memo)))
            .setView(UiHelper.padded(this, et))
            .setPositiveButton("적용") { _, _ ->
                val (s, e) = monthRange()
                db.bulkUpdateMemo(s, e, et.text.toString())
                loadMonth()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showBulkGongsuDialog() {
        val et = EditText(this)
        et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        et.hint = "공수 값 (예: 1.0)"
        AlertDialog.Builder(this)
            .setTitle(bulkTitle(getString(R.string.menu_bulk_gongsu)))
            .setView(UiHelper.padded(this, et))
            .setPositiveButton("적용") { _, _ ->
                val gongsu = et.text.toString().toDoubleOrNull()
                if (gongsu != null) {
                    val (s, e) = monthRange()
                    db.bulkUpdateGongsu(s, e, gongsu)
                    loadMonth()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showBulkPriceDialog() {
        val et = EditText(this)
        et.inputType = InputType.TYPE_CLASS_NUMBER
        et.hint = "단가 (원)"
        AlertDialog.Builder(this)
            .setTitle(bulkTitle(getString(R.string.menu_bulk_price)))
            .setView(UiHelper.padded(this, et))
            .setPositiveButton("적용") { _, _ ->
                val price = et.text.toString().toLongOrNull()
                if (price != null) {
                    val (s, e) = monthRange()
                    db.bulkUpdateUnitPrice(s, e, price)
                    loadMonth()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showBulkSettledDialog() {
        AlertDialog.Builder(this)
            .setTitle(bulkTitle(getString(R.string.menu_bulk_settled)))
            .setItems(arrayOf("정산완료로 변경", "정산안됨으로 변경")) { _, which ->
                val (s, e) = monthRange()
                db.bulkUpdateSettled(s, e, which == 0)
                loadMonth()
            }
            .show()
    }
}
