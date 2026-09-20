package com.gongsu.calendar.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.gongsu.calendar.R
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.data.GroupEntity
import com.gongsu.calendar.data.WorkEntry
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.util.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class EntryDialogFragment(
    private val date: LocalDate,
    private val onSaved: () -> Unit
) : DialogFragment() {

    private lateinit var prefs: AppPrefs
    private var gongsu: Double = 1.0
    private var isHoliday: Boolean = false
    private var unitPrice: Long = 0L
    private var selectedGroup: GroupEntity? = null
    private var groups: List<GroupEntity> = emptyList()
    private var existing: WorkEntry? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_entry, null)
        prefs = AppPrefs(requireContext())

        val tvDialogDate = view.findViewById<TextView>(R.id.tvDialogDate)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val tvGongsuValue = view.findViewById<TextView>(R.id.tvGongsuValue)
        val etPriceValue = view.findViewById<EditText>(R.id.etPriceValue)
        val tvTotalAmount = view.findViewById<TextView>(R.id.tvTotalAmount)
        val cbSettled = view.findViewById<CheckBox>(R.id.cbSettled)
        val btnGroupSelect = view.findViewById<Button>(R.id.btnGroupSelect)
        val etMemo = view.findViewById<EditText>(R.id.etMemo)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREA)
        tvDialogDate.text = date.format(formatter)

        unitPrice = prefs.defaultUnitPrice
        gongsu = prefs.defaultGongsu

        fun refreshTotal() {
            tvGongsuValue.text = if (isHoliday) "휴" else DateUtils.formatGongsu(gongsu)
            val total = if (isHoliday) 0L else (gongsu * unitPrice).toLong()
            tvTotalAmount.text = DateUtils.formatMoney(total)
        }

        etPriceValue.setText(unitPrice.toString())
        refreshTotal()

        fun applyPreset(value: Double, holiday: Boolean) {
            isHoliday = holiday
            gongsu = value
            refreshTotal()
        }

        view.findViewById<Button>(R.id.btnPreset05).setOnClickListener { applyPreset(0.5, false) }
        view.findViewById<Button>(R.id.btnPreset10).setOnClickListener { applyPreset(1.0, false) }
        view.findViewById<Button>(R.id.btnPreset15).setOnClickListener { applyPreset(1.5, false) }
        view.findViewById<Button>(R.id.btnPreset20).setOnClickListener { applyPreset(2.0, false) }
        view.findViewById<Button>(R.id.btnPresetHoliday).setOnClickListener { applyPreset(0.0, true) }

        view.findViewById<Button>(R.id.btnGongsuMinus).setOnClickListener {
            isHoliday = false
            gongsu = (gongsu - prefs.gongsuStep).coerceAtLeast(0.0)
            refreshTotal()
        }
        view.findViewById<Button>(R.id.btnGongsuPlus).setOnClickListener {
            isHoliday = false
            gongsu += prefs.gongsuStep
            refreshTotal()
        }

        view.findViewById<Button>(R.id.btnPriceMinus).setOnClickListener {
            unitPrice = (unitPrice - prefs.priceStep).coerceAtLeast(0L)
            etPriceValue.setText(unitPrice.toString())
            refreshTotal()
        }
        view.findViewById<Button>(R.id.btnPricePlus).setOnClickListener {
            unitPrice += prefs.priceStep
            etPriceValue.setText(unitPrice.toString())
            refreshTotal()
        }
        etPriceValue.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                unitPrice = s.toString().toLongOrNull() ?: 0L
                refreshTotal()
            }
        })

        btnGroupSelect.setOnClickListener {
            val names = mutableListOf("그룹없음")
            names.addAll(groups.map { it.name })
            AlertDialog.Builder(requireContext())
                .setTitle("그룹 선택")
                .setItems(names.toTypedArray()) { _, which ->
                    if (which == 0) {
                        selectedGroup = null
                        btnGroupSelect.text = "그룹없음"
                    } else {
                        selectedGroup = groups[which - 1]
                        btnGroupSelect.text = selectedGroup?.name
                    }
                }
                .show()
        }

        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).workEntryDao().deleteByDate(DateUtils.toKey(date))
                onSaved()
                dismiss()
            }
        }

        btnClose.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val entry = WorkEntry(
                date = DateUtils.toKey(date),
                gongsu = gongsu,
                isHoliday = isHoliday,
                unitPrice = unitPrice,
                memo = etMemo.text.toString(),
                groupId = selectedGroup?.id,
                isSettled = cbSettled.isChecked
            )
            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).workEntryDao().upsert(entry)
                onSaved()
                dismiss()
            }
        }

        // 그룹 목록 및 기존 데이터 로드
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            groups = db.groupDao().getAll()
            existing = db.workEntryDao().getByDate(DateUtils.toKey(date))
            existing?.let { e ->
                gongsu = e.gongsu
                isHoliday = e.isHoliday
                unitPrice = e.unitPrice
                etPriceValue.setText(unitPrice.toString())
                etMemo.setText(e.memo)
                cbSettled.isChecked = e.isSettled
                selectedGroup = groups.find { it.id == e.groupId }
                btnGroupSelect.text = selectedGroup?.name ?: "그룹없음"
                refreshTotal()
            }
        }

        return Dialog(requireContext()).apply {
            setContentView(view)
            setTitle(null)
        }
    }
}
