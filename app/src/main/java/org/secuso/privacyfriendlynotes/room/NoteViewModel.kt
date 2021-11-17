package org.secuso.privacyfriendlynotes.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val database: NoteDatabase = NoteDatabase.getInstance(application)
    val allNotes: LiveData<List<Note>> = database.noteDao().allNotes
    val activeNotes: LiveData<List<Note>> = database.noteDao().allActiveNotes
    val trashedNotes: LiveData<List<Note>> = database.noteDao().allTrashedNotes

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            database.noteDao().insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            database.noteDao().update(note)

        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            database.noteDao().delete(note)
        }
    }
}