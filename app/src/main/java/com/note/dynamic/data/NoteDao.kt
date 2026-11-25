package com.note.dynamic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE groupId IS NULL ORDER BY pinned DESC, updatedAt DESC")
    fun observeUngrouped(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE groupId = :groupId ORDER BY pinned DESC, updatedAt DESC")
    fun observeByGroup(groupId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%' ORDER BY updatedAt DESC")
    fun search(q: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    @Query("UPDATE notes SET pinned = :pinned, updatedAt = :ts WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, ts: Long)
}
