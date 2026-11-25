package com.note.dynamic.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.note.dynamic.NotesApp
import com.note.dynamic.data.Attachment
import com.note.dynamic.data.Note
import com.note.dynamic.data.NoteGroup
import com.note.dynamic.data.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class NotesViewModel(private val repo: Repository) : ViewModel() {

    val groups: StateFlow<List<NoteGroup>> =
        repo.observeGroups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ungroupedNotes: StateFlow<List<Note>> =
        repo.observeUngroupedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun notesInGroup(groupId: Long): StateFlow<List<Note>> =
        repo.observeNotes(groupId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun noteCount(groupId: Long): StateFlow<Int> =
        repo.noteCount(groupId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun search(q: String) = repo.search(q)

    // Groups
    fun saveGroup(group: NoteGroup, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        onDone(repo.saveGroup(group))
    }

    fun deleteGroup(group: NoteGroup) = viewModelScope.launch { repo.deleteGroup(group) }

    suspend fun getGroup(id: Long): NoteGroup? = repo.getGroup(id)

    // Notes
    suspend fun saveNoteReturnId(note: Note): Long = repo.saveNote(note)

    fun saveNote(note: Note, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        onDone(repo.saveNote(note))
    }

    fun deleteNote(note: Note) = viewModelScope.launch { repo.deleteNote(note) }

    fun togglePinned(note: Note) = viewModelScope.launch { repo.togglePinned(note) }

    suspend fun getNote(id: Long): Note? = repo.getNote(id)

    // Attachments
    fun observeAttachments(noteId: Long) = repo.observeAttachments(noteId)

    suspend fun listAttachments(noteId: Long) = repo.listAttachments(noteId)

    fun addImage(noteId: Long, uri: Uri, name: String, mime: String, size: Long) = viewModelScope.launch {
        repo.addAttachment(
            Attachment(noteId = noteId, type = "image", uri = uri.toString(), displayName = name, mime = mime, size = size)
        )
    }

    fun addFile(noteId: Long, uri: Uri, name: String, mime: String, size: Long) = viewModelScope.launch {
        repo.addAttachment(
            Attachment(noteId = noteId, type = "file", uri = uri.toString(), displayName = name, mime = mime, size = size)
        )
    }

    fun deleteAttachment(att: Attachment) = viewModelScope.launch { repo.deleteAttachment(att) }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = NotesApp.instance
                return NotesViewModel(app.repository) as T
            }
        }
    }
}
