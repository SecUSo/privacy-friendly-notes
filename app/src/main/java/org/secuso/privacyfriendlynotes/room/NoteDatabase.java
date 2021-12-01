package org.secuso.privacyfriendlynotes.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {Note.class,Category.class,Notification.class},
        version = 2
//        ,autoMigrations = { @AutoMigration(
//                from = 1,
//                to = 2
//        )}
        )
public abstract class NoteDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "allthenotes";
    private static NoteDatabase instance;
    public abstract NoteDao noteDao();
    public abstract CategoryDao categoryDao();
    public abstract NotificationDao notificationDao();

    public static synchronized NoteDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NoteDatabase.class, "allthenotes")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
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

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE notifications_new (_noteId INTEGER NOT NULL,"
                            + "time INTEGER,"
                            + "PRIMARY KEY(_noteId))");
            database.execSQL("INSERT INTO notifications_new(_noteId, time)"
                + "SELECT note, time FROM notifications");
            database.execSQL("DROP TABLE notifications");
            database.execSQL("ALTER TABLE notifications_new RENAME TO notifications");
        }
    };


}
