package org.secuso.privacyfriendlynotes;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.widget.EditText;
import android.widget.Spinner;

import org.secuso.privacyfriendlynotes.views.DrawView;

public class SketchActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";

    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    EditText etName;
    DrawView drawView;
    Spinner spinner;

    private ShareActionProvider mShareActionProvider = null;

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
        setContentView(R.layout.activity_sketch);
    }
}
