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
    public static final String KEY_DATE_ARCHIVED= "date_archived";
    public static final String KEY_LAST_CHANGED = "last_changed";
    public static final String KEY_ALARM = "alarm";
    public static final String KEY_ALARM_REPEAT = "alarm_repeat";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_VOICE_NOTE = "voice_note";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_COLOR = "color";
    public static final String KEY_ICON = "icon";

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

    private ContentValues createContentValues(int ID, String title, String content, String action_type, String action_info, String img, String pasw, long date_created, long date_reminded, long date_archived, long last_changed, long alarm, String alarm_repeat, String address, int priority, String voice_note, String user_id, String color, String icon) {
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
        values.put(KEY_DATE_ARCHIVED, date_archived);
        values.put(KEY_LAST_CHANGED, last_changed);
        values.put(KEY_ALARM, alarm);
        values.put(KEY_ALARM_REPEAT, alarm_repeat);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_PRIORITY, priority);
        values.put(KEY_VOICE_NOTE, voice_note);
        values.put(KEY_USER_ID, user_id);
        values.put(KEY_COLOR, color);
        values.put(KEY_ICON, icon);

        return values;
    }

    private ContentValues createContentValues(String title, String content, String action_type, String action_info, String img, String pasw, long date_created, long date_reminded, long date_archived, long last_changed, long alarm, String alarm_repeat, String address, int priority, String voice_note, String user_id, String color, String icon) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_CONTENT, content);
        values.put(KEY_ACTION_TYPE, action_type);
        values.put(KEY_ACTION_INFO, action_info);
        values.put(KEY_IMG, img);
        values.put(KEY_PASW, pasw);
        values.put(KEY_DATE_CREATE, date_created);
        values.put(KEY_DATE_REMINDED, date_reminded);
        values.put(KEY_DATE_ARCHIVED, date_archived);
        values.put(KEY_LAST_CHANGED, last_changed);
        values.put(KEY_ALARM, alarm);
        values.put(KEY_ALARM_REPEAT, alarm_repeat);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_PRIORITY, priority);
        values.put(KEY_VOICE_NOTE, voice_note);
        values.put(KEY_USER_ID, user_id);
        values.put(KEY_COLOR, color);
        values.put(KEY_ICON, icon);

        return values;
    }

    //create a reminder
    public long createReminder(Reminder r) {
        ContentValues initialValues = createContentValues(r.getTitle(), r.getContent(), r.getAction_type(), r.getAction_info(), r.getImg(), r.getPasw(), r.getDate_create(), r.getDate_reminded(), r.getDate_archived(), r.getLast_changed(), r.getAlarm(), r.getAlarm_repeat(), r.getAddress(), r.getPriority(), r.getVoice_note(), r.getUser_id(), r.getColor(), r.getIcon());
        return database.insertOrThrow(DATABASE_TABLE, null, initialValues);
    }

    //update a reminder
    public boolean updateReminder(Reminder r) {
        ContentValues updateValues = createContentValues(r.getId(), r.getTitle(), r.getContent(), r.getAction_type(), r.getAction_info(), r.getImg(), r.getPasw(), r.getDate_create(), r.getDate_reminded(), r.getDate_archived(), r.getLast_changed(), r.getAlarm(), r.getAlarm_repeat(), r.getAddress(), r.getPriority(), r.getVoice_note(), r.getUser_id(), r.getColor(), r.getIcon());
        return database.update(DATABASE_TABLE, updateValues, KEY_ID + "==" + r.getId(), null) > 0;
    }

    //delete a reminder
    public boolean deleteReminder(String ID) {
        return database.delete(DATABASE_TABLE, KEY_ID + "==" + ID, null) > 0;
    }

    //fetch all reminders
    public Cursor fetchAllReminders(boolean order, String user_id) {
        if (order) { // Alarm first
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON}, KEY_DATE_ARCHIVED + " == 0 AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, KEY_PRIORITY + " DESC, " + KEY_ALARM + ", " + KEY_DATE_CREATE + " DESC");
        } else {
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON}, KEY_DATE_ARCHIVED + " == 0 AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, KEY_PRIORITY + " DESC, " + KEY_DATE_CREATE + " DESC");
        }
    }

    //fetch all reminders
    public Cursor fetchAllRemindersWithArchived(boolean order, String user_id) {
        if (order) { // Alarm first
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON}, KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == ''", null, null, null, KEY_PRIORITY + " DESC, " + KEY_ALARM + ", " + KEY_DATE_CREATE + " DESC");
        } else {
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON}, KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == ''", null, null, null, KEY_PRIORITY + " DESC, " + KEY_DATE_CREATE + " DESC");
        }
    }

    //fetch all reminders
    public Cursor fetchAllRemindersFilterByTitle(boolean order, String query, String user_id) {
        if (order) { // Alarm first
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON}, "(" + KEY_TITLE + " like '%" + query + "%' OR " + KEY_CONTENT + " like '%" + query + "%') AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, KEY_PRIORITY + " DESC, " + KEY_ALARM + ", " + KEY_DATE_CREATE + " DESC");
        } else {
            return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON}, "(" + KEY_TITLE + " like '%" + query + "%' OR " + KEY_CONTENT + " like '%" + query + "%') AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, KEY_PRIORITY + " DESC, " + KEY_DATE_CREATE + " DESC");
        }
    }

    //fetch reminder filter by id
    public Cursor getReminderById(String id) {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON},
                KEY_ID + " == '" + id + "' OR " + KEY_USER_ID + " == ''", null, null, null, null, null);
    }

    public Cursor fetchAllAlarm(String user_id) {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON},
                KEY_ALARM + " > '0' AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, KEY_DATE_CREATE + " DESC", null);
    }

    public Cursor fetchRemindersByFilterWifi(String user_id) {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON},
                KEY_ALARM + " == '" + Costants.ALARM_TYPE_WIFI + "' AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, KEY_DATE_CREATE + " DESC", null);
    }

    public Cursor fetchRemindersByFilterBluetooth(String user_id) {
        return database.query(true, DATABASE_TABLE, new String[]{
                        KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_ACTION_TYPE, KEY_ACTION_INFO, KEY_IMG, KEY_PASW, KEY_DATE_CREATE, KEY_DATE_REMINDED, KEY_DATE_ARCHIVED, KEY_LAST_CHANGED, KEY_ALARM, KEY_ALARM_REPEAT, KEY_ADDRESS, KEY_PRIORITY, KEY_VOICE_NOTE, KEY_USER_ID, KEY_COLOR, KEY_ICON},
                KEY_ALARM + " == '" + Costants.ALARM_TYPE_BLUETOOTH + "' AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, KEY_DATE_CREATE + " DESC", null);
    }

    public int getRemindersN(String user_id) {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == ''", null, null, null, null)).getCount();
    }

    public int getRemindersNNotes(String user_id) {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_ALARM + " == 0 AND " + KEY_PRIORITY + " == 0  AND " + KEY_DATE_ARCHIVED + " == 0 AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, null)).getCount();
    }

    public int getRemindersNNotesArchived(String user_id) {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_DATE_ARCHIVED + " != 0 AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, null)).getCount();
    }

    public int getRemindersNReminders(String user_id) {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_ALARM + " != 0 AND " + KEY_PRIORITY + " == 0  AND " + KEY_DATE_ARCHIVED + " == 0 AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, null)).getCount();
    }

    public int getRemindersNStarred(String user_id) {
        return (database.query(DATABASE_TABLE, new String[]{KEY_ID}, KEY_PRIORITY + " == 1  AND " + KEY_DATE_ARCHIVED + " == 0 AND (" + KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == '')", null, null, null, null)).getCount();
    }

    public boolean deleteAllReminders(String user_id) {
        return database.delete(DATABASE_TABLE, KEY_USER_ID + " == '" + user_id + "' OR " + KEY_USER_ID + " == ''", null) > 0;
    }

}