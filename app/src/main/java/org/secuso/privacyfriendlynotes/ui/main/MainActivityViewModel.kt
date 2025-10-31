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
import android.util.Log
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
import org.secuso.privacyfriendlynotes.ui.notes.AudioNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.ChecklistNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.SketchActivity
import org.secuso.privacyfriendlynotes.ui.notes.TextNoteActivity
import org.secuso.privacyfriendlynotes.ui.util.ChecklistUtil
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
        kotlin.runCatching {
            SortingOrder.valueOf(prefManager.getString(PreferenceKeys.SP_NOTES_ORDERING, SortingOrder.AlphabeticalAscending.name)!!)
        }.getOrElse { SortingOrder.AlphabeticalAscending }
    )
    private var reversed: MutableStateFlow<Boolean> = MutableStateFlow(
        prefManager.getBoolean(PreferenceKeys.SP_NOTES_REVERSED, false)
    )
    private var category: MutableStateFlow<Int> = MutableStateFlow(CAT_ALL)

    val trashedNotes: Flow<List<Note>> = repository.noteDao().allTrashedNotes
        .triggerOn(filter)
        .filterNotes()
    val activeNotes: Flow<List<Note>> = repository.noteDao().allActiveNotes
        .triggerOn(filter, ordering, category, reversed)
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
        reversed.value = if (this.ordering.value != ordering) {
            prefManager.edit()
                .putString(PreferenceKeys.SP_NOTES_ORDERING, ordering.name)
                .apply()
            false
        } else {
            !reversed.value
        }
        prefManager.edit()
            .putBoolean(PreferenceKeys.SP_NOTES_REVERSED, reversed.value)
            .apply()
        this.ordering.value = ordering
    }

    fun getOrder(): SortingOrder {
        return this.ordering.value
    }

    fun isCustomOrdering(): Boolean {
        return this.ordering.value == SortingOrder.Custom
    }

    fun setCategory(id: Int) {
        this.category.value = id
    }

    fun getCategory(): Int {
        return this.category.value
    }

    fun isReversed(): Boolean {
        return this.reversed.value
    }

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.noteDao().insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.Main) {
            consumer.accept(repository.categoryDao().getCategoryColor(category))
        }
    }

    private fun StateFlow<SortingOrder>.comparator(): (Note, Note) -> Int {
        return when (this.value) {
            SortingOrder.AlphabeticalAscending -> { a, b -> a.name.compareTo(b.name) }
            SortingOrder.LastModified -> { b, a -> a.last_modified.compareTo(b.last_modified) }
            SortingOrder.Creation -> { b, a -> a._id.compareTo(b._id) }
            SortingOrder.Custom -> { a, b -> a.custom_order.compareTo(b.custom_order) }
            SortingOrder.TypeAscending -> { a, b -> a.type.compareTo(b.type) }
        }
    }

    private fun Flow<List<Note>>.filterNotes(): Flow<List<Note>> {
        return this.map {
            it.filter { note ->
                if (note.name.contains(filter.value, ignoreCase = true)) {
                    return@filter true
                }
                when (note.type) {
                    DbContract.NoteEntry.TYPE_TEXT -> {
                        return@filter Html.fromHtml(note.content).toString().contains(filter.value, ignoreCase = true)
                    }

                    DbContract.NoteEntry.TYPE_CHECKLIST -> {
                        return@filter ChecklistUtil.parse(note.content).joinToString(System.lineSeparator()).contains(filter.value, ignoreCase = true)
                    }

                    else -> return@filter false
                }
            }
        }
    }

    private fun Flow<List<Note>>.sortNotes(): Flow<List<Note>> {
        return this.map { it.sortedWith(ordering.comparator()).apply { return@map if (reversed.value) this.reversed() else this } }
    }

    private fun Flow<List<Note>>.filterCategories(): Flow<List<Note>> {
        return this.map {
            it.filter { note ->
                note.category == category.value // Note matches current category
                        || category.value == CAT_ALL // We're in the all notes category
                        || (category.value == 0 && note.category == -1) // Note is still in old default category (-1). Should still show in new default category (0)
            }
        }
    }

    private fun <T> Flow<T>.triggerOn(vararg flows: Flow<*>): Flow<T> {
        return flows.fold(this) { acc, flow -> acc.combine(flow) { a, _ -> a } }
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

    private fun loadSketchBitmap(file: String): BitmapDrawable? {
        File("${filesDir.path}/sketches${file}").apply {
            if (exists()) {
                return BitmapDrawable(resources, path)
            } else {
                throw FileNotFoundException("Cannot open sketch: $path")
            }
        }
    }

    fun sketchPreview(note: Note, size: Int): Bitmap? {
        if (note.type == DbContract.NoteEntry.TYPE_SKETCH) {
            try {
                return loadSketchBitmap(note.content)?.toBitmap(size, size, Bitmap.Config.ARGB_8888)
            } catch (error: FileNotFoundException) {
                Log.e("Sketch preview", error.stackTraceToString())
                return null
            }
        } else {
            throw IllegalArgumentException("Only sketch notes allowed")
        }
    }

    fun checklistPreview(note: Note): List<Pair<Boolean, String>> {
        if (note.type != DbContract.NoteEntry.TYPE_CHECKLIST) {
            throw IllegalArgumentException("Only checklist notes allowed")
        }
        return ChecklistUtil.parse(note.content).map { (checked, name) ->
            val preview = if (name.length > 30) name.take(30) + "..." else name.take(33)
            return@map Pair(checked, "[${if (checked) "x" else "  "}] $preview")
        }
    }

    fun zipAllNotes(notes: List<Note>, output: OutputStream) {
        ZipOutputStream(output).use { zipOut ->
            notes.forEach { note ->
                val name = note.name.replace("/", "_")
                lateinit var entry: String
                lateinit var inputStream: InputStream
                when(note.type) {
                    DbContract.NoteEntry.TYPE_TEXT -> {
                        entry = "text/" + name + "_" + System.currentTimeMillis() + "_" + TextNoteActivity.getFileExtension()
                        inputStream = ByteArrayInputStream(note.content.toByteArray())
                    }
                    DbContract.NoteEntry.TYPE_CHECKLIST -> {
                        entry = "checklist/" + name  + "_" + System.currentTimeMillis() + "_" + ChecklistNoteActivity.getFileExtension()
                        inputStream = ByteArrayInputStream(note.content.toByteArray())
                    }
                    DbContract.NoteEntry.TYPE_AUDIO -> {
                        entry = "audio/" + name + "_" + System.currentTimeMillis() + "_" + AudioNoteActivity.getFileExtension()
                        inputStream = FileInputStream(File(filesDir.path + "/audio_notes" + note.content))
                    }
                    DbContract.NoteEntry.TYPE_SKETCH -> {
                        entry ="sketch/" + name + "_" + System.currentTimeMillis() + "_" + SketchActivity.getFileExtension()
                        inputStream = FileInputStream(File(filesDir.path + "/sketches" + note.content))
                    }
                }
                zipOut.putNextEntry(ZipEntry(entry))
                inputStream.copyTo(zipOut)
                zipOut.closeEntry()
            }
        }
    }

    companion object {
        private const val CAT_ALL = -1
    }
}