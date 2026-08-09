package com.example.localnotes.data.repository

import com.example.localnotes.data.local.NoteDao
import com.example.localnotes.data.local.NoteEntity
import com.example.localnotes.data.model.Note
import com.example.localnotes.data.serialization.StrokeCodec
import com.example.localnotes.data.validation.NoteValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotesRepository(
    private val noteDao: NoteDao
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeAll().map { entities -> entities.map(::toDomain) }

    override suspend fun getNote(id: Long): Note? =
        noteDao.getById(id)?.let(::toDomain)

    override suspend fun saveNote(input: SaveNoteInput): SaveNoteResult {
        val validation = NoteValidator.validate(
            title = input.title,
            content = input.content,
            strokes = input.strokes
        )
        validation.toSaveNoteResult()?.let { return it }

        return try {
            val normalizedTitle = NoteValidator.normalizeTitle(input.title)
            val note = Note(
                id = input.id ?: System.currentTimeMillis(),
                title = normalizedTitle,
                content = input.content,
                strokes = input.strokes,
                timestamp = input.timestamp
            )
            noteDao.upsert(toEntity(note))
            SaveNoteResult.Success(note)
        } catch (exception: Exception) {
            SaveNoteResult.StorageError(exception)
        }
    }

    override suspend fun deleteNote(id: Long): DeleteNoteResult {
        return try {
            val deletedRows = noteDao.deleteById(id)
            if (deletedRows == 0) {
                DeleteNoteResult.NotFound(id)
            } else {
                DeleteNoteResult.Success
            }
        } catch (exception: Exception) {
            DeleteNoteResult.StorageError(exception)
        }
    }

    private fun toEntity(note: Note): NoteEntity =
        NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            strokesJson = StrokeCodec.encode(note.strokes),
            timestamp = note.timestamp
        )

    private fun toDomain(entity: NoteEntity): Note =
        Note(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            strokes = StrokeCodec.decode(entity.strokesJson),
            timestamp = entity.timestamp
        )
}
