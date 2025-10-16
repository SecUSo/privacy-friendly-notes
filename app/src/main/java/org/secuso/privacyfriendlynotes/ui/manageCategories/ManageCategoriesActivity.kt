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
package org.secuso.privacyfriendlynotes.ui.manageCategories

import android.content.DialogInterface
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eltos.simpledialogfragment.SimpleDialog.OnDialogResultListener
import eltos.simpledialogfragment.color.SimpleColorDialog
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.ui.SettingsActivity
import org.secuso.privacyfriendlynotes.ui.adapter.CategoryAdapter

/**
 * Activity provides possibility to add, delete categories.
 * Data is provided by the ManageCategoriesViewModel
 * @see ManageCategoriesViewModel
 */
class ManageCategoriesActivity : AppCompatActivity(), OnDialogResultListener {
    private val manageCategoriesViewModel: ManageCategoriesViewModel by lazy { ViewModelProvider(this)[ManageCategoriesViewModel::class.java] }
    private val recyclerList: RecyclerView by lazy { findViewById(R.id.recyclerview_category) }
    private val fab: FloatingActionButton by lazy { findViewById(R.id.fab_add) }
    private var onColorResult: ((String?) -> Unit)? = null
    private lateinit var adapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        this.recyclerList.layoutManager = LinearLayoutManager(this)
        this.recyclerList.setHasFixedSize(true)
        adapter = CategoryAdapter()
        adapter.displayColorDialog = { category, categoryHolder ->
            val bundle = Bundle()
            bundle.putInt(CATEGORY_COLOR, categoryHolder.bindingAdapterPosition)
            displayColorDialog(bundle)
        }
        adapter.updateCategory = { manageCategoriesViewModel.update(it) }
        adapter.displayChangeNameDialog = { category, categoryHolder ->
            displayChangeNameDialog(category)
        }
        this.recyclerList.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currentCategory = adapter.categories[viewHolder.bindingAdapterPosition]
                val deleteNotes = PreferenceManager.getDefaultSharedPreferences(this@ManageCategoriesActivity).getBoolean("settings_del_notes", false)
                MaterialAlertDialogBuilder(ContextThemeWrapper(this@ManageCategoriesActivity, R.style.AppTheme_PopupOverlay_DialogAlert))
                    .setTitle(String.format(getString(R.string.dialog_delete_title), currentCategory.name))
                    .setMessage(
                        String.format(
                            getString(
                                if (deleteNotes) R.string.dialog_delete_category_with_notes else R.string.dialog_delete_category_without_notes
                            ), currentCategory.name
                        )
                    )
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.dialog_option_delete) { dialog, which ->
                        adapter.notifyItemRemoved(viewHolder.adapterPosition)
                        deleteCategory(currentCategory)
                    }
                    .setOnDismissListener { adapter.notifyItemChanged(viewHolder.bindingAdapterPosition) }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }).attachToRecyclerView(recyclerList)
        fab.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_create_category, null)
            val name = view.findViewById<EditText>(R.id.etName)
            val colorSelector = view.findViewById<MaterialButton>(R.id.btn_color_selector)
            val colorMenu = view.findViewById<View>(R.id.color_menu)
            var color: String? = null
            this.onColorResult = {
                if (it == null) {
                    colorSelector.setIconResource(R.drawable.transparent_checker)
                    colorSelector.setBackgroundColor(resources.getColor(R.color.transparent))
                } else {
                    colorSelector.icon = null
                    colorSelector.setBackgroundColor(it.toColorInt())
                }
                color = it
            }
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("settings_color_category", true)) {
                val value = TypedValue()
                theme.resolveAttribute(R.attr.colorOnSurface, value, true)
                colorSelector.setBackgroundColor(value.data)
                colorSelector.setOnClickListener { displayColorDialog() }
            } else {
                colorMenu.visibility = View.GONE
            }

            MaterialAlertDialogBuilder(ContextThemeWrapper(this@ManageCategoriesActivity, R.style.AppTheme_PopupOverlay_DialogAlert))
                .setView(view)
                .setTitle(R.string.dialog_create_category_title)
                .setPositiveButton(R.string.dialog_create_category_btn) { _, _ ->
                    if (name.text.isNotEmpty()) {
                        val category = Category(name.text.toString(), color)
                        if (manageCategoriesViewModel.allCategories.value.count { it.name == category.name } == 0) {
                            manageCategoriesViewModel.insert(category)
                        }
                    }
                    onColorResult = null
                }
                .setOnDismissListener { onColorResult = null }
                .show()
        }

        lifecycleScope.launch {
            manageCategoriesViewModel.allCategories.collect {
                adapter.setCategories(it)
            }
        }
    }

    private fun deleteCategory(cat: Category) {

        // Delete all notes from category if the option is set
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp.getBoolean(SettingsActivity.PREF_DEL_NOTES, false)) {
            lifecycleScope.launch {
                manageCategoriesViewModel.notes.collect { notes ->
                    notes.filter { it.category == cat._id }.forEach { manageCategoriesViewModel.delete(it) }
                }
            }
        }
        manageCategoriesViewModel.delete(cat)
    }

    private fun displayColorDialog(bundle: Bundle = Bundle.EMPTY) {
        SimpleColorDialog.build()
            .title("")
            .allowCustom(true)
            .cancelable(true) //allows close by tapping outside of dialog
            .colors(this, R.array.mdcolor_500)
            .choiceMode(SimpleColorDialog.SINGLE_CHOICE_DIRECT) //auto-close on selection
            .neut(R.string.default_color)
            .neg(android.R.string.cancel)
            .extra(bundle)
            .show(this, TAG_COLORDIALOG)
    }

    private fun displayChangeNameDialog(category: Category) {
        val view = layoutInflater.inflate(R.layout.dialog_create_category, null)
        val name = view.findViewById<EditText>(R.id.etName)
        view.findViewById<MaterialButton>(R.id.btn_color_selector).visibility = View.GONE
        view.findViewById<View>(R.id.color_menu).visibility = View.GONE

        MaterialAlertDialogBuilder(ContextThemeWrapper(this@ManageCategoriesActivity, R.style.AppTheme_PopupOverlay_DialogAlert))
            .setView(view)
            .setTitle(R.string.dialog_change_category_name_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (name.text.isNotEmpty()) {
                    if (manageCategoriesViewModel.allCategories.value.count { it.name == category.name } == 0) {
                        manageCategoriesViewModel.update(category)
                    }
                }
            }
            .setOnDismissListener { }
            .show()
    }

    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        // 0 is dismiss
        if (dialogTag == TAG_COLORDIALOG && which != DialogInterface.BUTTON_NEGATIVE && which != 0) {
            val color = if (which == DialogInterface.BUTTON_POSITIVE) "#${Integer.toHexString(extras.getInt(SimpleColorDialog.COLOR))}" else null
            val position = extras.getInt(CATEGORY_COLOR, -1)

            // Check if the user changes a category color
            if (position != -1) {
                manageCategoriesViewModel.update(adapter.categories[position], color)
            } else {
                onColorResult?.let { it(color) }
            }
            return true
        }
        return false
    }

    companion object {
        private const val TAG_COLORDIALOG = "org.secuso.privacyfriendlynotes.COLORDIALOG"
        private const val CATEGORY_COLOR = "org.secuso.privacyfriendlynotes.CATEGORY_COLOR"
    }
}