package org.secuso.privacyfriendlynotes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CreateTextNoteActivity extends AppCompatActivity implements View.OnClickListener{

    EditText etName;
    EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_text_note);
        findViewById(R.id.btn_save).setOnClickListener(this);

        etName = (EditText) findViewById(R.id.etName);
        etContent = (EditText) findViewById(R.id.etContent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                saveNote();
                break;
            default:
        }
    }

    private void saveNote(){
        //TODO
        DbAccess.saveTextNote(getBaseContext(), etName.getText().toString(), etContent.getText().toString());
        Toast.makeText(getApplicationContext(), "TODO: Saving...", Toast.LENGTH_SHORT).show();
        finish();
    }
}
