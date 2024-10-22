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
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.ui.util.ChecklistUtil
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter

/**
 * Activity that allows to add, edit and delete text notes.
 */
class TextNoteActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_TEXT) {

    private val etContent: EditText by lazy { findViewById(R.id.etContent) }
    private val boldBtn: FloatingActionButton by lazy { findViewById(R.id.btn_bold) }
    private val italicsBtn: FloatingActionButton by lazy { findViewById(R.id.btn_italics) }
    private val underlineBtn: FloatingActionButton by lazy { findViewById(R.id.btn_underline) }

    private val isBold = MutableLiveData(false)
    private val isItalic = MutableLiveData(false)
    private val isUnderline = MutableLiveData(false)

    private var hasChanged = false
    private var oldText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_text_note)

        val fabMenuBtn = findViewById<FloatingActionButton>(R.id.fab_menu)
        val fabMenu = findViewById<View>(R.id.fab_menu_wrapper)
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

        isBold.observe(this) { b: Boolean ->
            boldBtn.backgroundTintList = ColorStateList.valueOf(if (b) Color.parseColor("#000000") else resources.getColor(R.color.colorSecuso))
        }
        isItalic.observe(this) { b: Boolean ->
            italicsBtn.backgroundTintList = ColorStateList.valueOf(if (b) Color.parseColor("#000000") else resources.getColor(R.color.colorSecuso))
        }
        isUnderline.observe(this) { b: Boolean ->
            underlineBtn.backgroundTintList = ColorStateList.valueOf(if (b) Color.parseColor("#000000") else resources.getColor(R.color.colorSecuso))
        }
        super.onCreate(savedInstanceState)
    }

    override fun onNoteLoadedFromDB(note: Note) {
        etContent.setText(Html.fromHtml(note.content))
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
                val text = InputStreamReader(contentResolver.openInputStream(uri)).readLines()
                super.setTitle(text[0])
                etContent.setText(Html.fromHtml(text.subList(1, text.size).joinToString("<br>")))
            }
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (text != null) {
                etContent.setText(Html.fromHtml(text))
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
            ActionResult(true, Note(name, Html.toHtml(etContent.text), DbContract.NoteEntry.TYPE_TEXT, category))
        }
    }

    override fun getMimeType() = "text/plain"

    override fun getFileExtension() = ".txt"

    private val saveToExternalStorageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileOutputStream: OutputStream? = contentResolver.openOutputStream(uri)
                fileOutputStream?.let {
                    val out = PrintWriter(it)
                    out.println(Html.toHtml(etContent.text))
                    out.close()
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
        val out = PrintWriter(outputStream)
        out.println(Html.fromHtml(Html.toHtml(etContent.text)).toString())
        out.close()
    }
}