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
package org.secuso.privacyfriendlynotes.ui.adapter

import android.graphics.Color
import android.preference.PreferenceManager
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.model.Category

/**
 * Adapter that provides a binding for categories
 * @see org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity
 */
class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {

    var displayColorDialog: ((Category, CategoryHolder) -> Unit)? = null
    var displayChangeNameDialog: ((Category, CategoryHolder) -> Unit)? = null
    var updateCategory: ((Category) -> Unit)? = null

    var categories: List<Category> = ArrayList()
        private set

    fun setCategories(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val (_, name, color) = categories[position]
        holder.textViewCategoryName.text = name

        if (PreferenceManager.getDefaultSharedPreferences(holder.itemView.context).getBoolean("settings_color_category", true)) {
            if (color == null) {
                holder.btnColorSelector.setIconResource(R.drawable.transparent_checker)
                holder.btnColorSelector.setBackgroundColor(holder.btnColorSelector.resources.getColor(R.color.transparent))
            } else {
                holder.btnColorSelector.icon = null
                holder.btnColorSelector.setBackgroundColor(Color.parseColor(color))
            }
        } else {
            holder.colorSelectorWrapper.visibility = View.GONE
        }
        holder.itemView.isLongClickable = true
        holder.itemView.setOnLongClickListener {
            displayChangeNameDialog?.invoke(categories[position], holder)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun setCategoryColor(color: Int, position: Int) {
        categories[position].color = "#${Integer.toHexString(color)}"
        updateCategory?.let { it(categories[position]) }
    }

    inner class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCategoryName: TextView = itemView.findViewById(R.id.item_name)
        val colorSelectorWrapper: CardView = itemView.findViewById(R.id.btn_color_selector_wrapper)
        val btnColorSelector: MaterialButton by lazy { itemView.findViewById(R.id.category_item_color_selector) }

        init {
            btnColorSelector.setOnClickListener { displayColorDialog?.let { it(categories[bindingAdapterPosition], this) } }
        }
    }

}