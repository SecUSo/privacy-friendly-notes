package org.secuso.privacyfriendlynotes.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.JsonWriter;
import android.util.Log;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlybackup.api.backup.FileUtil;
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator;
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil;
import org.secuso.privacyfriendlybackup.api.backup.PreferenceUtil;
import org.secuso.privacyfriendlynotes.NotesApplication;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.secuso.privacyfriendlynotes.room.NoteDatabase.DATABASE_NAME;

public class BackupCreator implements IBackupCreator {

    @Override
    public void writeBackup(@NonNull Context context, @NonNull OutputStream outputStream) {
        // lock application, so no changes can be made as long as this backup is created
        // depending on the size of the application - this could take a bit
        ((NotesApplication) context.getApplicationContext()).lock();

        Log.d("PFA BackupCreator", "createBackup() started");
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, UTF_8);
        JsonWriter writer = new JsonWriter(outputStreamWriter);
        writer.setIndent("");

        try {
            writer.beginObject();
            SQLiteDatabase dataBase = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);


            Log.d("PFA BackupCreator", "Writing database");
            writer.name("database");
            DatabaseUtil.writeDatabase(writer, dataBase);
            dataBase.close();

            Log.d("PFA BackupCreator", "Writing preferences");
            writer.name("preferences");
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            PreferenceUtil.writePreferences(writer, pref);

            Log.d("PFA BackupCreator", "Writing files");
            writer.name("files");
            writer.beginObject();
            for(String path : Arrays.asList("sketches", "audio_notes")) {
                writer.name(path);
                FileUtil.writePath(writer, new File(context.getFilesDir().getPath(), path), false);
            }
            Log.d("PFA BackupCreator", "finished writing files");
            writer.endObject();

            writer.endObject();

            writer.close();
        } catch (Exception e) {
            Log.e("PFA BackupCreator", "Error occurred", e);
            e.printStackTrace();
        }

        Log.d("PFA BackupCreator", "Backup created successfully");

        ((NotesApplication) context.getApplicationContext()).release();
    }
}
