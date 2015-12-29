package com.nego.flite.Receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.nego.flite.Costants;
import com.nego.flite.Functions.NotificationF;
import com.nego.flite.Reminder;
import com.nego.flite.database.DbAdapter;

import java.util.Calendar;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")) {

            DbAdapter dbHelper = new DbAdapter(context);
            dbHelper.open();

            Cursor cursor = dbHelper.fetchRemindersByFilterBluetooth();
            while (cursor.moveToNext()) {
                Reminder actual = new Reminder(cursor);
                if (actual.getAlarm() == Costants.ALARM_TYPE_BLUETOOTH) {
                    if (actual.getAlarm_repeat().split(Costants.LIST_ITEM_SEPARATOR)[0].equals(((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getAddress())) {
                        NotificationF.Notification(context, actual);
                    }
                }
            }
            cursor.close();

            dbHelper.close();
        }
    }
}