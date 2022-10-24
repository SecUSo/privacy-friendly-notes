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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.clans.fab.FloatingActionButton;

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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Activity that allows to add, edit and delete text notes.
 */

public class TextNoteActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, PopupMenu.OnMenuItemClickListener {
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";
    public static final String EXTRA_TITLE = "org.secuso.privacyfriendlynotes.TITLE";
    public static final String EXTRA_CONTENT = "org.secuso.privacyfriendlynotes.CONTENT";
    public static final String EXTRA_CATEGORY = "org.secuso.privacyfriendlynotes.CATEGORY";
    public static final String EXTRA_ISTRASH = "org.secuso.privacyfriendlynotes.ISTRASH";

    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    EditText etName;
    EditText etContent;
    Spinner spinner;

    private ShareActionProvider mShareActionProvider = null;

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
    private MenuItem item;
    private CreateEditNoteViewModel createEditNoteViewModel;

    FloatingActionButton boldBtn;
    FloatingActionButton italicsBtn;
    FloatingActionButton underlineBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_note);

        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_bold).setOnClickListener(this);
        findViewById(R.id.btn_italics).setOnClickListener(this);
        findViewById(R.id.btn_underline).setOnClickListener(this);

        boldBtn = findViewById(R.id.btn_bold);
        italicsBtn = findViewById(R.id.btn_italics);
        underlineBtn = findViewById(R.id.btn_underline);

        etName = (EditText) findViewById(R.id.etName);
        etContent = (EditText) findViewById(R.id.etContent);
        spinner = (Spinner) findViewById(R.id.spinner_category);

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation== Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_text_note);
        }
        else{
            setContentView(R.layout.activity_text_note);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
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
            etContent.setTextSize(Float.parseFloat(sp.getString(SettingsActivity.PREF_CUSTOM_FONT_SIZE, "15")));
            etName.setTextSize(Float.parseFloat(sp.getString(SettingsActivity.PREF_CUSTOM_FONT_SIZE, "15")));
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

        //fill in values if update
        if (edit) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            createEditNoteViewModel.getNoteByID(id).observe(this, noteFromDB -> {
                title = noteFromDB.getName();
                etName.setText(title);
                etContent.setText(Html.fromHtml(noteFromDB.getContent()));
                //find the current category and set spinner to that
                currentCat = noteFromDB.getCategory();


                //fill the notificationCursor
                if(notification.get_noteId() >= 0) {
                    hasAlarm = true;
                } else {
                    hasAlarm = false;
                }
                ((Button) findViewById(R.id.btn_save)).setText(getString(R.string.action_update));
            });
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
            getMenuInflater().inflate(R.menu.text, menu);
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
        } else if (id == R.id.action_save) {
            if (ContextCompat.checkSelfPermission(TextNoteActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(TextNoteActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(TextNoteActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_EXTERNAL_STORAGE);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(TextNoteActivity.this,
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
            sendIntent.putExtra(Intent.EXTRA_TEXT, etName.getText().toString() + "\n\n" + etContent.getText());
            startActivity(Intent.createChooser(sendIntent, null));
        } else if (id == R.id.action_delete) {
            if (edit) { //note only exists in edit mode
                displayTrashDialog();
            }
        } else if (id == R.id.action_cancel) {
            Toast.makeText(getBaseContext(), R.string.toast_canceled, Toast.LENGTH_SHORT).show();
            shouldSave = false;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int startSelection;
        int endSelection;
        final UnderlineSpan underlined;
        SpannableStringBuilder totalText;
        StyleSpan[] spans;
        switch (v.getId()) {
            case R.id.btn_save:
                Intent intent = getIntent();
                if(!Objects.equals(Html.toHtml(etContent.getText()),"")|| (currentCat != intent.getIntExtra(EXTRA_CATEGORY, -1) & -5 != intent.getIntExtra(EXTRA_CATEGORY, -5))){ //safe only if note is not empty
                    shouldSave = true; //safe on exit
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_emptyNote, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_bold:
                final StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
                totalText = (SpannableStringBuilder) etContent.getText();
                if(etContent.getSelectionStart() == etContent.getSelectionEnd()){
                    spans = totalText.getSpans(0, etContent.getSelectionEnd(), StyleSpan.class);
                    for (StyleSpan span : spans) {
                        if (totalText.getSpanEnd(span) == etContent.getSelectionEnd() & span.getStyle() == bold.getStyle()) {
                            totalText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),totalText.getSpanStart(span),totalText.getSpanEnd(span),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            totalText.removeSpan(span);
                        }
                    }
                }
                if(etContent.getSelectionStart() < etContent.getSelectionEnd()){
                    startSelection = etContent.getSelectionStart();
                    endSelection = etContent.getSelectionEnd();
                } else {
                    startSelection = etContent.getSelectionEnd();
                    endSelection = etContent.getSelectionStart();
                }

                totalText = (SpannableStringBuilder) etContent.getText();
                spans = totalText.getSpans(startSelection, endSelection, StyleSpan.class);
                Boolean alreadyBold = false;
                if(etContent.getSelectionStart() != etContent.getSelectionEnd()){
                    for (StyleSpan span : spans) {
                        if (span.getStyle() == bold.getStyle()) {
                            alreadyBold = true;
                            if(totalText.getSpanStart(span) >= startSelection && totalText.getSpanEnd(span) < endSelection){
                                totalText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),startSelection,endSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                if(totalText.getSpanStart(span) > startSelection){
                                    totalText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),startSelection,totalText.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                if(totalText.getSpanEnd(span) < endSelection) {
                                    totalText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), totalText.getSpanEnd(span), endSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            if(totalText.getSpanStart(span) < startSelection && totalText.getSpanEnd(span) >= endSelection){
                                totalText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), totalText.getSpanStart(span), startSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                if (totalText.getSpanStart(span) < startSelection && !(totalText.getSpanEnd(span) < endSelection)) {
                                    totalText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), totalText.getSpanStart(span), startSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                if (totalText.getSpanEnd(span) > endSelection && !(totalText.getSpanStart(span) > startSelection)) {
                                    totalText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), endSelection, totalText.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            totalText.removeSpan(span);
                        }
                    }
                    if(!alreadyBold){
                        totalText.setSpan(bold,startSelection,endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                etContent.setText(totalText);
                etContent.setSelection(startSelection);
                break;
            case R.id.btn_italics:

                totalText = (SpannableStringBuilder) etContent.getText();
                final StyleSpan italic = new StyleSpan(Typeface.ITALIC);
                totalText = (SpannableStringBuilder) etContent.getText();
                if(etContent.getSelectionStart() == etContent.getSelectionEnd()){
                    spans = totalText.getSpans(0, etContent.getSelectionEnd(), StyleSpan.class);
                    for (StyleSpan span : spans) {
                        if (totalText.getSpanEnd(span) == etContent.getSelectionEnd() & span.getStyle() == italic.getStyle()) {
                            totalText.setSpan(new StyleSpan(Typeface.ITALIC),totalText.getSpanStart(span),totalText.getSpanEnd(span),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            totalText.removeSpan(span);
                        }
                    }
                }
                if(etContent.getSelectionStart() < etContent.getSelectionEnd()){
                    startSelection = etContent.getSelectionStart();
                    endSelection = etContent.getSelectionEnd();
                } else {
                    startSelection = etContent.getSelectionEnd();
                    endSelection = etContent.getSelectionStart();
                }

                totalText = (SpannableStringBuilder) etContent.getText();
                spans = totalText.getSpans(startSelection, endSelection, StyleSpan.class);
                Boolean alreadyItalics = false;
                if(etContent.getSelectionStart() != etContent.getSelectionEnd()){
                    for (StyleSpan span : spans) {
                        if (span.getStyle() == italic.getStyle()) {
                            alreadyItalics = true;
                            if(totalText.getSpanStart(span) >= startSelection && totalText.getSpanEnd(span) < endSelection){
                                totalText.setSpan(new StyleSpan(Typeface.ITALIC),startSelection,endSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                if(totalText.getSpanStart(span) > startSelection){
                                    totalText.setSpan(new StyleSpan(Typeface.ITALIC),startSelection,totalText.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                if(totalText.getSpanEnd(span) < endSelection) {
                                    totalText.setSpan(new StyleSpan(Typeface.ITALIC), totalText.getSpanEnd(span), endSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            if(totalText.getSpanStart(span) < startSelection && totalText.getSpanEnd(span) >= endSelection){
                                totalText.setSpan(new StyleSpan(Typeface.ITALIC), totalText.getSpanStart(span), startSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                if (totalText.getSpanStart(span) < startSelection && !(totalText.getSpanEnd(span) < endSelection)) {
                                    totalText.setSpan(new StyleSpan(Typeface.ITALIC), totalText.getSpanStart(span), startSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                if (totalText.getSpanEnd(span) > endSelection && !(totalText.getSpanStart(span) > startSelection)) {
                                    totalText.setSpan(new StyleSpan(Typeface.ITALIC), endSelection, totalText.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            totalText.removeSpan(span);
                        }
                    }
                    if(!alreadyItalics){
                        totalText.setSpan(italic,startSelection,endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                etContent.setText(totalText);
                etContent.setSelection(startSelection);
                break;
            case R.id.btn_underline:
                underlined = new UnderlineSpan();
                Boolean alreadyUnderlined = false;
                totalText = (SpannableStringBuilder) etContent.getText();
                UnderlineSpan[] underlineSpans = totalText.getSpans(etContent.getSelectionStart(),etContent.getSelectionEnd(),UnderlineSpan.class);
                if(etContent.getSelectionStart() == etContent.getSelectionEnd()){
                    for (UnderlineSpan span : underlineSpans) {
                        if (totalText.getSpanEnd(span) == etContent.getSelectionEnd() & span.getSpanTypeId() == underlined.getSpanTypeId()) {
                            totalText.setSpan(new UnderlineSpan(),totalText.getSpanStart(span),totalText.getSpanEnd(span),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            totalText.removeSpan(span);
                        }
                    }
                }
                if(etContent.getSelectionStart() < etContent.getSelectionEnd()){
                    startSelection = etContent.getSelectionStart();
                    endSelection = etContent.getSelectionEnd();
                } else {
                    startSelection = etContent.getSelectionEnd();
                    endSelection = etContent.getSelectionStart();
                }
                if(etContent.getSelectionStart() != etContent.getSelectionEnd()){
                    for (UnderlineSpan span : underlineSpans) {
                        if (span.getSpanTypeId() == underlined.getSpanTypeId()) {
                            alreadyUnderlined = true;
                            if(totalText.getSpanStart(span) >= startSelection && totalText.getSpanEnd(span) < endSelection){
                                totalText.setSpan(new UnderlineSpan(),startSelection,endSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                if(totalText.getSpanStart(span) > startSelection){
                                    totalText.setSpan(new UnderlineSpan(),startSelection,totalText.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                if(totalText.getSpanEnd(span) < endSelection) {
                                    totalText.setSpan(new UnderlineSpan(), totalText.getSpanEnd(span), endSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            if(totalText.getSpanStart(span) < startSelection && totalText.getSpanEnd(span) >= endSelection){
                                totalText.setSpan(new UnderlineSpan(), totalText.getSpanStart(span), startSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                if (totalText.getSpanStart(span) < startSelection && !(totalText.getSpanEnd(span) < endSelection)) {
                                    totalText.setSpan(new UnderlineSpan(), totalText.getSpanStart(span), startSelection, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                if (totalText.getSpanEnd(span) > endSelection && !(totalText.getSpanStart(span) > startSelection)) {
                                    totalText.setSpan(new UnderlineSpan(), endSelection, totalText.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            totalText.removeSpan(span);
                        }
                    }
                    if(!alreadyUnderlined){
                        totalText.setSpan(new UnderlineSpan(),startSelection,endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                etContent.setText(totalText);
                etContent.setSelection(startSelection);
                break;
            default:
        }
    }

    private void updateNote(){
        fillNameIfEmpty();
        Note note = new Note(etName.getText().toString(),Html.toHtml(etContent.getText()),DbContract.NoteEntry.TYPE_TEXT,currentCat);
        note.set_id(id);
        createEditNoteViewModel.update(note);
        Toast.makeText(getApplicationContext(), R.string.toast_updated, Toast.LENGTH_SHORT).show();
    }

    private void saveNote(){
        fillNameIfEmpty();
        Note note = new Note(etName.getText().toString(),Html.toHtml(etContent.getText()),DbContract.NoteEntry.TYPE_TEXT,currentCat);
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
        new AlertDialog.Builder(TextNoteActivity.this)
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
                        startActivity(new Intent(TextNoteActivity.this, ManageCategoriesActivity.class));
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void displayTrashDialog() {
        SharedPreferences sp = getSharedPreferences(PreferenceKeys.SP_DATA, Context.MODE_PRIVATE);
        Intent intent = getIntent();
        Note note = new Note(intent.getStringExtra(EXTRA_TITLE),intent.getStringExtra(EXTRA_CONTENT),DbContract.NoteEntry.TYPE_TEXT,intent.getIntExtra(EXTRA_CATEGORY,-1));
        note.set_id(id);
        if (sp.getBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, true)){
            //we never displayed the message before, so show it now
            new AlertDialog.Builder(TextNoteActivity.this)
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
            note.setIn_trash(intent.getIntExtra(EXTRA_ISTRASH,0));
            if(note.getIn_trash() == 1){
                createEditNoteViewModel.delete(note);
            } else {
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
            c.setTimeInMillis(notification.getTime());
        }
        TimePickerDialog tpd = new TimePickerDialog(TextNoteActivity.this, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
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

        NotificationHelper.addNotificationToAlarmManager(this,id,DbContract.NoteEntry.TYPE_TEXT,title,alarmtime.getTimeInMillis());
        NotificationHelper.showAlertScheduledToast(this,dayOfMonth,monthOfYear,year,hourOfDay,minute);
        loadActivity(false);
    }

    private void cancelNotification(){
        //Create the intent that would be fired by AlarmManager
        NotificationHelper.removeNotificationFromAlarmManager(this,id,DbContract.NoteEntry.TYPE_TEXT,title);
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
            DatePickerDialog dpd = new DatePickerDialog(TextNoteActivity.this, this, year, month, day);
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

            File file = new File(path, "/text_" + etName.getText().toString() + ".txt");
            try {
                // Make sure the directory exists.
                boolean path_exists = path.exists() || path.mkdirs();
                if (path_exists) {

                    PrintWriter out = new PrintWriter(file);
                    out.println(etName.getText().toString());
                    out.println();
                    out.println(Html.toHtml(etContent.getText()));
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

}
