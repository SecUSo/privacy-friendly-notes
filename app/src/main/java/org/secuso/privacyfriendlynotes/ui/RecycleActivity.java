package org.secuso.privacyfriendlynotes.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;

import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.room.adapter.NoteAdapter;
import org.secuso.privacyfriendlynotes.ui.main.MainActivityViewModel;

import java.io.File;
import java.util.List;

public class RecycleActivity extends AppCompatActivity {

    MainActivityViewModel mainActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);


        RecyclerView recyclerView = findViewById(R.id.recyclerViewRecycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        final NoteAdapter adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mainActivityViewModel.getTrashedNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                adapter.setNotes(notes);
            }
        });

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                new AlertDialog.Builder(RecycleActivity.this)
                        .setTitle(String.format(getString(R.string.dialog_restore_title), note.getName()))
                        .setMessage(String.format(getString(R.string.dialog_restore_message), note.getName()))
                        .setNegativeButton(R.string.dialog_option_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainActivityViewModel.delete(note);
                                if (note.getType() == DbContract.NoteEntry.TYPE_AUDIO) {
                                    new File(getFilesDir().getPath()+"/audio_notes"+note.getContent() ).delete();
                                } else if (note.getType() == DbContract.NoteEntry.TYPE_SKETCH) {
                                    new File(getFilesDir().getPath()+"/sketches"+note.getContent() ).delete();
                                    new File(getFilesDir().getPath()+"/sketches"+ note.getContent().substring(0, note.getContent().length()-3) + "jpg").delete();
                                }
                            }
                        })
                        .setNeutralButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .setPositiveButton(R.string.dialog_option_restore, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                note.setIn_trash(0);
                                mainActivityViewModel.update(note);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
    }
}
