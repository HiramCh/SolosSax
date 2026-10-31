package com.saxsolo.taller.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class Click(val hz: Double) { LO(900.0), HI(1500.0), ACC(2400.0) }

/** Clics del metrónomo precargados (MODE_STATIC) para disparar con baja latencia. */
class Clicker {
    private val sr = 44100
    private val tracks = Click.entries.associateWith { make(it.hz) }

    private fun make(freq: Double): AudioTrack {
        val n = sr * 30 / 1000
        val pcm = ShortArray(n) { i -> (sin(2 * PI * freq * i / sr) * exp(-i / (n / 5.0)) * 26000).toInt().toShort() }
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sr).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(n * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        t.write(pcm, 0, n)
        return t
    }

    fun play(c: Click) {
        val t = tracks[c] ?: return
        try {
            t.stop()
            t.reloadStaticData()
            t.play()
        } catch (_: IllegalStateException) {
        }
    }

    fun release() = tracks.values.forEach { it.release() }
}
