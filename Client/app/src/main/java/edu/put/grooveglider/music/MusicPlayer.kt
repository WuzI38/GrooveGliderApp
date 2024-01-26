package edu.put.grooveglider.music

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object MusicPlayer {
    // Set encoding, rate and channels
    private const val encoding = AudioFormat.ENCODING_PCM_16BIT
    private const val sampleRate = 44100
    private const val channels = AudioFormat.CHANNEL_OUT_STEREO

    // Initialize AudioAttributes
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    // Initialize AudioFormat with the set format
    private val audioFormat = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setEncoding(encoding)
        .setChannelMask(channels)
        .build()

    private val audioSize = AudioTrack.getMinBufferSize( // 3896
        sampleRate,
        channels,
        encoding
    )

    // Initialize AudioTrack
    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(audioAttributes)
        .setAudioFormat(audioFormat)
        .setBufferSizeInBytes(audioSize) // This is the buffer size used by audioTrack, not the custom buffer size
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    // Start playing
    suspend fun start() = withContext(Dispatchers.IO) {
        audioTrack.play()
    }

    // Stop playing
    suspend fun stop() = withContext(Dispatchers.IO) {
        audioTrack.stop()
        audioTrack.flush()
    }

    // Play a single bytearray of music data
    suspend fun play(buffer: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val iterations = buffer.size / audioSize
            val remaining = buffer.size - iterations * audioSize

            // Play music for as long as there are values initialized by edu.put.grooveglider.internet.UDPHandler
            for (i in 0..iterations) {
                if (i < iterations) audioTrack.write(buffer, i * audioSize, audioSize) else audioTrack.write(buffer, i * audioSize, remaining)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}