package org.secuso.privacyfriendlynotes.room

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface CategoryDao {
    @Insert(onConflict = REPLACE)
    fun insert(category: Category)

    @Update(onConflict = REPLACE)
    fun update(category: Category)

    @Delete
    fun delete(category: Category)

    @get:Query("SELECT * FROM categories GROUP BY _name")
    val allCategoriesLive: LiveData<List<Category>>

    @Query("SELECT * FROM categories GROUP BY _name")
    suspend fun getAllCategories(): List<Category>
}