package org.secuso.privacyfriendlynotes;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

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
                        iv.setImageResource(R.drawable.ic_photo_black_24dp);
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
                        iv.setImageResource(R.drawable.ic_photo_black_24dp);
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
                displayRestoreDialog(c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)), c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME)),
                        c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT)), c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE)));

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recycle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_all) {
            deleteAll();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateList() {
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        CursorAdapter adapter = (CursorAdapter) notesList.getAdapter();
        String selection = DbContract.NoteEntry.COLUMN_TRASH + " = ?";
        String[] selectionArgs = { "1" };
        adapter.changeCursor(DbAccess.getCursorAllNotes(getBaseContext(), selection, selectionArgs));
    }

    private void displayRestoreDialog(final int id, final String name, final String content, final int type) {

        new AlertDialog.Builder(RecycleActivity.this)
                .setTitle(String.format(getString(R.string.dialog_restore_title), name))
                .setMessage(String.format(getString(R.string.dialog_restore_message), name))
                .setNegativeButton(R.string.dialog_option_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DbAccess.deleteNote(getBaseContext(), id);
                        if (type == DbContract.NoteEntry.TYPE_AUDIO) {
                            new File(getFilesDir().getPath()+content).delete();
                        }
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

    private void deleteAll(){
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        CursorAdapter ca = (CursorAdapter) notesList.getAdapter();
        Cursor c = ca.getCursor();
        c.moveToPosition(-1);
        while (c.moveToNext()){
            if (c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE)) == DbContract.NoteEntry.TYPE_AUDIO) {
                String filePath = getFilesDir().getPath() + c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_CONTENT));
                new File(filePath).delete();
            }
            DbAccess.deleteNote(getBaseContext(), c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
        }
        updateList();
    }
}
