package com.example.localnotes.data.model

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val strokes: List<Stroke> = emptyList(),
    val timestamp: Long
)
