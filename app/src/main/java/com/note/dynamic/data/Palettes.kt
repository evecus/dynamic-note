package com.note.dynamic.data

import androidx.compose.ui.graphics.Color

/**
 * 用于区分分组与便签的视觉色板。
 * 分组采用柔和渐变卡片，便签采用清爽的浅色调。
 */
object Palettes {

    /** 分组：柔和渐变，用于分组卡片与对话框。 */
    val groupGradients: List<List<Color>> = listOf(
        listOf(Color(0xFF8E9BFF), Color(0xFF5B62E8)),   // 靛蓝
        listOf(Color(0xFFFFB199), Color(0xFFFF7E5F)),   // 珊瑚
        listOf(Color(0xFFFFD48C), Color(0xFFFF9E5E)),   // 暖橙
        listOf(Color(0xFF9EE8D0), Color(0xFF4FBFA0)),   // 薄荷
        listOf(Color(0xFFC6B4FF), Color(0xFF8E7BE8)),   // 薰衣草
        listOf(Color(0xFF8FD3F4), Color(0xFF5C9FE8)),   // 天蓝
        listOf(Color(0xFFF9A8C4), Color(0xFFE56B9B)),   // 玫粉
        listOf(Color(0xFFD6E47E), Color(0xFF9BC158))    // 青柠
    )

    /** 便签：清爽浅色调 */
    val noteTints: List<Color> = listOf(
        Color(0xFFEAF0FE),
        Color(0xFFE8F6EF),
        Color(0xFFFFF5E3),
        Color(0xFFFDECF0),
        Color(0xFFF1EDFC),
        Color(0xFFE6F6FA),
        Color(0xFFEFF6E6),
        Color(0xFFF3EEE9)
    )

    /** 便签强调色（与 noteTints 一一对应） */
    val noteAccents: List<Color> = listOf(
        Color(0xFF4F63D8),
        Color(0xFF2E9E72),
        Color(0xFFEE8B21),
        Color(0xFFDC4E70),
        Color(0xFF8159C6),
        Color(0xFF1E9CB0),
        Color(0xFF6FA32E),
        Color(0xFF8C6B4F)
    )

    fun groupGradient(index: Int): List<Color> = groupGradients[index.floorMod(groupGradients.size)]

    fun noteTint(index: Int): Color = noteTints[index.floorMod(noteTints.size)]

    fun noteAccent(index: Int): Color = noteAccents[index.floorMod(noteAccents.size)]

    private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
}
