package org.secuso.privacyfriendlynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.secuso.privacyfriendlynotes.DbContract.*;

/**
 * Class that holds methods to access the database easily.
 * Created by Robin on 11.06.2016.
 */
public class DbAccess {

    /**
     * Returns a specific text note
     * @param c the current context
     * @param id the id of the note
     * @return the cursor to the note
     */
    public static Cursor getTextNote(Context c, int id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NoteEntry.COLUMN_ID, NoteEntry.COLUMN_TYPE, NoteEntry.COLUMN_NAME, NoteEntry.COLUMN_CONTENT};
        String selection = NoteEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {"" + id};

        return db.query(NoteEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                selection,                      // Columns for WHERE
                selectionArgs,                  // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }

    /**
     * Inserts a new text note into the database.
     * @param c the current context.
     * @param name the name of the note
     * @param content the content of the note
     */
    public static void saveTextNote(Context c, String name, String content){
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_TYPE, NoteEntry.TYPE_TEXT);
        values.put(NoteEntry.COLUMN_NAME, name);
        values.put(NoteEntry.COLUMN_CONTENT, content);
        db.insert(NoteEntry.TABLE_NAME, null, values);
    }

    /**
     * Updates a text note in the database.
     * @param c the current context
     * @param id the id of the note
     * @param name the new name of the note
     * @param content the new content of the note
     */
    public static void updateTextNote(Context c, int id, String name, String content) {
        //TODO
    }

    /**
     * Deletes a  text note from the database.
     * @param c the current context
     * @param id the ID of the note
     */
    public static void deleteTextNote(Context c, int id) {
        //TODO
    }

    /**
     * Returns a cursor over all the notes in the database.
     * @param c the current context
     * @return A {@link android.database.Cursor} over all the notes
     */
    public static Cursor getCursorAllNotes(Context c) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NoteEntry.COLUMN_ID, NoteEntry.COLUMN_TYPE, NoteEntry.COLUMN_NAME, NoteEntry.COLUMN_CONTENT};

        String sortOrder = NoteEntry.COLUMN_ID + " DESC";

        return db.query(NoteEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                null,                           // Columns for WHERE
                null,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }
}
