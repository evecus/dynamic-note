package com.note.dynamic

import android.app.Application
import com.note.dynamic.data.AppDatabase
import com.note.dynamic.data.Repository

class NotesApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: Repository by lazy {
        Repository(
            database.groupDao(),
            database.noteDao(),
            database.attachmentDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NotesApp
            private set
    }
}
