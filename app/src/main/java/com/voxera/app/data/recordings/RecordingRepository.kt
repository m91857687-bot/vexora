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
        val directories = listOfNotNull(
            appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            File(appContext.filesDir, "recordings")
        )

        val discoveredFiles = mutableListOf<File>()
        for (directory in directories) {
            if (!directory.exists()) {
                directory.mkdirs()
            }
            if (directory.exists() && directory.isDirectory) {
                directory.walkTopDown()
                    .filter { it.isFile && it.extension.equals("wav", true) }
                    .forEach { discoveredFiles.add(it) }
            }
        }

        val entities = discoveredFiles.mapNotNull { file ->
            val length = file.length()
            if (length < 44L) {
                null
            } else {
                val duration = ((length - 44L) * 1_000L) / (44_100L * 2L)
                RecordingEntity(
                    path = file.absolutePath,
                    name = file.nameWithoutExtension,
                    durationMillis = duration.coerceAtLeast(0L),
                    sizeBytes = length,
                    modifiedAt = file.lastModified()
                )
            }
        }

        if (entities.isNotEmpty()) {
            dao.upsertAll(entities)
        }

        // Resilient sync: Only delete records if the file actually does not exist on disk
        val existingPaths = entities.mapTo(HashSet()) { it.path }
        dao.getAllPaths().filterNot(existingPaths::contains).forEach { stalePath ->
            if (!File(stalePath).exists()) {
                dao.deleteByPath(stalePath)
            }
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
