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

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.FileProvider
import com.simplify.ink.InkView
import eltos.simpledialogfragment.SimpleDialog
import eltos.simpledialogfragment.color.SimpleColorDialog
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Activity that allows to add, edit and delete sketch notes.
 */
class SketchActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_SKETCH), SimpleDialog.OnDialogResultListener {

    private val drawView: InkView by lazy { findViewById(R.id.draw_view) }
    private val btnColorSelector: Button by lazy { findViewById(R.id.btn_color_selector) }
    private lateinit var undoButton: MenuItem
    private lateinit var redoButton: MenuItem
    private var mFileName = "finde_die_datei.mp4"
    private var mFilePath: String? = null
    private var sketchLoaded = false
    private val undoStates = mutableListOf<Bitmap>()
    private var redoStates = mutableListOf<Bitmap>()
    private var state: Bitmap? = null

    private fun emptyBitmap(): Bitmap {
        return Bitmap.createBitmap(
            drawView.bitmap.width,
            drawView.bitmap.height,
            drawView.bitmap.config
        )
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_sketch)

        btnColorSelector.setOnClickListener(this)
        btnColorSelector.setBackgroundColor(Color.BLACK)
        drawView.setColor(Color.BLACK)
        drawView.setMinStrokeWidth(1.5f)
        drawView.setMaxStrokeWidth(6f)
        drawView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.actionMasked == MotionEvent.ACTION_UP) {
                if (state == null) {
                    state = emptyBitmap()
                }
                undoStates.add(state!!)
                redoStates.clear()
                if (undoStates.size > 32) {
                    undoStates.removeFirst()
                }
                state = drawView.bitmap.copy(Bitmap.Config.ARGB_8888, false)
                undoButton.isEnabled = true
                redoButton.isEnabled = false
            }
            return@setOnTouchListener view.onTouchEvent(motionEvent)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onLoadActivity() {}
    override fun onNoteLoadedFromDB(note: Note) {
        mFileName = note.content
        mFilePath = filesDir.path + "/sketches" + mFileName
        drawView.background = BitmapDrawable(resources, mFilePath)
        sketchLoaded = true
    }

    override fun onNewNote() {
        mFileName = "/sketch_" + System.currentTimeMillis() + ".PNG"
        mFilePath = filesDir.path + "/sketches"
        File(mFilePath!!).mkdirs() //ensure that the file exists
        mFilePath = filesDir.path + "/sketches" + mFileName
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_sketch, menu)
        undoButton = menu!!.findItem(R.id.action_sketch_undo)
        redoButton = menu.findItem(R.id.action_sketch_redo)
        undoButton.isEnabled = false
        redoButton.isEnabled = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_sketch_undo -> {
                drawView.clear()
                if (undoStates.isNotEmpty()) {
                    redoStates.add(state!!)
                    undoRedoState(undoStates.removeLast())
                }
            }
            R.id.action_sketch_redo -> {
                if (redoStates.isNotEmpty()) {
                    undoStates.add(state!!)
                    undoRedoState(redoStates.removeLast())
                }
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun undoRedoState(state: Bitmap) {
        this.state = state
        drawView.drawBitmap(state, 0F, 0F, null)
        undoButton.isEnabled = undoStates.isNotEmpty()
        redoButton.isEnabled = redoStates.isNotEmpty()
    }

    override fun shareNote(name: String): ActionResult<Intent, Int> {
        val tempPath = mFilePath!!.substring(0, mFilePath!!.length - 3) + "jpg"
        val sketchFile = File(tempPath)

        val map = BitmapDrawable(resources, mFilePath).bitmap ?: emptyBitmap()
        val bm = overlay(map, drawView.bitmap)
        val canvas = Canvas(bm)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(
            overlay(
                map,
                drawView.bitmap
            ), 0f, 0f, null
        )
        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(sketchFile))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val contentUri = FileProvider.getUriForFile(
            applicationContext,
            "org.secuso.privacyfriendlynotes",
            sketchFile
        )
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "image/*"
        sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        sendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        return ActionResult(true, sendIntent)
    }

    override fun hasNoteChanged(title: String, category: Int): Pair<Boolean, Int> {
        val intent = intent
        return Pair(
            sketchLoaded ||  !drawView.bitmap.sameAs(emptyBitmap()) && -5 != intent.getIntExtra(EXTRA_CATEGORY, -5),
            R.string.toast_emptyNote
        )
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v.id == R.id.btn_color_selector) {
            displayColorDialog()
        }
    }

    override fun onNoteSave(name: String, category: Int): ActionResult<Note, Int> {
        val oldSketch = mFilePath?.let { BitmapDrawable(resources, it).bitmap } ?: emptyBitmap()
        val bitmap = overlay(oldSketch, drawView.bitmap)
        try {
            val fo = FileOutputStream(File(mFilePath!!))
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fo)
            fo.flush()
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (name.isEmpty() && bitmap.sameAs(emptyBitmap())) {
            return ActionResult(false, null)
        }
        return ActionResult(true, Note(name, mFileName, DbContract.NoteEntry.TYPE_SKETCH, category))
    }

    private fun displayColorDialog() {
        SimpleColorDialog.build()
            .title("")
            .allowCustom(true)
            .cancelable(true) //allows close by tapping outside of dialog
            .colors(this, R.array.mdcolor_500)
            .choiceMode(SimpleColorDialog.SINGLE_CHOICE_DIRECT) //auto-close on selection
            .show(this, SketchActivity.TAG)
    }

    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        if (dialogTag == SketchActivity.TAG && which == DialogInterface.BUTTON_POSITIVE) {
            @ColorInt val color = extras.getInt(SimpleColorDialog.COLOR)
            drawView.setColor(color)
            btnColorSelector.setBackgroundColor(color)
            return true
        }
        return false
    }

    override fun onSaveExternalStorage(basePath: File, name: String) {
        val file = File(basePath, "/$name.jpeg")
        try {
            // Make sure the directory exists.
            if (basePath.exists() || basePath.mkdirs()) {
                val bm = overlay(
                    BitmapDrawable(
                        resources, mFilePath
                    ).bitmap, drawView.bitmap
                )
                val canvas = Canvas(bm)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(
                    overlay(
                        BitmapDrawable(
                            resources, mFilePath
                        ).bitmap, drawView.bitmap
                    ), 0f, 0f, null
                )
                bm.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))

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

    companion object {
        private const val TAG = "org.secuso.privacyfriendlynotes.COLORDIALOG"

        //taken from http://stackoverflow.com/a/10616868
        fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
            val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
            val canvas = Canvas(bmOverlay)
            canvas.drawBitmap(bmp1, Matrix(), null)
            canvas.drawBitmap(bmp2, 0f, 0f, null)
            return bmOverlay
        }
    }
}