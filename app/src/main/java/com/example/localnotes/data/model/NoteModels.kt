package com.example.localnotes.data.model

enum class StrokeTool {
    PEN, ERASER
}

data class SerializedPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
)

data class Stroke(
    val points: List<SerializedPoint>,
    val colorHex: Long,
    val strokeWidth: Float,
    val tool: StrokeTool = StrokeTool.PEN,
    val timestampMs: Long = 0L
)

data class NotePage(
    val strokes: List<Stroke> = emptyList(),
    val content: String = "",
    val backgroundColorHex: Long = 0xFFFFFFFF // Default to White
)

data class AudioMarker(
    val timestampMs: Long,
    val strokeIndex: Int
)

data class NotabilityNote(
    val id: String,
    val title: String,
    val pdfUri: String? = null,
    val typedText: String = "",
    val strokes: List<Stroke> = emptyList(),
    val audioFilePath: String? = null,
    val audioMarkers: List<AudioMarker> = emptyList()
)
