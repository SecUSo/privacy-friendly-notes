package org.secuso.privacyfriendlynotes.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
        @PrimaryKey(autoGenerate = true)
        val _id: Int,
        val name: String) {

        constructor(name: String) : this(
                name = name,
                _id = 0
        )

}