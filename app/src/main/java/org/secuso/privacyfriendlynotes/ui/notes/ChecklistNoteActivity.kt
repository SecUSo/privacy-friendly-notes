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
package org.secuso.privacyfriendlynotes.ui.notes

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannedString
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.adapter.ChecklistAdapter
import org.secuso.privacyfriendlynotes.ui.util.ChecklistUtil
import java.io.OutputStream
import java.io.PrintWriter

/**
 * Activity that allows to add, edit and delete checklist notes.
 */
class ChecklistNoteActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_CHECKLIST) {
    private val etNewItem: EditText by lazy { findViewById(R.id.etNewItem) }
    private val btnAdd: Button by lazy { findViewById(R.id.btn_add) }
    private val checklist: RecyclerView by lazy { findViewById(R.id.itemList) }
    private lateinit var adapter: ChecklistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_checklist_note)
        findViewById<View>(R.id.btn_add).setOnClickListener(this)
        super.onCreate(savedInstanceState)
    }

    override fun onLoadActivity() {
        etNewItem.setOnEditorActionListener { _, _, event ->
            if (event == null && etNewItem.text.isNotEmpty()) {
                addItem()
            }
            return@setOnEditorActionListener true
        }

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun isLongPressDragEnabled() = false

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val to = target.bindingAdapterPosition
                val from = viewHolder.bindingAdapterPosition
                adapter.swap(from, to)
                adapter.notifyItemMoved(to, from)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeItem(viewHolder.bindingAdapterPosition)
            }
        }
        val ith = ItemTouchHelper(itemTouchCallback)
        adapter = ChecklistAdapter(mutableListOf()) { holder -> ith.startDrag(holder) }
        checklist.adapter = adapter
        checklist.layoutManager = LinearLayoutManager(this)
        btnAdd.setOnClickListener {
            if (etNewItem.text.isNotEmpty()) {
                addItem()
            }
        }
        ith.attachToRecyclerView(checklist)
        adaptFontSize(etNewItem)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_checklist, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_convert_to_note -> {
                super.convertNote(Html.toHtml(SpannedString(getContentString())), DbContract.NoteEntry.TYPE_TEXT) {
                    val i = Intent(application, TextNoteActivity::class.java)
                    i.putExtra(EXTRA_ID, it)
                    startActivity(i)
                    finish()
                }
            }

            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewNote() {

    }

    override fun onNoteLoadedFromDB(note: Note) {
        adapter.setAll(ChecklistUtil.parse(note.content))
    }

    override fun hasNoteChanged(title: String, category: Int): Pair<Boolean, Int> {
        val intent = intent
        return Pair(
            (title.isNotEmpty() || adapter.getItems().isNotEmpty()),
            R.string.toast_emptyNote
        )
    }

    override fun shareNote(name: String): ActionResult<Intent, Int> {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "$name\n\n${getContentString()}")
        return ActionResult(true, sendIntent)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_add && etNewItem.text.toString().isNotEmpty()) {
            addItem()
        }
    }

    override fun onNoteSave(name: String, category: Int): ActionResult<Note, Int> {
        val jsonArray = ChecklistUtil.json(adapter.getItems())
        if (name.isEmpty() && jsonArray.length() == 0) {
            return ActionResult(false, null)
        }
        return ActionResult(true, Note(name, jsonArray.toString(), DbContract.NoteEntry.TYPE_CHECKLIST, category))
    }

    override fun getMimeType() = "text/plain"

    override fun getFileExtension() = ".txt"

    override fun onSaveExternalStorage(outputStream: OutputStream) {
        val out = PrintWriter(outputStream)
        out.println(getContentString())
        out.close()
    }

    private fun getContentString(): String {
        return adapter.getItems().joinToString(System.lineSeparator()) { (checked, name) -> "- [${if (checked) "✓" else "   "}] $name" }
    }

    private fun addItem() {
        adapter.addItem(etNewItem.text.toString())
        etNewItem.setText("")
    }
}