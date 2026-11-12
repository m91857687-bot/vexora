package com.voxera.app.presentation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.voxera.app.audio.RecordingSession
import com.voxera.app.audio.RecordingStatus
import com.voxera.app.audio.RecordingUiState
import com.voxera.app.data.preferences.AppPreferences
import com.voxera.app.data.preferences.VoxeraThemeMode
import com.voxera.app.data.recordings.RecordingEntity
import com.voxera.app.data.recordings.RecordingRepository
import com.voxera.app.data.recordings.VoxeraDatabase
import com.voxera.app.presentation.library.SortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VoxeraAppLogicTest {

    private lateinit var context: Context
    private lateinit var repository: RecordingRepository
    private lateinit var tempDir: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = RecordingRepository(context)
        tempDir = File(context.filesDir, "test_recordings").apply { mkdirs() }
    }

    @After
    fun tearDown() = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
        tempDir.deleteRecursively()
        VoxeraDatabase.get(context).clearAllTables()
    }

    @Test
    fun appPreferences_savesAndRetrievesLanguageAndTheme() = runBlocking {
        val prefs = AppPreferences(context)

        // Test Language
        prefs.setLanguage("ar")
        assertEquals("ar", prefs.getOrCreateLanguage())
        prefs.setLanguage("en")
        assertEquals("en", prefs.getOrCreateLanguage())

        // Test Theme
        prefs.setTheme(VoxeraThemeMode.MIDNIGHT)
        assertEquals(VoxeraThemeMode.MIDNIGHT, prefs.getTheme())
        prefs.setTheme(VoxeraThemeMode.ROYAL)
        assertEquals(VoxeraThemeMode.ROYAL, prefs.getTheme())
        prefs.setTheme(VoxeraThemeMode.CLASSIC)
        assertEquals(VoxeraThemeMode.CLASSIC, prefs.getTheme())
        prefs.setTheme(VoxeraThemeMode.SYSTEM)
        assertEquals(VoxeraThemeMode.SYSTEM, prefs.getTheme())
    }

    @Test
    fun recordingSession_stateTransitionsWorkReliably() {
        RecordingSession.set(RecordingUiState(status = RecordingStatus.IDLE))
        assertEquals(RecordingStatus.IDLE, RecordingSession.state.value.status)

        RecordingSession.set(RecordingUiState(status = RecordingStatus.STARTING))
        assertEquals(RecordingStatus.STARTING, RecordingSession.state.value.status)

        RecordingSession.update { it.copy(status = RecordingStatus.RECORDING, elapsedMillis = 1500L) }
        assertEquals(RecordingStatus.RECORDING, RecordingSession.state.value.status)
        assertEquals(1500L, RecordingSession.state.value.elapsedMillis)

        RecordingSession.update { it.copy(status = RecordingStatus.PAUSED) }
        assertEquals(RecordingStatus.PAUSED, RecordingSession.state.value.status)

        RecordingSession.update { it.copy(status = RecordingStatus.STOPPED, outputPath = "/test/path.wav") }
        assertEquals(RecordingStatus.STOPPED, RecordingSession.state.value.status)
        assertEquals("/test/path.wav", RecordingSession.state.value.outputPath)
    }

    @Test
    fun recordingRepository_crudAndSyncOperationsWorkAccurately() = runBlocking {
        val dao = VoxeraDatabase.get(context).recordingDao()

        // 1. Insert recording
        val entity1 = RecordingEntity(
            path = File(tempDir, "rec_01.wav").absolutePath,
            name = "rec_01",
            durationMillis = 5000L,
            sizeBytes = 441000L,
            modifiedAt = 1000L
        )
        val entity2 = RecordingEntity(
            path = File(tempDir, "rec_02.wav").absolutePath,
            name = "rec_02",
            durationMillis = 10000L,
            sizeBytes = 882000L,
            modifiedAt = 2000L
        )
        dao.upsertAll(listOf(entity1, entity2))

        val all = dao.observeAll().first()
        assertEquals(2, all.size)

        // 2. Query paths
        val paths = dao.getAllPaths()
        assertTrue(paths.contains(entity1.path))
        assertTrue(paths.contains(entity2.path))

        // 3. Delete recording
        dao.deleteByPath(entity1.path)
        val remaining = dao.observeAll().first()
        assertEquals(1, remaining.size)
        assertEquals("rec_02", remaining.first().name)
    }

    @Test
    fun recordingRepository_renameValidatesName() = runBlocking {
        val wavFile = createMockWavFile("original_name")
        val entity = RecordingEntity(
            path = wavFile.absolutePath,
            name = "original_name",
            durationMillis = 1000L,
            sizeBytes = wavFile.length(),
            modifiedAt = wavFile.lastModified()
        )
        VoxeraDatabase.get(context).recordingDao().upsert(entity)

        val updated = repository.rename(entity, "renamed_recording")
        assertEquals("renamed_recording", updated.name)
        assertTrue(File(updated.path).exists())
        assertFalse(wavFile.exists())
    }

    private fun createMockWavFile(name: String): File {
        val file = File(tempDir, "$name.wav")
        val dataSize = 1000
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(36 + dataSize)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(44_100)
            putInt(88_200)
            putShort(2)
            putShort(16)
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataSize)
        }.array()
        file.writeBytes(header + ByteArray(dataSize))
        return file
    }
}
