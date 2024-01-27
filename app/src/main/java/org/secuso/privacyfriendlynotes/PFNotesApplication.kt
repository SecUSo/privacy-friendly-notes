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
package org.secuso.privacyfriendlynotes

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager.backupCreator
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager.backupRestorer
import org.secuso.privacyfriendlynotes.backup.BackupCreator
import org.secuso.privacyfriendlynotes.backup.BackupRestorer
class PFNotesApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        backupCreator = BackupCreator()
        backupRestorer = BackupRestorer()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}
