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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.TextKeyListener
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.helper.ArrowKeyLinkTouchMovementMethod
import org.secuso.privacyfriendlynotes.ui.helper.makeDraggable
import org.secuso.privacyfriendlynotes.ui.util.ChecklistUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.util.jar.Manifest
import kotlin.io.path.exists
import androidx.core.text.toHtml
import androidx.core.text.parseAsHtml
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.FileInputStream
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.moveTo
import kotlin.properties.Delegates
import kotlin.random.Random

/**
 * Activity that allows to add, edit and delete text notes.
 */
class TextNoteActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_TEXT) {

    private val etContent: EditText by lazy { findViewById(R.id.etContent) }
    private val boldBtn: FloatingActionButton by lazy { findViewById(R.id.btn_bold) }
    private val italicsBtn: FloatingActionButton by lazy { findViewById(R.id.btn_italics) }
    private val underlineBtn: FloatingActionButton by lazy { findViewById(R.id.btn_underline) }
    private val galleryBtn: FloatingActionButton by lazy { findViewById(R.id.btn_gallery) }
    private val cameraBtn: FloatingActionButton by lazy { findViewById(R.id.btn_camera) }
    private var lastCursorPosition = 0

    private val isBold = MutableLiveData(false)
    private val isItalic = MutableLiveData(false)
    private val isUnderline = MutableLiveData(false)

    private var hasChanged = false
    private var oldText: String? = null

    private val fileSizeLimit by lazy { PreferenceManager.getDefaultSharedPreferences(this@TextNoteActivity).getString("settings_import_text_file_size_limit", "10000")?.toInt() ?: 10000 }
    private val fileCharLimit by lazy { PreferenceManager.getDefaultSharedPreferences(this@TextNoteActivity).getString("settings_import_text_file_char_limit", "1000")?.toInt() ?: 1000 }

    // Remember all loaded images to delete all not used images at activity end
    private val loadedImages = mutableListOf<String>()
    val htmlImageGetter = Html.ImageGetter { source ->
        try {
            // This is intentionally not getImageFilePathForId as we want to produce a correct html file on export,
            // Which points correctly to the images directory next to the exported source file.
            val file = File("${filesDir.path}/text_notes/${id}/images", source)

            if (file.exists()) {
                val drawable = Drawable.createFromPath(file.absolutePath)
                drawable?.let {
                    it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                }
                loadedImages.add(source)
                return@ImageGetter drawable
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    private var imageFile: String? = null

    val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        Log.d("TextNoteActivity", "Received picture callback with result $it")
        if (!it || imageFile == null) {
            return@registerForActivityResult
        }
        // image was successfully stored in our requested location
        // so add the image to the text field
        insertImageToText(imageFile!!)
    }
    val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            imageFile = System.currentTimeMillis().toString() + ".png"
            getImageFilePathForId(id).apply {
                mkdirs()
                val file = File(this, imageFile!!)
                val uri = FileProvider.getUriForFile(this@TextNoteActivity, "org.secuso.privacyfriendlynotes", file)
                Log.d("TextNoteActivity", "Now attempting to take picture for ${imageFile} and uri ${uri}")
                takePictureLauncher.launch(uri)
            }
        }
    }

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) {
            return@registerForActivityResult
        }
        val file = "${System.currentTimeMillis()}.png"
        val inputStream = contentResolver.openInputStream(uri)
        getImageFilePathForId(id).apply {
            mkdirs()
            val outputStream = FileOutputStream(File(this, file))
            inputStream.use { input -> outputStream.use { input?.copyTo(it) } }
        }
        insertImageToText(file)
    }

    private fun insertImageToText(file: String) {
        // Use a random anchor to obtain the current cursor location and insert the image there.
        val anchor = UUID.randomUUID().toString()
        var html = SpannableStringBuilder(etContent.text).apply {
            insert(etContent.selectionStart, anchor)
        }.toHtml(HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        html = html.replace(anchor, "<br><img src=\"${file}\" alt=\"${file}\" />")
        etContent.setText(html.parseAsHtml(HtmlCompat.FROM_HTML_MODE_LEGACY, htmlImageGetter))
        etContent.setSelection(lastCursorPosition.coerceIn(0, etContent.text.length))
        Log.d("TextNoteActivity", "Successfully taken picture and inserted into text note")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_text_note)

        val fabMenuBtn = findViewById<FloatingActionButton>(R.id.fab_menu)
        val fabMenu = findViewById<View>(R.id.fab_menu_wrapper)
        fabMenuBtn.makeDraggable(fabMenuBtn.parent as View)
        var expanded = false
        fabMenuBtn.setOnClickListener {
            if (expanded) {
                expanded = false
                fabMenuBtn.setImageResource(R.drawable.ic_baseline_format_color_text_24)
                fabMenu.visibility = View.GONE
            } else {
                expanded = true
                fabMenuBtn.setImageResource(R.drawable.ic_baseline_close_24)
                fabMenu.visibility = View.VISIBLE
            }
        }
        boldBtn.setOnClickListener(this)
        italicsBtn.setOnClickListener(this)
        underlineBtn.setOnClickListener(this)
        galleryBtn.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        cameraBtn.setOnClickListener {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }


        isBold.observe(this) { b: Boolean ->
            boldBtn.backgroundTintList = ColorStateList.valueOf(if (b) Color.parseColor("#000000") else resources.getColor(R.color.colorSecuso))
        }
        isItalic.observe(this) { b: Boolean ->
            italicsBtn.backgroundTintList = ColorStateList.valueOf(if (b) Color.parseColor("#000000") else resources.getColor(R.color.colorSecuso))
        }
        isUnderline.observe(this) { b: Boolean ->
            underlineBtn.backgroundTintList = ColorStateList.valueOf(if (b) Color.parseColor("#000000") else resources.getColor(R.color.colorSecuso))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                isLocked.collect { readonly ->
                    if (readonly) {
                        etContent.keyListener = null
                        etContent.showSoftInputOnFocus = false
                    } else {
                        etContent.keyListener = TextKeyListener.getInstance()
                        etContent.showSoftInputOnFocus = true
                        etContent.movementMethod = ArrowKeyLinkTouchMovementMethod.getInstance()
                    }
                }
            }
        }

        etContent.movementMethod = ArrowKeyLinkTouchMovementMethod.getInstance()
        super.onCreate(savedInstanceState)
    }

    override fun onNoteLoadedFromDB(note: Note) {
        etContent.setText(
            note.content.parseAsHtml(
                HtmlCompat.FROM_HTML_MODE_LEGACY,
                htmlImageGetter
            ).trimEnd(' ', '\n'))
        etContent.setSelection(lastCursorPosition.coerceIn(0, etContent.text.length))
        oldText = etContent.text.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_text, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_convert_to_checklist -> {
                MaterialAlertDialogBuilder(ContextThemeWrapper(this@TextNoteActivity, R.style.AppTheme_PopupOverlay_DialogAlert))
                    .setTitle(R.string.dialog_convert_to_checklist_title)
                    .setMessage(R.string.dialog_convert_to_checklist_desc)
                    .setPositiveButton(R.string.dialog_convert_action) { _, _ ->
                        val json = ChecklistUtil.json(etContent.text.lines().filter { it.isNotBlank() }.map(ChecklistUtil::textToItem))
                        super.convertNote(json.toString(), DbContract.NoteEntry.TYPE_CHECKLIST) {
                            val i = Intent(application, ChecklistNoteActivity::class.java)
                            i.putExtra(EXTRA_ID, it)
                            startActivity(i)
                            finish()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
            R.id.action_export_plain -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.putExtra(Intent.EXTRA_TITLE, noteTitle + getFileExtension())
                intent.type = getMimeType()
                saveToExternalStorageResultLauncher.launch(intent)
            }

            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewNote() {
        if (intent != null) {
            val uri: Uri? = listOf(intent.data, intent.getParcelableExtra(Intent.EXTRA_STREAM)).firstNotNullOfOrNull { it }
            if (uri != null) {

                contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                    if (it.length > fileSizeLimit && fileSizeLimit > 0) {
                        Toast.makeText(applicationContext, R.string.toast_open_file_too_large, Toast.LENGTH_SHORT).show()
                        return
                    }
                }

                lateinit var title: String
                val text: MutableList<String> = mutableListOf()
                InputStreamReader(contentResolver.openInputStream(uri)).useLines {
                    val lines = it.iterator()
                    var characterRead = 0L;
                    val firstLine = if (lines.hasNext()) lines.next() else null
                    if (firstLine != null && PreferenceManager.getDefaultSharedPreferences(this@TextNoteActivity).getBoolean("settings_import_text_title_file_first_line", false)) {
                        title = firstLine
                    } else {
                        title = uri.path?.let { file -> File(file).nameWithoutExtension } ?: ""
                        if (firstLine != null) {
                            text += firstLine
                            characterRead += firstLine.length
                        }
                    }

                    // Limit max size of shared file to avoid unnecessary slow-down
                    // This should hopefully only apply to binary files as clear text files are normally smaller
                    for (line in lines.iterator()) {
                        if (characterRead > fileCharLimit && fileCharLimit > 0) {
                            Toast.makeText(applicationContext, R.string.toast_open_file_too_many_characters, Toast.LENGTH_SHORT).show()
                            break
                        }
                        text += line
                        characterRead += line.length
                    }
                }
                super.setTitle(Html.fromHtml(title).toString())
                etContent.setText(HtmlCompat.fromHtml(text.joinToString("<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY, htmlImageGetter, null))
            }
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                etContent.setText(HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY, htmlImageGetter, null))
            }
        }
    }

    override fun onLoadActivity() {
        adaptFontSize(etContent)
    }

    public override fun shareNote(name: String): ActionResult<Intent, Int> {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "$name \n\n ${etContent.text}")
        return ActionResult(true, sendIntent)
    }

    override fun hasNoteChanged(title: String, category: Int): Pair<Boolean, Int?> {
        hasChanged = hasChanged || (oldText?.trim() != etContent.text.toString().trim())
        return if (!hasChanged) {
            Pair(false, null)
        } else {
            Pair(title.isNotEmpty() || Html.toHtml(etContent.text).isNotEmpty(), R.string.toast_emptyNote)
        }
    }

    override fun onNoteWasSaved() {
        val target = getImageFilePathForId(id)
        // cleanup not used files
        target.apply {
            if (exists() && isDirectory) {
                listFiles()?.forEach {
                    if (!loadedImages.contains(it.name)) {
                        Log.d("TextNote", "Deleting file ${it.name}")
                        it.delete()
                    }
                }
            }
        }
    }

    override fun onPause() {
        lastCursorPosition = etContent.selectionStart
        super.onPause()
    }

    override fun onClick(v: View) {
        val startSelection: Int
        val endSelection: Int
        val underlined: UnderlineSpan
        val totalText: SpannableStringBuilder
        when (v.id) {
            R.id.btn_bold -> {
                hasChanged = true
                applyStyle(Typeface.BOLD, isBold)
            }
            R.id.btn_italics -> {
                hasChanged = true
                applyStyle(Typeface.ITALIC, isItalic)
            }
            R.id.btn_underline -> {
                hasChanged = true
                underlined = UnderlineSpan()
                var alreadyUnderlined = false
                totalText = etContent.text as SpannableStringBuilder
                val underlineSpans = totalText.getSpans(
                    etContent.selectionStart, etContent.selectionEnd,
                    UnderlineSpan::class.java
                )
                if (etContent.selectionStart == etContent.selectionEnd) {
                    if (isUnderline.value!!) {
                        isUnderline.value = false
                        for (span in underlineSpans) {
                            if (totalText.getSpanEnd(span) == etContent.selectionEnd && span.spanTypeId == underlined.spanTypeId) {
                                totalText.setSpan(
                                    UnderlineSpan(),
                                    totalText.getSpanStart(span),
                                    totalText.getSpanEnd(span),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                totalText.removeSpan(span)
                            }
                        }
                    } else {
                        isUnderline.value = true
                        totalText.setSpan(
                            underlined,
                            etContent.selectionStart,
                            etContent.selectionEnd,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                    }
                }
                if (etContent.selectionStart < etContent.selectionEnd) {
                    startSelection = etContent.selectionStart
                    endSelection = etContent.selectionEnd
                } else {
                    startSelection = etContent.selectionEnd
                    endSelection = etContent.selectionStart
                }
                if (etContent.selectionStart != etContent.selectionEnd) {
                    for (span in underlineSpans) {
                        if (span.spanTypeId == underlined.spanTypeId) {
                            alreadyUnderlined = true
                            if (totalText.getSpanStart(span) >= startSelection && totalText.getSpanEnd(
                                    span
                                ) < endSelection
                            ) {
                                totalText.setSpan(
                                    UnderlineSpan(),
                                    startSelection,
                                    endSelection,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            } else {
                                if (totalText.getSpanStart(span) > startSelection) {
                                    totalText.setSpan(
                                        UnderlineSpan(),
                                        startSelection,
                                        totalText.getSpanEnd(span),
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                                if (totalText.getSpanEnd(span) < endSelection) {
                                    totalText.setSpan(
                                        UnderlineSpan(),
                                        totalText.getSpanEnd(span),
                                        endSelection,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }
                            if (totalText.getSpanStart(span) < startSelection && totalText.getSpanEnd(
                                    span
                                ) >= endSelection
                            ) {
                                totalText.setSpan(
                                    UnderlineSpan(),
                                    totalText.getSpanStart(span),
                                    startSelection,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            } else {
                                if (totalText.getSpanStart(span) < startSelection && totalText.getSpanEnd(
                                        span
                                    ) >= endSelection
                                ) {
                                    totalText.setSpan(
                                        UnderlineSpan(),
                                        totalText.getSpanStart(span),
                                        startSelection,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                                if (totalText.getSpanEnd(span) > endSelection && totalText.getSpanStart(
                                        span
                                    ) <= startSelection
                                ) {
                                    totalText.setSpan(
                                        UnderlineSpan(),
                                        endSelection,
                                        totalText.getSpanEnd(span),
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }
                            totalText.removeSpan(span)
                        }
                    }
                    if (!alreadyUnderlined) {
                        totalText.setSpan(
                            UnderlineSpan(),
                            startSelection,
                            endSelection,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                etContent.text = totalText
                etContent.setSelection(startSelection)
            }
            R.id.btn_gallery -> {

            }

            else -> {}
        }
    }

    private fun applyStyle(style: Int, state: MutableLiveData<Boolean>) {
        var spans: Array<StyleSpan>
        val sty = StyleSpan(style)
        var totalText = etContent.text as SpannableStringBuilder
        if (etContent.selectionStart == etContent.selectionEnd) {
            if (state.value!!) {
                state.value = false
                spans = totalText.getSpans(0, etContent.selectionEnd, StyleSpan::class.java)
                spans.filter { span -> totalText.getSpanEnd(span) == etContent.selectionEnd && span.style == sty.style }
                    .forEach { span ->
                        totalText.setSpan(
                            StyleSpan(style),
                            totalText.getSpanStart(span),
                            totalText.getSpanEnd(span),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        totalText.removeSpan(span)
                    }
            } else {
                state.value = true
                totalText.setSpan(
                    sty,
                    etContent.selectionStart,
                    etContent.selectionEnd,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
        val (startSelection, endSelection) = if (etContent.selectionStart < etContent.selectionEnd) {
            Pair(etContent.selectionStart, etContent.selectionEnd)
        } else {
            Pair(etContent.selectionEnd, etContent.selectionStart)
        }
        totalText = etContent.text as SpannableStringBuilder
        spans = totalText.getSpans(startSelection, endSelection, StyleSpan::class.java)
        var alreadyStyled = false
        if (etContent.selectionStart != etContent.selectionEnd) {
            for (span in spans) {
                if (span.style == sty.style) {
                    alreadyStyled = true
                    if (totalText.getSpanStart(span) >= startSelection && totalText.getSpanEnd(span) < endSelection) {
                        totalText.setSpan(
                            StyleSpan(style),
                            startSelection,
                            endSelection,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        if (totalText.getSpanStart(span) > startSelection) {
                            totalText.setSpan(
                                StyleSpan(style),
                                startSelection,
                                totalText.getSpanEnd(span),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        if (totalText.getSpanEnd(span) < endSelection) {
                            totalText.setSpan(
                                StyleSpan(style),
                                totalText.getSpanEnd(span),
                                endSelection,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    if (totalText.getSpanStart(span) < startSelection && totalText.getSpanEnd(span) >= endSelection) {
                        totalText.setSpan(
                            StyleSpan(style),
                            totalText.getSpanStart(span),
                            startSelection,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        if (totalText.getSpanStart(span) < startSelection && totalText.getSpanEnd(
                                span
                            ) >= endSelection
                        ) {
                            totalText.setSpan(
                                StyleSpan(style),
                                totalText.getSpanStart(span),
                                startSelection,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        if (totalText.getSpanEnd(span) > endSelection && totalText.getSpanStart(span) <= startSelection) {
                            totalText.setSpan(
                                StyleSpan(style),
                                endSelection,
                                totalText.getSpanEnd(span),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    totalText.removeSpan(span)
                }
            }
            if (!alreadyStyled) {
                totalText.setSpan(
                    sty,
                    startSelection,
                    endSelection,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        etContent.text = totalText
        etContent.setSelection(startSelection)
    }

    override fun onNoteSave(name: String, category: Int): ActionResult<Note, Int> {
        return if (name.isEmpty() && etContent.text.toString().isEmpty()) {
            ActionResult(false, null)
        } else {
            ActionResult(true, Note(name, Html.toHtml(etContent.text).trimEnd(' ', '\n'), DbContract.NoteEntry.TYPE_TEXT, category))
        }
    }

    override fun getMimeType() = if (loadedImages.isEmpty()) {
        "text/plain"
    } else {
        "application/zip"
    }

    override fun getFileExtension() = if (loadedImages.isEmpty()) {
        TextNoteActivity.getFileExtension()
    } else {
        ".zip"
    }

    private val saveToExternalStorageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileOutputStream: OutputStream? = contentResolver.openOutputStream(uri)
                fileOutputStream?.let { outputStream ->
                    exportWithImages(filesDir, etContent.text.toHtml(), id, outputStream)
                    Toast.makeText(
                        applicationContext,
                        String.format(getString(R.string.toast_file_exported_to), uri.toString()),
                        Toast.LENGTH_LONG
                    ).show()
                }
                fileOutputStream?.close()
            }
        }
    }

    override fun onSaveExternalStorage(outputStream: OutputStream) {
        exportWithImages(filesDir, etContent.text.toHtml(), id, outputStream)
    }

    private fun getImageFilePathForId(id: Int) = getImageFilePathForId(filesDir, id)

    companion object {

        fun getImageFilePathForId(filesDir: File, id: Int): File {
            val path = File("${filesDir}/text_notes/$id/images")
            return path
        }
        fun getFileExtension(filesDir: File? = null, id: Int? = null): String {
            if (id != null && filesDir != null) {
                val dir = getImageFilePathForId(filesDir, id)
                return if (dir.exists() && dir.isDirectory && dir.listFiles()?.isNotEmpty() == true) {
                    ".zip"
                } else {
                    ".txt"
                }
            } else {
                return ".txt"
            }
        }

        fun exportWithImages(filesDir: File, content: String, id: Int, outputStream: OutputStream, zipped: Boolean = true) {
            val dir = getImageFilePathForId(filesDir, id)
            if (dir.exists() && dir.isDirectory) {
                ZipOutputStream(outputStream).use {
                    it.putNextEntry(ZipEntry("text.txt"))
                    ByteArrayInputStream(content.toByteArray()).copyTo(it)
                    it.closeEntry()
                    dir.listFiles()?.forEach { file ->
                        it.putNextEntry(ZipEntry("images/${file}"))
                        FileInputStream(file).copyTo(it)
                        it.closeEntry()
                    }
                }
            } else {
                PrintWriter(outputStream).use {
                    println(content)
                }
            }
        }
    }
}