package com.voxera.app.audio

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.pow
import kotlin.math.roundToInt

/** Offline, non-destructive PCM WAV processor. The source file is only read, never modified. */
object WavAudioProcessor {
    data class Settings(
        val gainDb: Float = 0f,
        val echoMix: Float = 0f,
        val delayMs: Int = 250
    )

    suspend fun process(
        source: File,
        destination: File,
        settings: Settings,
        onProgress: (Int) -> Unit = {}
    ) {
        require(source.isFile && source.canRead()) { "Source recording is not readable" }
        require(settings.gainDb in 0f..24f) { "Amplification must be between 0 and 24 dB" }
        require(settings.echoMix in 0f..0.70f) { "Echo amount is out of range" }
        require(settings.delayMs in 80..700) { "Echo delay must be between 80 and 700 ms" }

        val header = ByteArray(44)
        FileInputStream(source).use { input ->
            var offset = 0
            while (offset < header.size) {
                val count = input.read(header, offset, header.size - offset)
                if (count < 0) break
                offset += count
            }
            require(offset == header.size) { "WAV header is incomplete" }
        }
        val format = u16(header, 20)
        val channels = u16(header, 22)
        val sampleRate = u32(header, 24).toInt()
        val bits = u16(header, 34)
        val riffSize = u32(header, 4)
        val dataSize = u32(header, 40)
        require(String(header, 0, 4, Charsets.US_ASCII) == "RIFF" && String(header, 8, 4, Charsets.US_ASCII) == "WAVE") { "Unsupported WAV container" }
        require(format == 1 && channels == 1 && bits == 16 && sampleRate in 8_000..192_000) {
            "Only mono 16-bit PCM WAV recordings are supported"
        }
        require(String(header, 36, 4, Charsets.US_ASCII) == "data") { "Unsupported WAV data layout" }
        require(dataSize <= Int.MAX_VALUE.toLong()) { "WAV file is too large to process on this device" }
        val availableData = (source.length() - 44L).coerceAtLeast(0L)
        require(riffSize >= 36L + dataSize) { "WAV header contains inconsistent chunk sizes" }
        require(dataSize <= availableData) { "WAV audio data is truncated" }
        val pcmBytes = dataSize.toInt()
        require(pcmBytes % 2 == 0) { "WAV audio data is incomplete" }
        require(pcmBytes >= 2) { "Recording contains no audio samples" }

        destination.parentFile?.let { require(it.exists() || it.mkdirs()) { "Could not create output directory" } }
        val temp = File(destination.parentFile, destination.name + ".partial")
        val gain = 10.0.pow(settings.gainDb / 20.0).toFloat()
        val delaySamples = ((sampleRate * settings.delayMs) / 1000).coerceAtLeast(1)
        val echoLine = FloatArray(delaySamples)
        val wet = settings.echoMix
        val feedback = 0.32f
        var processedBytes = 0L
        var sampleIndex = 0
        val inputBuffer = ByteArray(32 * 1024)
        val outputBuffer = ByteArray(inputBuffer.size)
        var outIndex = 0

        try {
            BufferedInputStream(FileInputStream(source)).use { input ->
                var skipped = 0L
                while (skipped < 44L) {
                    val step = input.skip(44L - skipped)
                    if (step <= 0L) error("Could not reach WAV audio data")
                    skipped += step
                }
                BufferedOutputStream(FileOutputStream(temp)).use { output ->
                    val tailSamples = if (wet > 0f) sampleRate * 2 else 0
                    val outputDataBytes = pcmBytes.toLong() + tailSamples.toLong() * 2L
                    require(outputDataBytes <= 0xffff_ffffL - 36L) { "Processed recording is too large for WAV" }
                    output.write(makeHeader(outputDataBytes, sampleRate))
                    var remaining = pcmBytes
                    while (remaining > 0) {
                        currentCoroutineContext().ensureActive()
                        val requested = minOf(inputBuffer.size, remaining)
                        var count = 0
                        while (count < requested) {
                            val read = input.read(inputBuffer, count, requested - count)
                            if (read < 0) break
                            count += read
                        }
                        if (count == 0) break
                        require(count % 2 == 0) { "WAV audio data ended unexpectedly" }
                        var offset = 0
                        while (offset + 1 < count) {
                            val raw = ((inputBuffer[offset].toInt() and 0xff) or (inputBuffer[offset + 1].toInt() shl 8)).toShort().toInt()
                            val delayed = echoLine[sampleIndex % delaySamples]
                            val dry = raw.toFloat() * gain
                            val mixed = dry + delayed * wet
                            val clipped = mixed.coerceIn(-32768f, 32767f).roundToInt()
                            echoLine[sampleIndex % delaySamples] = (dry + delayed * feedback).coerceIn(-32768f, 32767f)
                            outputBuffer[outIndex++] = (clipped and 0xff).toByte()
                            outputBuffer[outIndex++] = ((clipped shr 8) and 0xff).toByte()
                            sampleIndex++
                            offset += 2
                        }
                        if (outIndex > 0) {
                            output.write(outputBuffer, 0, outIndex)
                            outIndex = 0
                        }
                        remaining -= count
                        processedBytes += count.toLong()
                        onProgress(((processedBytes * 100L) / pcmBytes.toLong()).toInt().coerceIn(0, 100))
                    }
                    if (wet > 0f) {
                        repeat(tailSamples) { tailIndex ->
                            if (tailIndex % 4096 == 0) currentCoroutineContext().ensureActive()
                            val delayed = echoLine[sampleIndex % delaySamples]
                            val tailSample = (delayed * wet).coerceIn(-32768f, 32767f).roundToInt()
                            echoLine[sampleIndex % delaySamples] = delayed * feedback
                            outputBuffer[outIndex++] = (tailSample and 0xff).toByte()
                            outputBuffer[outIndex++] = ((tailSample shr 8) and 0xff).toByte()
                            sampleIndex++
                            if (outIndex >= outputBuffer.size - 1) {
                                output.write(outputBuffer, 0, outIndex)
                                outIndex = 0
                            }
                        }
                    }
                    if (outIndex > 0) output.write(outputBuffer, 0, outIndex)
                }
            }
            // Replace the temporary file only after a complete successful render.
            if (destination.exists() && !destination.delete()) error("Could not replace existing output")
            check(temp.renameTo(destination)) { "Could not finalize processed WAV file" }
            onProgress(100)
        } catch (error: Throwable) {
            temp.delete()
            throw error
        }
    }

    private fun makeHeader(dataLength: Long, sampleRate: Int): ByteArray {
        val bytes = ByteArray(44)
        fun putText(offset: Int, value: String) { value.toByteArray(Charsets.US_ASCII).copyInto(bytes, offset) }
        fun put16(offset: Int, value: Int) { bytes[offset] = value.toByte(); bytes[offset + 1] = (value shr 8).toByte() }
        fun put32(offset: Int, value: Long) { repeat(4) { bytes[offset + it] = (value shr (8 * it)).toByte() } }
        putText(0, "RIFF"); put32(4, 36L + dataLength); putText(8, "WAVE")
        putText(12, "fmt "); put32(16, 16); put16(20, 1); put16(22, 1)
        put32(24, sampleRate.toLong()); put32(28, sampleRate.toLong() * 2L); put16(32, 2); put16(34, 16)
        putText(36, "data"); put32(40, dataLength)
        return bytes
    }

    private fun u16(bytes: ByteArray, offset: Int): Int = (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)
    private fun u32(bytes: ByteArray, offset: Int): Long = (0..3).fold(0L) { acc, i -> acc or ((bytes[offset + i].toLong() and 0xff) shl (8 * i)) }
}
