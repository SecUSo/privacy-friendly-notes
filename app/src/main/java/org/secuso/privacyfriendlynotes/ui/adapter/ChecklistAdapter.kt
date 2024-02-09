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

import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import org.secuso.privacyfriendlynotes.R
import java.util.Collections

/**
 * Provides bindings to show a Checklist-Item in a RecyclerView.
 * @author Patrick Schneider
 */
class ChecklistAdapter(
    private var items: MutableList<Pair<Boolean, String>>,
    private val startDrag: (ItemHolder) -> Unit,
) : RecyclerView.Adapter<ChecklistAdapter.ItemHolder>() {

    fun getItems(): List<Pair<Boolean, String>> {
        return items
    }
    fun swap(from: Int, to: Int) {
        Collections.swap(items, from, to)
    }

    fun setAll(items: Collection<Pair<Boolean, String>>) {
        if (this.items.isNotEmpty()) {
            this.items.clear()
        }
        this.items.addAll(items)
        notifyItemRangeChanged(0, this.items.size)
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
            items[holder.bindingAdapterPosition] = Pair(holder.checkbox.isChecked, holder.textView.text.toString())
            holder.textView.apply {
                paintFlags = if (holder.checkbox.isChecked) {
                    paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
        holder.textView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(text: Editable?) {
                items[holder.bindingAdapterPosition] = Pair(holder.checkbox.isChecked, (text ?: "").toString())
            }

        })

        holder.textView.apply {
            paintFlags = if (checked) {
                paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(item: String) {
        this.items.add(Pair(false,item))
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int) {
        this.items.removeAt(position);
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