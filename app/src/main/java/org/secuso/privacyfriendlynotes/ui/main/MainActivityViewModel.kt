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
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.NoteDatabase
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.model.Note
import java.io.File

/**
 * The MainActivityViewModel provides the data for the MainActivity.
 * It is also used for the RecycleActivity.
 * @see MainActivity
 * @see RecycleActivity
 */

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val activeNotes: LiveData<List<Note>> = repository.noteDao().allActiveNotes
    val trashedNotes: LiveData<List<Note>> = repository.noteDao().allTrashedNotes
    val allCategoriesLive: LiveData<List<Category>> = repository.categoryDao().allCategoriesLive
    val filesDir = application.filesDir
    val resources = application.resources

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

    private fun filterNoteFlow (filter: String, notes: Flow<List<Note?>?>): Flow<List<Note?>> {
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

    fun getActiveNotesFiltered(filter: String): LiveData<List<Note?>?> {
        var filteredNotes = MutableLiveData<List<Note?>>();
        viewModelScope.launch(Dispatchers.Main) {
            filterNoteFlow(filter, repository.noteDao().activeNotesFiltered(filter)).collect {
                filteredNotes.value = it
            }
        }
        return filteredNotes
    }

    fun getActiveNotesFilteredAlphabetical(filter: String): LiveData<List<Note?>?>{
        var filteredNotes = MutableLiveData<List<Note?>>();
        viewModelScope.launch(Dispatchers.Main) {
            filterNoteFlow(filter, repository.noteDao().activeNotesFilteredAlphabetical(filter)).collect {
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

    fun getActiveNotesFilteredFromCategory(filter: String,category: Integer): LiveData<List<Note?>?>{
        var filteredNotes = MutableLiveData<List<Note?>>();
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
        try {
            val content = JSONArray(note.content)
            return (0 until content.length()).map {
                val obj = content.getJSONObject(it)
                return@map Pair(obj.getBoolean("checked"), String.format("[%s] ${obj.getString("name")}", if (obj.getBoolean("checked")) "x" else "  "))
            }.toList()
        } catch (ex: JSONException) {
            return ArrayList()
        }
    }

}