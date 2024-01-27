package org.secuso.privacyfriendlynotes.ui.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.receiver.NotificationReceiver

object NotificationHelper {
    private const val TAG = "NotificationHelper"

    @JvmStatic
    fun addNotificationToAlarmManager(context: Context, noteId: Int, noteType: Int, notificationTitle: String, alarmTimeMillis: Long) {
        Log.d(TAG, "Scheduling notification. ID=$noteId;Type=$noteType;Title=$notificationTitle;Time=$alarmTimeMillis")

        //Create the intent that is fired by AlarmManager
        val pi = createNotificationPendingIntent(context, noteId, noteType, notificationTitle)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // For versions < S, we do not need to check for the permission
        // For versions >= S, we need to check for the permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pi)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pi)
            }
        } else {
            // We don't have the permission to schedule exact alarms
            return
        }
    }

    @JvmStatic
    fun removeNotificationFromAlarmManager(context: Context, noteId: Int, noteType: Int, notificationTitle: String) {
        //Create the intent that would be fired by AlarmManager
        val pi = createNotificationPendingIntent(context, noteId, noteType, notificationTitle)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pi)
    }

    private fun createNotificationPendingIntent(context: Context, noteId: Int, noteType: Int, notificationTitle: String): PendingIntent {
        val i = Intent(context, NotificationReceiver::class.java)
        i.putExtra(NotificationReceiver.NOTIFICATION_ID, noteId)
        i.putExtra(NotificationReceiver.NOTIFICATION_TYPE, noteType)
        i.putExtra(NotificationReceiver.NOTIFICATION_TITLE, notificationTitle)

        return PendingIntent.getBroadcast(context, noteId, i, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @JvmStatic
    fun showAlertScheduledToast(context: Context, dayOfMonth: Int, monthOfYear: Int, year: Int, hourOfDay: Int, minute: Int) {
        Toast.makeText(
            context.applicationContext,
            String.format(
                context.getString(R.string.toast_alarm_scheduled),
                dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year + " " + hourOfDay + ":" + String.format("%02d", minute)
            ),
            Toast.LENGTH_SHORT
        ).show()
    }
}