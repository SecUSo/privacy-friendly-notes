package org.secuso.privacyfriendlynotes.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)

    @get:Query("SELECT * FROM note_table ORDER BY category DESC")
    val allNotes: LiveData<List<Note>>

    @get:Query("SELECT * FROM note_table ORDER BY title DESC")
    val allNotesAlphabetical: LiveData<List<Note>>

    @get:Query("SELECT * FROM note_table WHERE isTrash = 0 ORDER BY category DESC")
    val allActiveNotes: LiveData<List<Note>>

    @get:Query("SELECT * FROM note_table WHERE isTrash = 1 ORDER BY category DESC")
    val allTrashedNotes: LiveData<List<Note>>
}