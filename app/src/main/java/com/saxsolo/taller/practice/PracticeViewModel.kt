package com.saxsolo.taller.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saxsolo.taller.audio.Click
import com.saxsolo.taller.audio.Clicker
import com.saxsolo.taller.audio.PitchDetector
import com.saxsolo.taller.music.Func
import com.saxsolo.taller.music.Key
import com.saxsolo.taller.music.Mode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Patrón de clics por corchea (pos 0..7 del compás). */
enum class Style(val label: String) {
    SWING("Swing"), CLAVE("Clave 2-3"), CUMBIA("Cumbia");

    fun pattern(bar: Int, pos: Int): Click? = when (this) {
        SWING -> when (pos) { 2, 6 -> Click.HI; 0, 4 -> Click.LO; else -> null } // golpe en 2 y 4
        CLAVE -> if (bar % 2 == 0) {                                              // lado 2: tiempos 2 y 3
            when (pos) { 2, 4 -> Click.HI; else -> null }
        } else {                                                                  // lado 3: 1, 2-y, 4
            when (pos) { 0, 3, 6 -> Click.HI; else -> null }
        }
        CUMBIA -> when {                                                          // bombo en 1 y 3, contratiempos
            pos % 2 == 1 -> Click.HI
            pos == 0 || pos == 4 -> Click.LO
            else -> null
        }
    }
}

enum class Limit(val label: String, val bars: Int? = null, val secs: Int? = null) {
    LIBRE("Libre"), C16("16 comp.", bars = 16), C32("32 comp.", bars = 32), M2("2 min", secs = 120), M5("5 min", secs = 300)
}

data class Preset(val name: String, val funcs: List<Func>)

private fun p(name: String, code: String) = Preset(name, code.map {
    when (it) { 'P' -> Func.PRIMERA; 'S' -> Func.SEGUNDA; 'T' -> Func.TERCERA; 'R' -> Func.RELATIVA; else -> Func.II }
})

val PRESETS = listOf(
    p("1ª y 2ª", "PPPPSSSS"),
    p("1ª y 2ª rápida", "PPSS"),
    p("1ª 3ª 2ª", "PPTTSSPP"),
    p("Cumbia", "PTSP"),
    p("ii – V – I", "ISPP"),
    p("Ranchera", "PPPPSSSSSSSSPPPP"),
    p("Blues 12", "PPPPTTPPSTPS"),
    p("Balada", "PSRT"),
)

data class Change(val next: Func, val beats: Int)

/** Cuántos tiempos faltan para que cambie la función armónica; null si la rueda no cambia nunca. */
fun changeInfo(funcs: List<Func>, bar: Int, beat: Int): Change? {
    val cur = funcs[bar % funcs.size]
    var k = 1
    while (k <= funcs.size && funcs[(bar + k) % funcs.size] == cur) k++
    if (k > funcs.size) return null
    return Change(funcs[(bar + k) % funcs.size], k * 4 - beat)
}

data class LabState(
    val running: Boolean = false,
    val bpm: Int = 100,
    val style: Style = Style.CUMBIA,
    val preset: Int = 0,
    val limit: Limit = Limit.LIBRE,
    val countIn: Int = 0,   // 4..1 durante la cuenta, 0 tocando
    val bar: Int = 0,
    val beat: Int = -1,     // 0..3, -1 detenido
    val elapsedSec: Int = 0,
) {
    val funcs get() = PRESETS[preset].funcs
    val current get() = funcs[bar % funcs.size]
    val change get() = changeInfo(funcs, bar, beat.coerceAtLeast(0))
}

class PracticeViewModel : ViewModel() {
    private val _key = MutableStateFlow(Key(0, Mode.MAYOR))
    val key: StateFlow<Key> = _key.asStateFlow()
    fun setKey(k: Key) { _key.value = k }

    private val _lab = MutableStateFlow(LabState())
    val lab: StateFlow<LabState> = _lab.asStateFlow()

    private val clicker = Clicker()
    val pitch = PitchDetector()
    private var job: Job? = null

    fun setBpm(v: Int) = restartWith { it.copy(bpm = v.coerceIn(40, 240)) }
    fun setStyle(s: Style) = restartWith { it.copy(style = s) }
    fun setPreset(i: Int) = restartWith { it.copy(preset = i) }
    fun setLimit(l: Limit) = restartWith { it.copy(limit = l) }
    fun startMic() = pitch.start(viewModelScope)

    private fun restartWith(f: (LabState) -> LabState) {
        val wasRunning = _lab.value.running
        stop()
        _lab.update(f)
        if (wasRunning) start()
    }

    fun toggle() = if (_lab.value.running) stop() else start()

    private fun start() {
        _lab.update { it.copy(running = true, countIn = 4, bar = 0, beat = 0, elapsedSec = 0) }
        val eighthNs = 30_000_000_000L / _lab.value.bpm
        job = viewModelScope.launch(Dispatchers.Default) {
            val t0 = System.nanoTime() + 80_000_000L
            var tick = 0
            while (isActive) {
                // tiempo absoluto: cada corchea se agenda desde t0, así el pulso no se atrasa
                val waitMs = (t0 + tick * eighthNs - System.nanoTime()) / 1_000_000
                if (waitMs > 0) delay(waitMs)
                if (!onTick(tick, t0)) break
                tick++
            }
        }
    }

    /** Devuelve false cuando se cumple el límite de práctica. */
    private fun onTick(tick: Int, t0: Long): Boolean {
        val s = _lab.value
        if (tick < 8) { // un compás de cuenta
            if (tick % 2 == 0) {
                clicker.play(if (tick == 0) Click.ACC else Click.LO)
                _lab.update { it.copy(countIn = 4 - tick / 2, beat = tick / 2) }
            }
            return true
        }
        val t = tick - 8
        val bar = t / 8
        val pos = t % 8
        val elapsed = ((System.nanoTime() - t0) / 1_000_000_000L).toInt()
        val lb = s.limit.bars
        val ls = s.limit.secs
        if ((lb != null && bar >= lb) || (ls != null && elapsed >= ls)) {
            stop()
            return false
        }
        val f = s.funcs
        val chordChange = pos == 0 && (bar == 0 || f[bar % f.size] != f[(bar - 1) % f.size])
        (if (chordChange) Click.ACC else s.style.pattern(bar, pos))?.let(clicker::play)
        if (pos % 2 == 0) _lab.update { it.copy(countIn = 0, bar = bar, beat = pos / 2, elapsedSec = elapsed) }
        return true
    }

    fun stop() {
        job?.cancel()
        job = null
        _lab.update { it.copy(running = false, countIn = 0, beat = -1, bar = 0) }
    }

    override fun onCleared() {
        stop()
        pitch.stop()
        clicker.release()
    }
}
