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
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.adapter.ChecklistAdapter
import org.secuso.privacyfriendlynotes.ui.util.ChecklistUtil
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.util.Collections

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
                Collections.swap(adapter.items, from, to)
                adapter.notifyItemMoved(to, from)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeItem(viewHolder.adapterPosition)
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

//        lvItemList.setMultiChoiceModeListener(object : MultiChoiceModeListener {
//            override fun onItemCheckedStateChanged(
//                mode: ActionMode,
//                position: Int,
//                id: Long,
//                checked: Boolean
//            ) {
//            }
//
//            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
//                // Inflate the menu for the CAB
//                val inflater = mode.menuInflater
//                inflater.inflate(R.menu.checklist_cab, menu)
//                return true
//            }
//
//            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
//                return false
//            }
//
//            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
//                // Respond to clicks on the actions in the CAB
//                return when (item.itemId) {
//                    R.id.action_delete -> {
//                        deleteSelectedItems()
//                        mode.finish() // Action picked, so close the CAB
//                        true
//                    }
//                    R.id.action_edit -> {
////                        lvItemList.checkedItemPositions.forEach { key, value -> if (value) temp.add(checklistAdapter.getItem(key)) }
//                        val selected = (0 until lvItemList.checkedItemPositions.size)
//                            .filter { lvItemList.checkedItemPositions.valueAt(it) }
//                            .map { checklistAdapter.getItem(it)!! }
//                        if (selected.size > 1) {
//                            Toast.makeText(applicationContext, R.string.toast_checklist_oneItem, Toast.LENGTH_SHORT).show()
//                            false
//                        } else {
//                            val taskEditText = EditText(this@ChecklistNoteActivity)
//                            taskEditText.setText(selected.first().name)
//                            val dialog = AlertDialog.Builder(this@ChecklistNoteActivity)
//                                .setTitle(String.format(getString(R.string.dialog_checklist_edit), selected.first().name))
//                                .setView(taskEditText)
//                                .setPositiveButton(
//                                    R.string.action_edit
//                                ) { _, _ ->
//                                    val text = taskEditText.text.toString()
//                                    val pos = checklistAdapter.getPosition(selected.first())
//                                    val newItem = CheckListItem(selected.first().isChecked, text)
//                                    checklistAdapter.remove(selected.first())
//                                    checklistAdapter.insert(newItem, pos)
//                                }
//                                .setNegativeButton(R.string.action_cancel, null)
//                                .create()
//                            dialog.show()
//                            true
//                        }
//                    }
//                    else -> false
//                }
//            }
//
//            override fun onDestroyActionMode(mode: ActionMode) {
//                val a = lvItemList.adapter as ArrayAdapter<*>
//                a.notifyDataSetChanged()
//            }
//        })
//        checklistAdapter = CheckListAdapter(baseContext, R.layout.item_checklist, itemNamesList)
//        lvItemList.adapter = checklistAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_checklist, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_convert_to_note -> {

                val text = adapter.items.joinToString(System.lineSeparator()) { (_, text) -> text }
                super.convertNote(text, DbContract.NoteEntry.TYPE_TEXT) {
                    Log.d("Test", "id: $it, content: $text")
                    val i = Intent(application, TextNoteActivity::class.java)
                    i.putExtra(BaseNoteActivity.EXTRA_ID, it)
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
        if (adapter.items.isNotEmpty()) {
            adapter.items.clear()
        }
        adapter.items.addAll(ChecklistUtil.parse(note.content))
        adapter.notifyDataSetChanged()
    }

    override fun hasNoteChanged(title: String, category: Int): Pair<Boolean, Int> {
        val intent = intent
        return Pair(
            (title.isNotEmpty() || adapter.items.isNotEmpty()) && -5 != intent.getIntExtra(EXTRA_CATEGORY, -5),
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
        val jsonArray = ChecklistUtil.json(adapter.items)
        if (name.isEmpty() && jsonArray.length() == 0) {
            return ActionResult(false, null)
        }
        return ActionResult(true, Note(name, jsonArray.toString(), DbContract.NoteEntry.TYPE_CHECKLIST, category))
    }

    override fun onSaveExternalStorage(basePath: File, name: String) {
        val file = File(basePath, "/checklist_$name.txt")
        try {
            // Make sure the directory exists.
            if (basePath.exists() || basePath.mkdirs()) {
                val out = PrintWriter(file)
                out.println(name)
                out.println()
                out.println(getContentString())
                out.close()
                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(
                    this, arrayOf(file.toString()), null
                ) { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                }
                Toast.makeText(
                    applicationContext,
                    String.format(getString(R.string.toast_file_exported_to), file.absolutePath),
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: IOException) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing $file", e)
        }
    }

    private fun getContentString(): String {
        return adapter.items.joinToString(separator = "\n") { (checked, name) -> "- $name [${if (checked) "✓" else "   "}]" }
    }

//    private fun deleteSelectedItems() {
//        val checkedItemPositions = lvItemList.checkedItemPositions
//        val temp = ArrayList<CheckListItem?>()
//        for (i in 0 until checkedItemPositions.size()) {
//            if (checkedItemPositions.valueAt(i)) {
//                temp.add(checklistAdapter.getItem(checkedItemPositions.keyAt(i)))
//            }
//        }
//        if (temp.isNotEmpty()) {
//            itemNamesList.removeAll(temp.toSet())
//        }
//    }

    private fun addItem() {
        adapter.addItem(etNewItem.text.toString())
        etNewItem.setText("")
    }
}