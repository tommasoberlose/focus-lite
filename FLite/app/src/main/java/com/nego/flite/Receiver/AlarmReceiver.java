package com.nego.flite.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nego.flite.Costants;
import com.nego.flite.Functions.NotificationF;
import com.nego.flite.Reminder;
import com.nego.flite.Utils;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Costants.ALARM_ACTION)) {
            try {
                Reminder r = Utils.getReminder(context, Integer.parseInt(intent.getStringExtra(Costants.EXTRA_REMINDER_ID)));
                NotificationF.CancelNotification(context, "" + r.getId());
                NotificationF.Notification(context, r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}