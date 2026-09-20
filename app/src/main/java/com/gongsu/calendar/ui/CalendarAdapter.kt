package com.gongsu.calendar.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gongsu.calendar.R
import com.gongsu.calendar.data.WorkEntry
import com.gongsu.calendar.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek

class CalendarAdapter(
    private val onDayClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var days: List<LocalDate> = emptyList()
    private var currentMonth: YearMonth = YearMonth.now()
    private var entriesByDate: Map<String, WorkEntry> = emptyMap()
    private var showAmount: Boolean = true
    private var showAdjacentGongsu: Boolean = true

    fun submit(
        days: List<LocalDate>,
        currentMonth: YearMonth,
        entries: List<WorkEntry>,
        showAmount: Boolean,
        showAdjacentGongsu: Boolean
    ) {
        this.days = days
        this.currentMonth = currentMonth
        this.entriesByDate = entries.associateBy { it.date }
        this.showAmount = showAmount
        this.showAdjacentGongsu = showAdjacentGongsu
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]
        val inMonth = YearMonth.from(date) == currentMonth
        val entry = entriesByDate[DateUtils.toKey(date)]
        holder.bind(date, inMonth, entry, showAmount, showAdjacentGongsu || inMonth, onDayClick)
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDayNumber)
        private val tvGongsu: TextView = itemView.findViewById(R.id.tvGongsu)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvMemo: TextView = itemView.findViewById(R.id.tvMemo)

        fun bind(
            date: LocalDate,
            inMonth: Boolean,
            entry: WorkEntry?,
            showAmount: Boolean,
            showGongsu: Boolean,
            onDayClick: (LocalDate) -> Unit
        ) {
            tvDay.text = date.dayOfMonth.toString()

            val dow = date.dayOfWeek
            val dayColor = when {
                !inMonth -> Color.parseColor("#BBBBBB")
                dow == DayOfWeek.SUNDAY -> Color.parseColor("#E53935")
                dow == DayOfWeek.SATURDAY -> Color.parseColor("#1E88E5")
                else -> Color.parseColor("#333333")
            }
            tvDay.setTextColor(dayColor)

            if (entry != null && showGongsu) {
                tvGongsu.visibility = View.VISIBLE
                tvGongsu.background = tvGongsu.background.mutate()
                if (entry.isHoliday) {
                    tvGongsu.text = "휴"
                    tvGongsu.background.setTint(Color.parseColor("#9E9E9E"))
                } else {
                    tvGongsu.text = DateUtils.formatGongsu(entry.gongsu)
                    tvGongsu.background.setTint(colorForGongsu(entry.gongsu))
                }

                if (showAmount && !entry.isHoliday) {
                    tvAmount.visibility = View.VISIBLE
                    tvAmount.text = DateUtils.formatMoney(entry.totalAmount)
                } else {
                    tvAmount.visibility = View.GONE
                }

                if (entry.memo.isNotBlank()) {
                    tvMemo.visibility = View.VISIBLE
                    tvMemo.text = entry.memo
                } else {
                    tvMemo.visibility = View.GONE
                }
            } else {
                tvGongsu.visibility = View.GONE
                tvAmount.visibility = View.GONE
                tvMemo.visibility = View.GONE
            }

            itemView.alpha = if (inMonth) 1.0f else 0.55f
            itemView.setOnClickListener { onDayClick(date) }
        }

        private fun colorForGongsu(g: Double): Int = when (g) {
            0.5 -> Color.parseColor("#3F51B5")
            1.0 -> Color.parseColor("#2196F3")
            1.5 -> Color.parseColor("#FFC107")
            2.0 -> Color.parseColor("#FF5722")
            else -> Color.parseColor("#FFA000")
        }
    }
}
