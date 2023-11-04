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
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import eltos.simpledialogfragment.SimpleDialog.OnDialogResultListener
import eltos.simpledialogfragment.color.SimpleColorDialog
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.ui.SettingsActivity
import org.secuso.privacyfriendlynotes.ui.adapter.CategoryAdapter

/**
 * Activity provides possibility to add, delete categories.
 * Data is provided by the ManageCategoriesViewModel
 * @see ManageCategoriesViewModel
 */
class ManageCategoriesActivity : AppCompatActivity(), View.OnClickListener, OnDialogResultListener {
    var manageCategoriesViewModel: ManageCategoriesViewModel? = null
    var allCategories: List<Category>? = null
    private val etName: EditText by lazy { findViewById(R.id.etName) }
    private val recyclerList: RecyclerView by lazy { findViewById(R.id.recyclerview_category) }
    private val btnResetColor: ImageButton by lazy { findViewById(R.id.category_menu_color_reset) }
    private val btnColorSelector: MaterialButton by lazy { findViewById(R.id.btn_color_selector) }
    private val btnExpandMenu: ImageButton by lazy { findViewById(R.id.category_expand_menu_button) }
    private val expandMenu: LinearLayout by lazy { findViewById(R.id.category_expand_menu) }
    private var catColor: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)
        findViewById<View>(R.id.btn_add).setOnClickListener(this)

        this.recyclerList.layoutManager = LinearLayoutManager(this)
        this.recyclerList.setHasFixedSize(true)
        val adapter = CategoryAdapter()
        this.recyclerList.adapter = adapter
        manageCategoriesViewModel = ViewModelProvider(this).get(ManageCategoriesViewModel::class.java)
        manageCategoriesViewModel!!.allCategoriesLive.observe(this) { categories ->
                adapter.setCategories(categories)
                allCategories = categories
        }
        adapter.setOnItemClickListener { currentCategory ->
            AlertDialog.Builder(this@ManageCategoriesActivity)
                .setTitle(String.format(getString(R.string.dialog_delete_title), currentCategory.name))
                .setMessage(String.format(getString(R.string.dialog_delete_message), currentCategory.name))
                .setNegativeButton(android.R.string.no) { dialog, which ->
                    //do nothing
                }
                .setPositiveButton(R.string.dialog_ok) { dialog, which -> deleteCategory(currentCategory) }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

        btnColorSelector.setBackgroundColor(resources.getColor(R.color.transparent))
        btnExpandMenu.setOnClickListener { expandMenu.visibility = if (expandMenu.visibility == View.GONE) { View.VISIBLE } else { View.GONE } }
        btnResetColor.setOnClickListener {
            btnColorSelector.setBackgroundColor(resources.getColor(R.color.transparent))
            catColor = null
        }
        btnColorSelector.setOnClickListener { displayColorDialog() }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_add) {
            if (etName.text.isNotEmpty()) {
                val category = Category(etName.text.toString(), catColor)
                if (allCategories!!.firstOrNull { it.name == category.name } == null) {
                    manageCategoriesViewModel!!.insert(category)
                }
            }
            etName.setText("")
        }
    }

    private fun deleteCategory(cat: Category) {

        // Delete all notes from category if the option is set
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp.getBoolean(SettingsActivity.PREF_DEL_NOTES, false)) {
            manageCategoriesViewModel!!.allNotesLiveData.observe(this) {
                notes -> notes.filter { it.category == cat._id }.forEach { manageCategoriesViewModel!!.delete(it) }
            }
        }
        manageCategoriesViewModel!!.delete(cat)
    }

    private fun displayColorDialog() {
        SimpleColorDialog.build()
            .title("")
            .allowCustom(true)
            .cancelable(true) //allows close by tapping outside of dialog
            .colors(this, R.array.mdcolor_500)
            .choiceMode(SimpleColorDialog.SINGLE_CHOICE_DIRECT) //auto-close on selection
            .show(this, TAG_COLORDIALOG)
    }

    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        if (dialogTag == TAG_COLORDIALOG && which == DialogInterface.BUTTON_POSITIVE) {
            @ColorInt val color = extras.getInt(SimpleColorDialog.COLOR)
            btnColorSelector.setBackgroundColor(color)
            catColor = "#${Integer.toHexString(color)}"
            return true
        }
        return false
    }

    companion object {
        private const val TAG_COLORDIALOG = "org.secuso.privacyfriendlynotes.COLORDIALOG"
    }
}