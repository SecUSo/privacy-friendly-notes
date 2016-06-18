package org.secuso.privacyfriendlynotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextNoteActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String EXTRA_ID = "org.secuso.privacyfriendlynotes.ID";

    EditText etName;
    EditText etContent;

    private boolean edit = false;
    private boolean shouldSave = true;
    private int id = -1;
    Cursor noteCursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_note);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        //Look for a note ID in the intent. If we got one, then we will edit that note. Otherwise we create a new one.
        Intent i = getIntent();
        id = i.getIntExtra(EXTRA_ID, -1);
        edit = (id != -1);

        etName = (EditText) findViewById(R.id.etName);
        etContent = (EditText) findViewById(R.id.etContent);
        if (edit) {
            noteCursor = DbAccess.getNote(getBaseContext(), id);
            noteCursor.moveToFirst();
            if (noteCursor.getCount() != 1) {
                Toast.makeText(getBaseContext(), "Too many or no notes found: " + noteCursor.getCount(), Toast.LENGTH_SHORT).show();
            } else {
                etName.setText(noteCursor.getString(noteCursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME)));
                etContent.setText(noteCursor.getString(noteCursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT)));
            }
            ((Button) findViewById(R.id.btn_save)).setText(getString(R.string.action_update));
        } else {
            findViewById(R.id.btn_delete).setEnabled(false);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                Toast.makeText(getBaseContext(), R.string.toast_canceled, Toast.LENGTH_SHORT).show();
                shouldSave = false;
                finish();
                break;
            case R.id.btn_delete:
                if (edit) { //note only exists in edit mode
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
                break;
            case R.id.btn_save:
                shouldSave = true; //safe on exit
                finish();
                break;
            default:
        }
    }

    private void updateNote(){
        DbAccess.updateNote(getBaseContext(), id, etName.getText().toString(), etContent.getText().toString());
        Toast.makeText(getApplicationContext(), R.string.toast_updated, Toast.LENGTH_SHORT).show();
    }

    private void saveNote(){
        DbAccess.addNote(getBaseContext(), etName.getText().toString(), etContent.getText().toString(), DbContract.NoteEntry.TYPE_TEXT);
        Toast.makeText(getApplicationContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show();
    }

    private boolean fieldsEmpty(){
        return etName.getText().toString().isEmpty() && etContent.getText().toString().isEmpty();
    }
}
