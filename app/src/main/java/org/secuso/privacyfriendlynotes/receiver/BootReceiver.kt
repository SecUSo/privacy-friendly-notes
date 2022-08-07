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
package org.secuso.privacyfriendlynotes.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.secuso.privacyfriendlynotes.room.NoteDatabase
import org.secuso.privacyfriendlynotes.room.model.Notification
import org.secuso.privacyfriendlynotes.ui.helper.NotificationHelper

class BootReceiver : BroadcastReceiver() {
    var allNotifications: List<Notification>? = null
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return

        Log.d(javaClass.simpleName, "Running onReceive...")
        runBlocking {
            launch(Dispatchers.IO) {
                allNotifications = NoteDatabase.getInstance(context).notificationDao().getAllNotifications()

                Log.d(javaClass.simpleName, allNotifications!!.size.toString() + " Notifications found.")
                for ((notification_id, time) in allNotifications!!) {

                    val alarmTime = time.toLong()
                    val note = NoteDatabase.getInstance(context).noteDao().getNoteByID(notification_id.toLong())
                    note?.let {
                        NotificationHelper.addNotificationToAlarmManager(context, note._id, note.type, note.name, alarmTime)
                    }
                }
            }
        }
    }
}