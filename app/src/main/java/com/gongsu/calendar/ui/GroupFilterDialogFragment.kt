package com.gongsu.calendar.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gongsu.calendar.R
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.data.GroupEntity
import com.gongsu.calendar.prefs.AppPrefs
import kotlinx.coroutines.launch

class GroupFilterDialogFragment(
    private val onFilterChanged: () -> Unit
) : DialogFragment() {

    private lateinit var prefs: AppPrefs
    private lateinit var adapter: GroupAdapter
    private var groups: MutableList<GroupEntity> = mutableListOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_group_filter, null)

        prefs = AppPrefs(requireContext())

        val etNewGroupName = view.findViewById<android.widget.EditText>(R.id.etNewGroupName)
        val btnAdd = view.findViewById<android.widget.Button>(R.id.btnAddGroup)
        val rv = view.findViewById<RecyclerView>(R.id.rvGroups)
        val btnClose = view.findViewById<android.widget.Button>(R.id.btnCloseGroupDialog)

        adapter = GroupAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadGroups()

        btnAdd.setOnClickListener {
            val name = etNewGroupName.text.toString().trim()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(requireContext())
                    db.groupDao().upsert(GroupEntity(name = name))
                    etNewGroupName.text.clear()
                    loadGroups()
                }
            }
        }

        btnClose.setOnClickListener { dismiss() }

        return Dialog(requireContext()).apply {
            setContentView(view)
            setTitle(null)
        }
    }

    private fun loadGroups() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            groups = db.groupDao().getAll().toMutableList()
            adapter.submit(groups)
        }
    }

    inner class GroupAdapter : RecyclerView.Adapter<GroupAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val rb: RadioButton = v.findViewById(R.id.rbSelect)
            val tv: TextView = v.findViewById(R.id.tvGroupName)
            val del: ImageButton = v.findViewById(R.id.btnDeleteGroup)
        }

        private var list: List<GroupEntity> = emptyList()
        fun submit(l: List<GroupEntity>) {
            list = l
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_filter, parent, false)
            return VH(v)
        }

        override fun getItemCount(): Int = list.size + 1 // +1 for "전체"

        override fun onBindViewHolder(holder: VH, position: Int) {
            if (position == 0) {
                holder.tv.text = "전체 보기"
                holder.rb.isChecked = prefs.activeGroupId == -1L
                holder.del.visibility = View.INVISIBLE
                holder.itemView.setOnClickListener {
                    prefs.activeGroupId = -1L
                    onFilterChanged()
                    notifyDataSetChanged()
                }
                return
            }
            val group = list[position - 1]
            holder.tv.text = group.name
            holder.rb.isChecked = prefs.activeGroupId == group.id
            holder.del.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                prefs.activeGroupId = group.id
                onFilterChanged()
                notifyDataSetChanged()
            }
            holder.del.setOnClickListener {
                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(holder.itemView.context)
                    db.groupDao().delete(group)
                    if (prefs.activeGroupId == group.id) prefs.activeGroupId = -1L
                    loadGroups()
                    onFilterChanged()
                }
            }
        }
    }
}
