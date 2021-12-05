package org.secuso.privacyfriendlynotes.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import org.secuso.privacyfriendlynotes.room.model.Category

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