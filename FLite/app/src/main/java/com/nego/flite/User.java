package com.nego.flite;

import android.content.Context;
import android.content.SharedPreferences;

public class User {

    private String id;
    private String name;
    private String email;
    private String photo;

    public User (String id, String name, String email, String photo) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photo = photo;
    }

    public User (Context context){
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        this.id = SP.getString(Costants.KEY_USER_ID, "");
        this.name = SP.getString(Costants.KEY_USER_NAME, "");
        this.email = SP.getString(Costants.KEY_USER_EMAIL, "");
        this.photo = SP.getString(Costants.KEY_USER_PHOTO, "");
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

    public void createUser(Context context) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        SP.edit()
                .putString(Costants.KEY_USER_ID, this.id)
                .putString(Costants.KEY_USER_NAME, this.name)
                .putString(Costants.KEY_USER_EMAIL, this.email)
                .putString(Costants.KEY_USER_PHOTO, this.photo)
                .apply();
        Utils.notification_add_update(context);
    }

    public void deleteUser(Context context) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        SP.edit()
                .remove(Costants.KEY_USER_ID)
                .remove(Costants.KEY_USER_NAME)
                .remove(Costants.KEY_USER_EMAIL)
                .remove(Costants.KEY_USER_PHOTO)
                .apply();
        Utils.notification_add_update(context);
    }
}
