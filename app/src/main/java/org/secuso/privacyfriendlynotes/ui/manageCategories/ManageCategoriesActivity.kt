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
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import eltos.simpledialogfragment.SimpleDialog.OnDialogResultListener
import eltos.simpledialogfragment.color.SimpleColorDialog
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.ui.adapter.CategoryAdapter

/**
 * Activity provides possibility to add, delete categories.
 * Data is provided by the ManageCategoriesViewModel
 * @see ManageCategoriesViewModel
 */
class ManageCategoriesActivity : AppCompatActivity(), View.OnClickListener, OnDialogResultListener {
    private val manageCategoriesViewModel: ManageCategoriesViewModel by lazy { ViewModelProvider(this)[ManageCategoriesViewModel::class.java] }
    private val etName: EditText by lazy { findViewById(R.id.etName) }
    private val recyclerList: RecyclerView by lazy { findViewById(R.id.recyclerview_category) }
    private val btnResetColor: ImageButton by lazy { findViewById(R.id.category_menu_color_reset) }
    private val btnColorSelector: MaterialButton by lazy { findViewById(R.id.btn_color_selector) }
    private val btnExpandMenu: ImageButton by lazy { findViewById(R.id.category_expand_menu_button) }
    private val expandMenu: LinearLayout by lazy { findViewById(R.id.category_expand_menu) }
    private var catColor: String? = null
    private lateinit var adapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)
        findViewById<View>(R.id.btn_add).setOnClickListener(this)

        this.recyclerList.layoutManager = LinearLayoutManager(this)
        this.recyclerList.setHasFixedSize(true)
        adapter = CategoryAdapter()
        adapter.displayColorDialog = { category, categoryHolder ->
            val bundle = Bundle()
            bundle.putInt(CATEGORY_COLOR,  categoryHolder.bindingAdapterPosition)
            displayColorDialog(bundle)
        }
        adapter.updateCategory = { manageCategoriesViewModel.update(it)}
        this.recyclerList.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currentCategory = adapter.categories[viewHolder.bindingAdapterPosition]
                AlertDialog.Builder(this@ManageCategoriesActivity)
                    .setTitle(String.format(getString(R.string.dialog_delete_title), currentCategory.name))
                    .setMessage(String.format(getString(R.string.dialog_delete_message), currentCategory.name))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(R.string.dialog_ok) { dialog, which -> deleteCategory(currentCategory) }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }).attachToRecyclerView(recyclerList)

        lifecycleScope.launch {
            manageCategoriesViewModel.allCategories.collect {
                adapter.setCategories(it)
            }
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("settings_color_category", true)) {
            val value = TypedValue()
            theme.resolveAttribute(R.attr.colorOnSurface, value, true)
            btnColorSelector.setBackgroundColor(value.data)
            btnExpandMenu.setOnClickListener { expandMenu.visibility = if (expandMenu.visibility == View.GONE) { View.VISIBLE } else { View.GONE } }
            btnResetColor.setOnClickListener {
                btnColorSelector.setBackgroundColor(resources.getColor(R.color.transparent))
                manageCategoriesViewModel
                catColor = null
            }
            btnColorSelector.setOnClickListener { displayColorDialog() }
        } else {
            btnExpandMenu.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_add) {
            if (etName.text.isNotEmpty()) {
                val category = Category(etName.text.toString(), catColor)
                if (manageCategoriesViewModel.allCategories.value.count { it.name == category.name } == 0) {
                    manageCategoriesViewModel.insert(category)
                }
            }
            etName.setText("")
        }
    }

    private fun deleteCategory(cat: Category) {

        // Delete all notes from category if the option is set
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp.getBoolean("settings_del_notes", false)) {
            lifecycleScope.launch {
                manageCategoriesViewModel.notes.collect {
                        notes -> notes.filter { it.category == cat._id }.forEach { manageCategoriesViewModel.delete(it) }
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
            .extra(bundle)
            .show(this, TAG_COLORDIALOG)
    }

    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        if (dialogTag == TAG_COLORDIALOG && which == DialogInterface.BUTTON_POSITIVE) {
            val color = extras.getInt(SimpleColorDialog.COLOR)
            val position = extras.getInt(CATEGORY_COLOR, -1)

            // Check if the user changes a category color
            if (position != -1) {
                manageCategoriesViewModel.update(adapter.categories[position], "#${Integer.toHexString(color)}")
            } else {
                btnColorSelector.setBackgroundColor(color)
                catColor = "#${Integer.toHexString(color)}"
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