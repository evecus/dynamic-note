package com.note.dynamic.data

import androidx.compose.ui.graphics.Color

/**
 * Color palettes used to differentiate groups from notes visually.
 * Groups use warm tints, notes use cool tints — distinct at a glance.
 */
object Palettes {

    // Groups: warm gradient-tinted cards
    val groupGradients: List<List<Color>> = listOf(
        listOf(Color(0xFFFFB199), Color(0xFFFF7E5F)),
        listOf(Color(0xFFFFD89B), Color(0xFFFF9A56)),
        listOf(Color(0xFFFFE29F), Color(0xFFFFA8A8)),
        listOf(Color(0xFFC2E9FB), Color(0xFFA1C4FD)),
        listOf(Color(0xFFD4FC79), Color(0xFF96E6A1)),
        listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC)),
        listOf(Color(0xFFFBC2EB), Color(0xFFA6C1EE)),
        listOf(Color(0xFFFDEB9F), Color(0xFFF6D365))
    )

    // Notes: softer, cooler material tints
    val noteTints: List<Color> = listOf(
        Color(0xFFE3F2FD),
        Color(0xFFE8F5E9),
        Color(0xFFFFF8E1),
        Color(0xFFFCE4EC),
        Color(0xFFF3E5F5),
        Color(0xFFE0F7FA),
        Color(0xFFF1F8E9),
        Color(0xFFEFEBE9)
    )

    val noteAccents: List<Color> = listOf(
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFFB8C00),
        Color(0xFFE53935),
        Color(0xFF8E24AA),
        Color(0xFF00ACC1),
        Color(0xFF7CB342),
        Color(0xFF6D4C41)
    )

    fun groupGradient(index: Int): List<Color> =
        groupGradients[index % groupGradients.size]

    fun noteTint(index: Int): Color =
        noteTints[index % noteTints.size]

    fun noteAccent(index: Int): Color =
        noteAccents[index % noteAccents.size]
}
