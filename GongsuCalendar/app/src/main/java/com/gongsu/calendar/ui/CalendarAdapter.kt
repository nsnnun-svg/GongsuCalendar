package com.gongsu.calendar.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gongsu.calendar.R
import com.gongsu.calendar.data.WorkEntry
import com.gongsu.calendar.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarAdapter(
    private val onClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.VH>() {

    private var days: List<LocalDate> = emptyList()
    private var month: YearMonth = YearMonth.now()
    private var entryMap: Map<String, WorkEntry> = emptyMap()
    private var groupNames: Map<Long, String> = emptyMap()

    private var showAmount = true
    private var showGongsu = true
    private var showMemo = true
    private var showGroupName = true
    private var showAdjacent = true

    fun submit(
        days: List<LocalDate>,
        currentMonth: YearMonth,
        entries: List<WorkEntry>,
        groupNames: Map<Long, String>,
        showAmount: Boolean,
        showGongsu: Boolean,
        showMemo: Boolean,
        showGroupName: Boolean,
        showAdjacentGongsu: Boolean
    ) {
        this.days = days
        this.month = currentMonth
        this.entryMap = entries.associateBy { it.date }
        this.groupNames = groupNames
        this.showAmount = showAmount
        this.showGongsu = showGongsu
        this.showMemo = showMemo
        this.showGroupName = showGroupName
        this.showAdjacent = showAdjacentGongsu
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val tvGroupName: TextView = view.findViewById(R.id.tvGroupName)
        val tvGongsu: TextView = view.findViewById(R.id.tvGongsu)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val dotMemo: View = view.findViewById(R.id.dotMemo)
        val cellRoot: View = view.findViewById(R.id.cellRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val date = days[position]
        val inMonth = YearMonth.from(date) == month

        holder.tvDay.text = date.dayOfMonth.toString()
        holder.tvDay.setTextColor(
            when (date.dayOfWeek) {
                DayOfWeek.SUNDAY -> 0xFFE53935.toInt()
                DayOfWeek.SATURDAY -> 0xFF1E88E5.toInt()
                else -> 0xFF222222.toInt()
            }
        )
        holder.cellRoot.alpha = if (inMonth) 1f else 0.35f
        holder.cellRoot.setBackgroundResource(
            if (date == LocalDate.now()) R.drawable.bg_cell_today else R.drawable.bg_cell
        )

        val e = entryMap[DateUtils.toKey(date)]
        val visible = e != null && (inMonth || showAdjacent)

        if (visible) {
            val entry = e!!

            if (showGroupName && entry.groupId != 0L) {
                holder.tvGroupName.visibility = View.VISIBLE
                holder.tvGroupName.text = groupNames[entry.groupId] ?: ""
            } else {
                holder.tvGroupName.visibility = View.GONE
            }

            if (entry.isHoliday) {
                holder.tvGongsu.visibility = View.VISIBLE
                holder.tvGongsu.text = "휴"
                holder.tvGongsu.setTextColor(0xFF757575.toInt())
                holder.tvAmount.visibility = View.GONE
            } else if (showGongsu) {
                holder.tvGongsu.visibility = View.VISIBLE
                holder.tvGongsu.text = DateUtils.formatGongsu(entry.gongsu)
                holder.tvGongsu.setTextColor(0xFF2E7D32.toInt())
                if (showAmount) {
                    holder.tvAmount.visibility = View.VISIBLE
                    holder.tvAmount.text = DateUtils.formatMoney(entry.totalAmount)
                    holder.tvAmount.setTextColor(
                        if (entry.isSettled) 0xFF1E88E5.toInt() else 0xFF444444.toInt()
                    )
                } else {
                    holder.tvAmount.visibility = View.GONE
                }
            } else {
                holder.tvGongsu.visibility = View.GONE
                if (showAmount) {
                    holder.tvAmount.visibility = View.VISIBLE
                    holder.tvAmount.text = DateUtils.formatMoney(entry.totalAmount)
                } else {
                    holder.tvAmount.visibility = View.GONE
                }
            }

            holder.dotMemo.visibility =
                if (showMemo && entry.memo.isNotBlank()) View.VISIBLE else View.GONE
        } else {
            holder.tvGroupName.visibility = View.GONE
            holder.tvGongsu.visibility = View.GONE
            holder.tvAmount.visibility = View.GONE
            holder.dotMemo.visibility = View.GONE
        }

        holder.root.setOnClickListener { onClick(date) }
    }
}
