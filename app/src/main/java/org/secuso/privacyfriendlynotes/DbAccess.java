package org.secuso.privacyfriendlynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.secuso.privacyfriendlynotes.NotesContract.*;

/**
 * Created by Robin on 11.06.2016.
 */
public class DbAccess {
    public static void saveTextNote(Context c, String name, String content){
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_NAME_NAME, name);
        values.put(NoteEntry.COLUMN_NAME_CONTENT, content);
        db.insert(NoteEntry.TABLE_NAME, null, values);
    }

    public static Cursor getCursorAllNotes(Context c) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NoteEntry.COLUMN_NAME_ID, NoteEntry.COLUMN_NAME_NAME, NoteEntry.COLUMN_NAME_CONTENT};

        String sortOrder = NoteEntry.COLUMN_NAME_ID + " DESC";

        return db.query(NoteEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                null,                           // Columns for WHERE
                null,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }
}
