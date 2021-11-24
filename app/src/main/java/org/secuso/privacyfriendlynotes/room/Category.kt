package org.secuso.privacyfriendlynotes.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(
        @PrimaryKey(autoGenerate = false)
        val _name: String) {

}