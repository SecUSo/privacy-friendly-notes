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

import android.app.Activity
import android.graphics.Color
import android.preference.PreferenceManager
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.main.MainActivityViewModel
import org.secuso.privacyfriendlynotes.ui.util.DarkModeUtil
import java.io.File

/**
 * Adapter that provides a binding for notes
 * @see org.secuso.privacyfriendlynotes.ui.main.MainActivity
 *
 * @see org.secuso.privacyfriendlynotes.ui.RecycleActivity
 */
class NoteAdapter(
    private val activity: Activity,
    private val mainActivityViewModel: MainActivityViewModel,
    var colorCategory: Boolean,
) : RecyclerView.Adapter<NoteAdapter.NoteHolder>() {
    var startDrag: ((NoteAdapter.NoteHolder) -> Unit)? = null
    var notes: MutableList<Note> = ArrayList()
        private set

    private var listener: ((Note, NoteHolder) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item, parent, false)
        return NoteHolder(itemView)
    }

    /**
     * Defines how notes are presented in the RecyclerView.
     * @see org.secuso.privacyfriendlynotes.ui.main.MainActivity
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        val currentNote = notes[position]
        holder.textViewTitle.text = currentNote.name
        holder.textViewDescription.text = ""
        val pref = PreferenceManager.getDefaultSharedPreferences(holder.itemView.context)
        holder.textViewDescription.visibility = if (pref.getBoolean("settings_show_preview", true)) View.VISIBLE else View.GONE
        holder.textViewExtraText.visibility = View.GONE
        holder.textViewExtraText.text = null
        holder.imageViewcategory.visibility = View.GONE
        holder.imageViewcategory.setImageResource(0)
        holder.dragHandle.setOnTouchListener { v, _ ->
            startDrag?.let { it(holder) }
            v.performClick()
        }

        if (colorCategory) {
            mainActivityViewModel.categoryColor(currentNote.category) {

                if (DarkModeUtil.isDarkMode(holder.textViewTitle.context)) {
                    val color: Int = it?.let { Color.parseColor(it) } ?: run {
                        val value = TypedValue()
                        holder.itemView.context.theme.resolveAttribute(R.attr.colorOnBackground, value, true)
                        value.data
                    }
                    holder.textViewTitle.setTextColor(color)
                    holder.textViewExtraText.setTextColor(color)
                } else {
                    val color: Int = it?.let { Color.parseColor(it) } ?: run {
                        val value = TypedValue()
                        holder.itemView.context.theme.resolveAttribute(R.attr.colorSurface, value, true)
                        value.data
                    }
                    holder.viewNoteItem.setBackgroundColor(color)
                }
            }
        }

        when (currentNote.type) {
            DbContract.NoteEntry.TYPE_TEXT -> {
                holder.textViewDescription.text = Html.fromHtml(currentNote.content)
                holder.textViewDescription.maxLines = 3
            }

            DbContract.NoteEntry.TYPE_AUDIO -> {
                holder.imageViewcategory.visibility = View.VISIBLE
                holder.imageViewcategory.setImageResource(R.drawable.ic_mic_icon_24dp)
            }

            DbContract.NoteEntry.TYPE_SKETCH -> {
                holder.imageViewcategory.visibility = View.VISIBLE
                holder.imageViewcategory.setBackgroundColor(run {
                    val value = TypedValue()
                    holder.itemView.context.theme.resolveAttribute(R.attr.colorSurfaceVariantLight, value, true)
                    value.data
                })
                if (pref.getBoolean("settings_show_preview", true)) {
                    holder.imageViewcategory.minimumHeight = 200; holder.imageViewcategory.minimumWidth = 200
                    Glide.with(activity).load(File("${activity.application.filesDir.path}/sketches${currentNote.content}"))
                        .placeholder(AppCompatResources.getDrawable(activity, R.drawable.ic_photo_icon_24dp))
                        .into(holder.imageViewcategory)
                } else {
                    holder.imageViewcategory.setImageResource(R.drawable.ic_photo_icon_24dp)
                }
            }

            DbContract.NoteEntry.TYPE_CHECKLIST -> {
                val preview = mainActivityViewModel.checklistPreview(currentNote)
                holder.textViewExtraText.text = "${preview.filter { it.first }.count()}/${preview.size}"
                holder.textViewExtraText.visibility = View.VISIBLE
                holder.imageViewcategory.visibility = View.GONE
                holder.textViewDescription.text = preview.take(3).joinToString(System.lineSeparator()) { it.second }
                holder.textViewDescription.maxLines = 3
            }
        }

        // if the Description is empty, don't show it
        if (holder.textViewDescription.text.toString().isEmpty()) {
            holder.textViewDescription.visibility = View.GONE
        }
        holder.dragHandle.visibility = if (mainActivityViewModel.isCustomOrdering()) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun setNotes(notes: List<Note>) {
        this.notes.clear()
        this.notes.addAll(notes)
        notifyDataSetChanged()
    }

    inner class NoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView
        val textViewDescription: TextView
        val imageViewcategory: ImageView
        val textViewExtraText: TextView
        val viewNoteItem: View
        val dragHandle: View

        init {
            textViewTitle = itemView.findViewById(R.id.text_view_title)
            textViewDescription = itemView.findViewById(R.id.text_view_description)
            imageViewcategory = itemView.findViewById(R.id.imageView_category)
            textViewExtraText = itemView.findViewById(R.id.note_text_extra)
            viewNoteItem = itemView.findViewById(R.id.note_item)
            dragHandle = itemView.findViewById(R.id.drag_handle)
            itemView.setOnClickListener {
                bindingAdapterPosition.apply {
                    if (listener != null && this != RecyclerView.NO_POSITION) {
                        listener!!(notes[this], this@NoteHolder)
                    }
                }
            }
        }
    }

    fun setOnItemClickListener(listener: (Note, NoteHolder) -> Unit) {
        this.listener = listener
    }

    fun getNoteAt(pos: Int): Note {
        return notes[pos]
    }
}