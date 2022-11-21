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
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AbsListView.MultiChoiceModeListener
import androidx.appcompat.app.AlertDialog
import androidx.core.util.forEach
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.util.CheckListAdapter
import org.secuso.privacyfriendlynotes.ui.util.CheckListItem
import java.io.File
import java.io.IOException
import java.io.PrintWriter

/**
 * Activity that allows to add, edit and delete checklist notes.
 */
class ChecklistNoteActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_CHECKLIST), AdapterView.OnItemClickListener {
    private val etNewItem: EditText by lazy { findViewById(R.id.etNewItem) }
    private val lvItemList: ListView by lazy { findViewById(R.id.itemList) }
    private val itemNamesList = ArrayList<CheckListItem>()
    private lateinit var checklistAdapter: CheckListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_checklist_note)
        findViewById<View>(R.id.btn_add).setOnClickListener(this)
        super.onCreate(savedInstanceState)
    }

    override fun onLoadActivity() {
        //get rid of the old data. Otherwise we would have duplicates.
        itemNamesList.clear()
        adaptFontSize(etNewItem)
        lvItemList.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        lvItemList.onItemClickListener = this
        lvItemList.setMultiChoiceModeListener(object : MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(
                mode: ActionMode,
                position: Int,
                id: Long,
                checked: Boolean
            ) {
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                // Inflate the menu for the CAB
                val inflater = mode.menuInflater
                inflater.inflate(R.menu.checklist_cab, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                // Respond to clicks on the actions in the CAB
                return when (item.itemId) {
                    R.id.action_delete -> {
                        deleteSelectedItems()
                        mode.finish() // Action picked, so close the CAB
                        true
                    }
                    R.id.action_edit -> {
                        val temp = ArrayList<CheckListItem?>()
                        lvItemList.checkedItemPositions.forEach { key, value -> if (value) temp.add(checklistAdapter.getItem(key)) }
                        if (temp.size > 1) {
                            Toast.makeText(
                                applicationContext,
                                R.string.toast_checklist_oneItem,
                                Toast.LENGTH_SHORT
                            ).show()
                            false
                        } else {
                            val taskEditText = EditText(this@ChecklistNoteActivity)
                            val dialog = AlertDialog.Builder(this@ChecklistNoteActivity)
                                .setTitle(getString(R.string.dialog_checklist_edit) + " " + temp[0]!!.name)
                                .setView(taskEditText)
                                .setPositiveButton(
                                    R.string.action_edit
                                ) { _, _ ->
                                    val text = taskEditText.text.toString()
                                    val pos = checklistAdapter.getPosition(temp[0])
                                    val newItem = CheckListItem(temp[0]!!.isChecked, text)
                                    checklistAdapter.remove(temp[0])
                                    checklistAdapter.insert(newItem, pos)
                                }
                                .setNegativeButton(R.string.action_cancel, null)
                                .create()
                            dialog.show()
                            true
                        }
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                val a = lvItemList.adapter as ArrayAdapter<*>
                a.notifyDataSetChanged()
            }
        })
        checklistAdapter = CheckListAdapter(baseContext, R.layout.item_checklist, itemNamesList)
        lvItemList.adapter = checklistAdapter
    }

    override fun onNewNote() {

    }

    override fun onNoteLoadedFromDB(note: Note) {
        try {
            val content = JSONArray(note.content)
            itemNamesList.clear()
            for (i in 0 until content.length()) {
                val o = content.getJSONObject(i)
                checklistAdapter.add(CheckListItem(o.getBoolean("checked"), o.getString("name")))
            }
            checklistAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun determineToSaveOnAction(category: Int): Pair<Boolean, Int> {
        val intent = intent
        return Pair(
            itemNamesList.isNotEmpty() || category != intent.getIntExtra(
                EXTRA_CATEGORY,
                -1
            ) && -5 != intent.getIntExtra(
                EXTRA_CATEGORY, -5
            ),
            R.string.toast_emptyNote
        )
    }

    override fun shareNote(name: String): Intent {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "$name\n\n${getContentString()}")
        return sendIntent
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_add && etNewItem.text.toString().isNotEmpty()) {
            itemNamesList.add(CheckListItem(false, etNewItem.text.toString()))
            etNewItem.setText("")
            (lvItemList.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        }
    }

    override fun updateNoteToSave(name: String, category: Int): Note {
        val a: Adapter = lvItemList.adapter
        val jsonArray = JSONArray()
        try {
            for (i in itemNamesList.indices) {
                val temp = a.getItem(i) as CheckListItem
                val jsonObject = JSONObject()
                jsonObject.put("name", temp.name)
                jsonObject.put("checked", temp.isChecked)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return Note(name, jsonArray.toString(), DbContract.NoteEntry.TYPE_CHECKLIST, category)
    }

    override fun noteToSave(name: String, category: Int): Note? {
        val a: Adapter = lvItemList.adapter
        val jsonArray = JSONArray()
        try {
            for (i in itemNamesList.indices) {
                val temp = a.getItem(i) as CheckListItem
                val jsonObject = JSONObject()
                jsonObject.put("name", temp.name)
                jsonObject.put("checked", temp.isChecked)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (name.isEmpty() && jsonArray.length() == 0) {
            return null
        }
        return Note(name, jsonArray.toString(), DbContract.NoteEntry.TYPE_CHECKLIST, category)
    }

    override fun noteFromIntent(intent: Intent): Note {
        return Note(
            intent.getStringExtra(EXTRA_TITLE)!!,
            intent.getStringExtra(EXTRA_CONTENT)!!,
            DbContract.NoteEntry.TYPE_CHECKLIST,
            intent.getIntExtra(
                EXTRA_CATEGORY, -1
            )
        )
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
        return itemNamesList.joinToString(separator = "\n") { item -> "- ${item.name} [${if (item.isChecked) "✓" else "   "}]" }
    }

    //Click on a listitem
    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val temp = checklistAdapter.getItem(position)
        temp!!.isChecked = !temp.isChecked
        checklistAdapter.notifyDataSetChanged()
    }

    private fun deleteSelectedItems() {
        val checkedItemPositions = lvItemList.checkedItemPositions
        val temp = ArrayList<CheckListItem?>()
        for (i in 0 until checkedItemPositions.size()) {
            if (checkedItemPositions.valueAt(i)) {
                temp.add(checklistAdapter.getItem(checkedItemPositions.keyAt(i)))
            }
        }
        if (temp.isNotEmpty()) {
            itemNamesList.removeAll(temp.toSet())
        }
    }
}