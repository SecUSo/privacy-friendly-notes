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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.ui.util.DarkModeUtil.Companion.isDarkMode

/**
 * Adapter that provides a binding for categories
 * @see org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity
 */
class CategoryAdapter(
) : RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {

    var displayColorDialog: ((Category, CategoryHolder) -> Unit)? = null
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
        val backgroundColor = if (color != null) Color.parseColor(color) else {
            val value = TypedValue()
            holder.itemView.context.theme.resolveAttribute(R.attr.colorOnSurface, value, true)
            value.data
        }
        if (PreferenceManager.getDefaultSharedPreferences(holder.itemView.context).getBoolean("settings_color_category", true)) {
            if (isDarkMode(holder.textViewCategoryName.context)) {
                holder.textViewCategoryName.setTextColor(backgroundColor)
                holder.btnColorSelector.setBackgroundColor(backgroundColor)
            } else {
                holder.itemView.setBackgroundColor(backgroundColor)
            }
        } else {
            holder.btnExpandMenu.visibility = View.GONE
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
        val btnExpandMenu: ImageButton by lazy { itemView.findViewById(R.id.category_expand_menu_button) }
        private val expandMenu: LinearLayout by lazy { itemView.findViewById(R.id.category_expand_menu) }
        private val btnResetColor: ImageButton by lazy { itemView.findViewById(R.id.category_item_color_reset) }
        val btnColorSelector: MaterialButton by lazy { itemView.findViewById(R.id.category_item_color_selector) }

        init {
            btnExpandMenu.setOnClickListener { expandMenu.visibility = if (expandMenu.visibility == View.GONE) { View.VISIBLE } else { View.GONE } }
            btnColorSelector.setOnClickListener { displayColorDialog?.let { it(categories[bindingAdapterPosition], this) } }
            btnResetColor.setOnClickListener {
                categories[bindingAdapterPosition].color = null
                updateCategory?.let { it(categories[bindingAdapterPosition]) }
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

}