package com.saxsolo.taller

import com.saxsolo.taller.music.Func
import com.saxsolo.taller.music.Key
import com.saxsolo.taller.music.Mode
import com.saxsolo.taller.music.Role
import com.saxsolo.taller.music.chordFor
import com.saxsolo.taller.music.chordNameConcert
import com.saxsolo.taller.music.chordNameTenor
import com.saxsolo.taller.music.classify
import com.saxsolo.taller.music.roleNotes
import com.saxsolo.taller.practice.changeInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TheoryTest {
    @Test fun transpone_tonalidades() {
        assertEquals("Re mayor", Key(0, Mode.MAYOR).tenorLabel)   // Do → Re
        assertEquals("Si menor", Key(9, Mode.MENOR).tenorLabel)   // La m → Si m
        assertEquals("Fa mayor", Key(3, Mode.MAYOR).tenorLabel)   // Mi♭ → Fa
        assertEquals("Do mayor", Key(10, Mode.MAYOR).tenorLabel)  // Si♭ → Do
    }

    @Test fun segunda_de_do() {
        val k = Key(0, Mode.MAYOR)
        val c = chordFor(Func.SEGUNDA, k.mode)
        assertEquals("Sol7", chordNameConcert(k, c))
        assertEquals("La7", chordNameTenor(k, c))
        val guia = roleNotes(c).filter { it.role == Role.ATACAR }.map { k.tenorName(it.semi) }
        assertEquals(listOf("Do#", "Sol"), guia)
    }

    @Test fun clasifica_lo_que_tocas() {
        val k = Key(0, Mode.MAYOR)            // sax en Re
        assertEquals(Role.REPOSO, classify(k, Func.PRIMERA, 2))   // Re
        assertEquals(Role.ATACAR, classify(k, Func.PRIMERA, 6))   // Fa#
        assertEquals(Role.ATACAR, classify(k, Func.SEGUNDA, 1))   // Do# sobre La7
        assertNull(classify(k, Func.PRIMERA, 3))                  // Re# fuera
    }

    @Test fun cuenta_para_el_cambio() {
        val f = listOf(Func.PRIMERA, Func.PRIMERA, Func.SEGUNDA, Func.SEGUNDA)
        assertEquals(8, changeInfo(f, 0, 0)!!.beats)
        assertEquals(1, changeInfo(f, 1, 3)!!.beats)
        assertEquals(Func.PRIMERA, changeInfo(f, 3, 0)!!.next)
        assertNull(changeInfo(listOf(Func.PRIMERA), 0, 0))
    }
}
