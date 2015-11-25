package com.nego.flite;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nego.flite.Functions.AlarmF;
import com.nego.flite.Functions.NotificationF;
import com.nego.flite.Functions.ReminderService;
import com.nego.flite.Widget.FocusWidget;
import com.nego.flite.database.DbAdapter;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Utils {

    public static boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    public static void oldReminder(Context context) {

        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (SP.getBoolean(Costants.PREFERENCES_VIEW_ALL, true)) {

            Cursor c = dbHelper.fetchAllReminders();
            while (c.moveToNext()) {
                NotificationF.NotificationFixed(context, new Reminder(c));
            }
            c.close();
        }

        Cursor a = dbHelper.fetchAllAlarm();
        while (a.moveToNext()) {
            Reminder reminder = new Reminder(a);
            if (!isOldDate(reminder.getAlarm()))
                AlarmF.updateAlarm(context, reminder.getId(), reminder.getAlarm(), reminder.getAlarm_repeat());
        }
        a.close();

        dbHelper.close();
    }


    public static int itemsToDo(Context context) {
        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();
        int count = dbHelper.getRemindersN();
        dbHelper.close();
        return count;
    }

    public static void SnackbarC(final Context context, String title, final View view) {

        Snackbar.make(view, title, Snackbar.LENGTH_LONG)
                .show();

    }

    public static void notification_add_update(Context context) {
        NotificationF.CancelAllNotification(context);

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (SP.getBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, true)) {
            NotificationF.NotificationAdd(context);
            oldReminder(context);
        } else {
            oldReminder(context);
            NotificationF.NotificationAdd(context);
        }
        updateWidget(context);
    }

    public static void notification_update(Context context, String action, Reminder r) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        NotificationF.CancelNotification(context, "" + r.getId());
        AlarmF.updateAlarm(context, r.getId(), r.getAlarm(), r.getAlarm_repeat());
        updateWidget(context);

        if (!action.equals(Costants.ACTION_DELETE)) {
            if (SP.getBoolean(Costants.PREFERENCES_VIEW_ALL, true)) {
                NotificationF.NotificationFixed(context, r);
            }
        }
    }

    public static String getDate(Context context, long date) {
        Calendar today = Calendar.getInstance();
        Calendar byR = Calendar.getInstance();
        byR.setTimeInMillis(date);
        SimpleDateFormat HM = new SimpleDateFormat("HH:mm");
        SimpleDateFormat DM = new SimpleDateFormat("MMM d, HH:mm");
        SimpleDateFormat MY = new SimpleDateFormat("MMM d y, HH:mm ");
        if (today.get(Calendar.YEAR) == byR.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == byR.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == byR.get(Calendar.DAY_OF_MONTH)) {
            return context.getString(R.string.created_at) + " " + HM.format(new Date(byR.getTimeInMillis()));
        } else if (today.get(Calendar.YEAR) == byR.get(Calendar.YEAR)) {
            return context.getString(R.string.created_on) + " " + DM.format(new Date(byR.getTimeInMillis()));
        } else {
            return context.getString(R.string.created_on) + " " + MY.format(new Date(byR.getTimeInMillis()));
        }
    }

    public static String getDateAlarm(Context context, long date) {
        Calendar today = Calendar.getInstance();
        Calendar byR = Calendar.getInstance();
        byR.setTimeInMillis(date);
        SimpleDateFormat HM = new SimpleDateFormat("HH:mm");
        SimpleDateFormat DM = new SimpleDateFormat("MMM d, HH:mm");
        SimpleDateFormat MY = new SimpleDateFormat("MMM d y, HH:mm ");
        if (date > today.getTimeInMillis()) {
            if (today.get(Calendar.YEAR) == byR.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == byR.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == byR.get(Calendar.DAY_OF_MONTH)) {
                return context.getString(R.string.text_snoozed_to_hour) + " " + HM.format(new Date(byR.getTimeInMillis()));
            } else if (today.get(Calendar.YEAR) == byR.get(Calendar.YEAR)) {
                return context.getString(R.string.text_snoozed_to) + " " + DM.format(new Date(byR.getTimeInMillis()));
            } else {
                return context.getString(R.string.text_snoozed_to) + " " + MY.format(new Date(byR.getTimeInMillis()));
            }
        } else {
            if (today.get(Calendar.YEAR) == byR.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == byR.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == byR.get(Calendar.DAY_OF_MONTH)) {
                return context.getString(R.string.text_reminded_at) + " " + HM.format(new Date(byR.getTimeInMillis()));
            } else if (today.get(Calendar.YEAR) == byR.get(Calendar.YEAR)) {
                return context.getString(R.string.text_reminded_on) + " " + DM.format(new Date(byR.getTimeInMillis()));
            } else {
                return context.getString(R.string.text_reminded_on) + " " + MY.format(new Date(byR.getTimeInMillis()));
            }
        }
    }

    public static String getDay(Context context, long date) {
        Calendar byR = Calendar.getInstance();
        byR.setTimeInMillis(date);
        if (date == 0)
            return context.getString(R.string.text_never);
        else if (isToday(date))
            return context.getString(R.string.text_today);
        else if (isTomorrow(date))
            return context.getString(R.string.text_tomorrow);
        else if (isYesterday(date))
            return  context.getString(R.string.text_yesterday);
        else {
            SimpleDateFormat MY = new SimpleDateFormat("d/MM/y");
            return MY.format(new Date(byR.getTimeInMillis()));
        }
    }

    public static String getTime(Context context, long date) {
        Calendar byR = Calendar.getInstance();
        byR.setTimeInMillis(date);
        SimpleDateFormat HM = new SimpleDateFormat("HH:mm");
        return HM.format(new Date(byR.getTimeInMillis()));
    }

    public static int getRepeat(Context context, String repeat) {
        switch (repeat) {
            case Costants.ALARM_REPEAT_DAY:
                return 1;
            case Costants.ALARM_REPEAT_WEEK:
                return 2;
            case Costants.ALARM_REPEAT_MONTH:
                return 3;
            case Costants.ALARM_REPEAT_YEAR:
                return 4;
            default:
                return 0;
        }
    }

    public static String getRepeatString(Context context, int repeat) {
        switch (repeat) {
            case 1:
                return Costants.ALARM_REPEAT_DAY;
            case 2:
                return Costants.ALARM_REPEAT_WEEK;
            case 3:
                return Costants.ALARM_REPEAT_MONTH;
            case 4:
                return Costants.ALARM_REPEAT_YEAR;
            default:
                return "";
        }
    }

    public static boolean isToday(long date) {
        Calendar today = Calendar.getInstance();
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTimeInMillis(date);
        return (today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) && today.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) == checkDate.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isTomorrow(long date) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTimeInMillis(date);
        return (tomorrow.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) && tomorrow.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH) && tomorrow.get(Calendar.DAY_OF_MONTH) == checkDate.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isYesterday(long date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTimeInMillis(date);
        return (yesterday.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) && yesterday.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH) && yesterday.get(Calendar.DAY_OF_MONTH) == checkDate.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isOldDate(long date) {
        Calendar today = Calendar.getInstance();
        return (today.getTimeInMillis() > date);
    }

    public static String checkURL(String s) {
        String url = "";
        String[] split = s.split(" ");
        for (String match : split) {
            try {
                URL url_tocheck = new URL(match);
                url = url_tocheck.toString();
                break;
            } catch (MalformedURLException e) {

            }
        }
        return url;
    }

    public static String checkAction(Context context, String s) {
        String[] split = s.split(" ");
        for (String match : split) {
            if (match.toLowerCase().equals(context.getString(R.string.action_call).toLowerCase()))
                return Costants.ACTION_CALL;
            if (match.toLowerCase().equals(context.getString(R.string.action_sms_c).toLowerCase()))
                return Costants.ACTION_SMS;
            if (match.toLowerCase().equals(context.getString(R.string.action_mail_c).toLowerCase()))
                return Costants.ACTION_MAIL;
        }
        return "";
    }

    public static void updateWidget(Context context) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, FocusWidget.class));
        Intent update = new Intent(context, FocusWidget.class);
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.list);
        context.sendBroadcast(update);
    }

    public static Reminder getReminder(Context context, String id) {
        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();
        Reminder r = null;
        Cursor c = dbHelper.getReminderById(id);
        while(c.moveToNext())
            r = new Reminder(c);

        dbHelper.close();
        c.close();
        return r;
    }

    public static boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1));
    }

    public static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }

    public static void tab_intro(final Context context) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (!SP.getBoolean(Costants.PREFERENCE_INTRO, false)) {
            SharedPreferences.Editor editor = SP.edit();
            editor.putBoolean(Costants.PREFERENCE_INTRO, true);
            editor.apply();
            ((Main) context).findViewById(R.id.first_use).setVisibility(View.VISIBLE);
            ((Main) context).findViewById(R.id.button_first_use).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Main) context).findViewById(R.id.first_use).setVisibility(View.GONE);
                }
            });
            context.startActivity(new Intent(context, Intro.class));
        }
    }

    public static void resetPasw(Context context) {

        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();

        Cursor c = dbHelper.fetchAllReminders();
        while (c.moveToNext()) {
            Reminder r = new Reminder(c);
            if (!r.getPasw().equals(""))
                ReminderService.startAction(context, Costants.ACTION_DELETE, r);
        }

        dbHelper.close();
    }

    public static String[] fetchContacts(Context context, String query) {

        String contact_id = null;
        String name = null;
        String photo = null;
        boolean found = false;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String PHOTO_URI = ContactsContract.Contacts.PHOTO_URI;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                photo = cursor.getString(cursor.getColumnIndex( PHOTO_URI ));

                if (name.toLowerCase().equals(query.toLowerCase()) || contact_id.equals(query)) {
                    found = true;
                    break;
                }

            }
        }

        cursor.close();
        if (found)
            return new String[]{contact_id, name, photo};
        else
            return null;
    }
}
