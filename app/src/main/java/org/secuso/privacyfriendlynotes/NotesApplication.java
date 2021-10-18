package org.secuso.privacyfriendlynotes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import org.secuso.privacyfriendlybackup.api.pfa.BackupManager;
import org.secuso.privacyfriendlynotes.backup.BackupCreator;
import org.secuso.privacyfriendlynotes.backup.BackupRestorer;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotesApplication extends Application implements Configuration.Provider {

    @Override
    public void onCreate() {
        super.onCreate();

        BackupManager.setBackupCreator(new BackupCreator());
        BackupManager.setBackupRestorer(new BackupRestorer());
    }

    // just a comment

    @Override
    public @NonNull Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build();
    }

    private AtomicBoolean lock = new AtomicBoolean(false);

    public void lock() {
        lock.set(true);
        showAlertDialog(shownView.get());
    }

    public void release() {
        lock.set(false);
    }

    private @NonNull WeakReference<Activity> shownView = new WeakReference<>(null);
    public void register(@NonNull Activity obs) {
        shownView = new WeakReference<>(obs);
        if(lock.get()) {
            showAlertDialog(obs);
        }
    }
    public void unregister() {
        shownView = new WeakReference<>(null);
    }

    private void showAlertDialog(Context context) {
        //AlertDialog.Builder builder = new AlertDialog.Builder(context);

    }

}
