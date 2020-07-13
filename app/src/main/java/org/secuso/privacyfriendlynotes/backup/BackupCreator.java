package org.secuso.privacyfriendlynotes.backup;

import android.content.Context;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator;

public class BackupCreator implements IBackupCreator {

    @Override
    public @NonNull String createBackup(@NonNull Context context) {
        return "";
    }
}
