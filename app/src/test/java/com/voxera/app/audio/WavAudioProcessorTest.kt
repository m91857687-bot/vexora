package com.voxera.app.audio

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavAudioProcessorTest {
    @Test
    fun gainExportHasValidHeaderAndDoesNotModifySource(): Unit = runBlocking {
        val source = createFixture(sampleCount = 8_820, amplitude = 4_000)
        val originalBytes = source.readBytes()
        val destination = File(source.parentFile, "gain_output.wav")

        WavAudioProcessor.process(source, destination, WavAudioProcessor.Settings(gainDb = 6f))

        val output = destination.readBytes()
        assertEquals("RIFF", output.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WAVE", output.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals(output.size - 8, readUInt32(output, 4).toInt())
        assertEquals(output.size - 44, readUInt32(output, 40).toInt())
        assertTrue(readInt16(output, 44) > 7_000)
        assertTrue(originalBytes.contentEquals(source.readBytes()))
        source.parentFile?.deleteRecursively()
    }

    @Test
    fun echoExportIncludesTwoSecondTailAndReportsCompletion(): Unit = runBlocking {
        val source = createFixture(sampleCount = 4_410, amplitude = 8_000)
        val destination = File(source.parentFile, "echo_output.wav")
        val progress = mutableListOf<Int>()

        WavAudioProcessor.process(
            source,
            destination,
            WavAudioProcessor.Settings(echoMix = 0.4f, delayMs = 120),
            onProgress = progress::add
        )

        val expectedDataBytes = (4_410L + 44_100L * 2L) * 2L
        assertEquals(expectedDataBytes, readUInt32(destination.readBytes(), 40))
        assertTrue(progress.isNotEmpty())
        assertEquals(100, progress.last())
        source.parentFile?.deleteRecursively()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTruncatedWavInsteadOfSilentlyProcessingPartialAudio(): Unit = runBlocking {
        val source = createFixture(sampleCount = 1_000, amplitude = 1_000)
        val bytes = source.readBytes().copyOf(80)
        source.writeBytes(bytes)
        try {
            WavAudioProcessor.process(source, File(source.parentFile, "bad.wav"), WavAudioProcessor.Settings())
        } finally {
            source.parentFile?.deleteRecursively()
        }
    }

    @Test
    fun maximumGainDoesNotOverflowOrCrash(): Unit = runBlocking {
        val source = createFixture(sampleCount = 2_000, amplitude = 30_000)
        val destination = File(source.parentFile, "max_gain.wav")

        // 24 dB gain applied to high amplitude sample must be clamped between -32768 and 32767
        WavAudioProcessor.process(source, destination, WavAudioProcessor.Settings(gainDb = 24f))

        val output = destination.readBytes()
        val firstSample = readInt16(output, 44)
        assertTrue("Sample must be clamped to Short.MAX_VALUE", firstSample == 32767)
        source.parentFile?.deleteRecursively()
    }

    @Test
    fun zeroGainAndZeroEchoPreservesOriginalSampleValues(): Unit = runBlocking {
        val source = createFixture(sampleCount = 1_000, amplitude = 12_345)
        val destination = File(source.parentFile, "unity.wav")

        WavAudioProcessor.process(source, destination, WavAudioProcessor.Settings(gainDb = 0f, echoMix = 0f))

        val output = destination.readBytes()
        assertEquals(12_345, readInt16(output, 44))
        source.parentFile?.deleteRecursively()
    }

    private fun createFixture(sampleCount: Int, amplitude: Int): File {
        val directory = kotlin.io.path.createTempDirectory("voxera_wav_test_").toFile()
        val file = File(directory, "source.wav")
        val dataSize = sampleCount * 2
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
        file.outputStream().use { output ->
            output.write(header)
            repeat(sampleCount) { index ->
                val sample = if (index % 100 < 50) amplitude else -amplitude
                output.write(sample and 0xff)
                output.write((sample shr 8) and 0xff)
            }
        }
        return file
    }

    private fun readUInt32(bytes: ByteArray, offset: Int): Long =
        (0..3).fold(0L) { value, index -> value or ((bytes[offset + index].toLong() and 0xff) shl (8 * index)) }

    private fun readInt16(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)).toShort().toInt()
}
