package com.saxsolo.taller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saxsolo.taller.music.Genre
import com.saxsolo.taller.music.Key
import com.saxsolo.taller.music.PHRASES
import com.saxsolo.taller.music.Phrase
import com.saxsolo.taller.music.chordFor
import com.saxsolo.taller.music.chordNameTenor
import com.saxsolo.taller.practice.PracticeViewModel

@Composable
fun PhrasesScreen(vm: PracticeViewModel) {
    val key by vm.key.collectAsState()
    var genre by rememberSaveable { mutableStateOf(Genre.JAZZ) }
    val list = PHRASES.filter { it.genre == genre && key.mode in it.modes }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Cap("Fraseos en ${key.tenorLabel} · tu sax")
            PillRow(Genre.entries, { it == genre }, { it.label }) { genre = it }
        }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list) { PhraseCard(key, it) }
        }
    }
}

@Composable
fun PhraseCard(key: Key, ph: Phrase) {
    val c = chordFor(ph.over, key.mode)
    Panel {
        Text(ph.name, color = Fg, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag("Sobre ${ph.over.short} · ${chordNameTenor(key, c)}", Brass)
            Tag(ph.feel, Sky)
        }
        // cada tiempo mide 56dp: el ancho del bloque es su duración
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            ph.notes.forEach { n ->
                val w = (n.beats * 56).dp - 3.dp
                if (n.semi == null) {
                    Box(Modifier.width(w).height(54.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, Surf2, RoundedCornerShape(10.dp)))
                } else {
                    val oct = if (n.semi >= 12) "↑" else if (n.semi < 0) "↓" else ""
                    Column(
                        Modifier.width(w).height(54.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (n.accent) Coral else Surf2),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (n.accent) Text(">", color = Bg, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(key.tenorName(c.rootSemi + n.semi) + oct, color = if (n.accent) Bg else Fg, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    }
                }
            }
        }
        Text(ph.tip, color = Muted, fontSize = 14.sp)
    }
}

@Composable
private fun Tag(text: String, color: androidx.compose.ui.graphics.Color) =
    Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, color, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
