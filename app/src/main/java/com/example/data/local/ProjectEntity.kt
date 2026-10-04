package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val lastModified: Long,
    val canvasRatioName: String,
    val bgTypeName: String,
    val bgColorHex: Long,
    val durationMs: Long,
    val clipsCount: Int,
    val projectJson: String
)
