package com.gongsu.calendar

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gongsu.calendar.backup.BackupManager
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.ui.CalendarAdapter
import com.gongsu.calendar.ui.EntryDialogFragment
import com.gongsu.calendar.ui.GroupFilterDialogFragment
import com.gongsu.calendar.ui.SettlementActivity
import com.gongsu.calendar.ui.TaxSettingsActivity
import com.gongsu.calendar.ui.UiSettingsActivity
import com.gongsu.calendar.util.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
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
        uri?.let {
            lifecycleScope.launch {
                val json = backupManager.exportToJson()
                backupManager.writeToUri(it, json)
                Toast.makeText(this@MainActivity, "백업이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val openDocLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val json = backupManager.readFromUri(it)
                backupManager.importFromJson(json)
                Toast.makeText(this@MainActivity, "복구가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                loadMonth()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = AppPrefs(this)
        drawerLayout = findViewById(R.id.drawerLayout)
        rvCalendar = findViewById(R.id.rvCalendar)
        tvMonthTitle = findViewById(R.id.tvMonthTitle)
        tvTotalAmountSummary = findViewById(R.id.tvTotalAmountSummary)
        tvGongsuCountSummary = findViewById(R.id.tvGongsuCountSummary)

        adapter = CalendarAdapter { date -> openEntryDialog(date) }
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = adapter

        findViewById<View>(R.id.btnMenu).setOnClickListener {
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
            Toast.makeText(this, "목록 보기 (준비중)", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnFolder).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnSettlement).setOnClickListener {
            val start = currentMonth.atDay(1)
            val end = currentMonth.atEndOfMonth()
            SettlementActivity.start(this, DateUtils.toKey(start), DateUtils.toKey(end))
        }
        findViewById<View>(R.id.btnGroupFilter).setOnClickListener {
            GroupFilterDialogFragment { loadMonth() }.show(supportFragmentManager, "group_filter")
        }

        buildDrawerContent()
        loadMonth()
    }

    override fun onResume() {
        super.onResume()
        loadMonth()
    }

    private fun openEntryDialog(date: LocalDate) {
        EntryDialogFragment(date) { loadMonth() }.show(supportFragmentManager, "entry_dialog")
    }

    private fun loadMonth() {
        tvMonthTitle.text = "${currentMonth.year}년 ${currentMonth.monthValue}월"
        val days = DateUtils.buildMonthGrid(currentMonth)
        val gridStart = days.first()
        val gridEnd = days.last()

        lifecycleScope.launch {
            var entries = AppDatabase.getInstance(this@MainActivity)
                .workEntryDao().getRange(DateUtils.toKey(gridStart), DateUtils.toKey(gridEnd))

            val activeGroup = prefs.activeGroupId
            if (activeGroup != -1L) {
                entries = entries.filter { it.groupId == activeGroup }
            }

            adapter.submit(
                days = days,
                currentMonth = currentMonth,
                entries = entries,
                showAmount = prefs.showAmountInMonth,
                showAdjacentGongsu = prefs.showAdjacentMonthGongsu
            )

            // 하단 합계는 "현재 월"에 속한 날짜만 집계
            val monthEntries = entries.filter { YearMonth.from(DateUtils.fromKey(it.date)) == currentMonth }
            val totalAmount = monthEntries.sumOf { it.totalAmount }
            val holidayCount = monthEntries.count { it.isHoliday }
            val workCount = monthEntries.size

            tvTotalAmountSummary.text = DateUtils.formatMoney(totalAmount)
            tvGongsuCountSummary.text = "공수 ${workCount}건(휴${holidayCount})"
        }
    }

    // ---------------------------------------------------------------
    // 좌측 드로어 메뉴 구성
    // ---------------------------------------------------------------
    private fun buildDrawerContent() {
        val container = findViewById<LinearLayout>(R.id.drawerContent)
        container.removeAllViews()

        fun section(title: String) {
            val v = TextView(this).apply {
                text = title
                setPadding(60, 50, 20, 16)
                textSize = 13f
                setTextColor(0xFF888888.toInt())
            }
            container.addView(v)
        }

        fun item(title: String, action: () -> Unit) {
            val v = TextView(this).apply {
                text = title
                setPadding(84, 36, 40, 36)
                textSize = 16f
                setTextColor(0xFF222222.toInt())
                background = androidx.appcompat.content.res.AppCompatResources
                    .getDrawable(this@MainActivity, android.R.drawable.list_selector_background)
                setOnClickListener {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    action()
                }
            }
            container.addView(v)
        }

        fun divider() {
            val v = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2
                )
                setBackgroundColor(0xFFE0E0E0.toInt())
            }
            container.addView(v)
        }

        section(getString(R.string.menu_data_management))
        item(getString(R.string.menu_backup_restore)) { showBackupRestoreDialog() }
        divider()

        section(getString(R.string.menu_settings))
        item(getString(R.string.menu_default_ui)) {
            startActivity(Intent(this, UiSettingsActivity::class.java))
        }
        item(getString(R.string.menu_tax_rate)) {
            startActivity(Intent(this, TaxSettingsActivity::class.java))
        }
        item(getString(R.string.menu_notification)) {
            Toast.makeText(this, "준비중인 기능입니다.", Toast.LENGTH_SHORT).show()
        }
        divider()

        section(getString(R.string.menu_bulk))
        item(getString(R.string.menu_bulk_memo)) { showBulkMemoDialog() }
        item(getString(R.string.menu_bulk_gongsu)) { showBulkGongsuDialog() }
        item(getString(R.string.menu_bulk_settled)) { showBulkSettledDialog() }
        item(getString(R.string.menu_bulk_price)) { showBulkPriceDialog() }
        divider()

        section(getString(R.string.menu_etc))
        item(getString(R.string.menu_app_update)) {
            Toast.makeText(this, "현재 최신 버전입니다.", Toast.LENGTH_SHORT).show()
        }
        item(getString(R.string.menu_rate)) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (e: Exception) {
                Toast.makeText(this, "스토어를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        item(getString(R.string.menu_contact)) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_SUBJECT, "[공수계산노트] 문의")
            }
            try { startActivity(intent) } catch (e: Exception) {
                Toast.makeText(this, "메일 앱을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        val shareBar = TextView(this).apply {
            text = getString(R.string.menu_share)
            setPadding(84, 40, 40, 40)
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF333333.toInt())
            setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                shareSummary()
            }
        }
        container.addView(shareBar)
    }

    private fun shareSummary() {
        val text = "${currentMonth.year}년 ${currentMonth.monthValue}월 공수 합계: " +
            "${tvTotalAmountSummary.text}원 (${tvGongsuCountSummary.text})"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "공유하기"))
    }

    private fun showBackupRestoreDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_backup_restore))
            .setItems(arrayOf("백업 파일로 저장", "백업 파일에서 복구")) { _, which ->
                if (which == 0) {
                    createDocLauncher.launch("gongsu_backup.json")
                } else {
                    openDocLauncher.launch(arrayOf("application/json"))
                }
            }
            .show()
    }

    // ---------------------------------------------------------------
    // 일괄변경 다이얼로그들 (현재 표시중인 달 전체에 적용)
    // ---------------------------------------------------------------
    private fun monthRange(): Pair<String, String> {
        val start = currentMonth.atDay(1)
        val end = currentMonth.atEndOfMonth()
        return DateUtils.toKey(start) to DateUtils.toKey(end)
    }

    private fun showBulkMemoDialog() {
        val et = EditText(this).apply { hint = "메모 내용" }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_bulk_memo) + " (${currentMonth.monthValue}월 전체)")
            .setView(et)
            .setPositiveButton("적용") { _, _ ->
                val (s, e) = monthRange()
                lifecycleScope.launch {
                    AppDatabase.getInstance(this@MainActivity).workEntryDao()
                        .bulkUpdateMemo(s, e, et.text.toString())
                    loadMonth()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showBulkGongsuDialog() {
        val et = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "공수 값 (예: 1.0)"
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_bulk_gongsu) + " (${currentMonth.monthValue}월 전체)")
            .setView(et)
            .setPositiveButton("적용") { _, _ ->
                val gongsu = et.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                val (s, e) = monthRange()
                lifecycleScope.launch {
                    AppDatabase.getInstance(this@MainActivity).workEntryDao()
                        .bulkUpdateGongsu(s, e, gongsu)
                    loadMonth()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showBulkPriceDialog() {
        val et = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "단가 (원)"
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_bulk_price) + " (${currentMonth.monthValue}월 전체)")
            .setView(et)
            .setPositiveButton("적용") { _, _ ->
                val price = et.text.toString().toLongOrNull() ?: return@setPositiveButton
                val (s, e) = monthRange()
                lifecycleScope.launch {
                    AppDatabase.getInstance(this@MainActivity).workEntryDao()
                        .bulkUpdateUnitPrice(s, e, price)
                    loadMonth()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showBulkSettledDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_bulk_settled) + " (${currentMonth.monthValue}월 전체)")
            .setItems(arrayOf("정산완료로 변경", "정산안됨으로 변경")) { _, which ->
                val settled = which == 0
                val (s, e) = monthRange()
                lifecycleScope.launch {
                    AppDatabase.getInstance(this@MainActivity).workEntryDao()
                        .bulkUpdateSettled(s, e, settled)
                    loadMonth()
                }
            }
            .show()
    }
}
