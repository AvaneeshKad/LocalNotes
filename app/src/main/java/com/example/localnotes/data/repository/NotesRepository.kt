package com.example.localnotes.data.repository

import com.example.localnotes.data.model.Note
import com.example.localnotes.data.model.Stroke
import com.example.localnotes.data.validation.NoteValidator
import kotlinx.coroutines.flow.Flow

/**
 * Production storage contract for LocalNotes.
 *
 * Replaces the exploratory in-memory [MutableStateFlow] approach with durable
 * on-device SQLite storage via Room.
 */
interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>

    suspend fun getNote(id: Long): Note?

    suspend fun saveNote(input: SaveNoteInput): SaveNoteResult

    suspend fun deleteNote(id: Long): DeleteNoteResult
}

data class SaveNoteInput(
    val id: Long? = null,
    val title: String,
    val content: String,
    val strokes: List<Stroke> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface SaveNoteResult {
    data class Success(val note: Note) : SaveNoteResult
    data class ValidationError(val errors: List<String>) : SaveNoteResult
    data class StorageError(val cause: Throwable) : SaveNoteResult
}

sealed interface DeleteNoteResult {
    data object Success : DeleteNoteResult
    data class NotFound(val id: Long) : DeleteNoteResult
    data class StorageError(val cause: Throwable) : DeleteNoteResult
}

internal fun NoteValidator.ValidationResult.toSaveNoteResult(): SaveNoteResult? =
    if (isValid) {
        null
    } else {
        SaveNoteResult.ValidationError(errors)
    }
