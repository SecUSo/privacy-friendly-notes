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
package org.secuso.privacyfriendlynotes.ui.manageCategories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.NoteDatabase
import org.secuso.privacyfriendlynotes.room.model.Note

/**
 * ViewModel that provides data for ManageCategoriesActivity.
 * @see ManageCategoriesActivity
 */

class ManageCategoriesViewModel (application: Application) : AndroidViewModel(application) {
    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val allCategories: StateFlow<List<Category>> = repository.categoryDao().allCategories.stateIn(viewModelScope, SharingStarted.Lazily, listOf())
    val notes: Flow<List<Note>> = repository.noteDao().allActiveNotes

    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().insert(category)
        }
    }

    fun update(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().update(category)
        }
    }

    fun update(category: Category, color: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().update(category._id, color)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().delete(category)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.noteDao().delete(note)
        }
    }
}