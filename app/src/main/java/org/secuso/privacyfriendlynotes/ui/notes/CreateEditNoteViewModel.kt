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
package org.secuso.privacyfriendlynotes.ui.notes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.room.NoteDatabase
import org.secuso.privacyfriendlynotes.room.model.Notification

/**
 * Provides data for all four note types.
 * @see AudioNoteActivity, ChecklistNoteActivity, SketchActivity, TextNoteActivity
 */

class CreateEditNoteViewModel(application: Application) : AndroidViewModel(application){

    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val allNotifications: LiveData<List<Notification>> = repository.notificationDao().allNotificationsLiveData
    val categories: Flow<List<Category>> = repository.categoryDao().allCategories
    private val _categoryName: MediatorLiveData<String?> = MediatorLiveData<String?>()
    private var _categoryNameLast: LiveData<String?>? = null
    private val database: NoteDatabase = NoteDatabase.getInstance(application)


    fun insert(notification: Notification){
        viewModelScope.launch(Dispatchers.Default){
            repository.notificationDao().insert(notification)
        }
    }
    fun update(notification: Notification){
        viewModelScope.launch(Dispatchers.Default){
            repository.notificationDao().update(notification)
        }
    }
    fun delete(notification: Notification){
        viewModelScope.launch(Dispatchers.Default){
            repository.notificationDao().delete(notification)
        }
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

    fun getCategoryNameFromId(categoryId: Int): LiveData<String?> {

        viewModelScope.launch(Dispatchers.Default){
            withContext(Dispatchers.Main){
                if(_categoryNameLast != null){
                    _categoryName.removeSource(_categoryNameLast!!)
                }
            }
            _categoryNameLast = repository.categoryDao().categoryNameFromId(categoryId as Integer)

            withContext(Dispatchers.Main){
                _categoryName.addSource(_categoryNameLast!!){
                    _categoryName.postValue(it)
                }
            }

        }
        return _categoryName
    }

    /**
     * Returns id
     */
    fun insert(note: Note): Int {
        val id = viewModelScope.run {
            database.noteDao().insert(note).toInt()
        }
        Log.e("id", "$id")
        return id
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

    fun getNoteByID(id: Long): LiveData<Note?> {
        val note = MutableLiveData<Note?>()
        Log.d(TAG, "Fetching note $id from database")
        viewModelScope.launch(Dispatchers.IO) {
            note.postValue(database.noteDao().getNoteByID(id))
        }
        return note
    }

    companion object{
        private const val TAG = "CreateEditNoteViewModel"
    }
}