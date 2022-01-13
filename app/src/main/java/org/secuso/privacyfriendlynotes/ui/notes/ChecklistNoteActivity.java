package org.secuso.privacyfriendlynotes.ui.notes;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.room.model.Category;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.room.model.Notification;
import org.secuso.privacyfriendlynotes.service.NotificationService;
import org.secuso.privacyfriendlynotes.preference.PreferenceKeys;
import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity;
import org.secuso.privacyfriendlynotes.ui.SettingsActivity;
import org.secuso.privacyfriendlynotes.ui.util.CheckListAdapter;
import org.secuso.privacyfriendlynotes.ui.util.CheckListItem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Activity that allows to add, edit and delete checklist notes.
 */

public class ChecklistNoteActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, PopupMenu.OnMenuItemClickListener, AdapterView.OnItemClickListener {
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";
    public static final String EXTRA_TITLE = "org.secuso.privacyfriendlynotes.TITLE";
    public static final String EXTRA_CONTENT = "org.secuso.privacyfriendlynotes.CONTENT";
    public static final String EXTRA_CATEGORY = "org.secuso.privacyfriendlynotes.CATEGORY";
    public static final String EXTRA_ISTRASH = "org.secuso.privacyfriendlynotes.ISTRASH";



    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    EditText etName;
    EditText etNewItem;
    ListView lvItemList;
    Spinner spinner;

    private int dayOfMonth, monthOfYear, year;

    private boolean edit = false;
    private boolean hasAlarm = false;
    private boolean shouldSave = true;
    private int id = -1;
    private int notification_id = -1;
    private int currentCat;
    Cursor notificationCursor = null;

    private Notification notification;
    private String title;
    List<Category> allCategories;
    ArrayAdapter<CharSequence> adapter;
    private MenuItem item;
    private CreateEditNoteViewModel createEditNoteViewModel;

    private ArrayList<CheckListItem> itemNamesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_note);

        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);

        etName = (EditText) findViewById(R.id.etName);
        etNewItem = (EditText) findViewById(R.id.etNewItem);
        lvItemList = (ListView) findViewById(R.id.itemList);
        spinner = (Spinner) findViewById(R.id.spinner_category);

        //CategorySpinner
        CreateEditNoteViewModel createEditNoteViewModel = new ViewModelProvider(this).get(CreateEditNoteViewModel.class);
        adapter = new ArrayAdapter(this,R.layout.simple_spinner_item);
        adapter.add(getString(R.string.default_category));

        createEditNoteViewModel.getAllCategoriesLive().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                allCategories = categories;
                for(Category currentCat : categories){
                    adapter.add(currentCat.getName());
                }
            }
        });

        Intent intent = getIntent();
        currentCat = intent.getIntExtra(EXTRA_CATEGORY, -1);

        createEditNoteViewModel.getCategoryNameFromId(currentCat).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Integer position = adapter.getPosition(s);
                spinner.setSelection(position);
            }
        });

        // observe notifications
        notification = new Notification(-1,-1);
        createEditNoteViewModel.getAllNotifications().observe(this, new Observer<List<Notification>>() {
            @Override
            public void onChanged(@Nullable List<Notification> notifications) {
                for(Notification currentNotification : notifications){
                    if(currentNotification.get_noteId() == id){
                        notification.set_noteId(id);
                        notification.setTime(currentNotification.getTime());
                    }
                }

            }
        });

        loadActivity(true);
    }

    private void loadActivity(boolean initial){
        //get rid of the old data. Otherwise we would have duplicates.
        itemNamesList.clear();

        //Look for a note ID in the intent. If we got one, then we will edit that note. Otherwise we create a new one.
        if (id == -1) {
            Intent intent = getIntent();
            id = intent.getIntExtra(EXTRA_ID, -1);
        }
        edit = (id != -1);

        // Should we set a custom font size?
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(SettingsActivity.PREF_CUSTOM_FONT, false)) {
            etName.setTextSize(Float.parseFloat(sp.getString(SettingsActivity.PREF_CUSTOM_FONT_SIZE, "15")));
            etNewItem.setTextSize(Float.parseFloat(sp.getString(SettingsActivity.PREF_CUSTOM_FONT_SIZE, "15")));
        }

        // Fill category spinner
        if (adapter.getCount() == 0) {
            displayCategoryDialog();
        } else {
            String[] from = {DbContract.CategoryEntry.COLUMN_NAME};
            int[] to = {R.id.text1};

            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String catName = (String) parent.getItemAtPosition(position);
                    currentCat = 0;
                    for(Category cat :allCategories){
                        if(catName == cat.getName()){
                            currentCat = cat.get_id();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }



        lvItemList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lvItemList.setOnItemClickListener(this);
        lvItemList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.checklist_cab, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteSelectedItems();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.action_edit:
                        ArrayAdapter adapter = (ArrayAdapter) lvItemList.getAdapter();
                        SparseBooleanArray checkedItemPositions = lvItemList.getCheckedItemPositions();
                        ArrayList<CheckListItem> temp = new ArrayList<>();
                        for (int i=0; i < checkedItemPositions.size(); i++) {
                            if(checkedItemPositions.valueAt(i)) {
                                temp.add((CheckListItem) adapter.getItem(checkedItemPositions.keyAt(i)));
                            }
                        }
                        if (temp.size() > 1) {
                            Toast.makeText(getApplicationContext(), R.string.toast_checklist_oneItem, Toast.LENGTH_SHORT).show();
                            return false;
                        } else {
                            final EditText taskEditText = new EditText(ChecklistNoteActivity.this);
                            AlertDialog dialog = new AlertDialog.Builder(ChecklistNoteActivity.this)
                                    .setTitle(getString(R.string.dialog_checklist_edit) +" "+ temp.get(0).getName())
                                    .setView(taskEditText)
                                    .setPositiveButton(R.string.action_edit, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String text = taskEditText.getText().toString();
                                            Integer pos = adapter.getPosition(temp.get(0));
                                            CheckListItem newItem = new CheckListItem(temp.get(0).isChecked(),text);
                                            adapter.remove(temp.get(0));
                                            adapter.insert(newItem,pos);
                                        }
                                    })
                                    .setNegativeButton(R.string.action_cancel, null)
                                    .create();
                            dialog.show();
                            return true;
                        }


                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ArrayAdapter a = (ArrayAdapter)lvItemList.getAdapter();
                a.notifyDataSetChanged();

            }
        });
        lvItemList.setAdapter(new CheckListAdapter(getBaseContext(), R.layout.item_checklist, itemNamesList));

        //fill in values if update
        if (edit) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            Intent intent = getIntent();
            etName.setText(intent.getStringExtra(EXTRA_TITLE));
            try {
                JSONArray content = new JSONArray(intent.getStringExtra(EXTRA_CONTENT));
                for (int i=0; i < content.length(); i++) {
                    JSONObject o = content.getJSONObject(i);
                    itemNamesList.add(new CheckListItem(o.getBoolean("checked"), o.getString("name")));
                }
                ((ArrayAdapter)lvItemList.getAdapter()).notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //find the current category and set spinner to that
            currentCat = intent.getIntExtra(EXTRA_CATEGORY, -1);


            //fill the notificationCursor
            if(notification.get_noteId() >= 0) {
                hasAlarm = true;
            } else {
                hasAlarm = false;
            }

            if (hasAlarm) {
                notification_id = notification.get_noteId();
            }
            findViewById(R.id.btn_delete).setEnabled(true);
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
        if (shouldSave) {
            if (edit) {
                updateNote();
            } else {
                saveNote();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadActivity(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (edit){
            getMenuInflater().inflate(R.menu.checklist, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        item = menu.findItem(R.id.action_reminder);
        if(notification.get_noteId() >= 0) {
            hasAlarm = true;
        } else {
            hasAlarm = false;
        }

        if (hasAlarm) {
            item.setIcon(R.drawable.ic_alarm_on_white_24dp);
        } else {
            if(edit){
                item.setIcon(R.drawable.ic_alarm_add_white_24dp);
            }
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

            //fill the notificationCursor
            if(notification.get_noteId() >= 0) {
                hasAlarm = true;
            } else {
                hasAlarm = false;
            }
            if (hasAlarm) {
                notification_id = notification.get_noteId();
            }

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

                DatePickerDialog dpd = new DatePickerDialog(ChecklistNoteActivity.this, this, year, month, day);
                dpd.getDatePicker().setMinDate(c.getTimeInMillis());
                dpd.show();
            }
            return true;
        } else if (id == R.id.action_save) {
            if (ContextCompat.checkSelfPermission(ChecklistNoteActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(ChecklistNoteActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(ChecklistNoteActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_EXTERNAL_STORAGE);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(ChecklistNoteActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_EXTERNAL_STORAGE);
                }
            } else {
                saveToExternalStorage();
            }
            return true;
        } else if (id == R.id.action_share){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, etName.getText().toString() + "\n\n" + getContentString());
            startActivity(Intent.createChooser(sendIntent, null));
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
                    displayTrashDialog();
                }
                break;
            case R.id.btn_save:
                Intent intent = getIntent();
                if(!itemNamesList.isEmpty() || currentCat != intent.getIntExtra(EXTRA_CATEGORY, -1)){ //safe only if note is not empty
                    shouldSave = true; //safe on exit
                    finish();
                    break;
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_emptyNote, Toast.LENGTH_SHORT).show();
                }

            case R.id.btn_add:
                if (!etNewItem.getText().toString().isEmpty()) {
                    itemNamesList.add(new CheckListItem(false, etNewItem.getText().toString()));
                    etNewItem.setText("");
                    ((ArrayAdapter)lvItemList.getAdapter()).notifyDataSetChanged();
                }
                break;
            default:
        }
    }

    private void updateNote(){
        Adapter a = lvItemList.getAdapter();
        JSONArray jsonArray = new JSONArray();

        try {
            CheckListItem temp;
            for (int i = 0; i < itemNamesList.size(); i++) {
                temp = (CheckListItem) a.getItem(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", temp.getName());
                jsonObject.put("checked", temp.isChecked());
                jsonArray.put(jsonObject);
            }
            fillNameIfEmpty();
            Note note = new Note(etName.getText().toString(),jsonArray.toString(),DbContract.NoteEntry.TYPE_CHECKLIST,currentCat);
            note.set_id(id);
            createEditNoteViewModel = new ViewModelProvider(this).get(CreateEditNoteViewModel.class);
            createEditNoteViewModel.update(note);
            Toast.makeText(getApplicationContext(), R.string.toast_updated, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveNote(){
        Adapter a = lvItemList.getAdapter();
        JSONArray jsonArray = new JSONArray();
        try {
            CheckListItem temp;
            for (int i = 0; i < itemNamesList.size(); i++) {
                temp = (CheckListItem) a.getItem(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", temp.getName());
                jsonObject.put("checked", temp.isChecked());
                jsonArray.put(jsonObject);
            }
            fillNameIfEmpty();
            //id = DbAccess.addNote(getBaseContext(), etName.getText().toString(), jsonArray.toString(), DbContract.NoteEntry.TYPE_CHECKLIST, currentCat);

            Note note = new Note(etName.getText().toString(),jsonArray.toString(),DbContract.NoteEntry.TYPE_CHECKLIST,currentCat);
            createEditNoteViewModel = new ViewModelProvider(this).get(CreateEditNoteViewModel.class);
            createEditNoteViewModel.insert(note);

            Toast.makeText(getApplicationContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillNameIfEmpty(){
        if (etName.getText().toString().isEmpty()) {
            SharedPreferences sp = getSharedPreferences(PreferenceKeys.SP_VALUES, Context.MODE_PRIVATE);
            int counter = sp.getInt(PreferenceKeys.SP_VALUES_NAMECOUNTER, 1);
            etName.setText(String.format(getString(R.string.note_standardname), counter));
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(PreferenceKeys.SP_VALUES_NAMECOUNTER, counter+1);
            editor.commit();
        }
    }

    private void displayCategoryDialog() {
        new AlertDialog.Builder(ChecklistNoteActivity.this)
                .setTitle(getString(R.string.dialog_need_category_title))
                .setMessage(getString(R.string.dialog_need_category_message))
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ChecklistNoteActivity.this, ManageCategoriesActivity.class));
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void displayTrashDialog() {
        SharedPreferences sp = getSharedPreferences(PreferenceKeys.SP_DATA, Context.MODE_PRIVATE);
        createEditNoteViewModel = new ViewModelProvider(this).get(CreateEditNoteViewModel.class);
        Intent intent = getIntent();
        Note note = new Note(intent.getStringExtra(EXTRA_TITLE),intent.getStringExtra(EXTRA_CONTENT),DbContract.NoteEntry.TYPE_CHECKLIST,intent.getIntExtra(EXTRA_CATEGORY,-1));
        note.set_id(id);

        if (sp.getBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, true)){
            //we never displayed the message before, so show it now
            new AlertDialog.Builder(ChecklistNoteActivity.this)
                    .setTitle(getString(R.string.dialog_trash_title))
                    .setMessage(getString(R.string.dialog_trash_message))
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shouldSave = false;
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, false);
                            editor.commit();
                            note.setIn_trash(1);
                            createEditNoteViewModel.update(note);
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, false);
            editor.commit();
        } else {
            shouldSave = false;
            note.set_id(id);
            note.setIn_trash(intent.getIntExtra(EXTRA_ISTRASH,0));
            if(note.getIn_trash() == 1){
                createEditNoteViewModel.delete(note);
            } else {
                note.set_id(id);
                note.setIn_trash(1);
                createEditNoteViewModel.update(note);
            }
            finish();
        }
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
        TimePickerDialog tpd = new TimePickerDialog(ChecklistNoteActivity.this, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tpd.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar alarmtime = Calendar.getInstance();
        alarmtime.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);

        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_ID, -1);
        Notification notificationTimeSet = new Notification(id, (int) alarmtime.getTimeInMillis());
        createEditNoteViewModel = new ViewModelProvider(this).get(CreateEditNoteViewModel.class);


        if (hasAlarm) {
            //Update the current alarm
            createEditNoteViewModel.update(notificationTimeSet);

        } else {
            //create new alarm
            createEditNoteViewModel.insert(notificationTimeSet);
            hasAlarm = true;
            notification = new Notification(id, (int) alarmtime.getTimeInMillis());
            item.setIcon(R.drawable.ic_alarm_on_white_24dp);
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
        createEditNoteViewModel = new ViewModelProvider(this).get(CreateEditNoteViewModel.class);


        PendingIntent pi = PendingIntent.getService(this, notification_id, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_ID, -1);
        Notification notification = new Notification(id, 0);
        createEditNoteViewModel.delete(notification);
        hasAlarm = false;
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
            DatePickerDialog dpd = new DatePickerDialog(ChecklistNoteActivity.this, this, year, month, day);
            dpd.getDatePicker().setMinDate(new Date().getTime());
            dpd.show();
            return true;
        } else if (id == R.id.action_reminder_delete) {
            cancelNotification();
            notification = new Notification(-1,-1);
            item.setIcon(R.drawable.ic_alarm_add_white_24dp);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Save the file
                    saveToExternalStorage();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_need_permission_write_external, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void saveToExternalStorage(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File path;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                path = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "/PrivacyFriendlyNotes");
            } else{
                path = new File(Environment.getExternalStorageDirectory(), "/PrivacyFriendlyNotes");
            }
            File file = new File(path, "/checklist_" + etName.getText().toString() + ".txt");
            try {
                // Make sure the directory exists.
                boolean path_exists = path.exists() || path.mkdirs();
                if (path_exists) {
                    PrintWriter out = new PrintWriter(file);
                    out.println(etName.getText().toString());
                    out.println();
                    out.println(getContentString());
                    out.close();
                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaScannerConnection.scanFile(this,
                            new String[] { file.toString() }, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });

                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.toast_file_exported_to), file.getAbsolutePath()), Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                Log.w("ExternalStorage", "Error writing " + file, e);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_external_storage_not_mounted, Toast.LENGTH_LONG).show();
        }
    }

    private String getContentString(){
        StringBuilder content = new StringBuilder();
        Adapter a = lvItemList.getAdapter();
        CheckListItem temp;
        for (int i=0; i < itemNamesList.size(); i++) {
            temp = (CheckListItem) a.getItem(i);
            content.append("- " + temp.getName() + " [" + (temp.isChecked() ? "✓" : "   ") + "]\n");
        }
        return content.toString();
    }

    //Click on a listitem
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayAdapter a = (ArrayAdapter)lvItemList.getAdapter();
        CheckListItem temp = (CheckListItem) a.getItem(position);
        temp.setChecked(!temp.isChecked());
        a.notifyDataSetChanged();
    }

    private void deleteSelectedItems(){
        ArrayAdapter adapter = (ArrayAdapter) lvItemList.getAdapter();
        SparseBooleanArray checkedItemPositions = lvItemList.getCheckedItemPositions();
        ArrayList<CheckListItem> temp = new ArrayList<>();
        for (int i=0; i < checkedItemPositions.size(); i++) {
            if(checkedItemPositions.valueAt(i)) {
                temp.add((CheckListItem) adapter.getItem(checkedItemPositions.keyAt(i)));
            }
        }
        if (temp.size() > 0) {
            itemNamesList.removeAll(temp);
        }
    }
}
