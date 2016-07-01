package org.secuso.privacyfriendlynotes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Cursor c = DbAccess.getAllNotifications(context);

        while (c.moveToNext()) {
            int notification_id = c.getInt(c.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_ID));
            long alarmTime = c.getLong(c.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_TIME));
            //Create the intent that is fired by AlarmManager
            Intent i = new Intent(context, NotificationService.class);
            i.putExtra(NotificationService.NOTIFICATION_ID, notification_id);

            PendingIntent pi = PendingIntent.getService(context, notification_id, i, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            }
        }
    }
}
