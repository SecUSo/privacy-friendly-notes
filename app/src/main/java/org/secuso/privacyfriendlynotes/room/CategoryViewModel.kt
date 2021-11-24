package org.secuso.privacyfriendlynotes.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
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

    fun getAllCategories(){
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().allCategoriesLive
        }
    }
    sealed class Result<out R> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }


    suspend fun getAllCategories2(): Result<List<Category>> {
            return Result.Success(repository.categoryDao().getAllCategories())
        }

}