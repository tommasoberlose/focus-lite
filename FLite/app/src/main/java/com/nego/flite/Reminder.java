package com.nego.flite;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.nego.flite.Functions.AlarmF;
import com.nego.flite.database.DbAdapter;

public class Reminder implements Parcelable {
    private int id;
    private String title;
    private String content;
    private String action_type;
    private String action_info;
    private String img;
    private String pasw;
    private long date_create;
    private long last_changed;
    private long alarm;
    private String alarm_repeat;

    public Reminder(String title, String content, String action_type, String action_info, String img, String pasw, long date_create, long last_changed, long alarm, String alarm_repeat){
        this.title = title;
        this.content = content;
        this.action_type = action_type;
        this.action_info = action_info;
        this.img = img;
        this.pasw = pasw;
        this.date_create = date_create;
        this.last_changed = last_changed;
        this.alarm = alarm;
        this.alarm_repeat = alarm_repeat;
    }

    public Reminder(int id, String title, String content, String action_type, String action_info, String img, String pasw, long date_create, long last_changed, long alarm, String alarm_repeat){
        this.id = id;
        this.title = title;
        this.content = content;
        this.action_type = action_type;
        this.action_info = action_info;
        this.img = img;
        this.pasw = pasw;
        this.date_create = date_create;
        this.last_changed = last_changed;
        this.alarm = alarm;
        this.alarm_repeat = alarm_repeat;
    }

    public Reminder(Cursor cursor){
        this.id = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_ID));
        this.title = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_TITLE));
        this.content = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_CONTENT));
        this.action_type = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_ACTION_TYPE));
        this.action_info = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_ACTION_INFO));
        this.img = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_IMG));
        this.pasw = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_PASW));
        this.date_create = cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_DATE_CREATE));
        this.last_changed = cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_LAST_CHANGED));
        this.alarm = cursor.getLong( cursor.getColumnIndex(DbAdapter.KEY_ALARM) );
        this.alarm_repeat = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_ALARM_REPEAT));
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setAction_type(String action_type) {
        this.action_type = action_type;
    }

    public String getAction_type() {
        return action_type;
    }

    public void setAction_info(String action_info) {
        this.action_info = action_info;
    }

    public String getAction_info() {
        return action_info;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setPasw(String pasw) {
        this.pasw = pasw;
    }

    public String getPasw() {
        return pasw;
    }
    public boolean checkPasw(String p) {
        return p.equals(pasw);
    }

    public void setDate_create(long date_create) {
        this.date_create = date_create;
    }

    public long getDate_create() {
        return date_create;
    }

    public void setLast_changed(long last_changed) {
        this.last_changed = last_changed;
    }

    public long getLast_changed() {
        return last_changed;
    }

    public void setAlarm(long alarm) {
        this.alarm = alarm;
    }

    public long getAlarm() {
        return alarm;
    }

    public void setAlarm_repeat(String alarm_repeat) {
        this.alarm_repeat = alarm_repeat;
    }

    public String getAlarm_repeat() {
        return alarm_repeat;
    }

    public boolean create_reminder(Context context, DbAdapter dbHelper) {
        this.id = (int) dbHelper.createReminder(this);
        if (id > 0) {
            AlarmF.addAlarm(context, this.getId(), this.getAlarm(), this.getAlarm_repeat());
            return true;
        }
        return false;
    }

    public boolean update_reminder(Context context, DbAdapter dbHelper) {
        if (dbHelper.updateReminder(this)) {
            AlarmF.updateAlarm(context, this.getId(), this.getAlarm(), this.getAlarm_repeat());
            return true;
        }
        return false;
    }

    public boolean delete_reminder(Context context, DbAdapter dbHelper) {
        if (dbHelper.deleteReminder("" + this.getId())) {
            AlarmF.deleteAlarm(context, this.getId());
            return true;
        }
        return false;
    }


    // PARCELIZZAZIONE

    public static final Parcelable.Creator<Reminder> CREATOR = new Creator<Reminder>() {
        public Reminder createFromParcel(Parcel source) {
            return new Reminder(source.readInt(), source.readString(), source.readString(), source.readString(), source.readString(), source.readString(), source.readString(), source.readLong(), source.readLong(), source.readLong(), source.readString());
        }
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(action_type);
        dest.writeString(action_info);
        dest.writeString(img);
        dest.writeString(pasw);
        dest.writeLong(date_create);
        dest.writeLong(last_changed);
        dest.writeLong(alarm);
        dest.writeString(alarm_repeat);
    }
}
