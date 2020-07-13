package org.secuso.privacyfriendlynotes.backup;

import android.content.Context;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer;

public class BackupRestorer implements IBackupRestorer {

    @Override
    public boolean restoreBackup(@NonNull Context context, @NonNull String restoreData) {
        return false;
    }
}
