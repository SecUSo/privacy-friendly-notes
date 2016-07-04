package org.secuso.privacyfriendlynotes;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RecycleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);

        String selection = DbContract.NoteEntry.COLUMN_TRASH + " = ?";
        String[] selectionArgs = { "1" };
        Cursor c = DbAccess.getCursorAllNotes(getBaseContext(), selection, selectionArgs);

        ListView notesList = (ListView) findViewById(R.id.notes_list);
        notesList.setAdapter(new CursorAdapter(getApplicationContext(), c, CursorAdapter.FLAG_AUTO_REQUERY) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.item_note, null);

                TextView text = (TextView) rowView.findViewById(R.id.item_name);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
                if (name.length() >= 30) {
                    text.setText(name.substring(0,27) + "...");
                } else {
                    text.setText(name);
                }

                ImageView iv = (ImageView) rowView.findViewById(R.id.item_icon);
                switch (cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
                    case DbContract.NoteEntry.TYPE_SKETCH:
                        iv.setImageResource(R.drawable.ic_photo_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_AUDIO:
                        iv.setImageResource(R.drawable.ic_mic_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_TEXT:
                        iv.setImageResource(R.drawable.ic_short_text_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_CHECKLIST:
                        iv.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                        break;
                    default:
                }
                return rowView;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text = (TextView) view.findViewById(R.id.item_name);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
                if (name.length() >= 30) {
                    text.setText(name.substring(0,27) + "...");
                } else {
                    text.setText(name);
                }

                ImageView iv = (ImageView) view.findViewById(R.id.item_icon);
                switch (cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
                    case DbContract.NoteEntry.TYPE_SKETCH:
                        iv.setImageResource(R.drawable.ic_photo_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_AUDIO:
                        iv.setImageResource(R.drawable.ic_mic_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_TEXT:
                        iv.setImageResource(R.drawable.ic_short_text_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_CHECKLIST:
                        iv.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                        break;
                    default:
                }
            }
        });

        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get details about the clicked note
                CursorAdapter ca = (CursorAdapter) parent.getAdapter();
                Cursor c = ca.getCursor();
                c.moveToPosition(position);
                displayRestoreDialog(c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)), c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME)));

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        CursorAdapter adapter = (CursorAdapter) notesList.getAdapter();
        String selection = DbContract.NoteEntry.COLUMN_TRASH + " = ?";
        String[] selectionArgs = { "1" };
        adapter.changeCursor(DbAccess.getCursorAllNotes(getBaseContext(), selection, selectionArgs));
    }

    private void displayRestoreDialog(final int id, final String name) {

        new AlertDialog.Builder(RecycleActivity.this)
                .setTitle(String.format(getString(R.string.dialog_restore_title), name))
                .setMessage(String.format(getString(R.string.dialog_restore_message), name))
                .setNegativeButton(R.string.dialog_option_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DbAccess.deleteNote(getBaseContext(), id);
                        updateList();
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
                        DbAccess.restoreNote(getBaseContext(), id);
                        updateList();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
