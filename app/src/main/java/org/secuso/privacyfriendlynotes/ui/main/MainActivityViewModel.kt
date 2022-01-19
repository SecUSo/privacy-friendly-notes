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
package org.secuso.privacyfriendlynotes.ui.main

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.room.NoteDatabase

/**
 * The MainActivityViewModel provides the data for the MainActivity.
 * It is also used for the RecycleActivity.
 * @see MainActivity
 * @see RecycleActivity
 */

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {


    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val activeNotes: LiveData<List<Note>> = repository.noteDao().allActiveNotes
    val trashedNotes: LiveData<List<Note>> = repository.noteDao().allTrashedNotes
    val allCategoriesLive: LiveData<List<Category>> = repository.categoryDao().allCategoriesLive


    private var _notesFromCategoryLast: LiveData<List<Note?>?>? = null
    private var _notesFromCategory: MediatorLiveData<List<Note?>?> = MediatorLiveData<List<Note?>?>()
    private var _notesFilteredLast: LiveData<List<Note?>?>? = null
    private var _notesFiltered: MediatorLiveData<List<Note?>?> = MediatorLiveData<List<Note?>?>()

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.noteDao().insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.noteDao().update(note)

        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.noteDao().delete(note)
        }
    }

    fun getActiveNotesFiltered(filter: String): LiveData<List<Note?>?>{
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                if (_notesFilteredLast != null) {
                    _notesFiltered.removeSource(_notesFilteredLast!!)
                }
            }
            _notesFilteredLast = repository.noteDao().activeNotesFiltered(filter)

            withContext(Dispatchers.Main) {
                _notesFiltered.addSource(_notesFilteredLast!!) {
                    _notesFiltered.postValue(it)
                }
            }
        }
        return _notesFiltered
    }

    fun getNotesFilteredAlphabetical(filter: String): LiveData<List<Note?>?>{
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                if (_notesFilteredLast != null) {
                    _notesFiltered.removeSource(_notesFilteredLast!!)
                }
            }
            _notesFilteredLast = repository.noteDao().activeNotesFilteredAlphabetical(filter)

            withContext(Dispatchers.Main) {
                _notesFiltered.addSource(_notesFilteredLast!!) {
                    _notesFiltered.postValue(it)
                }
            }
        }
        return _notesFiltered
    }

    fun getTrashedNotesFiltered(filter: String): LiveData<List<Note?>?>{
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                if (_notesFilteredLast != null) {
                    _notesFiltered.removeSource(_notesFilteredLast!!)
                }
            }
            _notesFilteredLast = repository.noteDao().trashedNotesFiltered(filter)

            withContext(Dispatchers.Main) {
                _notesFiltered.addSource(_notesFilteredLast!!) {
                    _notesFiltered.postValue(it)
                }
            }
        }
        return _notesFiltered
    }

    fun getActiveNotesFilteredFromCategory(filter: String,category: Integer): LiveData<List<Note?>?>{
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                if (_notesFilteredLast != null) {
                    _notesFiltered.removeSource(_notesFilteredLast!!)
                }
            }
            _notesFilteredLast = repository.noteDao().activeNotesFilteredFromCategory(filter,category)

            withContext(Dispatchers.Main) {
                _notesFiltered.addSource(_notesFilteredLast!!) {
                    _notesFiltered.postValue(it)
                }
            }
        }
        return _notesFiltered
    }

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

    fun delete(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().delete(category)
        }
    }

}