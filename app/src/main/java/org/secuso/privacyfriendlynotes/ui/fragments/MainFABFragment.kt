package org.secuso.privacyfriendlynotes.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.transition.TransitionManager
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialContainerTransform
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract

class MainFABFragment(
): Fragment(R.layout.main_content_fab_menu) {
    private val fabContainer: LinearLayout by lazy { requireView().findViewById(R.id.fab_container) }
    private val fab: FloatingActionButton by lazy { requireView().findViewById(R.id.fab) }
    private val closeFab: MaterialButton by lazy { requireView().findViewById(R.id.fabClose) }
    private val fabMenu: View by lazy { requireView().findViewById(R.id.fab_menu) }
    private var fabMenuExpanded = false
    var onCreateNote: ((Int) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab.setOnClickListener { open() }
        closeFab.setOnClickListener { close() }
        close()

        requireView().findViewById<View>(R.id.fab_text).setOnClickListener {
            onCreateNote?.let { create -> create(DbContract.NoteEntry.TYPE_TEXT) }
            close()
        }
        requireView().findViewById<View>(R.id.fab_checklist).setOnClickListener {
            onCreateNote?.let { create -> create(DbContract.NoteEntry.TYPE_CHECKLIST) }
            close()
        }
        requireView().findViewById<View>(R.id.fab_audio).setOnClickListener {
            onCreateNote?.let { create -> create(DbContract.NoteEntry.TYPE_AUDIO) }
            close()
        }
        requireView().findViewById<View>(R.id.fab_sketch).setOnClickListener {
            onCreateNote?.let { create -> create(DbContract.NoteEntry.TYPE_SKETCH) }
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