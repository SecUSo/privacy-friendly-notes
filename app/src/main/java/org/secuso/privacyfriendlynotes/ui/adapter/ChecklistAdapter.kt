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

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.ui.SettingsActivity
import org.secuso.privacyfriendlynotes.ui.util.ChecklistItem
import java.util.Collections

/**
 * Provides bindings to show a Checklist-Item in a RecyclerView.
 * @author Patrick Schneider
 */
class ChecklistAdapter(
    var isEnabled: Boolean,
    private val startDrag: (ItemHolder) -> Unit,
) : RecyclerView.Adapter<ChecklistAdapter.ItemHolder>() {

    private var items: MutableList<ChecklistItem> = mutableListOf()
    var hasChanged = false
        private set

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<ChecklistItem>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    fun getItems(): List<ChecklistItem> {
        return items
    }

    fun swap(from: Int, to: Int) {
        Collections.swap(items, from, to)
        hasChanged = true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        items.forEach { it.state = true }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deselectAll() {
        items.forEach { it.state = false }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return ItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val (checked, item) = items[position]
        holder.textView.text = item
        holder.checkbox.isChecked = checked
        holder.dragHandle.setOnTouchListener { v, _ ->
            startDrag(holder)
            v.performClick()
        }
        holder.checkbox.setOnClickListener { _ ->
            items[holder.bindingAdapterPosition].state = holder.checkbox.isChecked
            holder.textView.apply {
                paintFlags = if (holder.checkbox.isChecked) {
                    paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            hasChanged = true
        }
        holder.textView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(text: Editable?) {
                items[holder.bindingAdapterPosition].name = (text ?: "").toString()
                hasChanged = true
            }

        })

        holder.textView.apply {
            paintFlags = if (checked) {
                paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            textSize = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.PREF_CUSTOM_FONT_SIZE, "15")!!.toFloat()
        }

        holder.textView.isEnabled = isEnabled
        holder.checkbox.isEnabled = isEnabled
        holder.dragHandle.isEnabled = isEnabled
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(item: String) {
        this.items.add(ChecklistItem(false, item))
        notifyItemInserted(items.size - 1)
        hasChanged = true
    }

    fun removeItem(position: Int) {
        this.items.removeAt(position)
        hasChanged = true
        notifyItemRemoved(position)
    }

    /**
     * The view holder presenting a checklist item.
     * @author Patrick Schneider
     */
    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.item_name)
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.item_checkbox)
        val dragHandle: View = itemView.findViewById(R.id.drag_handle)
    }
}