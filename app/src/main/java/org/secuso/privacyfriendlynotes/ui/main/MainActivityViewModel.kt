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
package org.secuso.privacyfriendlynotes.ui.main

import android.app.Application
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.preference.PreferenceManager
import android.text.Html
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.Consumer
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlynotes.model.SortingOrder
import org.secuso.privacyfriendlynotes.preference.PreferenceKeys
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.NoteDatabase
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.util.ChecklistUtil
import java.io.File

/**
 * The MainActivityViewModel provides the data for the MainActivity.
 * It is also used for the RecycleActivity.
 * @see MainActivity
 * @see RecycleActivity
 */

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val prefManager = PreferenceManager.getDefaultSharedPreferences(application)

    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    private var filter: MutableStateFlow<String> = MutableStateFlow("")
    private var ordering: MutableStateFlow<SortingOrder> = MutableStateFlow(
        SortingOrder.valueOf(prefManager.getString(PreferenceKeys.SP_NOTES_ORDERING, SortingOrder.LastModified.name)!!)
    )
    private var category: MutableStateFlow<Int> = MutableStateFlow(CAT_ALL)

    val trashedNotes: Flow<List<Note>> = repository.noteDao().allTrashedNotes
        .triggerOn(filter)
        .filterNotes()
    val activeNotes: Flow<List<Note>> = repository.noteDao().allActiveNotes
        .triggerOn(filter, ordering, category)
        .filterCategories()
        .filterNotes()
        .sortNotes()
    val categories: Flow<List<Category>> = repository.categoryDao().allCategories
    private val filesDir: File = application.filesDir
    private val resources: Resources = application.resources

    fun setFilter(filter: String) {
        this.filter.value = filter
    }

    fun setOrder(ordering: SortingOrder) {
        prefManager.edit()
            .putString(PreferenceKeys.SP_NOTES_ORDERING, ordering.name)
            .apply()
        this.ordering.value = ordering
    }

    fun setCategory(id: Int) {
        this.category.value = id
    }

    fun getCategory(): Int {
        return this.category.value
    }

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.noteDao().insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.noteDao().update(note)
        }
    }

    fun updateAll(notes: List<Note>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.noteDao().updateAll(notes)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.noteDao().delete(note)
            if (note.type == DbContract.NoteEntry.TYPE_AUDIO) {
                File(filesDir.path + "/audio_notes" + note.content).delete()
            } else if (note.type == DbContract.NoteEntry.TYPE_SKETCH) {
                File(filesDir.path + "/sketches" + note.content).delete()
                File(filesDir.path + "/sketches" + note.content.substring(0, note.content.length - 3) + "jpg").delete()
            }
        }
    }

    fun categoryColor(category: Int, consumer: Consumer<String?>) {
        viewModelScope.launch(Dispatchers.Default) {
            consumer.accept(repository.categoryDao().getCategoryColor(category))
        }
    }

    private fun StateFlow<SortingOrder>.comparator(): (Note, Note) -> Int {
        return when (this.value) {
            SortingOrder.AlphabeticalAscending -> { a, b -> a.name.compareTo(b.name) }
            SortingOrder.LastModified -> { a, b -> a.last_modified.compareTo(b.last_modified) }
            SortingOrder.Creation -> { a, b -> a._id.compareTo(b._id) }
            SortingOrder.Custom -> { a, b -> a.custom_order.compareTo(b.custom_order) }
            SortingOrder.TypeAscending -> { a, b -> a.type.compareTo(b.type) }
        }
    }

    private fun Flow<List<Note>>.filterNotes(): Flow<List<Note>> {
        return this.map {
            it.filter { note ->
                if (note.name.contains(filter.value)) {
                    return@filter true
                }
                when(note.type) {
                    DbContract.NoteEntry.TYPE_TEXT -> {
                        return@filter Html.fromHtml(note.content).toString().contains(filter.value)
                    }
                    DbContract.NoteEntry.TYPE_CHECKLIST -> {
                        return@filter ChecklistUtil.parse(note.content).joinToString(System.lineSeparator()).contains(filter.value)
                    }
                    else -> return@filter false
                }
            }
        }
    }

    private fun Flow<List<Note>>.sortNotes(): Flow<List<Note>> {
        return this.map { it.sortedWith(ordering.comparator()) }
    }

    private fun Flow<List<Note>>.filterCategories(): Flow<List<Note>> {
        return this.map {
            it.filter { note -> note.category == category.value || category.value == CAT_ALL }
        }
    }

    private fun<T> Flow<T>.triggerOn(vararg flows: Flow<*>): Flow<T> {
        return flows.fold(this) { acc, flow -> acc.combine(flow) { a, _ -> a} }
    }

    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().insert(category)
        }
    }

    fun update(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().update(category)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().delete(category)
        }
    }

    fun sketchPreview(note: Note, size: Int): Bitmap? {
        if (note.type == DbContract.NoteEntry.TYPE_SKETCH) {
            val path = "${filesDir.path}/sketches${note.content}"
            return if (File(path).exists()) {
                BitmapDrawable( resources, filesDir.path + "/sketches" + note.content).toBitmap(size, size, Bitmap.Config.ARGB_8888)
            } else {
                null
            }
        } else {
            throw IllegalArgumentException("Only sketch notes allowed")
        }
    }

    fun checklistPreview(note: Note): List<Pair<Boolean, String>> {
        if (note.type != DbContract.NoteEntry.TYPE_CHECKLIST) {
            throw IllegalArgumentException("Only checklist notes allowed")
        }
        return ChecklistUtil.parse(note.content).map {(checked, name) ->
            return@map Pair(checked, String.format("[%s] $name", if (checked) "x" else "  "))
        }
    }

    companion object {
        private const val CAT_ALL = -1
    }
}