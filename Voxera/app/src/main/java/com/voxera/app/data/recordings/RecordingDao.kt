package com.voxera.app.data.recordings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY modifiedAt DESC")
    fun observeAll(): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RecordingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RecordingEntity)

    @Query("DELETE FROM recordings WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("SELECT path FROM recordings")
    suspend fun getAllPaths(): List<String>

    @Query("DELETE FROM recordings")
    suspend fun clear()
}
