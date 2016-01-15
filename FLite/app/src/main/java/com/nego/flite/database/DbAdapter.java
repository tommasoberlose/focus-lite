package com.nego.flite.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.nego.flite.Costants;
import com.nego.flite.Reminder;
import com.nego.flite.User;

import java.util.zip.InflaterOutputStream;

public class DbAdapter {


    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    // Database fields
    public static final String DATABASE_TABLE = "reminders";

    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_ACTION_TYPE = "action_type";
    public static final String KEY_ACTION_INFO = "action_info";
    public static final String KEY_IMG = "img";
    public static final String KEY_PASW = "pasw";
    public static final String KEY_DATE_CREATE = "date_create";
    public static final String KEY_DATE_REMINDED = "date_reminded";
    public static final String KEY_LAST_CHANGED = "last_changed";
    public static final String KEY_ALARM = "alarm";
    public static final String KEY_ALARM_REPEAT = "alarm_repeat";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PRIORITY = "priority";

    public DbAdapter(Context context) {
        this.context = context;
    }

    public DbAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        if (database.getVersion() < DatabaseHelper.DATABASE_VERSION)
            dbHelper.onUpgrade(database, database.getVersion(), DatabaseHelper.DATABASE_VERSION);
        else
            dbHelper.onCreate(database);
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    private ContentValues createContentValues(int ID, String title, String content, String action_type, String action_info, String img, String pasw, long date_created, long date_reminded, long last_changed, long alarm, String alarm_repeat, String address, int priority) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, ID);
        values.put(KEY_TITLE, title);
        values.put(KEY_CONTENT, content);
        values.put(KEY_ACTION_TYPE, action_type);
        values.put(KEY_ACTION_INFO, action_info);
        values.put(KEY_IMG, img);
        values.put(KEY_PASW, pasw);
        values.put(KEY_DATE_CREATE, date_created);
        values.put(KEY_DATE_REMINDED, date_reminded);
        values.put(KEY_LAST_CHANGED, last_changed);
        values.put(KEY_ALARM, alarm);
        values.put(KEY_ALARM_REPEAT, alarm_repeat);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_PRIORITY, priority);

        return values;
    }

    private ContentValues createContentValues(String title, String content, String action_type, String action_info, String img, String pasw, long date_created, long date_reminded, long last_changed, long alarm, String alarm_repeat, String address, int priority) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_CONTENT, content);
        values.put(KEY_ACTION_TYPE, action_type);
        values.put(KEY_ACTION_INFO, action_info);
        values.put(KEY_IMG, img);
        values.put(KEY_PASW, pasw);
        values.put(KEY_DATE_CREATE, date_created);
        values.put(KEY_LAST_CHANGED, last_changed);
        values.put(KEY_ALARM, alarm);
        values.put(KEY_ALARM_REPEAT, alarm_repeat);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_PRIORITY, priority);

        return values;
    }

    //create a reminder
    public long createReminder(Reminder r) {
        ContentValues initialValues = createContentValues(r.getTitle(), r.getContent(), r.getAction_type(), r.getAction_info(), r.getImg(), r.getPasw(), r.getDate_create(), r.getDate_reminded(), r.getLast_changed(), r.getAlarm(), r.getAlarm_repeat(), r.getAddress(), r.getPriority());
        return database.insertOrThrow(DATABASE_TABLE, null, initialValues);
    }

    //update a reminder
    public boolean updateReminder(Reminder r) {
        ContentValues updateValues = createContentValues(r.getId(), r.getTitle(), r.getContent(), r.getAction_type(), r.getAction_info(), r.getImg(), r.getPasw(), r.getDate_create(), r.getDate_reminded(), r.getLast_changed(), r.getAlarm(), r.getAlarm_repeat(), r.getAddress(), r.getPriority());
        return database.update(DATABASE_TABLE, updateValues, KEY_ID + "==" + r.getId(), null) > 0;
    }

    //delete a reminder
    public boolean deleteReminder(String ID) {
        return database.delete(DATABASE_TABLE, KEY_ID + "==" + ID, null) > 0;
    }

    //fetch all reminders
    public Cursor fetchAllReminders(boolean order) {
        if (order) { // Alarm first
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY}, null, null, null, null, KEY_PRIORITY + " DESC, " + KEY_ALARM + ", " + KEY_DATE_CREATE + " DESC");
        } else {
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY}, null, null, null, null, KEY_PRIORITY + " DESC, " + KEY_DATE_CREATE + " DESC");
        }
    }

    //fetch all reminders
    public Cursor fetchAllRemindersFilterByTitle(boolean order, String query) {
        if (order) { // Alarm first
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY}, KEY_TITLE + " like '%" + query + "%' OR " + KEY_CONTENT + " like '%" + query + "%'", null, null, null, KEY_PRIORITY + " DESC, " + KEY_ALARM + ", " + KEY_DATE_CREATE + " DESC");
        } else {
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY}, KEY_TITLE + " like '%" + query + "%' OR " + KEY_CONTENT + " like '%" + query + "%'", null, null, null, KEY_PRIORITY + " DESC, " + KEY_DATE_CREATE + " DESC");
        }
    }

    //fetch reminder filter by id
    public Cursor getReminderById(String id) {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY},
                KEY_ID + " == '" + id + "'", null, null, null, null, null);
    }

    public Cursor fetchAllAlarm() {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY},
                KEY_ALARM + " > '0'", null, null, null, KEY_DATE_CREATE + " DESC", null);
    }

    public Cursor fetchRemindersByFilterWifi() {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY},
                KEY_ALARM + " == '" + Costants.ALARM_TYPE_WIFI + "'", null, null, null, KEY_DATE_CREATE + " DESC", null);
    }

    public Cursor fetchRemindersByFilterBluetooth() {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY},
                KEY_ALARM + " == '" + Costants.ALARM_TYPE_BLUETOOTH + "'", null, null, null, KEY_DATE_CREATE + " DESC", null);
    }

    public int getRemindersN() {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, null, null, null, null, null)).getCount();
    }

    public int getRemindersNNotes() {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_ALARM + " == 0 AND " + KEY_PRIORITY + " == 0", null, null, null, null)).getCount();
    }

    public int getRemindersNReminders() {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_ALARM + " != 0 AND " + KEY_PRIORITY + " == 0", null, null, null, null)).getCount();
    }

    public int getRemindersNStarred() {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_ALARM + " == 0 AND " + KEY_PRIORITY + " == 1", null, null, null, null)).getCount();
    }

    public boolean deleteAllReminders() {
        return database.delete(DATABASE_TABLE, null, null) > 0;
    }

}