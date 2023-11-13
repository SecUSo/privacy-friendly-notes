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
package org.secuso.privacyfriendlynotes.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Provides category class with variables and constructor.
 */

@Entity(tableName = "categories")
data class Category(
        @PrimaryKey(autoGenerate = true)
        val _id: Int,
        val name: String,
        var color: String?
) {

        constructor(name: String): this(
                name = name,
                _id = 0,
                color = null
        )
        constructor(name: String, color: String?) : this(
                name = name,
                _id = 0,
                color = color
        )

}