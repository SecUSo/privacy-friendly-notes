package org.secuso.privacyfriendlynotes.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
        @PrimaryKey(autoGenerate = true)
        var _id : Int,
        var noteid: Int,
        var time: Int) {

        constructor(time: Int, noteid: Int) : this(
                time = time,
                noteid = noteid,
                _id = 0
        )

}