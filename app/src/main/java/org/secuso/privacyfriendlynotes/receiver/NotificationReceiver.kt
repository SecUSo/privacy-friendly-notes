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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.ui.notes.AudioNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.BaseNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.ChecklistNoteActivity
import org.secuso.privacyfriendlynotes.ui.notes.SketchActivity
import org.secuso.privacyfriendlynotes.ui.notes.TextNoteActivity

/**
 * This receiver is responsible to create and show notifications to the user.
 * As a receiver, this will run even if the app is closed if scheduled with AlarmManager.
 * Originally created by Robin on 26.06.2016 as NotificationService.java.
 * Migrated by Patrick on 27.01.2024.
 *
 * @author Robin
 * @author Patrick Schneider
 */
class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val NOTIFICATION_TYPE = "notification_type"
        const val NOTIFICATION_TITLE = "notification_title"
        const val NOTIFICATION_CHANNEL = "Notes_Notifications"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notification = intent.getIntExtra(NOTIFICATION_ID, -1)
        val type = intent.getIntExtra(NOTIFICATION_TYPE, -1)
        val name = intent.getStringExtra(NOTIFICATION_TITLE)
        Log.d(
            javaClass.simpleName,
            "onHandleIntent($NOTIFICATION_ID:$notification;$NOTIFICATION_TYPE:$type;$NOTIFICATION_TITLE:$name)"
        )
        if (notification != -1 && type != -1) {
            Log.d(javaClass.simpleName, "Creating intent for $context with type $type and intent $intent and id $notification and name $name")
            val pendingIntent = Intent(
                context, when (type) {
                    DbContract.NoteEntry.TYPE_TEXT -> TextNoteActivity::class.java
                    DbContract.NoteEntry.TYPE_AUDIO -> AudioNoteActivity::class.java
                    DbContract.NoteEntry.TYPE_SKETCH -> SketchActivity::class.java
                    DbContract.NoteEntry.TYPE_CHECKLIST -> ChecklistNoteActivity::class.java
                    else -> throw IllegalStateException("Note with type $type does not exist!")
                }
            ).apply {
                putExtra(BaseNoteActivity.EXTRA_ID, notification)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }.let {
                PendingIntent.getActivity(context, notification, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }
            val mNotifyMgr = ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = context.getString(R.string.app_name)
                mNotifyMgr.createNotificationChannel(notificationChannel)
            }
            val mBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            mBuilder.setSmallIcon(R.mipmap.ic_notification)
                .setColor(context.resources.getColor(R.color.colorPrimary))
                .setContentTitle(name)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            mNotifyMgr.notify(notification, mBuilder.build())
        }
    }

}