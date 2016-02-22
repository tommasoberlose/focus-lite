package com.nego.flite.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.nego.flite.Costants;
import com.nego.flite.Functions.NotificationF;
import com.nego.flite.Reminder;
import com.nego.flite.User;
import com.nego.flite.Utils;
import com.nego.flite.database.DbAdapter;

import java.util.Calendar;


public class WiFiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.net.wifi.STATE_CHANGE") && ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).isConnected()) {

            DbAdapter dbHelper = new DbAdapter(context);
            dbHelper.open();

            WifiManager wifiM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            Cursor cursor = dbHelper.fetchRemindersByFilterWifi(Utils.getActiveUserId(dbHelper));
            while (cursor.moveToNext()) {
                Reminder actual = new Reminder(cursor);
                if (actual.getDate_archived() == 0 && actual.getDate_reminded() == 0) {
                    if (actual.getAlarm() == Costants.ALARM_TYPE_WIFI) {
                        if (actual.getAlarm_repeat().split(Costants.LIST_ITEM_SEPARATOR)[0].equals("" + wifiM.getConnectionInfo().getNetworkId()) && actual.getDate_reminded() == 0) {
                            NotificationF.Notification(context, actual);
                        }
                    }
                }
            }
            cursor.close();
            dbHelper.close();
        }
    }
}