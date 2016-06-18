package org.secuso.privacyfriendlynotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ChecklistNoteActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_note);
    }
}
