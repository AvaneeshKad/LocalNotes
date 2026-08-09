package com.example.localnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localnotes.data.model.Note
import com.example.localnotes.data.model.NotePage
import com.example.localnotes.data.model.Stroke
import com.example.localnotes.data.repository.DeleteNoteResult
import com.example.localnotes.data.repository.NotesRepository
import com.example.localnotes.data.repository.SaveNoteInput
import com.example.localnotes.data.repository.SaveNoteResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.observeNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    var lastSaveError: String? = null
        private set

    fun addNote(title: String, pages: List<NotePage>) {
        viewModelScope.launch {
            saveOrUpdateNote(null, title, pages)
        }
    }

    suspend fun saveOrUpdateNote(
        id: Long? = null,
        title: String,
        pages: List<NotePage> = listOf(NotePage()),
        timestamp: Long = System.currentTimeMillis()
    ): SaveNoteResult {
        val result = repository.saveNote(
            SaveNoteInput(
                id = id,
                title = title,
                pages = pages,
                timestamp = timestamp
            )
        )
        lastSaveError = when (result) {
            is SaveNoteResult.Success -> null
            is SaveNoteResult.ValidationError -> result.errors.joinToString("\n")
            is SaveNoteResult.StorageError -> result.cause.message ?: "Unable to save note."
        }
        return result
    }

    fun getNote(id: Long): Note? = notes.value.find { it.id == id }

    fun renameNote(noteId: Long, newTitle: String) {
        viewModelScope.launch {
            val note = getNote(noteId) ?: return@launch
            saveOrUpdateNote(
                id = note.id,
                title = newTitle,
                pages = note.pages,
                timestamp = note.timestamp
            )
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            when (repository.deleteNote(noteId)) {
                is DeleteNoteResult.Success -> lastSaveError = null
                is DeleteNoteResult.NotFound -> {
                    lastSaveError = "Note $noteId was not found."
                }
                is DeleteNoteResult.StorageError -> {
                    lastSaveError = "Unable to delete note."
                }
            }
        }
    }
}
