package org.secuso.privacyfriendlynotes.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val allNotifications: LiveData<List<Notification>> = repository.notificationDao().allNotifications

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

    suspend fun getNotificationFromNoteId(noteId: Integer): Notification {
        return repository.notificationDao().notificationFromNoteId(noteId)

    }

}