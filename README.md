# TenorSolo Master

App Android nativa (Kotlin + Jetpack Compose) para practicar solos de **sax tenor** en Jazz, Jazz latino y Cumbia.

- **Transpositor:** tonalidad de concierto → tonalidad del tenor (B♭, +2 semitonos), con notas de reposo, notas guía para atacar (3ª y 7ª) y color (9ª, 13ª) para primera, segunda, tercera, relativa y ii.
- **Fraseos:** banco por género, transportado a tu tonalidad, con duración y acentos.
- **Laboratorio:** metrónomo (swing, clave 2-3, cumbia) con rueda armónica, cuenta para el cambio de primera a segunda, límite por compases o minutos, y micrófono que te dice si la nota que tocas es del acorde.

## Estructura
- `music/Theory.kt` transposición, funciones, roles de notas
- `music/Phrases.kt` banco de fraseos
- `practice/PracticeViewModel.kt` temporizador con corrutinas (tiempo absoluto, sin deriva)
- `audio/` clic de metrónomo y detección de tono
- `ui/` pantallas Compose

Cada push compila el APK en GitHub Actions y lo publica en **Releases**.
