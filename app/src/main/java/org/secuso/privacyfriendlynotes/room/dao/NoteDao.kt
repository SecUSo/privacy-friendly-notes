package org.secuso.privacyfriendlynotes.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.secuso.privacyfriendlynotes.room.model.Note

@Dao
interface NoteDao {
    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)

    @get:Query("SELECT * FROM notes ORDER BY category DESC")
    val allNotes: LiveData<List<Note>>

    @get:Query("SELECT * FROM notes WHERE in_trash = 0 ORDER BY name ASC")
    val allNotesAlphabetical: LiveData<List<Note>>

    @get:Query("SELECT * FROM notes WHERE in_trash = 0 ORDER BY name DESC")
    val allActiveNotes: LiveData<List<Note>>

    @get:Query("SELECT * FROM notes WHERE in_trash = 1 ORDER BY name DESC")
    val allTrashedNotes: LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE category=:thisCategory AND in_trash='0'")
    fun notesFromCategory(thisCategory: Integer): LiveData<List<Note?>?>

    @Query("SELECT * FROM notes WHERE ((LOWER(name) LIKE '%'|| LOWER(:thisFilterText) || '%') OR (LOWER(content) LIKE '%'|| LOWER(:thisFilterText) || '%' AND (category=1 OR category = 3))) AND in_trash='0'")
    fun notesFiltered(thisFilterText: String): LiveData<List<Note?>?>

    @Query("SELECT * FROM notes")
    fun getNotesDebug() : List<Note>
}