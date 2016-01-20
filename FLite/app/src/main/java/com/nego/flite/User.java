package com.nego.flite;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.nego.flite.database.DbAdapter;

public class User {

    private String id;
    private String name;
    private String email;
    private String photo;
    private int active;

    public User(String id, String name, String email, String photo, int active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.active = active;
    }

    public User(Cursor cursor) {
        this.id = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_USER_ID));
        this.name = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_USER_NAME));
        this.email = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_USER_EMAIL));
        this.photo = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_USER_PHOTO));
        this.active = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_USER_ACTIVE));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public boolean createUser(Context context, DbAdapter dbHelper) {
        if (dbHelper.createUser(this) > 0) {
            Utils.notification_add_update(context);
            return true;
        }
        return false;
    }

    public boolean updateUser(Context context, DbAdapter dbHelper) {
        if (dbHelper.updateUser(this)) {
            Utils.notification_add_update(context);
            return true;
        }
        return false;
    }

    public boolean deleteUser(Context context, DbAdapter dbHelper) {
        if (dbHelper.deleteUser(id + "")) {
            Utils.notification_add_update(context);
            return true;
        }
        return false;
    }
}
