package com.note.dynamic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(att: Attachment): Long

    @Update
    suspend fun update(att: Attachment)

    @Delete
    suspend fun delete(att: Attachment)

    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY createdAt ASC")
    fun observeByNote(noteId: Long): Flow<List<Attachment>>

    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY createdAt ASC")
    suspend fun listByNote(noteId: Long): List<Attachment>

    @Query("DELETE FROM attachments WHERE noteId = :noteId")
    suspend fun deleteByNote(noteId: Long)
}
