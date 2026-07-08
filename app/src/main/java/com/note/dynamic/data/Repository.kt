package com.note.dynamic.data

import kotlinx.coroutines.flow.Flow

class Repository(
    private val groupDao: GroupDao,
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao
) {
    // --- Groups ---
    fun observeGroups(): Flow<List<NoteGroup>> = groupDao.observeAll()
    fun noteCount(groupId: Long): Flow<Int> = groupDao.noteCountFlow(groupId)
    suspend fun getGroup(id: Long): NoteGroup? = groupDao.getById(id)

    suspend fun saveGroup(group: NoteGroup): Long {
        val now = System.currentTimeMillis()
        return if (group.id == 0L) {
            groupDao.insert(group.copy(createdAt = now, updatedAt = now))
        } else {
            groupDao.update(group.copy(updatedAt = now))
            group.id
        }
    }

    suspend fun deleteGroup(group: NoteGroup) = groupDao.delete(group)

    // --- Notes ---
    fun observeUngroupedNotes(): Flow<List<Note>> = noteDao.observeUngrouped()
    fun observeNotes(groupId: Long): Flow<List<Note>> = noteDao.observeByGroup(groupId)
    fun search(q: String): Flow<List<Note>> = noteDao.search(q)
    suspend fun getNote(id: Long): Note? = noteDao.getById(id)

    suspend fun saveNote(note: Note): Long {
        val now = System.currentTimeMillis()
        return if (note.id == 0L) {
            noteDao.insert(note.copy(createdAt = now, updatedAt = now))
        } else {
            noteDao.update(note.copy(updatedAt = now))
            note.id
        }
    }

    suspend fun deleteNote(note: Note) = noteDao.delete(note)
    suspend fun togglePinned(note: Note) =
        noteDao.setPinned(note.id, !note.pinned, System.currentTimeMillis())

    // --- Attachments ---
    fun observeAttachments(noteId: Long): Flow<List<Attachment>> =
        attachmentDao.observeByNote(noteId)
    suspend fun listAttachments(noteId: Long): List<Attachment> =
        attachmentDao.listByNote(noteId)
    suspend fun addAttachment(att: Attachment): Long = attachmentDao.insert(att)
    suspend fun deleteAttachment(att: Attachment) = attachmentDao.delete(att)
}
