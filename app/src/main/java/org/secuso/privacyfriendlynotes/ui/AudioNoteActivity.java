package org.secuso.privacyfriendlynotes.ui;

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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.secuso.privacyfriendlynotes.database.DbAccess;
import org.secuso.privacyfriendlynotes.database.DbContract;
import org.secuso.privacyfriendlynotes.room.Note;
import org.secuso.privacyfriendlynotes.room.NoteViewModel;
import org.secuso.privacyfriendlynotes.service.NotificationService;
import org.secuso.privacyfriendlynotes.preference.PreferenceKeys;
import org.secuso.privacyfriendlynotes.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;

public class AudioNoteActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, PopupMenu.OnMenuItemClickListener {
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";
    public static final String EXTRA_TITLE = "org.secuso.privacyfriendlynotes.TITLE";
    public static final String EXTRA_CONTENT = "org.secuso.privacyfriendlynotes.CONTENT";
    public static final String EXTRA_CATEGORY = "org.secuso.privacyfriendlynotes.CATEGORY";


    private static final int REQUEST_CODE_AUDIO = 1;
    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 2;

    EditText etName;
    ImageButton btnPlayPause;
    ImageButton btnRecord;
    TextView tvRecordingTime;
    SeekBar seekBar;
    Spinner spinner;

    private ShareActionProvider mShareActionProvider = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private Handler mHandler = new Handler();
    private String mFileName = "finde_die_datei.mp4";
    private String mFilePath;
    private boolean recording = false;
    private boolean playing = false;
    private long startTime = System.currentTimeMillis();

    private int dayOfMonth, monthOfYear, year;

    private boolean edit = false;
    private boolean hasAlarm = false;
    private boolean shouldSave = true;
    private int id = -1;
    private int notification_id = -1;
    private int currentCat;
    Cursor noteCursor = null;
    Cursor notificationCursor = null;

    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_note);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);

        etName = (EditText) findViewById(R.id.etName);
        btnPlayPause = (ImageButton) findViewById(R.id.btn_play_pause);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        btnRecord = (ImageButton) findViewById(R.id.btn_record);
        tvRecordingTime = (TextView) findViewById(R.id.recording_time);
        spinner = (Spinner) findViewById(R.id.spinner_category);

        findViewById(R.id.btn_record).setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(AudioNoteActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AudioNoteActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(AudioNoteActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_AUDIO);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(AudioNoteActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_AUDIO);
            }
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayer != null && fromUser) {
                    mPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        loadActivity(true);

    }

    private void loadActivity(boolean initial){
        //Look for a note ID in the intent. If we got one, then we will edit that note. Otherwise we create a new one.
        if (id == -1) {
            Intent intent = getIntent();
            id = intent.getIntExtra(EXTRA_ID, -1);
        }
        edit = (id != -1);

        SimpleCursorAdapter adapter = null;
        // Should we set a custom font size?
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(SettingsActivity.PREF_CUSTOM_FONT, false)) {
            etName.setTextSize(Float.parseFloat(sp.getString(SettingsActivity.PREF_CUSTOM_FONT_SIZE, "15")));
        }

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

            Intent intent = getIntent();
            etName.setText(intent.getStringExtra(EXTRA_TITLE));
            mFileName = intent.getStringExtra(EXTRA_CONTENT);
            mFilePath = getFilesDir().getPath() + "/audio_notes" + mFileName;
            btnPlayPause.setVisibility(View.VISIBLE);
            btnRecord.setVisibility(View.INVISIBLE);
            tvRecordingTime.setVisibility(View.INVISIBLE);
            //find the current category and set spinner to that
            currentCat = intent.getIntExtra(EXTRA_CATEGORY, -1);

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
            findViewById(R.id.btn_delete).setEnabled(true);
            ((Button) findViewById(R.id.btn_save)).setText(getString(R.string.action_update));
        } else {
            findViewById(R.id.btn_delete).setEnabled(false);
            mFileName = "/recording_" + System.currentTimeMillis() + ".aac";
            mFilePath = getFilesDir().getPath() + "/audio_notes";
            new File(mFilePath).mkdirs(); //ensure that the file exists
            mFilePath = getFilesDir().getPath() + "/audio_notes" + mFileName;
            seekBar.setEnabled(false);
            tvRecordingTime.setVisibility(View.VISIBLE);
            shouldSave = false; // will be set to true, once we have a recording
        }
        if(!initial) {
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //The Activity is not visible anymore. Save the work!
        if (shouldSave ) {
            if (edit) {
                updateNote();
            } else {
                saveNote();
            }
        } else {
            if(!edit) {
                new File(mFilePath).delete();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadActivity(false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            if (recording) {
                stopRecording();
                finish();
            } else if (playing) {
                pausePlaying();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (edit){
            getMenuInflater().inflate(R.menu.audio, menu);
            MenuItem item = menu.findItem(R.id.action_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            setShareIntent();
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

        setShareIntent();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reminder) {
            //open the schedule dialog
            final Calendar c = Calendar.getInstance();

            //fill the notificationCursor
            notificationCursor = DbAccess.getNotificationByNoteId(getBaseContext(), this.id);
            hasAlarm = notificationCursor.moveToFirst();
            if (hasAlarm) {
                notification_id = notificationCursor.getInt(notificationCursor.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_ID));
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

                DatePickerDialog dpd = new DatePickerDialog(AudioNoteActivity.this, this, year, month, day);
                dpd.getDatePicker().setMinDate(c.getTimeInMillis());
                dpd.show();
            }
            return true;
        } else if (id == R.id.action_save) {
            if (ContextCompat.checkSelfPermission(AudioNoteActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(AudioNoteActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(AudioNoteActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_EXTERNAL_STORAGE);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(AudioNoteActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_EXTERNAL_STORAGE);
                }
            } else {
                saveToExternalStorage();
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
                    displayTrashDialog();
                }
                break;
            case R.id.btn_save:
                shouldSave = true; //safe on exit
                finish();
                break;
            case R.id.btn_record:
                if (!recording) {
                    startRecording();
                } else {
                    stopRecording();
                }
                break;
            case R.id.btn_play_pause:
                if (!playing) {
                    startPlaying();
                } else {
                    pausePlaying();
                }
                break;
            default:
        }
    }

    private void startRecording() {
        recording = true;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.prepare();
            final Animation animation = new AlphaAnimation(1, (float)0.5); // Change alpha from fully visible to invisible
            animation.setDuration(500); // duration - half a second
            animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
            animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
            animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
            btnRecord.startAnimation(animation);
            startTime = System.currentTimeMillis();
            AudioNoteActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRecorder != null) {
                        long time = System.currentTimeMillis() - startTime;
                        int seconds = (int) time / 1000;
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                        tvRecordingTime.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                        mHandler.postDelayed(this, 100);
                    }
                }
            });

            mRecorder.start();
        } catch (IOException e) {
            recording = false;
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        Log.d("LALALA", "Stopped recording");
        mRecorder.stop();
        mRecorder.release();
        btnRecord.clearAnimation();
        mRecorder = null;
        recording = false;
        recordingFinished();
    }

    private void startPlaying() {
        playing = true;
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(mFilePath);
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
                togglePlayPauseButton();
                seekBar.setProgress(0);
                mPlayer.release();
                mPlayer = null;
            }
        });

        togglePlayPauseButton();
        seekBar.setMax(mPlayer.getDuration());
        AudioNoteActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null) {
                    seekBar.setProgress(mPlayer.getCurrentPosition());
                    mHandler.postDelayed(this, 100);
                }
            }
        });
        mPlayer.start();
    }

    private void pausePlaying() {
        playing = false;
        togglePlayPauseButton();
        try {
            mPlayer.pause();
        } catch (RuntimeException stopException) {
        }
    }

    private void recordingFinished() {
        shouldSave = true;
        btnRecord.setVisibility(View.INVISIBLE);
        btnPlayPause.setVisibility(View.VISIBLE);
        seekBar.setEnabled(true);
    }

    private void togglePlayPauseButton(){
        if (playing) {
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause_black_24dp);
        } else {
            btnPlayPause.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    private void updateNote(){
        fillNameIfEmpty();
        Note note = new Note(etName.getText().toString(),mFileName,DbContract.NoteEntry.TYPE_AUDIO,currentCat);
        note.setId(id);
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.update(note);
        Toast.makeText(getApplicationContext(), R.string.toast_updated, Toast.LENGTH_SHORT).show();
    }

    private void saveNote(){
        fillNameIfEmpty();
        Note note = new Note(etName.getText().toString(),mFileName,DbContract.NoteEntry.TYPE_AUDIO,currentCat);
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.insert(note);

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
        new AlertDialog.Builder(AudioNoteActivity.this)
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
                        startActivity(new Intent(AudioNoteActivity.this, ManageCategoriesActivity.class));
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void displayTrashDialog() {
        SharedPreferences sp = getSharedPreferences(PreferenceKeys.SP_DATA, Context.MODE_PRIVATE);
        if (sp.getBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, true)){
            //we never displayed the message before, so show it now
            new AlertDialog.Builder(AudioNoteActivity.this)
                    .setTitle(getString(R.string.dialog_trash_title))
                    .setMessage(getString(R.string.dialog_trash_message))
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, false);
                            editor.commit();
                            finish();
                            displayTrashDialog();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(PreferenceKeys.SP_DATA_DISPLAY_TRASH_MESSAGE, false);
            editor.commit();
        } else {
            shouldSave = false;
            Intent intent = getIntent();
            Note note = new Note(intent.getStringExtra(EXTRA_TITLE),intent.getStringExtra(EXTRA_CONTENT),DbContract.NoteEntry.TYPE_AUDIO,intent.getIntExtra(EXTRA_CATEGORY,-1));
            note.setId(id);
            noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
            if(note.isTrash() == 1){
                noteViewModel.delete(note);
            } else {
                note = new Note(etName.getText().toString(),mFileName,DbContract.NoteEntry.TYPE_AUDIO,currentCat);
                note.setId(id);
                note.setTrash(1);
                noteViewModel.update(note);
            }

            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Do nothing. App should work
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_need_permission_audio, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
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
            c.setTimeInMillis(notificationCursor.getLong(notificationCursor.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_TIME)));
        }
        TimePickerDialog tpd = new TimePickerDialog(AudioNoteActivity.this, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
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
            DatePickerDialog dpd = new DatePickerDialog(AudioNoteActivity.this, this, year, month, day);
            dpd.getDatePicker().setMinDate(new Date().getTime());
            dpd.show();
            return true;
        } else if (id == R.id.action_reminder_delete) {
            cancelNotification();
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
            File file = new File(path, "/" + etName.getText().toString() + ".aac");
            try {
                // Make sure the directory exists.
                boolean path_exists = path.exists() || path.mkdirs();
                if (path_exists) {
                    FileChannel source = null;
                    FileChannel destination = null;
                    try {
                        source = new FileInputStream(new File(mFilePath)).getChannel();
                        destination = new FileOutputStream(file).getChannel();
                        destination.transferFrom(source, 0, source.size());
                    } finally {
                        source.close();
                        destination.close();
                    }
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

    private void setShareIntent(){
        if (mShareActionProvider != null) {
            File audioFile = new File(mFilePath);
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "org.secuso.privacyfriendlynotes", audioFile);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("audio/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mShareActionProvider.setShareIntent(sendIntent);
        }
    }
}
