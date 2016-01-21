package com.nego.flite;

import android.Manifest;
import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Functions.AlarmF;
import com.nego.flite.Functions.NotificationF;
import com.nego.flite.Functions.ReminderService;
import com.nego.flite.Widget.FocusWidget;
import com.nego.flite.database.DbAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Utils {

    public static boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    public static void oldReminder(Context context) {

        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (SP.getBoolean(Costants.PREFERENCES_VIEW_ALL, true)) {

            Cursor c = dbHelper.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, false), getActiveUserId(dbHelper));
            for (c.moveToLast(); !c.isBeforeFirst(); c.moveToPrevious()) {
                NotificationF.NotificationFixed(context, new Reminder(c));
            }
            c.close();
        }

        dbHelper.close();
    }

    public static void oldAlarms(Context context) {
        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();

        Cursor a = dbHelper.fetchAllAlarm(getActiveUserId(dbHelper));
        while (a.moveToNext()) {
            Reminder reminder = new Reminder(a);
            // Missed alarms
            if (isMissedAlarm(reminder)) {
                NotificationF.Notification(context, reminder);
            }

            // Future alarm
            if (reminder.getAlarm() != 0 && !isOldAlarm(reminder)) {
                AlarmF.updateAlarm(context, reminder.getId(), reminder.getAlarm(), reminder.getAlarm_repeat());
            }
        }
        a.close();

        dbHelper.close();
    }

    public static void showOldStarred(Context context, SharedPreferences SP) {
        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();
        Cursor c = dbHelper.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, false), getActiveUserId(dbHelper));
        for (c.moveToLast(); !c.isBeforeFirst(); c.moveToPrevious()) {
            Reminder r = new Reminder(c);
            if (r.getPriority() == 1)
                NotificationF.NotificationFixed(context, r);
        }
        c.close();
        dbHelper.close();
    }

    public static int itemsToDo(Context context) {
        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();
        int count = dbHelper.getRemindersN(getActiveUserId(dbHelper));
        dbHelper.close();
        return count;
    }

    public static int[] itemsToDoMultiple(Context context) {
        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();
        int[] count = {dbHelper.getRemindersNNotes(getActiveUserId(dbHelper)), dbHelper.getRemindersNReminders(getActiveUserId(dbHelper)), dbHelper.getRemindersNStarred(getActiveUserId(dbHelper)), dbHelper.getRemindersNNotesArchived(getActiveUserId(dbHelper))};
        dbHelper.close();
        return count;
    }

    public static void SnackbarC(final Context context, String title, final View view) {

        Snackbar.make(view, title, Snackbar.LENGTH_LONG).show();

    }

    public static void notification_add_update(Context context) {
        NotificationF.CancelAllNotification(context);

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true)) {
            if (!SP.getBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, true)) {
                NotificationF.NotificationAdd(context);
                oldReminder(context);
            } else {
                oldReminder(context);
                NotificationF.NotificationAdd(context);
            }
        } else if (SP.getBoolean(Costants.PREFERENCE_SHOW_STARRED, true)) {
            showOldStarred(context, SP);
        }
        oldAlarms(context);
        updateWidget(context);
    }

    public static void notification_update(Context context, String action, Reminder r) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        NotificationF.CancelNotification(context, "" + r.getId());
        AlarmF.updateAlarm(context, r.getId(), r.getAlarm(), r.getAlarm_repeat());
        updateWidget(context);

        if (!action.equals(Costants.ACTION_DELETE)) {
            if (SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true)) {
                if (SP.getBoolean(Costants.PREFERENCES_VIEW_ALL, true)) {
                    NotificationF.NotificationFixed(context, r);
                }

                NotificationF.NotificationAdd(context);
            } else if (SP.getBoolean(Costants.PREFERENCE_SHOW_STARRED, true) && r.getPriority() == 1) {
                NotificationF.NotificationFixed(context, r);
            }
        }
    }

    public static String getDate(Context context, long date) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        Calendar today = Calendar.getInstance();
        Calendar byR = Calendar.getInstance();
        byR.setTimeInMillis(date);
        SimpleDateFormat HM = new SimpleDateFormat("HH:mm");
        SimpleDateFormat DM = new SimpleDateFormat("MMM d, HH:mm");
        SimpleDateFormat MY = new SimpleDateFormat("MMM d y, HH:mm");
        if (SP.getBoolean(Costants.PREFERENCE_TWELVE_HOUR_FORMAT, false)) {
            HM = new SimpleDateFormat("hh:mm a");
            DM = new SimpleDateFormat("MMM d, hh:mm a");
            MY = new SimpleDateFormat("MMM d y, hh:mm a");
        }
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

    public static String getAlarm(Context context, long alarm, String alarm_repeat, long date_reminded) {
        if (alarm > 0) {
            return getDateAlarm(context, alarm, alarm_repeat, date_reminded);
        } else {
            return context.getString(R.string.text_snoozed_to) + ": " + alarm_repeat.split(Costants.LIST_ITEM_SEPARATOR)[1];
        }
    }

    public static String getDateAlarm(Context context, long alarm, String alarm_repeat, long date_reminded) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        Calendar today = Calendar.getInstance();
        Calendar byR = Calendar.getInstance();
        byR.setTimeInMillis(alarm);

        SimpleDateFormat HM = new SimpleDateFormat("HH:mm");
        SimpleDateFormat DM = new SimpleDateFormat("MMM d, HH:mm");
        SimpleDateFormat MY = new SimpleDateFormat("MMM d y, HH:mm");
        if (SP.getBoolean(Costants.PREFERENCE_TWELVE_HOUR_FORMAT, false)) {
            HM = new SimpleDateFormat("hh:mm a");
            DM = new SimpleDateFormat("MMM d, hh:mm a");
            MY = new SimpleDateFormat("MMM d y, hh:mm a");
        }
        if (date_reminded == 0) {
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
            Calendar dateReminded = Calendar.getInstance();
            dateReminded.setTimeInMillis(date_reminded);
            if (today.get(Calendar.YEAR) == dateReminded.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == dateReminded.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == dateReminded.get(Calendar.DAY_OF_MONTH)) {
                return context.getString(R.string.text_reminded_at) + " " + HM.format(new Date(dateReminded.getTimeInMillis()));
            } else if (today.get(Calendar.YEAR) == dateReminded.get(Calendar.YEAR)) {
                return context.getString(R.string.text_reminded_on) + " " + DM.format(new Date(dateReminded.getTimeInMillis()));
            } else {
                return context.getString(R.string.text_reminded_on) + " " + MY.format(new Date(dateReminded.getTimeInMillis()));
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
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        Calendar byR = Calendar.getInstance();
        byR.setTimeInMillis(date);
        SimpleDateFormat HM = new SimpleDateFormat("HH:mm");
        if (SP.getBoolean(Costants.PREFERENCE_TWELVE_HOUR_FORMAT, false)) {
            HM = new SimpleDateFormat("hh:mm a");
        }
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

    public static boolean isOldAlarm(Reminder r) {
        return r.getAlarm_repeat().equals("") && isOldDate(r.getAlarm());
    }

    public static boolean isMissedAlarm(Reminder r) {
        if (r.getAlarm() != 0) {
            if (r.getAlarm_repeat().equals("")) {
                if (isOldDate(r.getAlarm()))
                    return r.getDate_reminded() == 0;
            } else {
                // ho perso qualche ripetizione?
                Calendar c = Calendar.getInstance();
                switch (r.getAlarm_repeat()) {
                    case Costants.ALARM_REPEAT_DAY:
                        return (c.getTimeInMillis() - r.getDate_reminded()) > AlarmManager.INTERVAL_DAY;
                    case Costants.ALARM_REPEAT_WEEK:
                        return (c.getTimeInMillis() - r.getDate_reminded()) > 7 * AlarmManager.INTERVAL_DAY;
                    case Costants.ALARM_REPEAT_MONTH:
                        c.add(Calendar.MONTH, -1);
                        return c.getTimeInMillis() > r.getDate_reminded();
                    case Costants.ALARM_REPEAT_YEAR:
                        c.add(Calendar.YEAR, -1);
                        return c.getTimeInMillis() > r.getDate_reminded();
                }
            }
        }
        return false;
    }

    public static boolean isOldDate(long date) {
        Calendar today = Calendar.getInstance();
        return (today.getTimeInMillis() > date);
    }

    public static String checkURL(String s) {
        String[] split = s.split(" ");
        for (String match : split) {
            if (Patterns.WEB_URL.matcher(match).matches()) {
                try {
                    URL url_tocheck = new URL(match);
                    return url_tocheck.toString();
                } catch (MalformedURLException e) {
                    return "";
                }
            }
        }
        return "";
    }

    public static String[] checkAction(String s) {
        String[] split = s.split(" ");
        for (String match : split) {
            String m = match.replace("/","").replace("-","");
            if (Patterns.PHONE.matcher(m).matches() && m.length() == 10)
                return new String[] {Costants.ACTION_CALL, m};
            if (Patterns.EMAIL_ADDRESS.matcher(m).matches())
                return new String[] {Costants.ACTION_MAIL, m};
        }
        return new String[] {""};
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

    public static Reminder getReminder(Context context, int id) {
        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();
        Reminder r = null;
        Cursor c = dbHelper.getReminderById("" + id);
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
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        DbAdapter dbHelper = new DbAdapter(context);
        dbHelper.open();

        Cursor c = dbHelper.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, false), getActiveUserId(dbHelper));
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

                if ((name != null && name.toLowerCase().equals(query.toLowerCase())) || (contact_id != null && contact_id.equals(query))) {
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

    public static boolean checkList(String c) {
        return  (c.contains(Costants.LIST_COSTANT));
    }

    public static String getContentList(Context context, String c) {
        if (!Utils.checkList(c)) {
            return c;
        } else {
            int n = 0;
            c = c.replace(Costants.LIST_COSTANT, "");
            String[] content_split = c.split(Costants.LIST_ITEM_SEPARATOR);
            for (String i : content_split) {
                if (i.split(Costants.LIST_ORDER_SEPARATOR, -1)[0].equals("0"))
                    n++;
            }
            return context.getString(R.string.num_items_todo, n);
        }
    }

    public static String getBigContentList(Context context, String c) {
        if (!Utils.checkList(c)) {
            return c;
        } else {
            String text = "";
            c = c.replace(Costants.LIST_COSTANT, "");
            String[] content_split = c.split(Costants.LIST_ITEM_SEPARATOR);
            for (String i : content_split) {
                if (i.split(Costants.LIST_ORDER_SEPARATOR, -1)[0].equals("1"))
                    text += i.split(Costants.LIST_ORDER_SEPARATOR, -1)[1];
            }
            return text.trim();
        }
    }

    public static Address getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;

        int i = 0;
        while (i<10) {
            try {
                address = coder.getFromLocationName(strAddress, 5);
                Log.i("address", address.get(0).toString());
                return address.get(0);

            } catch (Exception ex) {
                Log.i("errore_address", ex.toString());
            }
            i++;
        }

        return  null;

    }

    public static void expand(final View v) {
        if (v.getVisibility() != View.VISIBLE) {
            v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            final int targetHeight = v.getMeasuredHeight();

            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
            v.getLayoutParams().height = 1;
            v.setVisibility(View.VISIBLE);
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    v.getLayoutParams().height = interpolatedTime == 1
                            ? ViewGroup.LayoutParams.WRAP_CONTENT
                            : (int) (targetHeight * interpolatedTime);
                    v.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 2);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            v.startAnimation(a);
        }
    }

    public static void collapse(final View v) {
        if (v.getVisibility() == View.VISIBLE) {
            final int initialHeight = v.getMeasuredHeight();

            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime == 1) {
                        v.setVisibility(View.GONE);
                    } else {
                        v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                        v.requestLayout();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) * 2);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            v.startAnimation(a);
        }
    }

    public static String getOwnerName(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Cursor c = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
            if (c.moveToFirst()) {
                String name = c.getString(c.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
                if (name != null && !name.equals(""))
                    return name;
            }
            c.close();
        } else {
            return context.getString(R.string.name_unset);
        }
        return context.getString(R.string.name_unset);
    }

    public static ArrayList<String[]> getContactsList(Context context, String query) {
        ArrayList<String[]> arrayList = new ArrayList<>();
        if (query.startsWith("@") && query.length() > 1) {
            ContentResolver cr = context.getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (name.toLowerCase().contains(query.replace("@", "").toLowerCase())) {
                        arrayList.add(new String[]{id, name});
                    }
                }
            }
            cur.close();
        }
        return arrayList;
    }

    public static String getActiveUserId(DbAdapter dbAdapter) {
        Cursor c = dbAdapter.getActiveUser();

        String userId = "";
        if (c.moveToFirst()) {
            User activeUser = new User(c);
            userId = activeUser.getId();
        }
        c.close();
        return userId;
    }

    public static String savePhoto(Context context, String uri, String user_id) {
        String photo = "";
        File destination = new File(context.getFilesDir() + File.separator + "photo_" + user_id + ".jpeg");

        InputStream input = null;
        FileOutputStream output = null;

        try {

            input = new URL(uri).openConnection().getInputStream();
            output = new FileOutputStream(destination, false);

            int read;
            byte[] data = new byte[1024];
            while ((read = input.read(data)) != -1)
                output.write(data, 0, read);

        } catch (Exception e) {
            e.printStackTrace();
            return photo;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (Exception e) {
                e.printStackTrace();
                return photo;
            }
        }


        photo = Uri.fromFile(destination).toString();
        return photo;
    }
}
