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
package org.secuso.privacyfriendlynotes.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.adapter.NoteAdapter
import org.secuso.privacyfriendlynotes.ui.main.MainActivityViewModel

/**
 * Activity that allows to interact with trashed notes.
 */
class RecycleActivity : AppCompatActivity() {
    private val mainActivityViewModel: MainActivityViewModel by lazy { ViewModelProvider(this)[MainActivityViewModel::class.java] }
    private val searchView: SearchView by lazy { findViewById(R.id.searchViewFilterRecycle) }
    private val adapter: NoteAdapter by lazy { NoteAdapter(mainActivityViewModel, true) }
    private var filter: MutableLiveData<String> = MutableLiveData("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRecycle)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        mainActivityViewModel.trashedNotes.observe(
            this
        ) { notes -> adapter.setNotes(notes) }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                filter.value = newText
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                filter.value = query
                return true
            }
        })
        filter.observe(this) { it ->
            mainActivityViewModel.getTrashedNotesFiltered(it).observe(this) { notes ->
                if (notes != null) {
                    adapter.setNotes(notes)
                }
            }
        }
        adapter.setOnItemClickListener { note: Note ->
            AlertDialog.Builder(this@RecycleActivity)
                .setTitle(String.format(getString(R.string.dialog_restore_title), note.name))
                .setMessage(String.format(getString(R.string.dialog_restore_message), note.name))
                .setNegativeButton(R.string.dialog_option_delete) { _, _ ->
                    mainActivityViewModel.delete(note)
                }
                .setPositiveButton(R.string.dialog_option_restore) { _, _ ->
                    note.in_trash = 0
                    mainActivityViewModel.update(note)
                }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recycle, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_all -> {
                MaterialAlertDialogBuilder(this, R.style.AppTheme_PopupOverlay_DialogAlert)
                    .setTitle(getString(R.string.dialog_delete_all_title))
                    .setMessage(getString(R.string.dialog_delete_all_message))
                    .setPositiveButton(R.string.dialog_option_delete) { _, _ ->
                        mainActivityViewModel.getTrashedNotesFiltered(filter.value!!).observe(this) {notes ->
                            notes?.forEach { note -> if (note != null) { mainActivityViewModel.delete(note) }
                            }
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        // Do nothing
                    }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
