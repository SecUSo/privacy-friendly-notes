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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.secuso.privacyfriendlynotes.room.model.Note

/**
 * Data Access Object for notes that defines the interactions with the database like inserting, updating, deleting and more.
 */

@Dao
interface NoteDao {
    @Insert
    fun insert(note: Note): Long

    @Update
    fun update(note: Note)

    @Update
    fun updateAll(note: List<Note>)

    @Delete
    fun delete(note: Note)

    @get:Query("SELECT * FROM notes WHERE in_trash = 0 ORDER BY category DESC")
    val allActiveNotes: Flow<List<Note>>

    @get:Query("SELECT * FROM notes WHERE in_trash = 1 ORDER BY category DESC")
    val allTrashedNotes: Flow<List<Note>>

    @Query("SELECT * FROM notes")
    fun getNotesDebug(): List<Note>

    @Query("SELECT * FROM notes WHERE _id = :id")
    fun getNoteByID(id: Long): Note?
}