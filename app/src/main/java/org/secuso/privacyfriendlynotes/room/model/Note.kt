package org.secuso.privacyfriendlynotes.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
        @PrimaryKey(autoGenerate = true)
        var _id: Int = 0,
        var name: String,
        var content: String,
        var type: Int,
        var category: Int,
        var in_trash: Int = 0) {

        constructor(name: String, content: String, type: Int, category: Int) : this(
                name = name,
                content = content,
                type = type,
                category = category,
                in_trash = 0,
                _id = 0
        )
}