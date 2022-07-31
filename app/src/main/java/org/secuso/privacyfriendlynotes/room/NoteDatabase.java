/*
 This file is part of the application Privacy Friendly Notes.
 Privacy Friendly Notes is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 Privacy Friendly Notes is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Notes. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlynotes.room;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.SpannedString;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.secuso.privacyfriendlynotes.room.dao.CategoryDao;
import org.secuso.privacyfriendlynotes.room.dao.NoteDao;
import org.secuso.privacyfriendlynotes.room.dao.NotificationDao;
import org.secuso.privacyfriendlynotes.room.model.Category;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.room.model.Notification;

import java.io.File;

/**
 * The database that includes all used information like notes, notifications and categories.
 */

@Database(
        entities = {Note.class, Category.class, Notification.class},
        version = NoteDatabase.VERSION
        )
public abstract class NoteDatabase extends RoomDatabase {

    public static final int VERSION = 3;
    public static final String DATABASE_NAME = "allthenotes";
    private static NoteDatabase instance;
    public abstract NoteDao noteDao();
    public abstract CategoryDao categoryDao();
    public abstract NotificationDao notificationDao();

    public static synchronized NoteDatabase getInstance(Context context){
        return getInstance(context, DATABASE_NAME);
    }

    public static synchronized NoteDatabase getInstance(Context context, String databaseName){
        if (instance == null || !DATABASE_NAME.equals(databaseName)) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NoteDatabase.class, databaseName)
                    .addMigrations(MIGRATIONS)
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    public static synchronized NoteDatabase getInstance(Context context, String databaseName, File file){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NoteDatabase.class, databaseName)
                    .createFromFile(file)
                    .addMigrations(MIGRATIONS)
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };

    /**
     * Provides data migration from database version 1 (SQLite) to 2 (Room)
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE categories_new (_id INTEGER NOT NULL DEFAULT 0,"
                            + "name TEXT NOT NULL DEFAULT 'TEXT',"
                            + "PRIMARY KEY(_id))");
            database.execSQL("INSERT INTO categories_new(_id, name) SELECT _id, name FROM categories");
            database.execSQL("DROP TABLE categories");
            database.execSQL("ALTER TABLE categories_new RENAME TO categories");

            database.execSQL("DELETE FROM categories WHERE _id ='1'");

            database.execSQL(
                    "CREATE TABLE notes_new (_id INTEGER NOT NULL DEFAULT 0,"
                            + "in_trash INTEGER NOT NULL DEFAULT 0,"
                            + "name TEXT NOT NULL DEFAULT 'TEXT',"
                            + "type INTEGER NOT NULL DEFAULT 0,"
                            + "category INTEGER NOT NULL DEFAULT 0,"
                            + "content TEXT NOT NULL DEFAULT 'TEXT',"
                            + "PRIMARY KEY(_id))");
            database.execSQL("INSERT INTO notes_new(_id, in_trash,name,type,category,content) SELECT _id, in_trash,name,type,category,content FROM notes");
            database.execSQL("DROP TABLE notes");
            database.execSQL("ALTER TABLE notes_new RENAME TO notes");

            database.execSQL(
                    "CREATE TABLE notifications_new (_noteId INTEGER NOT NULL DEFAULT 0,"
                            + "time INTEGER NOT NULL DEFAULT 0,"
                            + "PRIMARY KEY(_noteId))");
            database.execSQL("INSERT INTO notifications_new(_noteId, time) SELECT note, time FROM notifications");
            database.execSQL("DROP TABLE notifications");
            database.execSQL("ALTER TABLE notifications_new RENAME TO notifications");

            Pair<Integer, String>[] encodedContent = null;
            Cursor c = database.query("SELECT * FROM notes WHERE type = 1");
            if(c != null) {
                if(c.moveToFirst()) {
                    encodedContent = new Pair[c.getCount()];
                    int i = 0;

                    while (!c.isAfterLast()) {
                        String note = c.getString(c.getColumnIndexOrThrow("content"));
                        String encodedNote = Html.toHtml(new SpannedString(note));

                        encodedContent[i] = new Pair<>(c.getInt(c.getColumnIndexOrThrow("_id")), encodedNote);
                        c.moveToNext();
                    }
                }
                c.close();
            }

            for(Pair<Integer,String> note : encodedContent) {
                ContentValues cv = new ContentValues();
                cv.put("content", note.second);
                database.update("notes", 0, cv, "_id = ?", new String[]{Integer.toString(note.first)});
            }
        }
    };
    static final Migration MIGRATION_1_3 = MIGRATION_1_2;
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Pair<Integer, String>[] encodedContent = null;
            Cursor c = database.query("SELECT * FROM notes WHERE type = 1");
            if(c != null) {
                if(c.moveToFirst()) {
                    encodedContent = new Pair[c.getCount()];
                    int i = 0;

                    while (!c.isAfterLast()) {
                        String note = c.getString(c.getColumnIndexOrThrow("content"));

                        if(note.startsWith("<p")) {
                            continue;
                        }

                        String encodedNote = Html.toHtml(new SpannedString(note));

                        encodedContent[i] = new Pair<>(c.getInt(c.getColumnIndexOrThrow("_id")), encodedNote);
                        c.moveToNext();
                    }
                }
                c.close();
            }

            for(Pair<Integer,String> note : encodedContent) {
                ContentValues cv = new ContentValues();
                cv.put("content", note.second);
                database.update("notes", 0, cv, "_id = ?", new String[]{Integer.toString(note.first)});
            }
        }
    };

    public static final Migration[] MIGRATIONS = {
            MIGRATION_1_2,
            MIGRATION_1_3,
            MIGRATION_2_3
    };
}
