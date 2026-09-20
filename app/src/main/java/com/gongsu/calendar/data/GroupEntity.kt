package com.gongsu.calendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_group")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val colorHex: String = "#2E7D32"
)
