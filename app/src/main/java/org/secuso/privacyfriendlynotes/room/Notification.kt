package org.secuso.privacyfriendlynotes.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class Notification(
        @PrimaryKey(autoGenerate = false)
        var noteid: Int,
        var time: Int) {


}