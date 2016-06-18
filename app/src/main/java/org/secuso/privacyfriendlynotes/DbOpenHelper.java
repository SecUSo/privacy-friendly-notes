package org.secuso.privacyfriendlynotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Robin on 11.06.2016.
 */
public class DbOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "allthenotes";

    private static final String NOTES_TABLE_CREATE =
            "CREATE TABLE " + DbContract.NoteEntry.TABLE_NAME + " (" +
                    DbContract.NoteEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DbContract.NoteEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                    DbContract.NoteEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    DbContract.NoteEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                    DbContract.NoteEntry.COLUMN_CATEGORY + " INTEGER);";

    private static final String CATEGORIES_TABLE_CREATE =
            "CREATE TABLE " + DbContract.CategoryEntry.TABLE_NAME + " (" +
                    DbContract.CategoryEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    DbContract.CategoryEntry.COLUMN_NAME + " TEXT NOT NULL UNIQUE);";

    DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NOTES_TABLE_CREATE);
        db.execSQL(CATEGORIES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + DbContract.NoteEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS" + DbContract.CategoryEntry.TABLE_NAME + ";");
        onCreate(db);
    }
}
