package com.example.localnotes

import android.app.Application
import com.example.localnotes.data.local.NotesDatabase
import com.example.localnotes.data.repository.RoomNotesRepository

class LocalNotesApplication : Application() {
    lateinit var notesRepository: RoomNotesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = NotesDatabase.create(this)
        notesRepository = RoomNotesRepository(database.noteDao())
    }
}
