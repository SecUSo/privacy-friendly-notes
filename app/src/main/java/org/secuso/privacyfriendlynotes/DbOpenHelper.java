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
            "CREATE TABLE " + NotesContract.NoteEntry.TABLE_NAME + " (" +
                    NotesContract.NoteEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NotesContract.NoteEntry.COLUMN_NAME_NAME + " TEXT NOT NULL, " +
                    NotesContract.NoteEntry.COLUMN_NAME_CONTENT + " TEXT NOT NULL);";

    DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NOTES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + NotesContract.NoteEntry.TABLE_NAME + ";");
        onCreate(db);
    }
}
