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
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.simplify.ink.InkView
import eltos.simpledialogfragment.SimpleDialog.OnDialogResultListener
import eltos.simpledialogfragment.color.SimpleColorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Activity that allows to add, edit and delete sketch notes.
 */
class SketchActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_SKETCH), OnDialogResultListener {
    private val drawView: InkView by lazy { findViewById(R.id.draw_view) }
    private val drawWrapper: LinearLayout by lazy { findViewById(R.id.sketch_wrapper) }
    private val btnColorSelector: Button by lazy { findViewById(R.id.btn_color_selector) }
    private lateinit var undoButton: MenuItem
    private lateinit var redoButton: MenuItem
    private var mFileName = "finde_die_datei.mp4"
    private var mFilePath: String? = null
    private var mTempFilePath: String? = null
    private var sketchLoaded = false
    private val undoStates = mutableListOf<Bitmap>()
    private var redoStates = mutableListOf<Bitmap>()
    private var state: Bitmap? = null
    private var oldSketch: BitmapDrawable? = null
    private var initialSize: Pair<Int, Int>? = null

    private val undoRedoEnabled by lazy { PreferenceManager.getDefaultSharedPreferences(this).getBoolean("settings_sketch_undo_redo", true) }

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

        drawView.viewTreeObserver.addOnGlobalLayoutListener {
            if (initialSize == null) {
                Log.d("Initial size", "${drawWrapper.width},${drawWrapper.height}")
                initialSize = Pair(drawWrapper.width, drawWrapper.height)
            }
            if (initialSize!!.first != drawView.layoutParams.width || initialSize!!.second != drawView.layoutParams.height) {
                Log.d("Set size", "to ${drawWrapper.width},${drawWrapper.height}, from ${drawView.width},${drawView.height}")
                drawView.layoutParams = LinearLayout.LayoutParams(initialSize!!.first, initialSize!!.second)
                if (oldSketch != null) {
                    drawView.background = oldSketch
                } else {
                    drawView.background = BitmapDrawable(resources, Bitmap.createScaledBitmap(drawView.bitmap, initialSize!!.first, initialSize!!.second, false))
                }
                if (state != null) {
                    drawView.drawBitmap(Bitmap.createScaledBitmap(state!!, initialSize!!.first, initialSize!!.second, false), 0f, 0f, null)
                }
            }
        }

        btnColorSelector.setOnClickListener(this)
        btnColorSelector.setBackgroundColor(Color.BLACK)
        drawView.setColor(Color.BLACK)
        drawView.setMinStrokeWidth(1.5f)
        drawView.setMaxStrokeWidth(6f)

        if (undoRedoEnabled) {
            drawView.setOnTouchListener { view, motionEvent ->
                view.onTouchEvent(motionEvent).let {
                    if (motionEvent.actionMasked == MotionEvent.ACTION_UP) {
                        if (state == null) {
                            state = emptyBitmap()
                        }
                        undoStates.add(state!!)
                        lifecycleScope.launch(Dispatchers.IO) {
                            saveBitmap(mTempFilePath!!)
                        }
                        redoStates.clear()
                        if (undoStates.size > 32) {
                            undoStates.removeFirst()
                        }
                        state = drawView.bitmap.copy(Bitmap.Config.ARGB_8888, false)
                        undoButton.isEnabled = true
                        redoButton.isEnabled = false
                    }

                    return@setOnTouchListener it
                }
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onLoadActivity() {}
    override fun onNoteLoadedFromDB(note: Note) {
        mFileName = note.content
        mFilePath = filesDir.path + "/sketches" + mFileName
        mTempFilePath = cacheDir.path + "/sketches" + mFileName
        File(cacheDir.path + "/sketches").mkdirs()
        oldSketch = try {
            loadSketchBitmap(this, note.content)
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "Cannot load sketch: ${e.printStackTrace()}")
            BitmapDrawable(resources, emptyBitmap())
        }
        drawView.background = oldSketch
        sketchLoaded = true
    }

    override fun onNewNote() {
        mFileName = "/sketch_" + System.currentTimeMillis() + ".PNG"
        mFilePath = filesDir.path + "/sketches"
        File(mFilePath!!).mkdirs() //ensure that the file exists
        File(cacheDir.path + "/sketches").mkdirs()
        mTempFilePath = cacheDir.path + "/sketches" + mFileName
        mFilePath = filesDir.path + "/sketches" + mFileName
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_sketch, menu)
        undoButton = menu!!.findItem(R.id.action_sketch_undo)
        redoButton = menu.findItem(R.id.action_sketch_redo)
        undoButton.isEnabled = false
        redoButton.isEnabled = false
        if (!undoRedoEnabled) {
            undoButton.setVisible(false)
            redoButton.setVisible(false)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sketch_undo -> {
                drawView.clear()
                if (undoStates.isNotEmpty()) {
                    redoStates.add(state!!)
                    undoRedoState(undoStates.removeLast())
                    lifecycleScope.launch(Dispatchers.IO) {
                        saveBitmap(mTempFilePath!!)
                    }
                }
            }

            R.id.action_sketch_redo -> {
                if (redoStates.isNotEmpty()) {
                    undoStates.add(state!!)
                    undoRedoState(redoStates.removeLast())
                    lifecycleScope.launch(Dispatchers.IO) {
                        saveBitmap(mTempFilePath!!)
                    }
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
        val bm = map.overlay(drawView.bitmap)
        val canvas = Canvas(bm)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(
            map.overlay(drawView.bitmap),
            0f,
            0f,
            null
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

    override fun hasNoteChanged(title: String, category: Int): Pair<Boolean, Int?> {
        return Pair(
            if (undoRedoEnabled) {
                undoStates.isNotEmpty()
            } else {
                drawView.bitmap != emptyBitmap()
            }, if (sketchLoaded) null else R.string.toast_emptyNote
        )
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v.id == R.id.btn_color_selector) {
            displayColorDialog()
        }
    }

    override fun onNoteSave(name: String, category: Int): ActionResult<Note, Int> {
        if (undoRedoEnabled) {
            File(mTempFilePath!!).apply {
                if (this.exists()) {
                    this.copyTo(File(mFilePath!!), overwrite = true)
                    this.delete()
                }
            }
        } else {
            runBlocking {
                saveBitmap(mFilePath!!)
            }
        }

        if (name.isEmpty() && drawView.bitmap.sameAs(emptyBitmap())) {
            return ActionResult(false, null)
        }
        return ActionResult(true, Note(name, mFileName, DbContract.NoteEntry.TYPE_SKETCH, category))
    }

    private suspend fun saveBitmap(path: String) {
        val bitmap = oldSketch?.overlay(drawView.bitmap) ?: emptyBitmap().overlay(drawView.bitmap)
        try {
            val fo = withContext(Dispatchers.IO) {
                FileOutputStream(File(path))
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fo)
            withContext(Dispatchers.IO) {
                fo.flush()
                fo.close()
            }
        } catch (e: FileNotFoundException) {
            Log.d("Bitmap Error", e.stackTraceToString())
            e.printStackTrace()
        } catch (e: IOException) {
            Log.d("Bitmap Error", e.stackTraceToString())
            e.printStackTrace()
        }
    }

    private fun displayColorDialog() {
        SimpleColorDialog.build()
            .title("")
            .allowCustom(true)
            .cancelable(true) //allows close by tapping outside of dialog
            .colors(this, R.array.mdcolor_500)
            .choiceMode(SimpleColorDialog.SINGLE_CHOICE_DIRECT) //auto-close on selection
            .show(this, COLOR_DIALOG_TAG)
    }

    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        if (dialogTag == COLOR_DIALOG_TAG && which == DialogInterface.BUTTON_POSITIVE) {
            @ColorInt val color = extras.getInt(SimpleColorDialog.COLOR)
            drawView.setColor(color)
            btnColorSelector.setBackgroundColor(color)
            return true
        }
        return false
    }

    override fun getFileExtension() = ".jpeg"
    override fun getMimeType() = "image/jpeg"

    override fun onSaveExternalStorage(outputStream: OutputStream) {
        val bm = BitmapDrawable(resources, mFilePath).bitmap.overlay(drawView.bitmap)
        val canvas = Canvas(bm)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(
            BitmapDrawable(resources, mFilePath).bitmap.overlay(drawView.bitmap),
            0f,
            0f,
            null
        )
        bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    }

    companion object {
        private const val TAG = "SketchActivity"
        private const val COLOR_DIALOG_TAG = "org.secuso.privacyfriendlynotes.COLORDIALOG"

        //taken from http://stackoverflow.com/a/10616868
        fun Bitmap.overlay(bitmap: Bitmap): Bitmap {
            val bmOverlay = Bitmap.createBitmap(width, height, config)
            val canvas = Canvas(bmOverlay)
            canvas.drawBitmap(this, Matrix(), null)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            return bmOverlay
        }

        fun BitmapDrawable.overlay(bitmap: Bitmap): Bitmap = this.bitmap.overlay(bitmap)

        fun loadSketchBitmap(context: Context, file: String): BitmapDrawable {
            File("${context.filesDir.path}/sketches${file}").apply {
                if (exists()) {
                    return BitmapDrawable(context.resources, path)
                } else {
                    throw FileNotFoundException("Cannot open sketch: $path")
                }
            }
        }
    }
}