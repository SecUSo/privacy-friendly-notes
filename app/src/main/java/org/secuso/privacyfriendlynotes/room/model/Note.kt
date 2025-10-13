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
import java.util.Calendar

/**
 * Provides note class with variables and constructor.
 */

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var _id: Int = 0,
    var name: String,
    var content: String,
    var type: Int,
    var category: Int,
    var in_trash: Int = 0,
    var last_modified: Long,
    var custom_order: Int
) {

    constructor(name: String, content: String, type: Int, category: Int) : this(
        name = name,
        content = content,
        type = type,
        category = category,
        in_trash = 0,
        _id = 0,
        last_modified = Calendar.getInstance().timeInMillis,
        custom_order = 0
    )

    constructor(name: String, content: String, type: Int, category: Int, custom_order: Int) : this(
        name = name,
        content = content,
        type = type,
        category = category,
        in_trash = 0,
        _id = 0,
        last_modified = Calendar.getInstance().timeInMillis,
        custom_order = custom_order
    )
}