package org.secuso.privacyfriendlynotes.ui.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.service.NotificationService

object NotificationHelper {
    private val TAG = "NotificationHelper"

    @JvmStatic
    fun addNotificationToAlarmManager(context: Context, note_id: Int, noteType: Int, notificationTitle: String, alarmTimeMillis: Long) {
        Log.d(TAG, "Scheduling notification. ID=$note_id;Type=$noteType;Title=$notificationTitle;Time=$alarmTimeMillis")

        //Create the intent that is fired by AlarmManager
        val pi = createNotificationPendingIntent(context, note_id, noteType, notificationTitle)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pi)
    }

    @JvmStatic
    fun removeNotificationFromAlarmManager(context: Context, note_id: Int, noteType: Int, notificationTitle: String) {
        //Create the intent that would be fired by AlarmManager
        val pi = createNotificationPendingIntent(context, note_id, noteType, notificationTitle)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pi)
    }

    private fun createNotificationPendingIntent(context: Context, note_id: Int, noteType: Int, notificationTitle: String): PendingIntent {
        val i = Intent(context, NotificationService::class.java)
        i.putExtra(NotificationService.NOTIFICATION_ID, note_id)
        i.putExtra(NotificationService.NOTIFICATION_TYPE, noteType)
        i.putExtra(NotificationService.NOTIFICATION_TITLE, notificationTitle)

        val pi = PendingIntent.getService(context, note_id, i, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return pi
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