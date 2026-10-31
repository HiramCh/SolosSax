package com.saxsolo.taller.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/** Escucha el micrófono y publica la frecuencia (Hz) de lo que suena, o null si no hay nota clara. */
class PitchDetector {
    private val _hz = MutableStateFlow<Float?>(null)
    val hz: StateFlow<Float?> = _hz
    private val _on = MutableStateFlow(false)
    val on: StateFlow<Boolean> = _on
    private var job: Job? = null

    @SuppressLint("MissingPermission") // el permiso se pide en la UI antes de llamar
    fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return
        _on.value = true
        job = scope.launch(Dispatchers.Default) {
            val sr = 44100
            val size = 2048
            val minBuf = AudioRecord.getMinBufferSize(sr, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val rec = AudioRecord(MediaRecorder.AudioSource.MIC, sr, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, max(minBuf, size * 2))
            val buf = ShortArray(size)
            val f = FloatArray(size)
            try {
                rec.startRecording()
                while (isActive) {
                    var read = 0
                    while (read < size) {
                        val r = rec.read(buf, read, size - read)
                        if (r <= 0) break
                        read += r
                    }
                    for (i in 0 until size) f[i] = buf[i] / 32768f
                    val hz = detect(f, sr)
                    // rango real del sax tenor ≈ La1–Mi5: lo demás es ruido o el clic
                    _hz.value = if (hz in 95f..720f) hz else null
                }
            } finally {
                runCatching { rec.stop() }
                rec.release()
                _hz.value = null
                _on.value = false
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    companion object {
        /** Autocorrelación (port del ejemplo MIT de Chris Wilson, webaudiodemos pitchdetect). -1 = sin nota. */
        fun detect(input: FloatArray, sr: Int): Float {
            var rms = 0f
            for (v in input) rms += v * v
            if (sqrt(rms / input.size) < 0.012f) return -1f
            var r1 = 0
            var r2 = input.size - 1
            for (i in 0 until input.size / 2) if (abs(input[i]) < 0.2f) { r1 = i; break }
            for (i in 1 until input.size / 2) if (abs(input[input.size - i]) < 0.2f) { r2 = input.size - i; break }
            val b = input.copyOfRange(r1, r2)
            val n = b.size
            if (n < 4) return -1f
            val c = FloatArray(n)
            for (i in 0 until n) {
                var s = 0f
                for (j in 0 until n - i) s += b[j] * b[j + i]
                c[i] = s
            }
            var d = 0
            while (d < n - 1 && c[d] > c[d + 1]) d++
            var maxV = -1f
            var maxP = -1
            for (i in d until n) if (c[i] > maxV) { maxV = c[i]; maxP = i }
            if (maxP <= 0 || maxP >= n - 1) return -1f
            var t = maxP.toFloat()
            val x1 = c[maxP - 1]
            val x2 = c[maxP]
            val x3 = c[maxP + 1]
            val a = (x1 + x3 - 2 * x2) / 2
            val bb = (x3 - x1) / 2
            if (a != 0f) t -= bb / (2 * a)
            return sr / t
        }
    }
}
