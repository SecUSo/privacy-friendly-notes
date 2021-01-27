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

    public NotificationService (){
        super("Notification service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int notification_id = intent.getIntExtra(NOTIFICATION_ID, -1);
        if (notification_id != -1) {
            //get the cursor on the notification
            Cursor cNotification = DbAccess.getNotification(getBaseContext(), notification_id);
            cNotification.moveToFirst();
            //get all the necessary attributes
            int note_id = cNotification.getInt(cNotification.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_NOTE));

            //get the corresponding note
            Cursor cNote = DbAccess.getNote(getBaseContext(), note_id);
            cNote.moveToFirst();

            //Gather the info for the notification itself
            int type = cNote.getInt(cNote.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE));
            String name = cNote.getString(cNote.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
            Intent i = null;
            switch (type) {
                case DbContract.NoteEntry.TYPE_TEXT:
                    i = new Intent(getBaseContext(), TextNoteActivity.class);
                    i.putExtra(TextNoteActivity.EXTRA_ID, note_id);
                    break;
                case DbContract.NoteEntry.TYPE_AUDIO:
                    i = new Intent(getBaseContext(), AudioNoteActivity.class);
                    i.putExtra(AudioNoteActivity.EXTRA_ID, note_id);
                    break;
                case DbContract.NoteEntry.TYPE_SKETCH:
                    i = new Intent(getBaseContext(), SketchActivity.class);
                    i.putExtra(SketchActivity.EXTRA_ID, note_id);
                    break;
                case DbContract.NoteEntry.TYPE_CHECKLIST:
                    i = new Intent(getApplication(), ChecklistNoteActivity.class);
                    i.putExtra(ChecklistNoteActivity.EXTRA_ID, note_id);
                    break;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext());
            mBuilder.setSmallIcon(R.mipmap.ic_notification)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(name)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            // Sets an ID for the notification
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(getBaseContext().NOTIFICATION_SERVICE);
            mNotifyMgr.notify(notification_id, mBuilder.build());
            cNote.close();
            cNotification.close();
            //Delete the database entry
            DbAccess.deleteNotification(getBaseContext(), notification_id);
        }
    }
}
