package com.example.localnotes.data.validation

import com.example.localnotes.data.model.NotePage
import com.example.localnotes.data.model.Stroke

/**
 * Validates note payloads before they are persisted to on-device storage.
 */
object NoteValidator {

    const val MAX_TITLE_LENGTH = 200
    const val MAX_CONTENT_LENGTH = 50_000
    const val MAX_STROKES_PER_PAGE = 5_000
    const val MIN_STROKE_WIDTH = 0.5f
    const val MAX_STROKE_WIDTH = 100f
    const val MAX_PAGES = 50

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    ) {
        companion object {
            fun success(): ValidationResult = ValidationResult(isValid = true)

            fun failure(vararg errors: String): ValidationResult =
                ValidationResult(isValid = false, errors = errors.toList())
        }
    }

    fun validate(title: String, pages: List<NotePage>): ValidationResult {
        val errors = mutableListOf<String>()

        if (title.length > MAX_TITLE_LENGTH) {
            errors += "Title exceeds maximum length of $MAX_TITLE_LENGTH characters."
        }
        
        if (pages.size > MAX_PAGES) {
            errors += "Note exceeds maximum of $MAX_PAGES pages."
        }

        pages.forEachIndexed { pageIndex, page ->
            if (page.content.length > MAX_CONTENT_LENGTH) {
                errors += "Page ${pageIndex + 1} content exceeds maximum length of $MAX_CONTENT_LENGTH characters."
            }
            if (page.strokes.size > MAX_STROKES_PER_PAGE) {
                errors += "Page ${pageIndex + 1} stroke count exceeds maximum of $MAX_STROKES_PER_PAGE."
            }

            page.strokes.forEachIndexed { strokeIndex, stroke ->
                if (stroke.points.isEmpty()) {
                    errors += "Page ${pageIndex + 1}, stroke $strokeIndex has no points."
                }
                if (stroke.strokeWidth < MIN_STROKE_WIDTH || stroke.strokeWidth > MAX_STROKE_WIDTH) {
                    errors += "Page ${pageIndex + 1}, stroke $strokeIndex width must be between $MIN_STROKE_WIDTH and $MAX_STROKE_WIDTH."
                }
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(*errors.toTypedArray())
        }
    }
    
    // Legacy support
    fun validate(title: String, content: String, strokes: List<Stroke>): ValidationResult {
        return validate(title, listOf(NotePage(strokes = strokes, content = content)))
    }

    fun normalizeTitle(title: String): String =
        title.trim().ifBlank { DEFAULT_TITLE }

    const val DEFAULT_TITLE = "Untitled Note"
}
