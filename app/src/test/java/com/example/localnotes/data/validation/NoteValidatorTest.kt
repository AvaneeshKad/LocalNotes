package com.example.localnotes.data.validation

import com.example.localnotes.data.model.SerializedPoint
import com.example.localnotes.data.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteValidatorTest {

    @Test
    fun validate_acceptsTypicalNote() {
        val result = NoteValidator.validate(
            title = "Test_5",
            content = "-App Storage\n-Right now all notes are being stored in the RAM",
            strokes = listOf(
                Stroke(
                    points = listOf(SerializedPoint(1f, 2f)),
                    colorHex = 0xFF000000,
                    strokeWidth = 6f
                )
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun validate_rejectsOversizedTitle() {
        val result = NoteValidator.validate(
            title = "a".repeat(NoteValidator.MAX_TITLE_LENGTH + 1),
            content = "hello",
            strokes = emptyList()
        )

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
    }

    @Test
    fun validate_rejectsEmptyStroke() {
        val result = NoteValidator.validate(
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

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Stroke 0 has no points") })
    }

    @Test
    fun normalizeTitle_usesDefaultWhenBlank() {
        assertEquals(NoteValidator.DEFAULT_TITLE, NoteValidator.normalizeTitle("   "))
        assertEquals("Meeting notes", NoteValidator.normalizeTitle("  Meeting notes "))
    }
}
