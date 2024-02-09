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
package org.secuso.privacyfriendlynotes.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialContainerTransform
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract

/**
 * This fragment represents the FAB of the main notes overview.
 * It transforms on-click to a sheet containing elements to create each note type.
 *
 * @author Patrick Schneider
 */
class MainFABFragment(
    private val onCreateNote: (Int) -> Unit
) : Fragment(R.layout.main_content_fab_menu) {
    private val fabContainer: LinearLayout by lazy { requireView().findViewById(R.id.fab_container) }
    private val fab: FloatingActionButton by lazy { requireView().findViewById(R.id.fab) }
    private val closeFab: MaterialButton by lazy { requireView().findViewById(R.id.fabClose) }
    private val fabMenu: View by lazy { requireView().findViewById(R.id.fab_menu) }
    private var fabMenuExpanded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab.setOnClickListener { open() }
        closeFab.setOnClickListener { close() }
        close()

        requireView().findViewById<View>(R.id.fab_text).setOnClickListener {
            onCreateNote(DbContract.NoteEntry.TYPE_TEXT)
            close()
        }
        requireView().findViewById<View>(R.id.fab_checklist).setOnClickListener {
            onCreateNote(DbContract.NoteEntry.TYPE_CHECKLIST)
            close()
        }
        requireView().findViewById<View>(R.id.fab_audio).setOnClickListener {
            onCreateNote(DbContract.NoteEntry.TYPE_AUDIO)
            close()
        }
        requireView().findViewById<View>(R.id.fab_sketch).setOnClickListener {
            onCreateNote(DbContract.NoteEntry.TYPE_SKETCH)
            close()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private val openTransition by lazy {
        MaterialContainerTransform().apply {
            startView = fabContainer
            endView = fabMenu

            addTarget(endView!!)

            scrimColor = Color.TRANSPARENT
        }
    }
    private val closeTransition by lazy {
        MaterialContainerTransform().apply {
            startView = fabMenu
            endView = fabContainer

            addTarget(endView!!)

            scrimColor = Color.TRANSPARENT
        }
    }

    fun close() {
        fabMenu.visibility = View.GONE
        TransitionManager.beginDelayedTransition(fabContainer, closeTransition)
        fab.visibility = View.VISIBLE
        fabMenuExpanded = false
    }

    fun open() {
        fab.visibility = View.GONE
        TransitionManager.beginDelayedTransition(fabContainer, openTransition)
        fabMenu.visibility = View.VISIBLE
        fabMenuExpanded = true
    }
}