package org.secuso.privacyfriendlynotes;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class TextNoteActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, PopupMenu.OnMenuItemClickListener {
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";
    public static final String EXTRA_NOTIFICATION_ID = "org.secuso.privacyfriendlynotes.notificationID";

    EditText etName;
    EditText etContent;
    Spinner spinner;

    private int dayOfMonth, monthOfYear, year;

    private boolean edit = false;
    private boolean hasAlarm = false;
    private boolean shouldSave = true;
    private int id = -1;
    private int notification_id = -1;
    private int currentCat;
    Cursor noteCursor = null;
    Cursor notificationCursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_note);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);

        loadActivity(true);

    }

    private void loadActivity(boolean initial){
        //Look for a note ID in the intent. If we got one, then we will edit that note. Otherwise we create a new one.
        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_ID, -1);
        edit = (id != -1);

        etName = (EditText) findViewById(R.id.etName);
        etContent = (EditText) findViewById(R.id.etContent);
        spinner = (Spinner) findViewById(R.id.spinner_category);

        SimpleCursorAdapter adapter = null;

        //CategorySpinner
        Cursor c = DbAccess.getCategories(getBaseContext());
        if (c.getCount() == 0) {
            displayCategoryDialog();
        } else {
            String[] from = {DbContract.CategoryEntry.COLUMN_NAME};
            int[] to = {R.id.text1};
            adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.simple_spinner_item, c, from, to, CursorAdapter.FLAG_AUTO_REQUERY);
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Cursor c = (Cursor) parent.getItemAtPosition(position);
                    currentCat = c.getInt(c.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_ID));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        //fill in values if update
        if (edit) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            noteCursor = DbAccess.getNote(getBaseContext(), id);
            noteCursor.moveToFirst();
            etName.setText(noteCursor.getString(noteCursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME)));
            etContent.setText(noteCursor.getString(noteCursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT)));
            //find the current category and set spinner to that
            currentCat = noteCursor.getInt(noteCursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CATEGORY));

            for (int i = 0; i < adapter.getCount(); i++){
                c.moveToPosition(i);
                if (c.getInt(c.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_ID)) == currentCat) {
                    spinner.setSelection(i);
                    break;
                }
            }
            //fill the notificationCursor
            notificationCursor = DbAccess.getNotificationByNoteId(getBaseContext(), id);
            hasAlarm = notificationCursor.moveToFirst();
            if (hasAlarm) {
                notification_id = notificationCursor.getInt(notificationCursor.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_ID));
            }
            ((Button) findViewById(R.id.btn_save)).setText(getString(R.string.action_update));
        } else {
            findViewById(R.id.btn_delete).setEnabled(false);
        }
        if(!initial) {
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //The Activity is not visible anymore. Save the work!
        if (shouldSave && !fieldsEmpty()) {
            if (edit) {
                updateNote();
            } else {
                saveNote();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (edit){
            getMenuInflater().inflate(R.menu.text, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_reminder);
        if (hasAlarm) {
            item.setIcon(R.drawable.ic_alarm_on_white_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reminder) {
            //open the schedule dialog
            final Calendar c = Calendar.getInstance();
            if (hasAlarm) {
                //ask whether to delete or update the current alarm
                PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_reminder));
                popupMenu.inflate(R.menu.reminder);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
            } else {
                //create a new one
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(TextNoteActivity.this, this, year, month, day);
                dpd.getDatePicker().setMinDate(c.getTimeInMillis());
                dpd.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                Toast.makeText(getBaseContext(), R.string.toast_canceled, Toast.LENGTH_SHORT).show();
                shouldSave = false;
                finish();
                break;
            case R.id.btn_delete:
                if (edit) { //note only exists in edit mode
                    displayDeleteDialog();
                }
                break;
            case R.id.btn_save:
                shouldSave = true; //safe on exit
                finish();
                break;
            default:
        }
    }

    private void updateNote(){
        DbAccess.updateNote(getBaseContext(), id, etName.getText().toString(), etContent.getText().toString(), currentCat);
        Toast.makeText(getApplicationContext(), R.string.toast_updated, Toast.LENGTH_SHORT).show();
    }

    private void saveNote(){
        DbAccess.addNote(getBaseContext(), etName.getText().toString(), etContent.getText().toString(), DbContract.NoteEntry.TYPE_TEXT, currentCat);
        Toast.makeText(getApplicationContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show();
    }

    private boolean fieldsEmpty(){
        return etName.getText().toString().isEmpty() && etContent.getText().toString().isEmpty();
    }

    private void displayCategoryDialog() {
        new AlertDialog.Builder(TextNoteActivity.this)
                .setTitle(getString(R.string.dialog_need_category_title))
                .setMessage(getString(R.string.dialog_need_category_message))
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(TextNoteActivity.this, ManageCategoriesActivity.class));
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void displayDeleteDialog() {
        new AlertDialog.Builder(TextNoteActivity.this)
                .setTitle(String.format(getString(R.string.dialog_delete_title), etName.getText().toString()))
                .setMessage(String.format(getString(R.string.dialog_delete_message), etName.getText().toString()))
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shouldSave = false;
                        DbAccess.deleteNote(getBaseContext(), id);
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        this.year = year;
        final Calendar c = Calendar.getInstance();
        if (hasAlarm) {
            c.setTimeInMillis(notificationCursor.getLong(notificationCursor.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_TIME)));
        }
        TimePickerDialog tpd = new TimePickerDialog(TextNoteActivity.this, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tpd.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar alarmtime = Calendar.getInstance();
        alarmtime.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);

        if (hasAlarm) {
            //Update the current alarm
            DbAccess.updateNotificationTime(getBaseContext(), notification_id, alarmtime.getTimeInMillis());
        } else {
            //create new alarm
            notification_id = (int) (long) DbAccess.addNotification(getBaseContext(), id, alarmtime.getTimeInMillis());
        }
        //Store a reference for the notification in the database. This is later used by the service.

        //Create the intent that is fired by AlarmManager
        Intent i = new Intent(this, NotificationService.class);
        i.putExtra(NotificationService.NOTIFICATION_ID, notification_id);

        PendingIntent pi = PendingIntent.getService(this, notification_id, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmtime.getTimeInMillis(), pi);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmtime.getTimeInMillis(), pi);
        }
        Toast.makeText(getApplicationContext(), String.format(getString(R.string.toast_alarm_scheduled), dayOfMonth + "." + (monthOfYear+1) + "." + year + " " + hourOfDay + ":" + String.format("%02d",minute)), Toast.LENGTH_SHORT).show();
        loadActivity(false);
    }

    private void cancelNotification(){
        //Create the intent that would be fired by AlarmManager
        Intent i = new Intent(this, NotificationService.class);
        i.putExtra(NotificationService.NOTIFICATION_ID, notification_id);

        PendingIntent pi = PendingIntent.getService(this, notification_id, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
        DbAccess.deleteNotification(getBaseContext(), notification_id);
        loadActivity(false);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reminder_edit) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(notificationCursor.getLong(notificationCursor.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_TIME)));
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dpd = new DatePickerDialog(TextNoteActivity.this, this, year, month, day);
            dpd.getDatePicker().setMinDate(new Date().getTime());
            dpd.show();
            return true;
        } else if (id == R.id.action_reminder_delete) {
            cancelNotification();
            return true;
        }
        return false;
    }
}
