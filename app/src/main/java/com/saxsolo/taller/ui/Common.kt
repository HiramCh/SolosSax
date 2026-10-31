package com.saxsolo.taller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saxsolo.taller.music.Key
import com.saxsolo.taller.music.Mode
import com.saxsolo.taller.music.Notes
import com.saxsolo.taller.music.Role

val Bg = Color(0xFF141019)
val Surf = Color(0xFF1E1828)
val Surf2 = Color(0xFF2A2236)
val Fg = Color(0xFFF3EDF7)
val Muted = Color(0xFF9D8FAE)
val Brass = Color(0xFFE7B54D)
val Ink = Color(0xFF271A03)
val Coral = Color(0xFFFF7A59)
val Sky = Color(0xFF5FB8F0)
val Mint = Color(0xFF5FD18B)

fun roleColor(r: Role?) = when (r) {
    Role.REPOSO -> Brass
    Role.ATACAR -> Coral
    Role.COLOR -> Sky
    null -> Muted
}

@Composable
fun TenorTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(
        primary = Brass, onPrimary = Ink, background = Bg, onBackground = Fg,
        surface = Surf, onSurface = Fg, surfaceVariant = Surf2, onSurfaceVariant = Muted,
        secondaryContainer = Surf2, onSecondaryContainer = Brass,
    ),
    content = content,
)

@Composable
fun Cap(text: String, color: Color = Muted) =
    Text(text.uppercase(), color = color, fontSize = 12.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) = Column(
    modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Surf).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
) { content() }

/** Botón tipo píldora; seleccionado = latón. */
@Composable
fun Pill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) = Box(
    modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) Brass else Surf2)
        .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
) {
    Text(text, color = if (selected) Ink else Fg, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold, fontSize = 15.sp)
}

/** Nota grande con su grado debajo. */
@Composable
fun NoteChip(note: String, sub: String? = null, color: Color = Surf2, filled: Boolean = true, big: Boolean = false) = Column(
    Modifier.clip(RoundedCornerShape(12.dp))
        .background(if (filled) color else Color.Transparent)
        .border(2.dp, color, RoundedCornerShape(12.dp))
        .defaultMinSize(minWidth = if (big) 64.dp else 48.dp)
        .padding(horizontal = 10.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val txt = if (filled && color != Surf2) Bg else Fg
    Text(note, color = if (filled) txt else color, fontWeight = FontWeight.Black, fontSize = if (big) 24.sp else 18.sp)
    if (sub != null) Text(sub, color = if (filled) txt.copy(alpha = .75f) else color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun <T> PillRow(items: List<T>, selected: (T) -> Boolean, label: (T) -> String, onPick: (T) -> Unit) =
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(items) { _, it -> Pill(label(it), selected(it)) { onPick(it) } }
    }

/** Selector de tonalidad de concierto (12 notas) + mayor/menor. */
@Composable
fun KeyPicker(key: Key, onChange: (Key) -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Notes.CONCERT_KEYS.chunked(6).forEachIndexed { row, names ->
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            names.forEachIndexed { i, name ->
                val pc = row * 6 + i
                Pill(name, key.concert == pc, Modifier.weight(1f)) { onChange(key.copy(concert = pc)) }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Mode.entries.forEach { m ->
            Pill(m.label.replaceFirstChar { it.uppercase() }, key.mode == m, Modifier.weight(1f)) { onChange(key.copy(mode = m)) }
        }
    }
}

@Composable
fun Legend() = Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
    Role.entries.forEach { r ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.padding(0.dp).clip(RoundedCornerShape(4.dp)).background(roleColor(r)).padding(6.dp))
            Text(r.label, color = Fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
