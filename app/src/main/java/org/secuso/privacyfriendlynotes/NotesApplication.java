package org.secuso.privacyfriendlynotes;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import org.secuso.privacyfriendlybackup.api.pfa.BackupManager;
import org.secuso.privacyfriendlynotes.backup.BackupCreator;
import org.secuso.privacyfriendlynotes.backup.BackupRestorer;

public class NotesApplication extends Application implements Configuration.Provider {

    @Override
    public void onCreate() {
        super.onCreate();

        BackupManager.setBackupCreator(new BackupCreator());
        BackupManager.setBackupRestorer(new BackupRestorer());
    }

    @Override
    public @NonNull Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build();
    }
}
