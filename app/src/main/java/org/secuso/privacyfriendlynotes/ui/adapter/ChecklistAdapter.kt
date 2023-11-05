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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import org.secuso.privacyfriendlynotes.R


class ChecklistAdapter(var items: MutableList<Pair<Boolean, String>>) : RecyclerView.Adapter<ChecklistAdapter.ItemHolder>() {

    private var onLongClickListener: ((View) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return ItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val (checked, item) = items[position]
        holder.textView.text = item
        holder.checkbox.isChecked = checked

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

    fun setOnLongClickListener(listener: (View) -> Unit) {
        this.onLongClickListener = listener
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.item_name)
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.item_checkbox)

        init {
            checkbox.setOnClickListener { _ ->
                items[adapterPosition] = Pair(checkbox.isChecked, items[adapterPosition].second)
                notifyItemChanged(adapterPosition)
            }
        }
    }
}