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
package org.secuso.privacyfriendlynotes.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.ui.notes.AudioNoteActivity;
import org.secuso.privacyfriendlynotes.ui.notes.ChecklistNoteActivity;
import org.secuso.privacyfriendlynotes.ui.notes.SketchActivity;
import org.secuso.privacyfriendlynotes.ui.notes.TextNoteActivity;

/***
 * Service does handle the activated notifications with a PendingIntent
 * Created by Robin on 26.06.2016.
 *
 * @deprecated THIS CLASS IS ONLY FOR LEGACY REASONS. USE NotificationReceiver instead.
 */
@Deprecated(forRemoval = true)
public class NotificationService extends IntentService {
    public static final String NOTIFICATION_ID = "notification_id";
    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final String NOTIFICATION_TITLE = "notification_title";

    public static final String NOTIFICATION_CHANNEL = "Notes_Notifications";


    public NotificationService() {
        super("Notification service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int notification_id = intent.getIntExtra(NOTIFICATION_ID, -1);
        int type = intent.getIntExtra(NOTIFICATION_TYPE, -1);
        String name = intent.getStringExtra(NOTIFICATION_TITLE);
        Log.d(getClass().getSimpleName(), "onHandleIntent(" + NOTIFICATION_ID + ":" + notification_id + ";" + NOTIFICATION_TYPE + ":" + type + ";" + NOTIFICATION_TITLE + ":" + name + ")");
        if (notification_id != -1 && type != -1) {
            //Gather the info for the notification itself
            Intent i = null;
            switch (type) {
                case DbContract.NoteEntry.TYPE_TEXT:
                    i = new Intent(getBaseContext(), TextNoteActivity.class);
                    i.putExtra(TextNoteActivity.EXTRA_ID, notification_id);
                    break;
                case DbContract.NoteEntry.TYPE_AUDIO:
                    i = new Intent(getBaseContext(), AudioNoteActivity.class);
                    i.putExtra(AudioNoteActivity.EXTRA_ID, notification_id);
                    break;
                case DbContract.NoteEntry.TYPE_SKETCH:
                    i = new Intent(getBaseContext(), SketchActivity.class);
                    i.putExtra(SketchActivity.EXTRA_ID, notification_id);
                    break;
                case DbContract.NoteEntry.TYPE_CHECKLIST:
                    i = new Intent(getApplication(), ChecklistNoteActivity.class);
                    i.putExtra(ChecklistNoteActivity.EXTRA_ID, notification_id);
                    break;
            }

            Log.d(getClass().getSimpleName(), "Creating intent for " + getBaseContext() + " with type " + type + " and intent " + intent + " and id " + notification_id + " and name " + name);
            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(getString(R.string.app_name));
                mNotifyMgr.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext(), NOTIFICATION_CHANNEL);
            mBuilder.setSmallIcon(R.mipmap.ic_notification)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(name)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            mNotifyMgr.notify(notification_id, mBuilder.build());
        }
    }
}
