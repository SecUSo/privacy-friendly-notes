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

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
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
    val allNotificationsLiveData: LiveData<List<Notification>>

    @Query("SELECT * FROM notifications")
    fun getAllNotifications() : List<Notification>
}