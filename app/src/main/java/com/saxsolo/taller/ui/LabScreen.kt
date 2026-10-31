package com.saxsolo.taller.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.saxsolo.taller.music.Func
import com.saxsolo.taller.music.Notes
import com.saxsolo.taller.music.Role
import com.saxsolo.taller.music.chordFor
import com.saxsolo.taller.music.chordNameConcert
import com.saxsolo.taller.music.chordNameTenor
import com.saxsolo.taller.music.classify
import com.saxsolo.taller.music.roleNotes
import com.saxsolo.taller.practice.Limit
import com.saxsolo.taller.practice.PRESETS
import com.saxsolo.taller.practice.PracticeViewModel
import com.saxsolo.taller.practice.Style
import kotlin.math.log2
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabScreen(vm: PracticeViewModel) {
    val key by vm.key.collectAsState()
    val lab by vm.lab.collectAsState()
    val hz by vm.pitch.hz.collectAsState()
    val ctx = LocalContext.current
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) vm.startMic() }
    val micOn by vm.pitch.on.collectAsState()

    val func = lab.current
    val chord = chordFor(func, key.mode)
    val change = lab.change
    val soon = lab.running && lab.countIn == 0 && change != null && change.beats <= 4

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${key.concertLabel} → ", color = Muted, fontSize = 15.sp)
            Text(key.tenorLabel, color = Brass, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("%02d:%02d".format(lab.elapsedSec / 60, lab.elapsedSec % 60), color = Muted, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        // --- Acorde actual + cuenta para el cambio ---
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(if (soon) Color(0xFF331C1C) else Surf)
                .border(2.dp, if (soon) Coral else Color.Transparent, RoundedCornerShape(24.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Cap(if (lab.countIn > 0) "Cuenta" else "${func.full} · ${func.degree}", Brass)
                Text(chordNameTenor(key, chord), color = Fg, fontSize = 52.sp, fontWeight = FontWeight.Black, lineHeight = 56.sp)
                Text("suena ${chordNameConcert(key, chord)}", color = Muted, fontSize = 13.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roleNotes(chord).filter { it.role != Role.COLOR }.forEach {
                        NoteChip(key.tenorName(it.semi), it.degree, roleColor(it.role))
                    }
                }
            }
            Column(
                Modifier.width(92.dp).clip(RoundedCornerShape(18.dp)).background(Surf2).padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    lab.countIn > 0 -> { Cap("entra en"); Big(lab.countIn.toString(), Fg) }
                    !lab.running -> { Cap("listo"); Big("▶", Muted) }
                    change == null -> { Cap("fijo"); Big("∞", Muted) }
                    soon -> { Cap("cambia", Coral); Big(change.beats.toString(), Coral) }
                    else -> { Cap("compases"); Big(((change.beats + 3) / 4).toString(), Fg) }
                }
                if (change != null) {
                    val nc = chordFor(change.next, key.mode)
                    Text("${change.next.short} ${chordNameTenor(key, nc)}", color = if (soon) Coral else Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Tiempos del compás ---
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp))
                    .background(if (lab.beat == i) (if (lab.countIn > 0) Fg else Brass) else Surf2))
            }
        }

        // --- Rueda armónica ---
        val cur = if (lab.running && lab.countIn == 0) lab.bar % lab.funcs.size else -1
        LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            itemsIndexed(lab.funcs) { i, f ->
                val on = i == cur
                Text("${f.short} ${chordNameTenor(key, chordFor(f, key.mode)).removeSuffix("maj7").removeSuffix("7")}",
                    color = if (on) Ink else Muted, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (on) Brass else Surf).padding(horizontal = 9.dp, vertical = 6.dp))
            }
        }

        // --- Lo que estás tocando ---
        val heard = hz
        if (micOn && heard != null) {
            val midi = 69 + 12 * log2(heard / 440f)
            val r = midi.roundToInt()
            val tenorPc = Notes.pc(r + 14)
            val role = classify(key, if (lab.running) func else Func.PRIMERA, tenorPc)
            val cents = ((midi - r) * 100).roundToInt()
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(roleColor(role)).padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(key.tenorName(tenorPc - key.tenor), color = Bg, fontSize = 40.sp, fontWeight = FontWeight.Black)
                Column {
                    Text(when (role) { Role.REPOSO -> "Reposo ✓"; Role.ATACAR -> "Nota guía ✓"; Role.COLOR -> "Color / paso"; null -> "Fuera" },
                        color = Bg, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(if (kotlin.math.abs(cents) <= 8) "afinada" else if (cents > 0) "alta +$cents" else "baja $cents", color = Bg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (micOn) {
            Text("Escuchando… toca tu sax", color = Mint, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        // --- Controles ---
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.clip(RoundedCornerShape(18.dp)).background(Surf), verticalAlignment = Alignment.CenterVertically) {
                Step("−") { vm.setBpm(lab.bpm - 5) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(lab.bpm.toString(), color = Fg, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("BPM", color = Muted, fontSize = 10.sp)
                }
                Step("+") { vm.setBpm(lab.bpm + 5) }
            }
            Spacer(Modifier.weight(1f))
            Round(if (micOn) "🎤" else "🎙", if (micOn) Mint else Surf2, 56) {
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) vm.startMic()
                else askMic.launch(Manifest.permission.RECORD_AUDIO)
            }
            Round(if (lab.running) "■" else "▶", Brass, 72, Ink) { vm.toggle() }
        }

        Panel {
            Cap("Ritmo del clic")
            PillRow(Style.entries, { it == lab.style }, { it.label }, vm::setStyle)
            Cap("Rueda armónica")
            PillRow(PRESETS.indices.toList(), { it == lab.preset }, { PRESETS[it].name }, vm::setPreset)
            Cap("Duración")
            PillRow(Limit.entries, { it == lab.limit }, { it.label }, vm::setLimit)
        }
    }
}

@Composable
private fun Big(t: String, c: Color) = Text(t, color = c, fontSize = 40.sp, fontWeight = FontWeight.Black)

@Composable
private fun Step(t: String, onClick: () -> Unit) = Box(Modifier.size(52.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
    Text(t, color = Fg, fontSize = 26.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun Round(t: String, bg: Color, sizeDp: Int, fg: Color = Fg, onClick: () -> Unit) = Box(
    Modifier.size(sizeDp.dp).clip(CircleShape).background(bg).clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) { Text(t, color = fg, fontSize = (sizeDp / 2.6).sp, fontWeight = FontWeight.Black) }
