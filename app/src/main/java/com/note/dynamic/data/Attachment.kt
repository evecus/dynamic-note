package com.note.dynamic.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val type: String,        // "image" or "file"
    val uri: String,         // content uri or file path
    val displayName: String, // original file name
    val mime: String = "*/*",
    val size: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
