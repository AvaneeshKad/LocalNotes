package com.example.localnotes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val content: String,
    val strokesJson: String,
    val timestamp: Long
)
