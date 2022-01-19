/*
 This file is part of the application Privacy Friendly Notes.
 Privacy Friendly Notes is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 Privacy Friendly Notes is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Notes. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlynotes.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.secuso.privacyfriendlynotes.room.model.Note

/**
 * Data Access Object for notes that defines the interactions with the database like inserting, updating, deleting and more.
 */

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

    @Query("SELECT * FROM notes WHERE ((LOWER(name) LIKE '%'|| LOWER(:thisFilterText) || '%') OR (LOWER(content) LIKE '%'|| LOWER(:thisFilterText) || '%' AND (type = 1 OR type = 3))) AND in_trash='0' ORDER BY name DESC")
    fun activeNotesFiltered(thisFilterText: String): LiveData<List<Note?>?>

    @Query("SELECT * FROM notes WHERE ((LOWER(name) LIKE '%'|| LOWER(:thisFilterText) || '%') OR (LOWER(content) LIKE '%'|| LOWER(:thisFilterText) || '%' AND (type = 1 OR type = 3))) AND in_trash='0' ORDER BY name ASC")
    fun activeNotesFilteredAlphabetical(thisFilterText: String): LiveData<List<Note?>?>

    @Query("SELECT * FROM notes WHERE ((LOWER(name) LIKE '%'|| LOWER(:thisFilterText) || '%') OR (LOWER(content) LIKE '%'|| LOWER(:thisFilterText) || '%' AND (type = 1 OR type = 3))) AND in_trash='1' ORDER BY name DESC")
    fun trashedNotesFiltered(thisFilterText: String): LiveData<List<Note?>?>

    @Query("SELECT * FROM notes WHERE (category=:thisCategory) AND (in_trash='0') AND ((LOWER(name) LIKE '%'|| LOWER(:thisFilterText) || '%') OR (LOWER(content) LIKE '%'|| LOWER(:thisFilterText) || '%' AND (type = 1 OR type = 3)))  ORDER BY name DESC")
    fun activeNotesFilteredFromCategory(thisFilterText: String, thisCategory: Integer): LiveData<List<Note?>?>

    @Query("SELECT * FROM notes")
    fun getNotesDebug() : List<Note>
}