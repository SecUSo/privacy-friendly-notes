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
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import java.io.File
import java.io.IOException
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

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_text_note)

        val fabMenu = findViewById<FloatingActionButton>(R.id.fab_menu)
        fabMenu.setOnClickListener {
            if (fabMenu.isExpanded) {
                fabMenu.isExpanded = false
                fabMenu.setImageResource(R.drawable.ic_baseline_format_color_text_24)
            } else {
                fabMenu.isExpanded = true
                fabMenu.setImageResource(R.drawable.ic_baseline_close_24)
            }
        }
        boldBtn.setOnClickListener(this)
        italicsBtn.setOnClickListener(this)
        underlineBtn.setOnClickListener(this)

        isBold.observe(this) { b: Boolean ->
            boldBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(if (b) "#000000" else "#0274b2"))
        }
        isItalic.observe(this) { b: Boolean ->
            italicsBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(if (b) "#000000" else "#0274b2"))
        }
        isUnderline.observe(this) { b: Boolean ->
            underlineBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(if (b) "#000000" else "#0274b2"))
        }
        super.onCreate(savedInstanceState)
    }

    override fun onNoteLoadedFromDB(note: Note) {
        etContent.setText(Html.fromHtml(note.content))
    }

    override fun onNewNote() {

    }

    override fun onLoadActivity() {
        adaptFontSize(etContent)
    }

    public override fun shareNote(name: String): Intent {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "$name \n\n ${etContent.text}")
        return sendIntent
    }

    override fun determineToSave(title: String, category: Int): Pair<Boolean, Int> {
        val intent = intent
        return Pair<Boolean, Int>(
            (title.isNotEmpty() || Html.toHtml(etContent.text) != "") && -5 != intent.getIntExtra(EXTRA_CATEGORY, -5),
            R.string.toast_emptyNote
        )
    }

    override fun onClick(v: View) {
        val startSelection: Int
        val endSelection: Int
        val underlined: UnderlineSpan
        val totalText: SpannableStringBuilder
        when (v.id) {
            R.id.btn_bold -> applyStyle(Typeface.BOLD, isBold)
            R.id.btn_italics -> applyStyle(Typeface.ITALIC, isItalic)
            R.id.btn_underline -> {
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

    override fun updateNoteToSave(name: String, category: Int): Note {
        return Note(name, Html.toHtml(etContent.text), DbContract.NoteEntry.TYPE_TEXT, category)
    }

    override fun noteToSave(name: String, category: Int): Note? {
        return if (name.isEmpty() && etContent.text.toString().isEmpty()) {
            null
        } else {
            Note(name, Html.toHtml(etContent.text), DbContract.NoteEntry.TYPE_TEXT, category)
        }
    }

    override fun onSaveExternalStorage(basePath: File, name: String) {
        val file = File(basePath, "/text_${name}.txt")
        try {
            // Make sure the directory exists.
            if (basePath.exists() || basePath.mkdirs()) {
                val out = PrintWriter(file)
                out.println(name)
                out.println()
                out.println(Html.toHtml(etContent.text))
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
            } else {
                Log.e("file", "${file.exists()} ${file.mkdirs()}")
            }
        } catch (e: IOException) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing $file", e)
        }
    }
}