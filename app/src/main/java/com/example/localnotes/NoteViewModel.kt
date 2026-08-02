package com.example.localnotes

import androidx.lifecycle.ViewModel
import com.example.localnotes.data.model.Stroke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Note(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val strokes: List<Stroke> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class NoteViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    fun addNote(title: String, content: String, strokes: List<Stroke> = emptyList()) {
        val newNote = Note(title = title, content = content, strokes = strokes)
        _notes.value = _notes.value + newNote
    }

    fun deleteNote(noteId: Long) {
        _notes.value = _notes.value.filterNot { it.id == noteId }
    }
}
