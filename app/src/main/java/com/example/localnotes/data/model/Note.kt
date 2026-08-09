package com.example.localnotes.data.model

data class Note(
    val id: Long,
    val title: String,
    val pages: List<NotePage> = listOf(NotePage()),
    val timestamp: Long
) {
    // Helper for backwards compatibility or single-page usage
    val content: String get() = pages.firstOrNull()?.content ?: ""
    val strokes: List<Stroke> get() = pages.firstOrNull()?.strokes ?: emptyList()
}
