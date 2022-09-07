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
package org.secuso.privacyfriendlynotes.ui.notes;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplify.ink.InkView;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.preference.PreferenceKeys;
import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.room.model.Category;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.room.model.Notification;
import org.secuso.privacyfriendlynotes.ui.SettingsActivity;
import org.secuso.privacyfriendlynotes.ui.helper.NotificationHelper;
import org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;

/**
 * Activity that allows to add, edit and delete sketch notes.
 */

public class SketchActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, PopupMenu.OnMenuItemClickListener, SimpleDialog.OnDialogResultListener {
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";
    public static final String EXTRA_TITLE = "org.secuso.privacyfriendlynotes.TITLE";
    public static final String EXTRA_CONTENT = "org.secuso.privacyfriendlynotes.CONTENT";
    public static final String EXTRA_CATEGORY = "org.secuso.privacyfriendlynotes.CATEGORY";
    public static final String EXTRA_ISTRASH = "org.secuso.privacyfriendlynotes.ISTRASH";
    public static final String TAG_COLORDIALOG = "org.secuso.privacyfriendlynotes.COLORDIALOG";



    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    EditText etName;
    InkView drawView;
    Button btnColorSelector;
    Spinner spinner;

    private String mFileName = "finde_die_datei.mp4";
    private String mFilePath;

    private int dayOfMonth, monthOfYear, year;

    private boolean edit = false;
    private boolean hasAlarm = false;
    private boolean shouldSave = true;
    private int id = -1;
    private int currentCat;

    private Notification notification;
    private String title;
    List<Category> allCategories;
    ArrayAdapter<CharSequence> adapter;
    private Menu menu;
    private MenuItem item;
    private CreateEditNoteViewModel createEditNoteViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);

        etName = (EditText) findViewById(R.id.etName);
        drawView = (InkView) findViewById(R.id.draw_view);
        btnColorSelector = (Button) findViewById(R.id.btn_color_selector);
        spinner = (Spinner) findViewById(R.id.spinner_category);

        btnColorSelector.setOnClickListener(this);
        drawView.setColor(Color.BLACK);
        drawView.setMinStrokeWidth(1.5f);
        drawView.setMaxStrokeWidth(6f);

        //CategorySpinner
        createEditNoteViewModel = new ViewModelProvider(this).get(CreateEditNoteViewModel.class);
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

    @Override
    public void onBackPressed() {
        Toast.makeText(getBaseContext(), R.string.toast_canceled, Toast.LENGTH_SHORT).show();
        shouldSave = false;
        finish();
    }

    private void loadActivity(boolean initial){

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
        }




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

        //fill in values if update
        if (edit) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            createEditNoteViewModel.getNoteByID(id).observe(this, noteFromDB -> {
                title = noteFromDB.getName();
                etName.setText(title);
                mFileName = noteFromDB.getContent();
                mFilePath = getFilesDir().getPath() + "/sketches" + mFileName;
                //find the current category and set spinner to that
                currentCat = noteFromDB.getCategory();


                findViewById(R.id.btn_delete).setEnabled(true);
                ((Button) findViewById(R.id.btn_save)).setText(getString(R.string.action_update));
                drawView.setBackground(new BitmapDrawable(getResources(), mFilePath));
            });
        } else {
            findViewById(R.id.btn_delete).setEnabled(false);
            mFileName = "/sketch_" + System.currentTimeMillis() + ".PNG";
            mFilePath = getFilesDir().getPath() + "/sketches";
            new File(mFilePath).mkdirs(); //ensure that the file exists
            mFilePath = getFilesDir().getPath() + "/sketches" + mFileName;

            // = false; // will be set to true, once we have a drawing
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
            getMenuInflater().inflate(R.menu.audio, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
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

                DatePickerDialog dpd = new DatePickerDialog(SketchActivity.this, this, year, month, day);
                dpd.getDatePicker().setMinDate(c.getTimeInMillis());
                dpd.show();
            }
            return true;
        } else if (id == R.id.action_save) {
            if (ContextCompat.checkSelfPermission(SketchActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(SketchActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(SketchActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_EXTERNAL_STORAGE);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(SketchActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_EXTERNAL_STORAGE);
                }
            } else {
                saveToExternalStorage();
            }
            return true;
        } else if (id == R.id.action_share){
            String tempPath = mFilePath.substring(0, mFilePath.length()-3) + "jpg";
            File sketchFile = new File(tempPath);

            Bitmap bm = overlay(new BitmapDrawable(getResources(), mFilePath).getBitmap(), drawView.getBitmap());
            Canvas canvas = new Canvas(bm);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(overlay(new BitmapDrawable(getResources(), mFilePath).getBitmap(), drawView.getBitmap()), 0, 0, null);
            try {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(sketchFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "org.secuso.privacyfriendlynotes", sketchFile);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
                Bitmap emptyBitmap = Bitmap.createBitmap(drawView.getBitmap().getWidth(), drawView.getBitmap().getHeight(), drawView.getBitmap().getConfig());
                Intent intent = getIntent();
                if(!drawView.getBitmap().sameAs(emptyBitmap) || -5 != intent.getIntExtra(EXTRA_CATEGORY, -5)){ //safe only if note is not empty
                    shouldSave = true; //safe on exit
                    finish();
                    break;
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_emptyNote, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_color_selector:
                displayColorDialog();
                break;
            default:
        }
    }

    private void updateNote(){
        fillNameIfEmpty();
        Bitmap oldSketch = new BitmapDrawable(getResources(), mFilePath).getBitmap();
        Bitmap newSketch = drawView.getBitmap();
        try {
            FileOutputStream fo = new FileOutputStream(new File(mFilePath));
            overlay(oldSketch, newSketch).compress(Bitmap.CompressFormat.PNG, 0, fo);
            fo.flush();
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Note note = new Note(etName.getText().toString(),mFileName,DbContract.NoteEntry.TYPE_SKETCH,currentCat);
        note.set_id(id);
        createEditNoteViewModel.update(note);
        Toast.makeText(getApplicationContext(), R.string.toast_updated, Toast.LENGTH_SHORT).show();
    }

    private void saveNote(){
        fillNameIfEmpty();
        Bitmap bitmap = drawView.getBitmap();
        try {
            FileOutputStream fo = new FileOutputStream(new File(mFilePath));
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fo);
            fo.flush();
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Note note = new Note(etName.getText().toString(),mFileName,DbContract.NoteEntry.TYPE_SKETCH,currentCat);
        createEditNoteViewModel.insert(note);

        Toast.makeText(getApplicationContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(SketchActivity.this)
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
                        startActivity(new Intent(SketchActivity.this, ManageCategoriesActivity.class));
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void displayTrashDialog() {
        SharedPreferences sp = getSharedPreferences(PreferenceKeys.SP_DATA, Context.MODE_PRIVATE);
        if (sp.getBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, true)){
            //we never displayed the message before, so show it now
            new AlertDialog.Builder(SketchActivity.this)
                    .setTitle(getString(R.string.dialog_trash_title))
                    .setMessage(getString(R.string.dialog_trash_message))
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shouldSave = false;
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, false);
                            editor.commit();
                            Intent intent = getIntent();
                            Note note = new Note(intent.getStringExtra(EXTRA_TITLE),intent.getStringExtra(EXTRA_CONTENT),DbContract.NoteEntry.TYPE_SKETCH,intent.getIntExtra(EXTRA_CATEGORY,-1));
                            note.set_id(id);
                            note.setIn_trash(1);
                            createEditNoteViewModel.update(note);

                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } else {
            shouldSave = false;
            Intent intent = getIntent();
            Note note = new Note(intent.getStringExtra(EXTRA_TITLE),intent.getStringExtra(EXTRA_CONTENT),DbContract.NoteEntry.TYPE_SKETCH,intent.getIntExtra(EXTRA_CATEGORY,-1));
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

    private void displayColorDialog() {
        SimpleColorDialog.build()
                .title("")
                .allowCustom(false)
                .cancelable(true)//allows close by tapping outside of dialog
                .colors(this, R.array.mdcolor_500)
                .choiceMode(SimpleColorDialog.SINGLE_CHOICE_DIRECT)//auto-close on selection
                .show(this, TAG_COLORDIALOG);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (dialogTag.equals(TAG_COLORDIALOG) && which == BUTTON_POSITIVE){
            @ColorInt int color = extras.getInt(SimpleColorDialog.COLOR);
            drawView.setColor(color);
            btnColorSelector.setBackgroundColor(color);
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

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        this.year = year;
        final Calendar c = Calendar.getInstance();
        if (hasAlarm) {
            c.setTimeInMillis(notification.getTime());
        }
        TimePickerDialog tpd = new TimePickerDialog(SketchActivity.this, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tpd.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar alarmtime = Calendar.getInstance();
        alarmtime.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_ID, -1);
        Notification notificationTimeSet = new Notification(id, (int) alarmtime.getTimeInMillis());

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

        NotificationHelper.addNotificationToAlarmManager(this,id, DbContract.NoteEntry.TYPE_SKETCH, title, alarmtime.getTimeInMillis());
        NotificationHelper.showAlertScheduledToast(this, dayOfMonth,monthOfYear,year,hourOfDay,minute);

        loadActivity(false);
    }

    private void cancelNotification(){
        NotificationHelper.removeNotificationFromAlarmManager(this,id, DbContract.NoteEntry.TYPE_SKETCH, title);

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
            c.setTimeInMillis(notification.getTime());
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dpd = new DatePickerDialog(SketchActivity.this, this, year, month, day);
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
            File file = new File(path, "/" + etName.getText().toString() + ".jpeg");
            try {
                // Make sure the directory exists.
                boolean path_exists = path.exists() || path.mkdirs();
                if (path_exists) {
                    Bitmap bm = overlay(new BitmapDrawable(getResources(), mFilePath).getBitmap(), drawView.getBitmap());
                    Canvas canvas = new Canvas(bm);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(overlay(new BitmapDrawable(getResources(), mFilePath).getBitmap(), drawView.getBitmap()), 0, 0, null);
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));

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

    //taken from http://stackoverflow.com/a/10616868
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }
}
