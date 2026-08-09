package com.example.localnotes.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.localnotes.data.local.NotesDatabase
import com.example.localnotes.data.model.SerializedPoint
import com.example.localnotes.data.model.Stroke
import com.example.localnotes.data.validation.NoteValidator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomNotesRepositoryTest {

    private lateinit var repository: RoomNotesRepository
    private lateinit var database: NotesDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomNotesRepository(database.noteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveNote_persistsAndReloadsStrokes() = runTest {
        val strokes = listOf(
            Stroke(
                points = listOf(SerializedPoint(1f, 2f)),
                colorHex = 0xFF388E3C,
                strokeWidth = 6f,
                timestampMs = 42L
            )
        )

        val saveResult = repository.saveNote(
            SaveNoteInput(
                title = "Test_5",
                content = "App Storage",
                strokes = strokes,
                timestamp = 100L
            )
        )

        assertTrue(saveResult is SaveNoteResult.Success)
        val savedNote = (saveResult as SaveNoteResult.Success).note

        val loaded = repository.getNote(savedNote.id)
        assertEquals(savedNote, loaded)
        assertEquals(strokes, loaded?.strokes)
    }

    @Test
    fun saveNote_normalizesBlankTitle() = runTest {
        val result = repository.saveNote(
            SaveNoteInput(title = "   ", content = "typed text")
        )

        assertTrue(result is SaveNoteResult.Success)
        assertEquals(NoteValidator.DEFAULT_TITLE, (result as SaveNoteResult.Success).note.title)
    }

    @Test
    fun saveNote_rejectsInvalidPayload() = runTest {
        val result = repository.saveNote(
            SaveNoteInput(
                title = "Ink",
                content = "",
                strokes = listOf(
                    Stroke(
                        points = emptyList(),
                        colorHex = 0xFF000000,
                        strokeWidth = 6f
                    )
                )
            )
        )

        assertTrue(result is SaveNoteResult.ValidationError)
    }

    @Test
    fun deleteNote_removesPersistedNote() = runTest {
        val saved = repository.saveNote(
            SaveNoteInput(title = "Delete me", content = "temporary")
        ) as SaveNoteResult.Success

        val deleteResult = repository.deleteNote(saved.note.id)
        assertTrue(deleteResult is DeleteNoteResult.Success)
        assertNull(repository.getNote(saved.note.id))
        assertTrue(repository.observeNotes().first().isEmpty())
    }
}
