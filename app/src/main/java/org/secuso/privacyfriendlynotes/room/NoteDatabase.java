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

import android.annotation.SuppressLint;
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
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;

import org.secuso.privacyfriendlynotes.room.dao.CategoryDao;
import org.secuso.privacyfriendlynotes.room.dao.NoteDao;
import org.secuso.privacyfriendlynotes.room.dao.NotificationDao;
import org.secuso.privacyfriendlynotes.room.model.Category;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.room.model.Notification;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The database that includes all used information like notes, notifications and categories.
 */

@Database(
        entities = {Note.class, Category.class, Notification.class},
        version = NoteDatabase.VERSION
)
public abstract class NoteDatabase extends RoomDatabase {

    public static final int VERSION = 7;
    public static final String DATABASE_NAME = "allthenotes";

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN readonly INTEGER NOT NULL DEFAULT 0;");
        }
    };
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE notes_new (_id INTEGER NOT NULL DEFAULT 0,"
                            + "in_trash INTEGER NOT NULL DEFAULT 0,"
                            + "name TEXT NOT NULL DEFAULT 'TEXT',"
                            + "type INTEGER NOT NULL DEFAULT 0,"
                            + "category INTEGER NOT NULL DEFAULT 0,"
                            + "content TEXT NOT NULL DEFAULT 'TEXT',"
                            + "last_modified INTEGER NOT NULL DEFAULT(unixepoch('subsec') * 1000),"
                            + "custom_order INTEGER NOT NULL DEFAULT 0,"
                            + "PRIMARY KEY(_id));");
            // Prepare INSERT statement
            SupportSQLiteStatement stmt = database.compileStatement(
                    "INSERT INTO notes_new(_id, in_trash, name, type, category, content, last_modified, custom_order)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
            );
            // Previous date format: Calendar.getInstance().time.toString() -> "Thu Jul 08 12:34:56 UTC 2023"
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault());
            try (Cursor cursor = database.query(new SimpleSQLiteQuery("SELECT * FROM notes;"))) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String lastModified = cursor.getString(cursor.getColumnIndex("last_modified"));
                    long lastModifiedMillis;
                    try {
                        Date parsed = sdf.parse(lastModified);
                        lastModifiedMillis = parsed.getTime();
                    } catch (ParseException | NullPointerException e) {
                        try {
                            lastModifiedMillis = Date.parse(lastModified);
                        } catch (IllegalArgumentException iae) {
                            lastModifiedMillis = 0L;
                        }
                    }
                    @SuppressLint("Range") int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    @SuppressLint("Range") int in_trash = cursor.getInt(cursor.getColumnIndex("in_trash"));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                    @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex("type"));
                    @SuppressLint("Range") int category = cursor.getInt(cursor.getColumnIndex("category"));
                    @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex("content"));
                    @SuppressLint("Range") int custom_order = cursor.getInt(cursor.getColumnIndex("custom_order"));
                    stmt.bindLong(1, _id);
                    stmt.bindLong(2, in_trash);
                    stmt.bindString(3, name);
                    stmt.bindLong(4, type);
                    stmt.bindLong(5, category);
                    stmt.bindString(6, content);
                    stmt.bindLong(7, lastModifiedMillis);
                    stmt.bindLong(8, custom_order);

                    // Execute insert
                    stmt.executeInsert();

                    // Clear bindings to be ready for next row
                    stmt.clearBindings();
                }
            }
            database.execSQL("DROP TABLE notes;");
            database.execSQL("ALTER TABLE notes_new RENAME TO notes;");

            database.execSQL(
                    "CREATE TRIGGER [UpdateLastModified] AFTER UPDATE ON notes FOR EACH ROW " +
                            "WHEN NEW.last_modified = OLD.last_modified AND NEW.custom_order = OLD.custom_order AND NEW.in_trash = OLD.in_trash " +
                            "BEGIN " +
                            "UPDATE notes SET last_modified = DateTime('now') WHERE _id=NEW._id; " +
                            "END;"
            );
            database.execSQL(
                    "CREATE TRIGGER [InsertCustomOrder] AFTER INSERT ON notes FOR EACH ROW " +
                            "BEGIN " +
                            "UPDATE notes SET custom_order = _id WHERE _id=NEW._id; " +
                            "END;"
            );
            // This trigger ensures that a custom_order cannot be updated to an invalid value <= 0 and defers to the old value or the id to ensure valid custom_orders.
            database.execSQL(
                    "CREATE TRIGGER [UpdateCustomOrder] AFTER UPDATE OF custom_order ON notes FOR EACH ROW " +
                            "WHEN NEW.custom_order <= 0 " +
                            "BEGIN " +
                            "UPDATE notes SET custom_order = (CASE WHEN OLD.custom_order <= 0 THEN OLD._id ELSE OLD.custom_order END) WHERE _id=NEW._id; " +
                            "END;"
            );
        }
    };
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Adds new color field
            database.execSQL("ALTER TABLE categories ADD COLUMN color TEXT");

            // Adds new fields to sort by
            database.execSQL(
                    "CREATE TABLE notes_new (_id INTEGER NOT NULL DEFAULT 0,"
                            + "in_trash INTEGER NOT NULL DEFAULT 0,"
                            + "name TEXT NOT NULL DEFAULT 'TEXT',"
                            + "type INTEGER NOT NULL DEFAULT 0,"
                            + "category INTEGER NOT NULL DEFAULT 0,"
                            + "content TEXT NOT NULL DEFAULT 'TEXT',"
                            + "last_modified TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                            + "custom_order INTEGER NOT NULL DEFAULT 0,"
                            + "PRIMARY KEY(_id));");
            database.execSQL("INSERT INTO notes_new(_id, in_trash,name,type,category,content,custom_order) SELECT _id, in_trash,name,type,category,content,_id as custom_order FROM notes ORDER BY _id ASC;");
            database.execSQL("DROP TABLE notes;");
            database.execSQL("ALTER TABLE notes_new RENAME TO notes");
            database.execSQL(
                    "CREATE TRIGGER [UpdateLastModified] AFTER UPDATE ON notes FOR EACH ROW " +
                            "WHEN NEW.last_modified = OLD.last_modified AND NEW.custom_order = OLD.custom_order AND NEW.in_trash = OLD.in_trash " +
                            "BEGIN " +
                            "UPDATE notes SET last_modified = DateTime('now') WHERE _id=NEW._id; " +
                            "END;"
            );
            database.execSQL(
                    "CREATE TRIGGER [InsertCustomOrder] AFTER INSERT ON notes FOR EACH ROW " +
                            "BEGIN " +
                            "UPDATE notes SET custom_order = _id WHERE _id=NEW._id; " +
                            "END;"
            );
            // This trigger ensures that a custom_order cannot be updated to an invalid value <= 0 and defers to the old value or the id to ensure valid custom_orders.
            database.execSQL(
                    "CREATE TRIGGER [UpdateCustomOrder] AFTER UPDATE OF custom_order ON notes FOR EACH ROW " +
                            "WHEN NEW.custom_order <= 0 " +
                            "BEGIN " +
                            "UPDATE notes SET custom_order = (CASE WHEN OLD.custom_order <= 0 THEN OLD._id ELSE OLD.custom_order END) WHERE _id=NEW._id; " +
                            "END;"
            );
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
            database.execSQL("INSERT INTO notes_new(_id, in_trash,name,type,category,content) SELECT _id, in_trash,name,type,category,content FROM notes WHERE category IS NOT null");
            database.execSQL("INSERT INTO notes_new(_id, in_trash,name,type,category,content) SELECT _id, in_trash,name,type,0,content FROM notes WHERE category IS null");
            database.execSQL("DROP TABLE notes");
            database.execSQL("ALTER TABLE notes_new RENAME TO notes");

            database.execSQL(
                    "CREATE TABLE notifications_new (_noteId INTEGER NOT NULL DEFAULT 0,"
                            + "time INTEGER NOT NULL DEFAULT 0,"
                            + "PRIMARY KEY(_noteId))");
            database.execSQL("INSERT INTO notifications_new(_noteId, time) SELECT note, time FROM notifications");
            database.execSQL("DROP TABLE notifications");
            database.execSQL("ALTER TABLE notifications_new RENAME TO notifications");

            Pair<Integer, String>[] encodedContent = new Pair[0];
            Cursor c = database.query("SELECT * FROM notes WHERE type = 1");
            if (c != null) {
                if (c.moveToFirst()) {
                    encodedContent = new Pair[c.getCount()];
                    int i = 0;

                    while (!c.isAfterLast()) {
                        String note = c.getString(c.getColumnIndexOrThrow("content"));
                        String encodedNote = Html.toHtml(new SpannedString(note));

                        encodedContent[i++] = new Pair<>(c.getInt(c.getColumnIndexOrThrow("_id")), encodedNote);
                        c.moveToNext();
                    }
                }
                c.close();
            }

            for (Pair<Integer, String> note : encodedContent) {
                ContentValues cv = new ContentValues();
                cv.put("content", note.second);
                database.update("notes", 0, cv, "_id = ?", new String[]{Integer.toString(note.first)});
            }
        }
    };
    /**
     * Provides data migration from database version 3 to 4 which checks for an error in the previous
     * migration when a backup was imported
     */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // get current schema and check if it needs to be fixed
            String result = "";
            Cursor c = database.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='notes';");
            if (c != null) {
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        result = c.getString(c.getColumnIndexOrThrow("sql"));
                        c.moveToNext();
                    }
                }
                c.close();
            }

            String categorySQL = result.split("category")[1].split(",")[0];

            if (categorySQL != null && categorySQL.toUpperCase().contains("INTEGER") && !categorySQL.toUpperCase().contains("NOT NULL")) {
                MIGRATION_1_2.migrate(database);
            }
        }
    };
    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            MIGRATION_1_2.migrate(database);
        }
    };
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Pair<Integer, String>[] encodedContent = new Pair[0];
            Cursor c = database.query("SELECT * FROM notes WHERE type = 1");
            if (c != null) {
                if (c.moveToFirst()) {
                    encodedContent = new Pair[c.getCount()];
                    int i = 0;

                    while (!c.isAfterLast()) {
                        String note = c.getString(c.getColumnIndexOrThrow("content"));

                        if (note.startsWith("<p dir=")) {
                            i++;
                            c.moveToNext();
                            continue;
                        }

                        String encodedNote = Html.toHtml(new SpannedString(note));

                        encodedContent[i++] = new Pair<>(c.getInt(c.getColumnIndexOrThrow("_id")), encodedNote);
                        c.moveToNext();
                    }
                }
                c.close();
            }

            for (Pair<Integer, String> note : encodedContent) {
                if (note == null) {
                    continue;
                }
                ContentValues cv = new ContentValues();
                cv.put("content", note.second);
                database.update("notes", 0, cv, "_id = ?", new String[]{Integer.toString(note.first)});
            }
        }
    };
    public static final Migration[] MIGRATIONS = {
            MIGRATION_1_2,
            MIGRATION_1_3,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
    };
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            // Adds a trigger to auto-set custom_order to _id
            // Room currently supports no DEFAULT = COLUMN or @Trigger Annotation
            db.execSQL(
                    "CREATE TRIGGER [InsertCustomOrder] AFTER INSERT ON notes FOR EACH ROW " +
                            "BEGIN " +
                            "UPDATE notes SET custom_order = _id WHERE _id=NEW._id; " +
                            "END;"
            );
            super.onCreate(db);
        }
    };
    private static NoteDatabase instance;

    public static synchronized NoteDatabase getInstance(Context context) {
        return getInstance(context, DATABASE_NAME);
    }

    public static synchronized NoteDatabase getInstance(Context context, String databaseName) {
        if (instance == null || !DATABASE_NAME.equals(databaseName)) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            NoteDatabase.class, databaseName)
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATIONS)
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    public static synchronized NoteDatabase getInstance(Context context, String databaseName, File file) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            NoteDatabase.class, databaseName)
                    .createFromFile(file)
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATIONS)
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    public abstract NoteDao noteDao();

    public abstract CategoryDao categoryDao();

    public abstract NotificationDao notificationDao();
}
