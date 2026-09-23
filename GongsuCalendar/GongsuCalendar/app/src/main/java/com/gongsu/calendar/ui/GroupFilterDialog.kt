package com.gongsu.calendar.ui

import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gongsu.calendar.data.AppDatabase
import com.gongsu.calendar.prefs.AppPrefs

class GroupFilterDialog(
    private val activity: AppCompatActivity,
    private val onChanged: () -> Unit
) {
    private val db = AppDatabase.getInstance(activity)
    private val prefs = AppPrefs(activity)

    fun show() {
        val groups = db.getGroups()
        val names = ArrayList<String>()
        names.add("전체 보기")
        names.add("그룹없음")
        for (g in groups) names.add(g.name)

        var current = 0
        val active = prefs.activeGroupId
        if (active == 0L) {
            current = 1
        } else if (active > 0L) {
            val idx = groups.indexOfFirst { it.id == active }
            current = if (idx >= 0) idx + 2 else 0
        }

        AlertDialog.Builder(activity)
            .setTitle("그룹 필터")
            .setSingleChoiceItems(names.toTypedArray(), current) { d, which ->
                prefs.activeGroupId = when (which) {
                    0 -> -1L
                    1 -> 0L
                    else -> groups[which - 2].id
                }
                d.dismiss()
                onChanged()
            }
            .setPositiveButton("그룹 추가") { _, _ -> promptAdd() }
            .setNeutralButton("그룹 삭제") { _, _ -> promptDelete() }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun promptAdd() {
        val et = EditText(activity)
        et.hint = "그룹 이름"
        AlertDialog.Builder(activity)
            .setTitle("그룹 추가")
            .setView(UiHelper.padded(activity, et))
            .setPositiveButton("추가") { _, _ ->
                val name = et.text.toString().trim()
                if (name.isNotEmpty()) {
                    db.addGroup(name)
                    onChanged()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun promptDelete() {
        val groups = db.getGroups()
        if (groups.isEmpty()) {
            Toast.makeText(activity, "삭제할 그룹이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val names = ArrayList<String>()
        for (g in groups) names.add(g.name)
        AlertDialog.Builder(activity)
            .setTitle("삭제할 그룹 선택")
            .setItems(names.toTypedArray()) { _, which ->
                val g = groups[which]
                db.deleteGroup(g.id)
                if (prefs.activeGroupId == g.id) prefs.activeGroupId = -1L
                onChanged()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
