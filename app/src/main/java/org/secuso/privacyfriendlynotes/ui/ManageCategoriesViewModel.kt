package org.secuso.privacyfriendlynotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlynotes.room.Category
import org.secuso.privacyfriendlynotes.room.NoteDatabase

class ManageCategoriesViewModel (application: Application) : AndroidViewModel(application) {
    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val allCategoriesLive: LiveData<List<Category>> = repository.categoryDao().allCategoriesLive

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