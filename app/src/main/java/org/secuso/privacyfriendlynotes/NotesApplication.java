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
