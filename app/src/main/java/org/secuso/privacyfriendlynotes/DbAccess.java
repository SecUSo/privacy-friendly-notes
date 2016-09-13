package org.secuso.privacyfriendlynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import org.secuso.privacyfriendlynotes.DbContract.CategoryEntry;
import org.secuso.privacyfriendlynotes.DbContract.NoteEntry;
import org.secuso.privacyfriendlynotes.DbContract.NotificationEntry;

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

        String[] projection = { NoteEntry.COLUMN_ID, NoteEntry.COLUMN_TYPE, NoteEntry.COLUMN_NAME, NoteEntry.COLUMN_CONTENT, NoteEntry.COLUMN_CATEGORY };
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
    public static int addNote(Context c, String name, String content, int type, int category){
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_TYPE, type);
        values.put(NoteEntry.COLUMN_NAME, name);
        values.put(NoteEntry.COLUMN_CONTENT, content);
        values.put(NoteEntry.COLUMN_CATEGORY, category);
        int id = (int)(long)db.insert(NoteEntry.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    /**
     * Updates a text note in the database.
     * @param c the current context
     * @param id the id of the note
     * @param name the new name of the note
     * @param content the new content of the note
     */
    public static void updateNote(Context c, int id, String name, String content, int category) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_NAME, name);
        values.put(NoteEntry.COLUMN_CONTENT, content);
        values.put(NoteEntry.COLUMN_CATEGORY, category);

        String selection = NoteEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        db.update(NoteEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    /**
     * Moves a note to trash
     * @param c the current context
     * @param id the id of the note
     */
    public static void trashNote(Context c, int id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_TRASH, 1);
        String selection = NoteEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        db.update(NoteEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();
        deleteNotificationsByNoteId(c, id);
    }

    /**
     * Restores a note from the trash
     * @param c the current context
     * @param id the id of the note
     */
    public static void restoreNote(Context c, int id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_TRASH, 0);
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

        //TODO delete the file for sketch and audio

        String selection = NoteEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(NoteEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
        deleteNotificationsByNoteId(c, id);
    }

    /**
     * Delete notes by specifying the category id
     * @param c the current context
     * @param cat_id the category id
     */
    public static void trashNotesByCategoryId(Context c, int cat_id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Selection arguments for all the notes belonging to that category
        String selection = NoteEntry.COLUMN_CATEGORY + " = ?";
        String[] selectionArgs = { String.valueOf(cat_id) };
        //Temporary save them
        Cursor cur = getCursorAllNotes(c, selection, selectionArgs);
        if (cur.getCount() > 0) {
            while(cur.moveToNext()) {
                trashNote(c, cur.getInt(cur.getColumnIndexOrThrow(NoteEntry.COLUMN_ID)));
            }
        }

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

        String sortOrder = NoteEntry.COLUMN_NAME + " COLLATE NOCASE ASC";

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

        String sortOrder = NoteEntry.COLUMN_NAME + " COLLATE NOCASE ASC";

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
     * Returns a cursor over all the categories in the database. Does not include the default category.
     * @param c the current context
     * @return A {@link android.database.Cursor} over all the notes
     */
    public static Cursor getCategoriesWithoutDefault(Context c){
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {CategoryEntry.COLUMN_ID, CategoryEntry.COLUMN_NAME};
        String selection = CategoryEntry.COLUMN_NAME + " != ?";
        String[] selectionArgs = { c.getString(R.string.default_category) };

        return db.query(CategoryEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                selection,                      // Columns for WHERE
                selectionArgs,                  // Values for WHERE
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

        //delete the category
        String selection = CategoryEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(CategoryEntry.TABLE_NAME, selection, selectionArgs);

        //delete the id from the notes
        ContentValues values = new ContentValues();
        values.putNull(NoteEntry.COLUMN_CATEGORY);
        String selection2 = NoteEntry.COLUMN_CATEGORY + " = ?";
        String[] selectionArgs2 = { String.valueOf(id) };
        db.update(NoteEntry.TABLE_NAME, values, selection2, selectionArgs2);

        db.close();
    }

    /**
     * Inserts a new Notification into the database
     * @param c the current context
     * @param note_id the id of the note
     * @return the rowID
     */
    public static long addNotification(Context c, int note_id, long time) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotificationEntry.COLUMN_NOTE, note_id);
        values.put(NotificationEntry.COLUMN_TIME, time);

        long rowId = db.insert(NotificationEntry.TABLE_NAME, null, values);
        db.close();
        return rowId;
    }

    /**
     * Updates the time of a notification
     * @param c the current context
     * @param id the notification id
     * @param time the new time
     */
    public static void updateNotificationTime(Context c, int id, long time) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotificationEntry.COLUMN_TIME, time);

        String selection = NotificationEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.update(NotificationEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    /**
     * Returns a specific notification
     * @param c the current context
     * @param id the id of the notification
     * @return the cursor to the notification
     */
    public static Cursor getNotification(Context c, int id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NotificationEntry.COLUMN_ID, NotificationEntry.COLUMN_NOTE, NotificationEntry.COLUMN_TIME};
        String selection = NotificationEntry.COLUMN_ID + " = ?";
        String[] selectionArgs =  { String.valueOf(id) };

        return db.query(NotificationEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                selection,                      // Columns for WHERE
                selectionArgs,                  // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }

    /**
     * Deletes a notification from the database
     * @param c the current context
     * @param id the id of the notification
     */
    public static void deleteNotification(Context c, int id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = NotificationEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(NotificationEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    /**
     * Get a notification by its note id
     * @param c the current context
     * @param note_id the note id
     * @return the cursor to the notification
     */
    public static Cursor getNotificationByNoteId(Context c, int note_id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NotificationEntry.COLUMN_ID, NotificationEntry.COLUMN_NOTE, NotificationEntry.COLUMN_TIME};
        String selection = NotificationEntry.COLUMN_NOTE + " = ?";
        String[] selectionArgs = { String.valueOf(note_id) };

        return db.query(NotificationEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                selection,                      // Columns for WHERE
                selectionArgs,                  // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }

    /**
     * Delete notifications by specifying the note id
     * @param c the current context
     * @param note_id the note id
     */
    public static void deleteNotificationsByNoteId(Context c, int note_id) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = NotificationEntry.COLUMN_NOTE + " = ?";
        String[] selectionArgs = { String.valueOf(note_id) };
        db.delete(NotificationEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    /**
     * Returns a cursor over all the notifications in the database.
     * @param c the current context
     * @return A {@link android.database.Cursor} over all the notes
     */
    public static Cursor getAllNotifications(Context c) {
        DbOpenHelper dbHelper = new DbOpenHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {NotificationEntry.COLUMN_ID, NotificationEntry.COLUMN_NOTE, NotificationEntry.COLUMN_TIME};

        return db.query(NotificationEntry.TABLE_NAME,   // Table name
                projection,                     // SELECT
                null,                           // Columns for WHERE
                null,                           // Values for WHERE
                null,                           // Group
                null,                           // Filter by Group
                null);                     // Sort Order
    }
}
