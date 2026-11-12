package com.voxera.app.data.recordings

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class RecordingRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = VoxeraDatabase.get(appContext).recordingDao()

    fun observeRecordings(): Flow<List<RecordingEntity>> = dao.observeAll()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val directory = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: File(appContext.filesDir, "recordings")
        if (!directory.exists() && !directory.mkdirs()) {
            throw java.io.IOException("Could not access the recordings directory")
        }
        val files = directory.listFiles()
            ?: throw java.io.IOException("Could not read the recordings directory")
        val entities = files.filter { it.isFile && it.extension.equals("wav", true) }.map { file ->
            RecordingEntity(
                path = file.absolutePath,
                name = file.nameWithoutExtension,
                durationMillis = ((file.length() - 44L).coerceAtLeast(0L) * 1_000L) / (44_100L * 2L),
                sizeBytes = file.length(),
                modifiedAt = file.lastModified()
            )
        }
        if (entities.isNotEmpty()) dao.upsertAll(entities)
        val existingPaths = entities.mapTo(HashSet()) { it.path }
        dao.getAllPaths().filterNot(existingPaths::contains).forEach { stalePath ->
            dao.deleteByPath(stalePath)
        }
    }

    suspend fun rename(item: RecordingEntity, newName: String): RecordingEntity = withContext(Dispatchers.IO) {
        val cleanName = newName.trim().removeSuffix(".wav").take(80)
        require(cleanName.isNotBlank()) { "Name cannot be empty" }
        require(cleanName.none { it in "/\\\\:*?\"<>|" }) { "Name contains invalid characters" }
        val oldFile = File(item.path)
        check(oldFile.exists()) { "Recording file not found" }
        val newFile = File(oldFile.parentFile, "$cleanName.wav")
        require(!newFile.exists() || newFile.absolutePath == oldFile.absolutePath) { "A recording with this name already exists" }
        if (newFile.absolutePath != oldFile.absolutePath) check(oldFile.renameTo(newFile)) { "Could not rename recording" }
        val updated = item.copy(path = newFile.absolutePath, name = cleanName, sizeBytes = newFile.length(), modifiedAt = newFile.lastModified())
        dao.deleteByPath(item.path)
        dao.upsert(updated)
        updated
    }

    suspend fun delete(item: RecordingEntity) = withContext(Dispatchers.IO) {
        val file = File(item.path)
        check(!file.exists() || file.delete()) { "Could not delete recording" }
        dao.deleteByPath(item.path)
    }

    fun fileFor(item: RecordingEntity): File = File(item.path)
}
