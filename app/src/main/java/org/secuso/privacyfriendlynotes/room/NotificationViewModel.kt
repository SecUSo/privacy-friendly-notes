package org.secuso.privacyfriendlynotes.room

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationViewModel(application: Application) : AndroidViewModel(application) {


    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val allNotifications: LiveData<List<Notification>> = repository.notificationDao().allNotifications
    private val _notificationLiveData: MediatorLiveData<Notification?> = MediatorLiveData<Notification?>()
    private var _notificationLiveDataLast: LiveData<Notification?>? = null

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

//    fun getNotificationFromNoteId(noteId: Integer): LiveData<Notification?> {
//
//        viewModelScope.launch(Dispatchers.Default){
//            withContext(Main){
//                if(_notificationLiveDataLast != null){
//                    _notificationLiveData.removeSource(_notificationLiveDataLast!!)
//                }
//            }
//            _notificationLiveDataLast = repository.notificationDao().notificationFromNoteId(noteId)
//
//            withContext(Main){
//                _notificationLiveData.addSource(_notificationLiveDataLast!!){
//                    _notificationLiveData.postValue(it)
//                }
//            }
//
//        }
//        return _notificationLiveData
//    }

}