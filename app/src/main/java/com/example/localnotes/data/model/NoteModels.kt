package com.example.localnotes.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class SerializedPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
)

data class Stroke(
    val points: List<SerializedPoint>,
    val colorHex: Long,
    val strokeWidth: Float,
    val timestampMs: Long = 0L
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
