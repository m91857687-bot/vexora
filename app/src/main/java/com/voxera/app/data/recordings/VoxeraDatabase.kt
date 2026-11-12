package com.voxera.app.data.recordings

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecordingEntity::class], version = 1, exportSchema = false)
abstract class VoxeraDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile private var INSTANCE: VoxeraDatabase? = null
        fun get(context: Context): VoxeraDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                VoxeraDatabase::class.java,
                "voxera_library.db"
            ).build().also { INSTANCE = it }
        }
    }
}
