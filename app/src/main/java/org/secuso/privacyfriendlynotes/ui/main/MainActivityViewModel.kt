package org.secuso.privacyfriendlynotes.ui.main

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.room.NoteDatabase

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {


    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val allNotes: LiveData<List<Note>> = repository.noteDao().allNotes
    val activeNotes: LiveData<List<Note>> = repository.noteDao().allActiveNotes
    val trashedNotes: LiveData<List<Note>> = repository.noteDao().allTrashedNotes
    val allNotesAlphabetical: LiveData<List<Note>> = repository.noteDao().allNotesAlphabetical
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


    fun getNotesFromCategory(categoryID: Integer): LiveData<List<Note?>?>{
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                if (_notesFromCategoryLast != null) {
                    _notesFromCategory.removeSource(_notesFromCategoryLast!!)
                }
            }
            _notesFromCategoryLast = repository.noteDao().notesFromCategory(categoryID)

            withContext(Dispatchers.Main) {
                _notesFromCategory.addSource(_notesFromCategoryLast!!) {
                    _notesFromCategory.postValue(it)
                }
            }
        }
            return _notesFromCategory
    }


    fun getNotesFromFilter(filter: String): LiveData<List<Note?>?>{
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                if (_notesFilteredLast != null) {
                    _notesFiltered.removeSource(_notesFilteredLast!!)
                }
            }
            _notesFilteredLast = repository.noteDao().notesFiltered(filter)

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