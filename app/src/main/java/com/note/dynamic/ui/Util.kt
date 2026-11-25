package com.note.dynamic.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Format {
    private val dateFmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    private val dateShortFmt = SimpleDateFormat("MM/dd", Locale.getDefault())

    fun time(ts: Long): String = dateFmt.format(Date(ts))
    fun timeShort(ts: Long): String = dateShortFmt.format(Date(ts))

    fun size(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1fKB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1fMB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2fGB", bytes / (1024.0 * 1024.0 * 1024.0))
    }

    fun preview(text: String, max: Int = 80): String {
        val oneLine = text.replace("\n", " ").trim()
        return if (oneLine.length <= max) oneLine else oneLine.take(max) + "…"
    }
}

data class PickedFile(
    val uri: Uri,
    val name: String,
    val mime: String,
    val size: Long
)

fun Uri.queryFileInfo(context: Context): PickedFile {
    var name = "file"
    var size = 0L
    try {
        context.contentResolver.query(this, null, null, null, null)?.use { c ->
            val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
            if (nameIdx >= 0 && c.moveToFirst()) name = c.getString(nameIdx) ?: name
            if (sizeIdx >= 0 && c.moveToFirst()) size = c.getLong(sizeIdx)
        }
    } catch (_: Exception) { }
    val mime = context.contentResolver.getType(this) ?: "*/*"
    return PickedFile(this, name, mime, size)
}
