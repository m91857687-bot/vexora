package com.voxera.app.data.recordings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey val path: String,
    val name: String,
    val durationMillis: Long,
    val sizeBytes: Long,
    val modifiedAt: Long
)
