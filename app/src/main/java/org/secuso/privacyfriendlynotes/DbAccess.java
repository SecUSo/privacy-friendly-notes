package org.secuso.privacyfriendlynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import org.secuso.privacyfriendlynotes.DbContract.CategoryEntry;
import org.secuso.privacyfriendlynotes.DbContract.NoteEntry;

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
    public static Cursor getNote(Context c, int id) {
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
    public static void addNote(Context c, String name, String content, int type){
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_TYPE, type);
        values.put(NoteEntry.COLUMN_NAME, name);
        values.put(NoteEntry.COLUMN_CONTENT, content);
        db.insert(NoteEntry.TABLE_NAME, null, values);
        db.close();
    }

    /**
     * Updates a text note in the database.
     * @param c the current context
     * @param id the id of the note
     * @param name the new name of the note
     * @param content the new content of the note
     */
    public static void updateNote(Context c, int id, String name, String content) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_NAME, name);
        values.put(NoteEntry.COLUMN_CONTENT, content);
        String selection = NoteEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.update(NoteEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    /**
     * Deletes a  text note from the database.
     * @param c the current context
     * @param id the ID of the note
     */
    public static void deleteNote(Context c, int id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = NoteEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(NoteEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
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

        return db.query(NoteEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                null,                           // Columns for WHERE
                null,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }

    /**
     * Returns a cursor over all the notes in the database.
     * @param c the current context
     * @param selection the selection string to use with the query
     * @param selectionArgs the selection arguments to use with the query
     * @return A {@link android.database.Cursor} over all the notes
     */
    public static Cursor getCursorAllNotes(Context c, String selection, String[] selectionArgs) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NoteEntry.COLUMN_ID, NoteEntry.COLUMN_TYPE, NoteEntry.COLUMN_NAME, NoteEntry.COLUMN_CONTENT};

        return db.query(NoteEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                selection,                           // Columns for WHERE
                selectionArgs,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }

    /**
     * Returns a cursor over all the notes in the database in alphabetical ordering.
     * @param c the current context
     * @return A {@link android.database.Cursor} over all the notes
     */
    public static Cursor getCursorAllNotesAlphabetical(Context c) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NoteEntry.COLUMN_ID, NoteEntry.COLUMN_TYPE, NoteEntry.COLUMN_NAME, NoteEntry.COLUMN_CONTENT};

        String sortOrder = NoteEntry.COLUMN_NAME + " ASC";

        return db.query(NoteEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                null,                           // Columns for WHERE
                null,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                sortOrder);                     // Sort Order
    }

    /**
     * Returns a cursor over all the notes in the database in alphabetical ordering.
     * @param c the current context
     * @param selection the selection string to use with the query
     * @param selectionArgs the selection arguments to use with the query
     * @return A {@link android.database.Cursor} over all the notes
     */
    public static Cursor getCursorAllNotesAlphabetical(Context c, String selection, String[] selectionArgs) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NoteEntry.COLUMN_ID, NoteEntry.COLUMN_TYPE, NoteEntry.COLUMN_NAME, NoteEntry.COLUMN_CONTENT};

        String sortOrder = NoteEntry.COLUMN_NAME + " ASC";

        return db.query(NoteEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                selection,                           // Columns for WHERE
                selectionArgs,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                sortOrder);                     // Sort Order
    }

    /**
     * Returns a cursor over all the categories in the database.
     * @param c the current context
     * @return A {@link android.database.Cursor} over all the notes
     */
    public static Cursor getCategories(Context c){
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {CategoryEntry.COLUMN_ID, CategoryEntry.COLUMN_NAME};

        return db.query(CategoryEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                null,                           // Columns for WHERE
                null,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by
                null);                     // Sort Order
    }

    /**
     * Inserts a new category into the database.
     * @param c the current context.
     * @param name the name of the category
     */
    public static boolean addCategory(Context c, String name) {
        boolean success = true;
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CategoryEntry.COLUMN_NAME, name.trim());
        try {
            db.insertOrThrow(CategoryEntry.TABLE_NAME, null, values);
        } catch (SQLiteConstraintException e) {
            success = false;
        }
        db.close();
        return success;
    }

    /**
     * Deletes a  category from the database.
     * @param c the current context
     * @param id the ID of the category
     */
    public static void deleteCategory(Context c, int id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = CategoryEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(CategoryEntry.TABLE_NAME, selection, selectionArgs);
        //TODO delete category id from notes
        db.close();
    }
}
