package org.secuso.privacyfriendlynotes;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Robin on 26.06.2016.
 */
public class NotificationService extends IntentService {
    public static final String NOTE_ID = "note_id";

    public NotificationService (){
        super("Notification service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int note_id = intent.getIntExtra(NOTE_ID, -1);
        if (note_id != -1) {
            Cursor c = DbAccess.getNote(getBaseContext(), note_id);
            c.moveToFirst();
            int type = c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE));
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
            Intent i = null;
            switch (type) {
                case DbContract.NoteEntry.TYPE_TEXT:
                    i = new Intent(getBaseContext(), TextNoteActivity.class);
                    i.putExtra(TextNoteActivity.EXTRA_ID, note_id);
                    break;
                case DbContract.NoteEntry.TYPE_AUDIO:
                    //TODO start the audio note
                    break;
                case DbContract.NoteEntry.TYPE_SKETCH:
                    //TODO start the sketch note
                    break;
                case DbContract.NoteEntry.TYPE_CHECKLIST:
                    Intent i4 = new Intent(getApplication(), ChecklistNoteActivity.class);
                    i4.putExtra(ChecklistNoteActivity.EXTRA_ID, note_id);
                    break;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext());
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(name)
                    .setContentIntent(pendingIntent);
            // Sets an ID for the notification
            int mNotificationId = 001; //change that to the one from the database
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(getBaseContext().NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
            c.close();
        }
    }
}
