package com.nego.flite.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.nego.flite.Costants;
import com.nego.flite.PasswordCheck;
import com.nego.flite.Utils;

public class HideNotesReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Costants.ACTION_HIDE_ALL)) {
            SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = SP.edit();
            editor.putBoolean(Costants.PREFERENCES_VIEW_ALL, false);
            editor.apply();
            Utils.notification_add_update(context);
        } else if (intent.getAction().equals(Costants.ACTION_VIEW_ALL)) {
            final SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
            if (SP.getString(Costants.PREFERENCE_PASSWORD, "").equals("")) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(Costants.PREFERENCES_VIEW_ALL, true);
                editor.apply();
                Utils.notification_add_update(context);
            } else {
                Intent i = new Intent(context, PasswordCheck.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setAction(Costants.ACTION_VIEW_ALL);
                context.startActivity(i);
            }
        }
    }
}