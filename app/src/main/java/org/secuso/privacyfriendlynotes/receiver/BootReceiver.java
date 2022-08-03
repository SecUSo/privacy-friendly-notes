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
import org.secuso.privacyfriendlynotes.room.NoteDatabase;
import org.secuso.privacyfriendlynotes.ui.notes.CreateEditNoteViewModel;
import org.secuso.privacyfriendlynotes.room.model.Notification;
import org.secuso.privacyfriendlynotes.service.NotificationService;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    List<Notification> allNotifications;

    public BootReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //CreateEditNoteViewModel createEditNoteViewModel = new ViewModelProvider(context.getApplicationContext()).get(CreateEditNoteViewModel.class);
        //ArrayAdapter<CharSequence> adapter = new ArrayAdapter(context, R.layout.simple_spinner_item);

        allNotifications = NoteDatabase.getInstance(context).notificationDao().getAllNotifications();

//        createEditNoteViewModel.getAllNotifications().observe((LifecycleOwner) context, new Observer<List<Notification>>() {
//            @Override
//            public void onChanged(List<Notification> notifications) {
//                allNotifications = notifications;
//            }
//        });


        for(Notification currentNot: allNotifications){

            int notification_id = currentNot.get_noteId();
            long alarmTime = currentNot.getTime();
            //Create the intent that is fired by AlarmManager
            Intent i = new Intent(context, NotificationService.class);
            i.putExtra(NotificationService.NOTIFICATION_ID, notification_id);

            PendingIntent pi = PendingIntent.getService(context, notification_id, i, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            }
        }
    }
}
