package com.gongsu.calendar.ui

import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gongsu.calendar.R
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.data.WorkEntry
import com.gongsu.calendar.prefs.AppPrefs
import com.gongsu.calendar.util.DateUtils
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 날짜를 눌렀을 때 뜨는 입력창.
 * DialogFragment 대신 일반 클래스 + AlertDialog 를 써서 화면 회전/복원 시 종료되는 문제를 막음.
 */
class EntryDialog(
    private val activity: AppCompatActivity,
    initialDate: LocalDate,
    private val onDone: () -> Unit
) {
    private val db = AppDatabase.getInstance(activity)
    private val prefs = AppPrefs(activity)
    private val origKey = DateUtils.toKey(initialDate)

    private var date: LocalDate = initialDate
    private var gongsu = 1.0
    private var price = 0L
    private var holiday = false
    private var groupId = 0L
    private var updating = false

    fun show() {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_entry, null)

        val btnDate = view.findViewById<Button>(R.id.btnDate)
        val tvDate = view.findViewById<TextView>(R.id.tvDialogDate)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val btnHoliday = view.findViewById<Button>(R.id.btnPresetHoliday)
        val tvGongsu = view.findViewById<TextView>(R.id.tvGongsuValue)
        val etPrice = view.findViewById<EditText>(R.id.etPriceValue)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalAmount)
        val cbSettled = view.findViewById<CheckBox>(R.id.cbSettled)
        val btnGroup = view.findViewById<Button>(R.id.btnGroupSelect)
        val etMemo = view.findViewById<EditText>(R.id.etMemo)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        val presetValues = listOf(
            prefs.preset1.toDouble(), prefs.preset2.toDouble(),
            prefs.preset3.toDouble(), prefs.preset4.toDouble()
        )
        val presetButtons = listOf(
            view.findViewById<Button>(R.id.btnPreset05),
            view.findViewById<Button>(R.id.btnPreset10),
            view.findViewById<Button>(R.id.btnPreset15),
            view.findViewById<Button>(R.id.btnPreset20)
        )
        val presets = presetButtons.zip(presetValues)
        for ((btn, v) in presets) {
            btn.text = DateUtils.formatGongsu(v)
        }

        // 기존 기록 불러오기 (없으면 기본값)
        val existing = db.getEntry(origKey)
        if (existing != null) {
            gongsu = existing.gongsu
            price = existing.unitPrice
            holiday = existing.isHoliday
            groupId = existing.groupId
            cbSettled.isChecked = existing.isSettled
            etMemo.setText(existing.memo)
            btnDelete.isEnabled = true
        } else {
            gongsu = prefs.defaultGongsu.toDouble()
            price = prefs.defaultUnitPrice
            groupId = if (prefs.activeGroupId > 0L) prefs.activeGroupId else 0L
            btnDelete.isEnabled = false
        }

        fun total(): Long = if (holiday) 0L else Math.round(gongsu * price)

        fun groupName(): String {
            if (groupId == 0L) return "그룹없음"
            val g = db.getGroups().firstOrNull { it.id == groupId }
            return g?.name ?: "그룹없음"
        }

        fun refresh() {
            val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            tvDate.text = "${date.year}.${date.monthValue}.${date.dayOfMonth} ($dow)"
            tvGongsu.text = DateUtils.formatGongsu(gongsu)
            tvTotal.text = DateUtils.formatMoney(total())
            val want = price.toString()
            if (etPrice.text.toString() != want) {
                updating = true
                etPrice.setText(want)
                etPrice.setSelection(want.length)
                updating = false
            }
            for (p in presets) {
                p.first.alpha = if (!holiday && gongsu == p.second) 1f else 0.45f
            }
            btnHoliday.alpha = if (holiday) 1f else 0.45f
            btnGroup.text = groupName()
        }

        for (p in presets) {
            p.first.setOnClickListener {
                holiday = false
                gongsu = p.second
                refresh()
            }
        }
        btnHoliday.setOnClickListener {
            holiday = true
            refresh()
        }
        val gongsuStep = prefs.gongsuStep.toDouble()
        val priceStep = prefs.priceStep

        view.findViewById<Button>(R.id.btnGongsuMinus).setOnClickListener {
            gongsu = Math.max(0.0, gongsu - gongsuStep)
            holiday = false
            refresh()
        }
        view.findViewById<Button>(R.id.btnGongsuPlus).setOnClickListener {
            gongsu += gongsuStep
            holiday = false
            refresh()
        }
        view.findViewById<Button>(R.id.btnPriceMinus).setOnClickListener {
            price = Math.max(0L, price - priceStep)
            refresh()
        }
        view.findViewById<Button>(R.id.btnPricePlus).setOnClickListener {
            price += priceStep
            refresh()
        }

        etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (updating) return
                price = s?.toString()?.toLongOrNull() ?: 0L
                tvTotal.text = DateUtils.formatMoney(total())
            }
        })

        btnDate.setOnClickListener {
            DatePickerDialog(
                activity,
                DatePickerDialog.OnDateSetListener { _, y, m, d ->
                    date = LocalDate.of(y, m + 1, d)
                    refresh()
                },
                date.year, date.monthValue - 1, date.dayOfMonth
            ).show()
        }

        btnGroup.setOnClickListener {
            val groups = db.getGroups()
            val names = ArrayList<String>()
            names.add("그룹없음")
            for (g in groups) names.add(g.name)
            AlertDialog.Builder(activity)
                .setTitle("그룹 선택")
                .setItems(names.toTypedArray()) { _, which ->
                    groupId = if (which == 0) 0L else groups[which - 1].id
                    refresh()
                }
                .show()
        }

        val dialog = AlertDialog.Builder(activity).setView(view).create()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnDelete.setOnClickListener {
            db.deleteEntry(origKey)
            dialog.dismiss()
            onDone()
        }
        btnSave.setOnClickListener {
            val newKey = DateUtils.toKey(date)
            if (newKey != origKey) db.deleteEntry(origKey)
            db.upsert(
                WorkEntry(
                    date = newKey,
                    gongsu = gongsu,
                    unitPrice = price,
                    isHoliday = holiday,
                    isSettled = cbSettled.isChecked,
                    groupId = groupId,
                    memo = etMemo.text.toString()
                )
            )
            dialog.dismiss()
            onDone()
        }

        refresh()
        dialog.show()
    }
}
