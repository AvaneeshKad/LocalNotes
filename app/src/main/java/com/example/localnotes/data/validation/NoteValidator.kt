package com.example.localnotes.data.validation

import com.example.localnotes.data.model.Stroke

/**
 * Validates note payloads before they are persisted to on-device storage.
 *
 * Inputs: raw title, content, and stroke list from the editor.
 * Output: [ValidationResult] with either success or a list of human-readable errors.
 */
object NoteValidator {

    const val MAX_TITLE_LENGTH = 200
    const val MAX_CONTENT_LENGTH = 50_000
    const val MAX_STROKES = 10_000
    const val MIN_STROKE_WIDTH = 0.5f
    const val MAX_STROKE_WIDTH = 100f

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

    fun validate(title: String, content: String, strokes: List<Stroke>): ValidationResult {
        val errors = mutableListOf<String>()

        if (title.length > MAX_TITLE_LENGTH) {
            errors += "Title exceeds maximum length of $MAX_TITLE_LENGTH characters."
        }
        if (content.length > MAX_CONTENT_LENGTH) {
            errors += "Content exceeds maximum length of $MAX_CONTENT_LENGTH characters."
        }
        if (strokes.size > MAX_STROKES) {
            errors += "Stroke count exceeds maximum of $MAX_STROKES."
        }

        strokes.forEachIndexed { index, stroke ->
            if (stroke.points.isEmpty()) {
                errors += "Stroke $index has no points."
            }
            if (stroke.strokeWidth < MIN_STROKE_WIDTH || stroke.strokeWidth > MAX_STROKE_WIDTH) {
                errors += "Stroke $index width must be between $MIN_STROKE_WIDTH and $MAX_STROKE_WIDTH."
            }
            if (stroke.timestampMs < 0L) {
                errors += "Stroke $index has a negative timestamp."
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(*errors.toTypedArray())
        }
    }

    fun normalizeTitle(title: String): String =
        title.trim().ifBlank { DEFAULT_TITLE }

    const val DEFAULT_TITLE = "Untitled Note"
}
