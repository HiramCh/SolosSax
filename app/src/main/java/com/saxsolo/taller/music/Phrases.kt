package com.saxsolo.taller.music

enum class Genre(val label: String) { JAZZ("Jazz"), LATINO("Jazz latino"), CUMBIA("Cumbia") }

/** semi relativo a la raíz del acorde de [Phrase.over]; null = silencio. beats en negras (0.5 = corchea). */
data class PNote(val semi: Int?, val beats: Float, val accent: Boolean = false)

data class Phrase(
    val name: String,
    val genre: Genre,
    val over: Func,
    val modes: Set<Mode>,
    val feel: String,
    val tip: String,
    val notes: List<PNote>,
)

private fun n(s: Int, b: Float = .5f, a: Boolean = false) = PNote(s, b, a)
private fun r(b: Float = .5f) = PNote(null, b)
private val MAY = setOf(Mode.MAYOR)
private val MEN = setOf(Mode.MENOR)
private val AMBOS = setOf(Mode.MAYOR, Mode.MENOR)

val PHRASES = listOf(
    // ---------- Jazz / Bebop ----------
    Phrase("Encierro a la 3ª", Genre.JAZZ, Func.PRIMERA, MAY, "Swing",
        "Rodea la 3ª por arriba y por abajo (cromático) y cae en ella en tiempo fuerte.",
        listOf(n(7), n(5), n(3), n(4, 1.5f, true), r(1f))),
    Phrase("Escala bebop que resuelve", Genre.JAZZ, Func.SEGUNDA, MAY, "Swing",
        "La 7ª mayor de paso mantiene las notas del acorde en los tiempos. Resuelve a la 3ª de la primera.",
        listOf(n(12, a = true), n(11), n(10), n(9), n(7, a = true), n(5), n(4), n(2), n(-3, 2f, true))),
    Phrase("Arpegio 3-9 en el ii", Genre.JAZZ, Func.II, MAY, "Swing",
        "Empieza en la ♭3 del ii y sube hasta la 9ª: suena moderno sin salirse.",
        listOf(n(3, a = true), n(7), n(10), n(14), n(12, 1f, true), r(1f))),
    Phrase("♭9 hacia la menor", Genre.JAZZ, Func.SEGUNDA, MEN, "Swing",
        "Arpegio con ♭9 que cae en la ♭3 de la primera menor.",
        listOf(n(4, a = true), n(7), n(10), n(13), n(8, 2f, true))),
    Phrase("Blues menor", Genre.JAZZ, Func.PRIMERA, MEN, "Swing",
        "Pentatónica menor con la ♭5 de paso; termina en la tónica.",
        listOf(n(0), n(3), n(5), n(6), n(7, 1f, true), n(3), n(0, 1.5f, true))),

    // ---------- Jazz latino ----------
    Phrase("Montuno en clave 2-3", Genre.LATINO, Func.PRIMERA, MEN, "Clave 2-3",
        "Entra a contratiempo y deja que la clave marque: notas cortas, acentos en los huecos.",
        listOf(r(), n(7, a = true), n(10), n(7), r(), n(9, a = true), n(7), n(3), n(5, 1f, true), n(3), n(0, 1.5f))),
    Phrase("Dórico con 6ª", Genre.LATINO, Func.PRIMERA, MEN, "Clave 2-3",
        "La 6ª mayor es el sabor del modo dórico: apóyate en ella.",
        listOf(n(0), n(2), n(3), n(5), n(7, a = true), n(9, 1f, true), n(7, 1.5f))),
    Phrase("Mixolidio a contratiempo", Genre.LATINO, Func.SEGUNDA, AMBOS, "Clave 3-2",
        "Solo toca en los 'y' del tiempo: 3ª, 5ª y ♭7 del acorde.",
        listOf(r(), n(4, a = true), r(), n(7, a = true), r(), n(10, a = true), n(9), n(7, 1f))),
    Phrase("Mambo de metales", Genre.LATINO, Func.PRIMERA, MAY, "Clave 3-2",
        "Repite la nota y respóndete un tono arriba, como sección de metales.",
        listOf(n(7, a = true), n(7), r(), n(9, a = true), r(), n(7), n(4, 1f, true))),

    // ---------- Cumbia ----------
    Phrase("Motivo de metales", Genre.CUMBIA, Func.PRIMERA, MAY, "Contratiempo",
        "Nota corta en cada 'y': el golpe característico de la cumbia.",
        listOf(r(), n(4, a = true), r(), n(4, a = true), r(), n(7, a = true), n(4), n(2, 1f))),
    Phrase("Pentatónica que baja", Genre.CUMBIA, Func.PRIMERA, MAY, "Cumbia",
        "Baja por la pentatónica y remata en la tónica con dos golpes.",
        listOf(n(9), n(7), n(4), n(2), n(0, 1f, true), r(), n(0, a = true))),
    Phrase("Respuesta en segunda", Genre.CUMBIA, Func.SEGUNDA, AMBOS, "Contratiempo",
        "Mismo golpe a contratiempo, ahora sobre la segunda. Cierra en su 3ª.",
        listOf(r(), n(7, a = true), r(), n(7, a = true), n(5), n(4), n(2, 1f))),
    Phrase("Cumbia menor", Genre.CUMBIA, Func.PRIMERA, MEN, "Contratiempo",
        "Pentatónica menor con acentos en el contratiempo; descansa en la tónica.",
        listOf(r(), n(3, a = true), r(), n(5, a = true), n(7), n(5), n(3), n(0, 1.5f, true))),
)
