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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.util.Function
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.model.SortingOrder
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.AboutActivity
import org.secuso.privacyfriendlynotes.ui.HelpActivity
import org.secuso.privacyfriendlynotes.ui.RecycleActivity
import org.secuso.privacyfriendlynotes.ui.SettingsActivity
import org.secuso.privacyfriendlynotes.ui.TutorialActivity
import org.secuso.privacyfriendlynotes.ui.adapter.NoteAdapter
import org.secuso.privacyfriendlynotes.ui.fragments.MainFABFragment
import org.secuso.privacyfriendlynotes.ui.helper.SortingOptionDialog
import org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity
import org.secuso.privacyfriendlynotes.ui.notes.AudioNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.BaseNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.ChecklistNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.SketchActivity
import org.secuso.privacyfriendlynotes.ui.notes.TextNoteActivity
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Collections


/**
 * The MainActivity includes the functionality of the primary screen.
 * It provides the possibility to access existing notes and add new ones.
 * Data is provided by the MainActivityViewModel.
 * @see MainActivityViewModel
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    //New Room variables
    private val mainActivityViewModel: MainActivityViewModel by lazy { ViewModelProvider(this)[MainActivityViewModel::class.java] }
    lateinit var adapter: NoteAdapter
    private val searchView: SearchView by lazy { findViewById(R.id.searchViewFilter) }
    private lateinit var fab: MainFABFragment
    private var skipNextNoteFlow = false

    // A launcher to receive and react to a NoteActivity returning a category
    // The category is used to set the selectecCategory
    private var setCategoryResultAfter = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null && data.hasExtra(BaseNoteActivity.EXTRA_CATEGORY)) {
            mainActivityViewModel.setCategory(data.getIntExtra(BaseNoteActivity.EXTRA_CATEGORY, CAT_ALL))
        }
        fab.close()
    }

    private val saveToExternalStorageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileOutputStream = contentResolver.openOutputStream(uri)
                if (fileOutputStream == null) {
                    return@registerForActivityResult
                }
                Toast.makeText(
                    applicationContext,
                    String.format(getString(R.string.toast_file_export_all_start)),
                    Toast.LENGTH_LONG
                ).show()
                CoroutineScope(Dispatchers.IO).launch {
                    mainActivityViewModel.zipAllNotes(adapter.notes, fileOutputStream)
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            String.format(getString(R.string.toast_file_exported_to), uri.toString()),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                if (className == MainFABFragment::class.java.name) {
                    this@MainActivity.fab = MainFABFragment {
                        Log.d("Received", "$it")
                        val i = Intent(
                            application, when (it) {
                                DbContract.NoteEntry.TYPE_TEXT -> TextNoteActivity::class.java
                                DbContract.NoteEntry.TYPE_CHECKLIST -> ChecklistNoteActivity::class.java
                                DbContract.NoteEntry.TYPE_AUDIO -> AudioNoteActivity::class.java
                                DbContract.NoteEntry.TYPE_SKETCH -> SketchActivity::class.java
                                else -> throw NotImplementedError("Note of type $it cannot be created")
                            }
                        )
                        i.putExtra(BaseNoteActivity.EXTRA_CATEGORY, mainActivityViewModel.getCategory())
                        setCategoryResultAfter.launch(i)
                        fab.close()
                    }
                    return fab
                }
                return super.instantiate(classLoader, className)
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        //Fill from Room database
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = NoteAdapter(
            this,
            mainActivityViewModel,
            PreferenceManager.getDefaultSharedPreferences(this).getBoolean("settings_color_category", true)
                    && mainActivityViewModel.getCategory() == CAT_ALL
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            mainActivityViewModel.activeNotes.collect { notes ->
                if (!skipNextNoteFlow) {
                    adapter.setNotes(notes)
                }
                skipNextNoteFlow = false
            }
        }

        val ith = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val to = target.bindingAdapterPosition
                val from = viewHolder.bindingAdapterPosition

                // swap custom_orders
                val temp = adapter.notes[from].custom_order
                adapter.notes[from].custom_order = adapter.notes[to].custom_order
                adapter.notes[to].custom_order = temp
                Collections.swap(adapter.notes, from, to)
                skipNextNoteFlow = true
                mainActivityViewModel.updateAll(listOf(adapter.notes[from], adapter.notes[to]))

                adapter.notifyItemMoved(to, from)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = adapter.getNoteAt(viewHolder.adapterPosition)
                if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getBoolean("settings_dialog_on_trashing", false)) {
                    MaterialAlertDialogBuilder(ContextThemeWrapper(this@MainActivity, R.style.AppTheme_PopupOverlay_DialogAlert))
                        .setTitle(String.format(getString(R.string.dialog_delete_title), note.name))
                        .setMessage(String.format(getString(R.string.dialog_delete_message), note.name))
                        .setPositiveButton(R.string.dialog_option_delete) { _, _ ->
                            adapter.notifyItemRemoved(viewHolder.adapterPosition)
                            trashNote(note)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .setOnDismissListener { adapter.notifyItemChanged(viewHolder.bindingAdapterPosition) }
                        .show()
                } else {
                    trashNote(note)
                }
            }
        })
        ith.attachToRecyclerView(recyclerView)
        adapter.startDrag = { holder -> ith.startDrag(holder) }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                mainActivityViewModel.setFilter(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }
        })
        searchView.setOnClickListener { searchView.onActionViewExpanded() }
        searchView.setOnQueryTextFocusChangeListener { _, focus ->
            TypedValue().apply {
                Log.d("Focus", focus.toString())
                this@MainActivity.theme.resolveAttribute(if (focus) R.attr.colorSurfaceVariant else R.attr.colorBackground, this, true)
                searchView.setBackgroundColor(this.data)
            }
        }

        /*
         * Handels when a note is clicked.
         */
        adapter.setOnItemClickListener { (_id, name, content, type, category, in_trash): Note, _ ->
            val launchActivity =
                Function<Class<out BaseNoteActivity?>, Void?> { activity: Class<out BaseNoteActivity?>? ->
                    val i = Intent(application, activity)
                    i.putExtra(BaseNoteActivity.EXTRA_ID, _id)
                    i.putExtra(BaseNoteActivity.EXTRA_TITLE, name)
                    i.putExtra(BaseNoteActivity.EXTRA_CONTENT, content)
                    i.putExtra(BaseNoteActivity.EXTRA_CATEGORY, category)
                    i.putExtra(BaseNoteActivity.EXTRA_ISTRASH, in_trash)
                    startActivity(i)
                    null
                }
            when (type) {
                DbContract.NoteEntry.TYPE_TEXT -> launchActivity.apply(TextNoteActivity::class.java)
                DbContract.NoteEntry.TYPE_AUDIO -> launchActivity.apply(AudioNoteActivity::class.java)
                DbContract.NoteEntry.TYPE_SKETCH -> launchActivity.apply(SketchActivity::class.java)
                DbContract.NoteEntry.TYPE_CHECKLIST -> launchActivity.apply(ChecklistNoteActivity::class.java)
            }
            fab.close()
        }
        val theme = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_day_night_theme", "-1")
        AppCompatDelegate.setDefaultNightMode(theme!!.toInt())
    }

    // taken from https://dev.to/ahmmedrejowan/hide-the-soft-keyboard-and-remove-focus-from-edittext-in-android-ehp on 14/03/2024
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                Rect().apply {
                    v.getGlobalVisibleRect(this)
                    if (!this.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        v.clearFocus()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        buildDrawerMenu()
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_sort_alphabetical) {
            val dialog = SortingOptionDialog(
                this,
                R.array.notes_sort_ordering_text,
                R.array.notes_sort_ordering_icons,
                mainActivityViewModel.getOrder(),
                mainActivityViewModel.isReversed(),
            ) { option: SortingOrder? ->
                mainActivityViewModel.setOrder(option!!)
                updateList(searchView.query.toString())
            }
            dialog.chooseSortingOption()
        } else if (id == R.id.action_export_all) {
            exportAllNotes()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Handles clicks on navigation items
     * @param item
     * @return
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        item.isCheckable = true
        item.isChecked = true
        val id = item.itemId
        if (id == R.id.nav_trash) {
            startActivity(Intent(application, RecycleActivity::class.java))
        } else if (id == R.id.nav_all) {
            mainActivityViewModel.setCategory(CAT_ALL)
        } else if (id == R.id.nav_manage_categories) {
            startActivity(Intent(application, ManageCategoriesActivity::class.java))
        } else if (id == R.id.nav_settings) {
            startActivity(Intent(application, SettingsActivity::class.java))
        } else if (id == R.id.nav_help) {
            startActivity(Intent(application, HelpActivity::class.java))
        } else if (id == R.id.nav_about) {
            startActivity(Intent(application, AboutActivity::class.java))
        } else if (id == R.id.nav_tutorial) {
            startActivity(Intent(application, TutorialActivity::class.java))
        } else {
            mainActivityViewModel.setCategory(id)
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onClick(v: View) {
    }

    override fun onPause() {
        // Save all changed orders if activity is paused
//        mainActivityViewModel.updateAll(adapter.notes)
        super.onPause()
    }

    private fun buildDrawerMenu() {
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val navMenu = navigationView.menu
        //reset the menu
        navMenu.clear()
        //Inflate the standard stuff
        val menuInflater = MenuInflater(applicationContext)
        menuInflater.inflate(R.menu.activity_main_drawer, navMenu)

        //Get the rest from the database
        lifecycleScope.launch {
            mainActivityViewModel.categories.collect {
                navMenu.add(R.id.drawer_group2, 0, Menu.NONE, getString(R.string.default_category)).setIcon(R.drawable.ic_label_black_24dp)
                for ((id, name) in it) {
                    navMenu.add(R.id.drawer_group2, id, Menu.NONE, name).setIcon(R.drawable.ic_label_black_24dp)
                }
            }
        }
    }

    /**
     * Sorts filtered notes alphabetical in descending or ascending order.
     * @param filter
     */
    private fun updateList(filter: String) {
        mainActivityViewModel.setFilter(filter)
    }

    private fun trashNote(note: Note) {
        note.in_trash = 1
        Toast.makeText(this@MainActivity, getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show()
        mainActivityViewModel.update(note)
    }

    private fun exportAllNotes() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        intent.putExtra(Intent.EXTRA_TITLE, "notes_export.zip")
        intent.type = "application/zip"
        saveToExternalStorageResultLauncher.launch(intent)
    }

    companion object {
        private const val CAT_ALL = -1
        private const val TAG_WELCOME_DIALOG = "welcome_dialog"
    }
}