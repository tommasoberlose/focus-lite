package com.nego.flite.Functions;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.R;
import com.nego.flite.Reminder;
import com.nego.flite.Utils;
import com.nego.flite.database.DbAdapter;

public class ReminderService extends IntentService {

    public static void startAction(Context context, String action, Reminder r) {
        Intent intent = new Intent(context, ReminderService.class);
        intent.setAction(action);
        intent.putExtra(Costants.EXTRA_REMINDER, r);
        context.startService(intent);
    }

    private void sendResponse(String s, Reminder r) {
        if (!s.equals(Costants.ACTION_UPDATE_DATE))
            Utils.notification_update(this, s, r);
    }

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Costants.ACTION_CREATE.equals(action)) {
                final Reminder r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                createReminder(r);
            } else if (Costants.ACTION_UPDATE.equals(action)) {
                final Reminder r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                updateReminder(r);
            } else if (Costants.ACTION_UPDATE_DATE.equals(action)) {
                final Reminder r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                updateReminderDate(r);
            } else if (Costants.ACTION_DELETE.equals(action)) {
                final Reminder r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                deleteReminder(r);
            }
        }
    }

    private void createReminder(Reminder r) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();
        if (r.create_reminder(this, dbHelper)) {
            sendResponse(Costants.ACTION_CREATE, r);
        }
        dbHelper.close();
    }

    private void updateReminder(Reminder r) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();
        if (r.update_reminder(this, dbHelper)) {
            sendResponse(Costants.ACTION_UPDATE, r);
        }
        dbHelper.close();
    }

    private void updateReminderDate(Reminder r) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();
        if (r.update_reminder_date(this, dbHelper)) {
            sendResponse(Costants.ACTION_UPDATE_DATE, r);
        }
        dbHelper.close();
    }

    private void deleteReminder(Reminder r) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();
        if (r.delete_reminder(this, dbHelper)) {
            sendResponse(Costants.ACTION_DELETE, r);
        }
        dbHelper.close();
    }


}
