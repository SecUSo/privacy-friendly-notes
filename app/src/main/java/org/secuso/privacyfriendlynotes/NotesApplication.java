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
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.compose.runtime.State;
import androidx.work.Configuration;

import org.secuso.privacyfriendlybackup.api.pfa.BackupManager;
import org.secuso.privacyfriendlycore.model.PFApplication;
import org.secuso.privacyfriendlycore.ui.AboutData;
import org.secuso.privacyfriendlycore.ui.help.Help;
import org.secuso.privacyfriendlycore.ui.settings.ISettings;
import org.secuso.privacyfriendlycore.ui.settings.SettingData;
import org.secuso.privacyfriendlycore.ui.settings.Settings;
import org.secuso.privacyfriendlynotes.backup.BackupCreator;
import org.secuso.privacyfriendlynotes.backup.BackupRestorer;
import org.secuso.privacyfriendlynotes.model.PFHelp;
import org.secuso.privacyfriendlynotes.model.PFNoteSettings;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotesApplication extends PFApplication implements Configuration.Provider {

    @NonNull
    @Override
    public AboutData getAbout() {
        return new AboutData(
            getString(R.string.app_name_long),
            BuildConfig.VERSION_NAME,
            getString(R.string.about_author_names),
            "https://github.com/SecUSo/privacy-friendly-notes"
        );
    }

    @NonNull
    @Override
    public Help getHelp() {
        return PFHelp.Companion.instance(this).getHelp();
    }

    @NonNull
    @Override
    public ISettings getSettings() {
        return PFNoteSettings.Companion.instance(this).getSettings();
    }

    @NonNull
    @Override
    public String getApplicationName() {
        return getString(R.string.app_name);
    }

    @Override
    public boolean getLightMode() {
        return PFNoteSettings.Companion.instance(this).getLightMode();
    }

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
