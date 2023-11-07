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
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.Html
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.Consumer
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.secuso.privacyfriendlynotes.model.SortingOrder
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

    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val trashedNotes: LiveData<List<Note>> = repository.noteDao().allTrashedNotes
    val allCategoriesLive: LiveData<List<Category>> = repository.categoryDao().allCategoriesLive
    val filesDir = application.filesDir
    val resources = application.resources
    val sortingOrder = SortingOrder(application)

    fun setOrder(ordering: SortingOrder.Options) {
        this.sortingOrder.ordering = ordering
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

    private fun filterNoteFlow (filter: String, notes: Flow<List<Note>?>): Flow<List<Note>> {
        return notes.map {
            it.orEmpty().filter { note ->
                if (note!!.type == 1) {
                    val spanned = Html.fromHtml(note!!.content)
                    val text = spanned.toString()
                    if (text.contains(filter)) {
                        return@filter true
                    }
                } else {
                    if (note!!.type == 3) {
                        try {
                            val content = JSONArray(note!!.content)
                            for (i in 0 until content.length()) {
                                val o = content.getJSONObject(i)
                                if (o.getString("name")
                                        .contains(filter) || note!!.name.contains(filter)
                                ) {
                                    return@filter true
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        return@filter true
                    }
                }
                return@filter false;
            };
        };
    }

    fun getActiveNotes(): LiveData<List<Note>?> {
        val notes = MutableLiveData<List<Note>>();
        viewModelScope.launch(Dispatchers.Main) {
            val flow = when(sortingOrder.ordering) {
                SortingOrder.Options.AlphabeticalAscending -> repository.noteDao().allActiveNotesAlphabetical
                SortingOrder.Options.TypeAscending -> repository.noteDao().allActiveNotesType
                SortingOrder.Options.Creation -> repository.noteDao().allActiveNotesCreation
                SortingOrder.Options.LastModified -> repository.noteDao().allActiveNotesModified
                SortingOrder.Options.Custom -> repository.noteDao().allActiveNotesCustom
            }
            flow.collect {
                notes.value = it
            }
        }
        return notes
    }

    fun getActiveNotesFiltered(filter: String): LiveData<List<Note>?> {
        val filteredNotes = MutableLiveData<List<Note>>();
        viewModelScope.launch(Dispatchers.Main) {
            val flow = when(sortingOrder.ordering) {
                SortingOrder.Options.AlphabeticalAscending -> repository.noteDao().activeNotesFilteredAlphabetical(filter)
                SortingOrder.Options.TypeAscending -> repository.noteDao().activeNotesFilteredType(filter)
                SortingOrder.Options.Creation -> repository.noteDao().activeNotesFilteredCreation(filter)
                SortingOrder.Options.LastModified -> repository.noteDao().activeNotesFilteredModified(filter)
                SortingOrder.Options.Custom -> repository.noteDao().activeNotesFilteredCustom(filter)
            }
            filterNoteFlow(filter, flow).collect {
                filteredNotes.value = it
            }
        }
        return filteredNotes
    }

    fun getTrashedNotesFiltered(filter: String): LiveData<List<Note>?>{
        var filteredNotes = MutableLiveData<List<Note>>();
        viewModelScope.launch(Dispatchers.Main) {
            filterNoteFlow(filter, repository.noteDao().trashedNotesFiltered(filter)).collect {
                filteredNotes.value = it.filterNotNull()
            }
        }
        return filteredNotes
    }

    fun getActiveNotesFilteredFromCategory(filter: String,category: Int): LiveData<List<Note>?>{
        var filteredNotes = MutableLiveData<List<Note>>();
        viewModelScope.launch(Dispatchers.Main) {
            filterNoteFlow(filter, repository.noteDao().activeNotesFilteredFromCategory(filter,category)).collect {
                filteredNotes.value = it
            }
        }
        return filteredNotes
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

    fun swapNotesCustomOrder(a: Note, b: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.noteDao().update(a)
            repository.noteDao().update(b)
        }
    }

}