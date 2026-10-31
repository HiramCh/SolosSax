package com.saxsolo.taller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saxsolo.taller.music.Func
import com.saxsolo.taller.music.Key
import com.saxsolo.taller.music.chordFor
import com.saxsolo.taller.music.chordNameConcert
import com.saxsolo.taller.music.chordNameTenor
import com.saxsolo.taller.music.roleNotes
import com.saxsolo.taller.practice.PracticeViewModel

@Composable
fun TransposerScreen(vm: PracticeViewModel) {
    val key by vm.key.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Panel { Cap("Tonalidad de la canción"); KeyPicker(key, vm::setKey) } }
        item {
            Panel {
                Cap("Concierto ${key.concertLabel}")
                Text("Tu sax: ${key.tenorLabel}".uppercase(), color = Brass, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Legend()
            }
        }
        items(Func.entries) { f -> FunctionCard(key, f) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FunctionCard(key: Key, f: Func) {
    val c = chordFor(f, key.mode)
    Panel {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Cap("${f.full} · ${f.degree}", Brass)
            Text("suena ${chordNameConcert(key, c)}", color = Muted, fontSize = 13.sp)
        }
        Text(chordNameTenor(key, c), color = Fg, fontSize = 38.sp, fontWeight = FontWeight.Black)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            roleNotes(c).forEach { NoteChip(key.tenorName(it.semi), it.degree, roleColor(it.role), filled = it.role != com.saxsolo.taller.music.Role.COLOR) }
        }
        Cap("Escala ${c.scaleName}")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            c.scale.forEach { NoteChip(key.tenorName(c.rootSemi + it)) }
        }
    }
}
