package org.secuso.privacyfriendlynotes.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import org.secuso.privacyfriendlynotes.room.model.Notification

/**
 * Data Access Object for notifications that define the interactions with the database
 */

@Dao
interface NotificationDao {
    @Insert(onConflict = REPLACE)
    fun insert(notification: Notification)

    @Update(onConflict = REPLACE)
    fun update(notification: Notification)

    @Delete
    fun delete(notification: Notification)

//    @Query("SELECT * FROM notifications WHERE noteid=:thisNoteid ")
//    fun notificationFromNoteId(thisNoteid: Integer): LiveData<Notification?>

    @get:Query("SELECT * FROM notifications")
    val allNotifications: LiveData<List<Notification>>
}