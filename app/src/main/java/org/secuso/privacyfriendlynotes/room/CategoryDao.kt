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

    @get:Query("SELECT * FROM category_table GROUP BY name")
    val allCategories: LiveData<List<Category>>


}