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
import androidx.preference.PreferenceManager
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val trashedNotes by lazy {
        mainActivityViewModel.trashedNotes.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).stateIn(lifecycleScope, SharingStarted.Lazily, listOf())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRecycle)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            trashedNotes.collect { adapter.setNotes(it) }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                mainActivityViewModel.setFilter(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                mainActivityViewModel.setFilter(query)
                return true
            }
        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = adapter.notes[viewHolder.bindingAdapterPosition]
                MaterialAlertDialogBuilder(ContextThemeWrapper(this@RecycleActivity, R.style.AppTheme_PopupOverlay_DialogAlert))
                    .setTitle(String.format(getString(R.string.dialog_restore_title), note.name))
                    .setMessage(String.format(getString(R.string.dialog_restore_message), note.name))
                    .setPositiveButton(R.string.dialog_option_delete) { _, _ ->
                        mainActivityViewModel.delete(note)
                        adapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
                    }
                    .setNegativeButton(R.string.dialog_option_restore) { _, _ ->
                        note.in_trash = 0
                        mainActivityViewModel.update(note)
                        adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
                    }
                    .setOnDismissListener { adapter.notifyItemChanged(viewHolder.bindingAdapterPosition) }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }).attachToRecyclerView(recyclerView)

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recycle, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_all -> {
                MaterialAlertDialogBuilder(ContextThemeWrapper(this, R.style.AppTheme_PopupOverlay_DialogAlert))
                    .setTitle(getString(R.string.dialog_delete_all_recycle_bin_title))
                    .setMessage(getString(R.string.dialog_delete_all_recycle_bin_message))
                    .setPositiveButton(R.string.dialog_option_delete) { _, _ ->
                        lifecycleScope.launch { trashedNotes.value.forEach { mainActivityViewModel.delete(it) } }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog(note: Note, position: Int) {

    }
}
