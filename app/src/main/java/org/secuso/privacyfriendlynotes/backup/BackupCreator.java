package org.secuso.privacyfriendlynotes.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.JsonWriter;
import android.util.Log;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator;
import org.secuso.privacyfriendlybackup.api.util.DatabaseUtil;
import org.secuso.privacyfriendlybackup.api.util.PreferenceUtil;

import java.io.IOException;
import java.io.StringWriter;

import static org.secuso.privacyfriendlynotes.database.DbOpenHelper.DATABASE_NAME;

public class BackupCreator implements IBackupCreator {

    @Override
    public @NonNull String createBackup(@NonNull Context context) {
        Log.d("PFA BackupCreator", "createBackup() started");
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");

        try {
            writer.beginObject();
            SQLiteDatabase dataBase = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Log.d("PFA BackupCreator", "Writing database");
            writer.name("database");
            DatabaseUtil.writeDatabase(writer, dataBase);

            Log.d("PFA BackupCreator", "Writing preferences");
            writer.name("preferences");
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            PreferenceUtil.writePreferences(writer, pref);

            writer.endObject();

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = stringWriter.toString();

        Log.d("PFA BackupCreator", result);
        return result;
    }
}
