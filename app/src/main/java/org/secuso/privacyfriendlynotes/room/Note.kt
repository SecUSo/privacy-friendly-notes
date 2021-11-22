package org.secuso.privacyfriendlynotes.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class Note(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var title: String,
        var content: String,
        var type: Int,
        var category: Int,
        var isTrash: Int = 0) {

        constructor(title: String, content: String, type: Int, category: Int) : this(
                title = title,
                content = content,
                type = type,
                category = category,
                isTrash = 0,
                id = 0
        )
}