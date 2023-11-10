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

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.preference.PreferenceKeys
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.room.model.Notification
import org.secuso.privacyfriendlynotes.ui.SettingsActivity
import org.secuso.privacyfriendlynotes.ui.helper.NotificationHelper.addNotificationToAlarmManager
import org.secuso.privacyfriendlynotes.ui.helper.NotificationHelper.removeNotificationFromAlarmManager
import org.secuso.privacyfriendlynotes.ui.helper.NotificationHelper.showAlertScheduledToast
import org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity
import java.io.OutputStream
import java.util.*


abstract class BaseNoteActivity(noteType: Int) : AppCompatActivity(), View.OnClickListener, OnDateSetListener, OnTimeSetListener, PopupMenu.OnMenuItemClickListener {
    companion object {
        const val EXTRA_ID = "org.secuso.privacyfriendlynotes.ID"
        const val EXTRA_TITLE = "org.secuso.privacyfriendlynotes.TITLE"
        const val EXTRA_CONTENT = "org.secuso.privacyfriendlynotes.CONTENT"
        const val EXTRA_CATEGORY = "org.secuso.privacyfriendlynotes.CATEGORY"
        const val EXTRA_ISTRASH = "org.secuso.privacyfriendlynotes.ISTRASH"

        const val REQUEST_CODE_EXTERNAL_STORAGE = 1
        const val REQUEST_CODE_AUDIO = 2
        const val REQUEST_CODE_POST_NOTIFICATION = 3

    }

    private val etName by lazy { findViewById<View>(R.id.etName) as EditText }
    private val catSelection by lazy { findViewById<View>(R.id.spinner_category) as AutoCompleteTextView }
    private lateinit var reminder: MenuItem

    private var fontSize: Float = 15.0F
    private var edit = false
    private var hasAlarm = false
    private var savedCat = 0

    private var dayOfMonth = 0
    private var monthOfYear = 0
    private var year = 0

    protected var shouldSave = true
    private var currentCat = 0
    private var id = -1

    private var notification: Notification? = null
    private var allCategories: List<Category>? = null
    private var adapter: ArrayAdapter<CharSequence>? = null
    private lateinit var createEditNoteViewModel: CreateEditNoteViewModel

    private val noteType by lazy { noteType }

    protected abstract fun noteToSave(name: String, category: Int): ActionResult<Note, Int>
    protected abstract fun updateNoteToSave(name: String, category: Int): ActionResult<Note, Int>
    protected abstract fun onLoadActivity()
    protected abstract fun onSaveExternalStorage(outputStream: OutputStream)

    protected abstract fun getFileExtension(): String
    protected abstract fun getMimeType(): String
    protected abstract fun shareNote(name: String): ActionResult<Intent, Int>
    protected abstract fun onNoteLoadedFromDB(note: Note)
    protected abstract fun onNewNote()
    protected abstract fun determineToSave(title: String, category: Int): Pair<Boolean, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createEditNoteViewModel = ViewModelProvider(this)[CreateEditNoteViewModel::class.java]

        //CategorySpinner
        adapter = ArrayAdapter<CharSequence>(
            this, R.layout.simple_spinner_item,
            mutableListOf(getString(R.string.default_category)) as List<CharSequence>
        )
        catSelection.setAdapter(adapter)
        catSelection.threshold = 0
        catSelection.setOnDismissListener {
            val catName = catSelection.text.toString()
            if (catName == getString(R.string.default_category)) {
                currentCat = 0
                return@setOnDismissListener
            }
            currentCat = -1
            for (cat: Category in allCategories!!) {
                if (catName == cat.name) {
                    currentCat = cat._id
                }
            }
        }

        lifecycleScope.launch {
            createEditNoteViewModel.categories.collect { categories ->
                allCategories = categories
                adapter!!.addAll(categories.map { cat -> cat.name })
            }
        }


        val intent = intent
        currentCat = intent.getIntExtra(EXTRA_CATEGORY, 0)
        savedCat = currentCat

        // Return the given intent as result to return to the same category as started
        setResult(Activity.RESULT_OK, intent)

        createEditNoteViewModel.getCategoryNameFromId(currentCat).observe(this) { s ->
            catSelection.setText(s ?: getString(R.string.default_category), false)
        }

        // observe notifications
        notification = Notification(-1, -1)
        createEditNoteViewModel.allNotifications.observe(this) { notifications ->
            for (currentNotification in notifications!!) {
                if (currentNotification._noteId == id) {
                    notification!!._noteId = id
                    notification!!.time = currentNotification.time
                }
            }
        }
        loadActivity(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.base_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        reminder = menu.findItem(R.id.action_reminder)
        hasAlarm = notification!!._noteId >= 0
        if (hasAlarm) {
            reminder.setIcon(R.drawable.ic_alarm_on_white_24dp)
        } else {
            reminder.setIcon(R.drawable.ic_alarm_add_white_24dp)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadActivity(initial: Boolean) {
        //Look for a note ID in the intent. If we got one, then we will edit that note. Otherwise we create a new one.
        if (id == -1) {
            val intent = intent
            id = intent.getIntExtra(EXTRA_ID, -1)
        }
        edit = id != -1

        // Should we set a custom font size?
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp.getBoolean(SettingsActivity.PREF_CUSTOM_FONT, false)) {
            fontSize = sp.getString(SettingsActivity.PREF_CUSTOM_FONT_SIZE, "15")!!.toFloat()
            adaptFontSize(catSelection)
            adaptFontSize(etName)
        }

        // Fill category spinner
        if (adapter!!.count == 0) {
            displayCategoryDialog()
        }

        //fill in values if update
        if (edit) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            createEditNoteViewModel.getNoteByID(id.toLong()).observe(
                this
            ) { note ->
                if (note != null) {
                    etName.setText(note.name)

                    //find the current category and set spinner to that
                    currentCat = note.category
                    savedCat = currentCat

                    //fill the notificationCursor
                    hasAlarm = notification!!._noteId >= 0

                    onNoteLoadedFromDB(note)
                }
            }
        } else {
            onNewNote()
        }
        if (!initial) {
            invalidateOptionsMenu()
        }
        onLoadActivity()
    }

    protected fun adaptFontSize(element: TextView) {
        element.textSize = fontSize
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //
        // Note that any actions listed here will only be clickable if the menu is shown
        // That's currently the case iff the user edits an existing note
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            R.id.action_reminder -> {
                saveOrUpdateNote()

                //Check for notification permission and exact alarm permission
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !(getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms())
                ) {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.dialog_need_permission_notifications_and_exact_alarm_message)
                        .setPositiveButton(R.string.dialog_ok) { _, _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATION)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !(getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) {
                                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            }
                        }
                        .setTitle(R.string.dialog_need_permission_notifications_and_exact_alarm_title)
                        .setCancelable(true)
                        .create()
                        .show()
                    return true
                }
                //open the schedule dialog
                val c = Calendar.getInstance()

                //fill the notificationCursor
                hasAlarm = notification!!._noteId >= 0
                if (hasAlarm) {
                    //ask whether to delete or update the current alarm
                    val popupMenu = PopupMenu(this, findViewById(R.id.action_reminder))
                    popupMenu.inflate(R.menu.reminder)
                    popupMenu.setOnMenuItemClickListener(this)
                    popupMenu.show()
                } else {
                    //create a new one
                    val year = c[Calendar.YEAR]
                    val month = c[Calendar.MONTH]
                    val day = c[Calendar.DAY_OF_MONTH]
                    val dpd = DatePickerDialog(ContextThemeWrapper(this, R.style.AppTheme_PopupOverlay_Calendar), this, year, month, day)
                    dpd.datePicker.minDate = c.timeInMillis
                    dpd.show()
                }
                return true
            }

            R.id.action_export -> {
                saveOrUpdateNote()

                saveToExternalStorage()
                return true
            }

            R.id.action_share -> {
                val result = shareNote(etName.text.toString())
                if (saveOrUpdateNote()) {
                    if (result.isOk()) {
                        startActivity(Intent.createChooser(result.ok, null))
                    } else {
                        Toast.makeText(applicationContext, result.err!!, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            R.id.action_delete -> {
                displayTrashDialog()
            }

            R.id.action_cancel -> {
                Toast.makeText(baseContext, R.string.toast_canceled, Toast.LENGTH_SHORT).show()
                shouldSave = false
                finish()
            }

            R.id.action_save -> {
                shouldSave = true
                finish()
            }

            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {

    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_reminder_edit) {
            val c = Calendar.getInstance()
            c.timeInMillis = notification!!.time.toLong()
            val year = c[Calendar.YEAR]
            val month = c[Calendar.MONTH]
            val day = c[Calendar.DAY_OF_MONTH]
            val dpd = DatePickerDialog(this, this, year, month, day)
            dpd.datePicker.minDate = Date().time
            dpd.show()
            return true
        } else if (id == R.id.action_reminder_delete) {
            cancelNotification()
            notification = Notification(-1, -1)
            item.setIcon(R.drawable.ic_alarm_add_white_24dp)
            return true
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        //The Activity is not visible anymore. Save the work!
        if (shouldSave) {
            saveOrUpdateNote()
        }
    }

    override fun onBackPressed() {
        shouldSave = true
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        loadActivity(false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_AUDIO -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Do nothing. App should work
            } else {
                Toast.makeText(baseContext, R.string.toast_need_permission_audio, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun saveOrUpdateNote(): Boolean {
        val (toSave, mes) = determineToSave(etName.text.toString(), if (currentCat >= 0) currentCat else savedCat)
        return if (toSave) {
            if (etName.text.isEmpty()) {
                etName.setText(generateStandardName())
            }
            if (edit) updateNote()
            else saveNote()
        } else {
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun saveNote(): Boolean {
        val note = noteToSave(
            etName.text.toString(),
            if (currentCat >= 0) currentCat else savedCat
        )
        if (note.isOk()) {
            insertNoteIntoDB(note.ok)
        }
        return note.isOk()
    }

    private fun updateNote(): Boolean {
        val note = updateNoteToSave(
            etName.text.toString(),
            if (currentCat >= 0) currentCat else savedCat
        )
        if (note.isOk()) {
            insertNoteIntoDB(note.ok)
        }
        return note.isOk()
    }

    private fun insertNoteIntoDB(note: Note?) {
        if (note != null) {
            if (etName.text.toString() != note.name) {
                etName.setText(note.name)
            }
            if (id != -1) {
                note._id = id
                createEditNoteViewModel.update(note)
                Toast.makeText(applicationContext, R.string.toast_updated, Toast.LENGTH_SHORT).show()
            } else {
                id = createEditNoteViewModel.insert(note)
                Toast.makeText(applicationContext, R.string.toast_saved, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, R.string.note_not_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelNotification() {
        //Create the intent that would be fired by AlarmManager
        removeNotificationFromAlarmManager(
            this, id, DbContract.NoteEntry.TYPE_TEXT, etName.text.toString()
        )
        val intent = intent
        id = intent.getIntExtra(EXTRA_ID, -1)
        val notification = Notification(id, 0)
        createEditNoteViewModel.delete(notification)
        loadActivity(false)
    }

    private fun displayCategoryDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_need_category_title))
            .setMessage(getString(R.string.dialog_need_category_message))
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                startActivity(
                    Intent(this, ManageCategoriesActivity::class.java)
                )
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun displayTrashDialog() {
        val sp = getSharedPreferences(PreferenceKeys.SP_DATA, MODE_PRIVATE)
        createEditNoteViewModel.getNoteByID(id.toLong()).observe(this) { note ->
            if (note == null) {
                shouldSave = false
                finish()
                return@observe
            }
            if (sp.getBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, true)) {
                //we never displayed the message before, so show it now
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_trash_title))
                    .setMessage(getString(R.string.dialog_trash_message))
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        shouldSave = false
                        sp.edit().putBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, false).apply()
                        note.in_trash = 1
                        createEditNoteViewModel.update(note)
                        finish()
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                sp.edit().putBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, false).apply()
            } else {
                shouldSave = false
                note.in_trash = intent.getIntExtra(EXTRA_ISTRASH, 0)
                if (note.in_trash == 1) {
                    createEditNoteViewModel.delete(note)
                } else {
                    note.in_trash = 1
                    createEditNoteViewModel.update(note)
                }
                finish()
            }
        }
    }

    val saveToExternalStorageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {uri ->
                val fileOutputStream: OutputStream? = contentResolver.openOutputStream(uri)
                fileOutputStream?.let {
                    onSaveExternalStorage(it)
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

    private fun saveToExternalStorage() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_TITLE, etName.text.toString() + getFileExtension())
        intent.type = getMimeType()
        saveToExternalStorageResultLauncher.launch(intent)
    }

    private fun generateStandardName(): String {
        val sp = getSharedPreferences(PreferenceKeys.SP_VALUES, MODE_PRIVATE)
        val counter = sp.getInt(PreferenceKeys.SP_VALUES_NAMECOUNTER, 1)
        sp.edit().putInt(PreferenceKeys.SP_VALUES_NAMECOUNTER, counter + 1).apply()
        return String.format(getString(R.string.note_standardname), counter)
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        this.dayOfMonth = dayOfMonth
        this.monthOfYear = monthOfYear
        this.year = year
        val c = Calendar.getInstance()
        if (hasAlarm) {
            c.timeInMillis = notification!!.time.toLong()
        }
        val tpd = TimePickerDialog(
            this, this,
            c[Calendar.HOUR_OF_DAY], c[Calendar.MINUTE], true
        )
        tpd.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val alarmtime = Calendar.getInstance()
        alarmtime[year, monthOfYear, dayOfMonth, hourOfDay] = minute
        val intent = intent
        if (id == -1) {
            id = intent.getIntExtra(EXTRA_ID, -1)
        }
        val notificationTimeSet = Notification(id, alarmtime.timeInMillis.toInt())
        if (hasAlarm) {
            //Update the current alarm
            createEditNoteViewModel.update(notificationTimeSet)
        } else {
            //create new alarm
            createEditNoteViewModel.insert(notificationTimeSet)
            hasAlarm = true
            notification = Notification(id, alarmtime.timeInMillis.toInt())
            reminder.setIcon(R.drawable.ic_alarm_on_white_24dp)
        }

        //Store a reference for the notification in the database. This is later used by the service.
        addNotificationToAlarmManager(
            this, id, noteType, etName.text.toString(), alarmtime.timeInMillis
        )
        showAlertScheduledToast(this, dayOfMonth, monthOfYear, year, hourOfDay, minute)
        loadActivity(false)
    }

    fun setTitle(title: String) {
        etName.setText(title)
    }

    class ActionResult<O, E>(private val status: Boolean, val ok: O?, val err: E? = null) {
        fun isOk(): Boolean {
            return this.status
        }

        fun isErr(): Boolean {
            return !this.status
        }
    }
}