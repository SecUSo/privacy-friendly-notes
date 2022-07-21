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
import androidx.room.OnConflictStrategy.REPLACE
import org.secuso.privacyfriendlynotes.room.model.Category

/**
 * Data Access Object for categories that define the interactions with the database
 */

@Dao
interface CategoryDao {
    @Insert(onConflict = REPLACE)
    fun insert(category: Category)

    @Update(onConflict = REPLACE)
    fun update(category: Category)

    @Delete
    fun delete(category: Category)

    @get:Query("SELECT * FROM categories GROUP BY name")
    val allCategoriesLive: LiveData<List<Category>>

    @Query("SELECT * FROM categories GROUP BY name")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT name FROM categories WHERE _id=:thisCategoryId ")
    fun categoryNameFromId(thisCategoryId: Integer): LiveData<String?>
}