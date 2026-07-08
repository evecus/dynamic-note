package com.note.dynamic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: NoteGroup): Long

    @Update
    suspend fun update(group: NoteGroup)

    @Delete
    suspend fun delete(group: NoteGroup)

    @Query("SELECT * FROM groups ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteGroup>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getById(id: Long): NoteGroup?

    @Query("SELECT COUNT(*) FROM notes WHERE groupId = :groupId")
    fun noteCountFlow(groupId: Long): Flow<Int>
}
