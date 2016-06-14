package org.secuso.privacyfriendlynotes;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TextNoteActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";

    EditText etName;
    EditText etContent;

    private boolean edit = false;
    private int id = -1;
    Cursor noteCursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_note);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        //Look for a note ID in the intent. If we got one, then we will edit that note. Otherwise we create a new one.
        Intent i = getIntent();
        id = i.getIntExtra(EXTRA_ID, -1);
        edit = (id != -1);

        etName = (EditText) findViewById(R.id.etName);
        etContent = (EditText) findViewById(R.id.etContent);
        if (edit) {
            noteCursor = DbAccess.getTextNote(getBaseContext(), id);
            noteCursor.moveToFirst();
            if (noteCursor.getCount() != 1) {
                Toast.makeText(getBaseContext(), "Too many or no notes found: " + noteCursor.getCount(), Toast.LENGTH_SHORT).show();
            } else {
                etName.setText(noteCursor.getString(noteCursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME)));
                etContent.setText(noteCursor.getString(noteCursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT)));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //The
        if (edit) {
            updateNote();
        } else {
            saveNote();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                Toast.makeText(getBaseContext(), R.string.toast_canceled, Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    private void updateNote(){
        //TODO
    }

    private void saveNote(){
        DbAccess.saveTextNote(getBaseContext(), etName.getText().toString(), etContent.getText().toString());
        Toast.makeText(getApplicationContext(), "Saving Note", Toast.LENGTH_SHORT).show();
        finish();
    }
}
