package com.saxsolo.taller.music

enum class Mode(val label: String) { MAYOR("mayor"), MENOR("menor") }

object Notes {
    val SHARP = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
    val FLAT = listOf("Do", "Re♭", "Re", "Mi♭", "Mi", "Fa", "Sol♭", "Sol", "La♭", "La", "Si♭", "Si")
    val CONCERT_KEYS = listOf("Do", "Re♭", "Re", "Mi♭", "Mi", "Fa", "Fa#", "Sol", "La♭", "La", "Si♭", "Si")

    fun pc(n: Int) = ((n % 12) + 12) % 12
    fun name(pc: Int, flats: Boolean) = (if (flats) FLAT else SHARP)[pc(pc)]

    /** Tonalidades que se escriben con bemoles. */
    fun prefersFlats(tonic: Int, mode: Mode) =
        pc(tonic) in (if (mode == Mode.MENOR) setOf(2, 7, 0, 5, 10, 3) else setOf(5, 10, 3, 8, 1, 6))
}

/** Sax tenor en Si♭: suena una 9ª mayor abajo, así que se lee 2 semitonos (+ 1 octava) arriba del concierto. */
const val TENOR_OFFSET = 2

fun toTenor(concertPc: Int) = Notes.pc(concertPc + TENOR_OFFSET)

data class Key(val concert: Int, val mode: Mode) {
    val tenor get() = toTenor(concert)
    private val tenorFlats get() = Notes.prefersFlats(tenor, mode)
    private val concertFlats get() = Notes.prefersFlats(concert, mode)

    /** Nombre de la nota para el sax, en semitonos desde la tónica de la tonalidad. */
    fun tenorName(semi: Int) = Notes.name(tenor + semi, tenorFlats)
    fun concertName(semi: Int) = Notes.name(concert + semi, concertFlats)
    val concertLabel get() = "${concertName(0)} ${mode.label}"
    val tenorLabel get() = "${tenorName(0)} ${mode.label}"
}

enum class Quality(val suffix: String, val intervals: List<Int>) {
    MAJ7("maj7", listOf(0, 4, 7, 11)),
    DOM7("7", listOf(0, 4, 7, 10)),
    MIN7("m7", listOf(0, 3, 7, 10)),
    HALFDIM("m7♭5", listOf(0, 3, 6, 10)),
}

/** Funciones armónicas como las nombran los músicos: primera, segunda, tercera… */
enum class Func(val short: String, val full: String, val degree: String) {
    PRIMERA("1ª", "Primera", "I"),
    SEGUNDA("2ª", "Segunda", "V7"),
    TERCERA("3ª", "Tercera", "IV"),
    RELATIVA("Rel", "Relativa", "vi"),
    II("ii", "Preparación", "ii"),
}

/** rootSemi y scale en semitonos; scale relativa a la raíz del acorde. */
data class Chord(val rootSemi: Int, val quality: Quality, val scaleName: String, val scale: List<Int>)

fun chordFor(f: Func, m: Mode): Chord = when (m) {
    Mode.MAYOR -> when (f) {
        Func.PRIMERA -> Chord(0, Quality.MAJ7, "Jónica", listOf(0, 2, 4, 5, 7, 9, 11))
        Func.SEGUNDA -> Chord(7, Quality.DOM7, "Mixolidia", listOf(0, 2, 4, 5, 7, 9, 10))
        Func.TERCERA -> Chord(5, Quality.MAJ7, "Lidia", listOf(0, 2, 4, 6, 7, 9, 11))
        Func.RELATIVA -> Chord(9, Quality.MIN7, "Eólica", listOf(0, 2, 3, 5, 7, 8, 10))
        Func.II -> Chord(2, Quality.MIN7, "Dórica", listOf(0, 2, 3, 5, 7, 9, 10))
    }
    Mode.MENOR -> when (f) {
        Func.PRIMERA -> Chord(0, Quality.MIN7, "Dórica", listOf(0, 2, 3, 5, 7, 9, 10))
        Func.SEGUNDA -> Chord(7, Quality.DOM7, "Mixolidia ♭9 ♭13", listOf(0, 1, 4, 5, 7, 8, 10))
        Func.TERCERA -> Chord(5, Quality.MIN7, "Dórica", listOf(0, 2, 3, 5, 7, 9, 10))
        Func.RELATIVA -> Chord(8, Quality.MAJ7, "Lidia", listOf(0, 2, 4, 6, 7, 9, 11))
        Func.II -> Chord(2, Quality.HALFDIM, "Locria", listOf(0, 1, 3, 5, 6, 8, 10))
    }
}

/** Reposo = donde resuelves o te quedas; Atacar = notas guía (3ª y 7ª); Color = tensiones y notas de paso. */
enum class Role(val label: String) { REPOSO("Reposo"), ATACAR("Atacar"), COLOR("Color") }

/** semi: semitonos desde la tónica de la tonalidad. */
data class RoleNote(val semi: Int, val degree: String, val role: Role)

fun roleNotes(c: Chord): List<RoleNote> {
    val i = c.quality.intervals
    val ninth = c.scale[1]
    val thirteenth = c.scale[5]
    return listOf(
        RoleNote(c.rootSemi + i[0], "1", Role.REPOSO),
        RoleNote(c.rootSemi + i[2], if (i[2] == 6) "♭5" else "5", Role.REPOSO),
        RoleNote(c.rootSemi + i[1], if (i[1] == 3) "♭3" else "3", Role.ATACAR),
        RoleNote(c.rootSemi + i[3], if (i[3] == 11) "7" else "♭7", Role.ATACAR),
        RoleNote(c.rootSemi + ninth, if (ninth == 1) "♭9" else "9", Role.COLOR),
        RoleNote(c.rootSemi + thirteenth, if (thirteenth == 8) "♭13" else "13", Role.COLOR),
    )
}

fun chordNameTenor(k: Key, c: Chord) = k.tenorName(c.rootSemi) + c.quality.suffix
fun chordNameConcert(k: Key, c: Chord) = k.concertName(c.rootSemi) + c.quality.suffix

/** Qué es la nota que estás tocando (clase de nota escrita para tenor) sobre el acorde actual; null = fuera. */
fun classify(k: Key, f: Func, tenorPc: Int): Role? {
    val c = chordFor(f, k.mode)
    val rel = Notes.pc(tenorPc - k.tenor - c.rootSemi)
    return when (rel) {
        c.quality.intervals[0], c.quality.intervals[2] -> Role.REPOSO
        c.quality.intervals[1], c.quality.intervals[3] -> Role.ATACAR
        in c.scale -> Role.COLOR
        else -> null
    }
}
