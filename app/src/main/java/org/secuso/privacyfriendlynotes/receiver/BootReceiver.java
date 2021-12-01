package org.secuso.privacyfriendlynotes.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ArrayAdapter;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.ui.EditNoteViewModel;
import org.secuso.privacyfriendlynotes.room.Notification;
import org.secuso.privacyfriendlynotes.service.NotificationService;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    List<Notification> allNotifications;
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        EditNoteViewModel editNoteViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(EditNoteViewModel.class);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(context, R.layout.simple_spinner_item);

        editNoteViewModel.getAllNotifications().observe((LifecycleOwner) context, new Observer<List<Notification>>() {
            @Override
            public void onChanged(List<Notification> notifications) {
                allNotifications = notifications;
            }
        });


        for(Notification currentNot: allNotifications){

            int notification_id = currentNot.get_noteId();
            long alarmTime = currentNot.getTime();
            //Create the intent that is fired by AlarmManager
            Intent i = new Intent(context, NotificationService.class);
            i.putExtra(NotificationService.NOTIFICATION_ID, notification_id);

            PendingIntent pi = PendingIntent.getService(context, notification_id, i, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            }
        }
    }
}
