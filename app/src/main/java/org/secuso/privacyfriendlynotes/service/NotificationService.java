package org.secuso.privacyfriendlynotes.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import androidx.core.app.NotificationCompat;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.database.DbAccess;
import org.secuso.privacyfriendlynotes.database.DbContract;
import org.secuso.privacyfriendlynotes.ui.AudioNoteActivity;
import org.secuso.privacyfriendlynotes.ui.ChecklistNoteActivity;
import org.secuso.privacyfriendlynotes.ui.SketchActivity;
import org.secuso.privacyfriendlynotes.ui.TextNoteActivity;

/**
 * Created by Robin on 26.06.2016.
 */
public class NotificationService extends IntentService {
    public static final String NOTIFICATION_ID = "notification_id";
    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final String NOTIFICATION_TITLE = "notification_title";



    public NotificationService (){
        super("Notification service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int notification_id = intent.getIntExtra(NOTIFICATION_ID, -1);
        int type = intent.getIntExtra(NOTIFICATION_TYPE,-1);
        String name = intent.getStringExtra(NOTIFICATION_TITLE);
        if (notification_id != -1) {
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

            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext());
            mBuilder.setSmallIcon(R.mipmap.ic_notification)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(name)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

        }
    }
}
