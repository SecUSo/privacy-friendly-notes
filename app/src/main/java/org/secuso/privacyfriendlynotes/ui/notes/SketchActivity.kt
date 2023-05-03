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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import com.simplify.ink.InkView
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import petrov.kristiyan.colorpicker.ColorPicker
import petrov.kristiyan.colorpicker.ColorPicker.OnFastChooseColorListener
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Activity that allows to add, edit and delete sketch notes.
 */
class SketchActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_SKETCH) {
    private val drawView: InkView by lazy { findViewById(R.id.draw_view) }
    private val emptyBitmap by lazy {
        Bitmap.createBitmap(
            drawView.bitmap.width,
            drawView.bitmap.height,
            drawView.bitmap.config
        )
    }
    private val btnColorSelector: Button by lazy { findViewById(R.id.btn_color_selector) }
    private var mFileName = "finde_die_datei.mp4"
    private var mFilePath: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_sketch)

        btnColorSelector.setOnClickListener(this)
        btnColorSelector.setBackgroundColor(Color.BLACK)
        drawView.setColor(Color.BLACK)
        drawView.setMinStrokeWidth(1.5f)
        drawView.setMaxStrokeWidth(6f)
        super.onCreate(savedInstanceState)
    }

    override fun onLoadActivity() {}
    override fun onNoteLoadedFromDB(note: Note) {
        mFileName = note.content
        mFilePath = filesDir.path + "/sketches" + mFileName
        drawView.background = BitmapDrawable(resources, mFilePath)
    }

    override fun onNewNote() {
        mFileName = "/sketch_" + System.currentTimeMillis() + ".PNG"
        mFilePath = filesDir.path + "/sketches"
        File(mFilePath!!).mkdirs() //ensure that the file exists
        mFilePath = filesDir.path + "/sketches" + mFileName
    }

    override fun shareNote(name: String): Intent {
        val tempPath = mFilePath!!.substring(0, mFilePath!!.length - 3) + "jpg"
        val sketchFile = File(tempPath)

        val map = BitmapDrawable(resources, mFilePath).bitmap ?: emptyBitmap
        val bm = overlay(drawView.bitmap, map)
        val canvas = Canvas(bm)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(
            overlay(
                drawView.bitmap,
                map
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
        return sendIntent
    }

    override fun determineToSave(title: String, category: Int): Pair<Boolean, Int> {
        val intent = intent
        return Pair(
            !drawView.bitmap.sameAs(emptyBitmap) && -5 != intent.getIntExtra(EXTRA_CATEGORY, -5),
            R.string.toast_emptyNote
        )
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v.id == R.id.btn_color_selector) {
            displayColorDialog()
        }
    }

    override fun updateNoteToSave(name: String, category: Int): Note {
        val oldSketch = BitmapDrawable(resources, mFilePath).bitmap
        val newSketch = drawView.bitmap
        try {
            val fo = FileOutputStream(File(mFilePath!!))
            overlay(oldSketch, newSketch).compress(Bitmap.CompressFormat.PNG, 0, fo)
            fo.flush()
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Note(name, mFileName, DbContract.NoteEntry.TYPE_SKETCH, category)
    }

    override fun noteToSave(name: String, category: Int): Note? {
        val bitmap = drawView.bitmap
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
        if (name.isEmpty() && bitmap.sameAs(emptyBitmap)) {
            return null
        }
        return Note(name, mFileName, DbContract.NoteEntry.TYPE_SKETCH, category)
    }

    private fun displayColorDialog() {
        ColorPicker(this)
            .setOnFastChooseColorListener(object : OnFastChooseColorListener {
                override fun setOnFastChooseColorListener(position: Int, color: Int) {
                    drawView.setColor(color)
                    btnColorSelector.setBackgroundColor(color)
                }

                override fun onCancel() {}
            })
            .setColors(R.array.mdcolor_500)
            .setTitle(null)
            .show()
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