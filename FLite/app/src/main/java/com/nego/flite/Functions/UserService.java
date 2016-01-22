package com.nego.flite.Functions;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.R;
import com.nego.flite.Reminder;
import com.nego.flite.User;
import com.nego.flite.Utils;
import com.nego.flite.database.DbAdapter;

import java.io.File;
import java.util.Calendar;

public class UserService extends IntentService {

    public static void startAction(Context context, String action, User u) {
        Intent intent = new Intent(context, UserService.class);
        intent.setAction(action);
        intent.putExtra(Costants.EXTRA_USER, u);
        context.startService(intent);
    }

    private void sendResponse(String s, User u) {
        Intent i = new Intent(Costants.ACTION_UPDATE_LIST_ACCOUNT);
        i.putExtra(Costants.EXTRA_ACTION_TYPE, s);
        i.putExtra(Costants.EXTRA_USER, u);
        sendBroadcast(i);
    }

    public UserService() {
        super("UserService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Costants.ACTION_CREATE.equals(action)) {
                final User u = intent.getParcelableExtra(Costants.EXTRA_USER);
                createUser(u);
            } else if (Costants.ACTION_UPDATE.equals(action)) {
                final User u = intent.getParcelableExtra(Costants.EXTRA_USER);
                updateUser(u);
            } else if (Costants.ACTION_DELETE.equals(action)) {
                final User u = intent.getParcelableExtra(Costants.EXTRA_USER);
                deleteUser(u);
            } else if (Costants.ACTION_SET_ACTIVE_USER.equals(action)) {
                final User u = intent.getParcelableExtra(Costants.EXTRA_USER);
                setActiveUser(u);
            }
        }
    }

    private void createUser(User user) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();

        if (dbHelper.fetchAllUsers().getCount() == 0)
            user.setActive(1);

        Cursor c = dbHelper.getUserById(user.getId());
        if (c.moveToFirst()) {
            updateUser(user);
        } else {
            if (user.createUser(this, dbHelper)) {
                sendResponse(Costants.ACTION_CREATE, user);
            }
        }
        c.close();
        dbHelper.close();
    }

    private void updateUser(User u) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();
        if (u.updateUser(this, dbHelper)) {
            sendResponse(Costants.GENERAL_ERROR, u);
        }
        dbHelper.close();
    }

    private void deleteUser(User u) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();

        File photo = new File(u.getPhoto());
        if (photo.exists())
            photo.delete();

        if (u.deleteUser(this, dbHelper)) {
            if (u.getActive() == 1) {
                Cursor c = dbHelper.fetchAllUsers();
                if (c.moveToFirst()) {
                    User newActiveUser = new User(c);
                    newActiveUser.setActive(1);
                    newActiveUser.updateUser(this, dbHelper);
                }
                c.close();
            }
            sendResponse(Costants.ACTION_DELETE, u);
        }
        dbHelper.close();
    }

    private void setActiveUser(User user) {
        DbAdapter dbHelper = new DbAdapter(this);
        dbHelper.open();


        User activeUser = null;
        Cursor c = dbHelper.getActiveUser();
        if (c.moveToFirst())
            activeUser = new User(c);
        if (activeUser != null) {
            activeUser.setActive(0);
            activeUser.updateUser(this, dbHelper);
        }
        c.close();

        user.setActive(1);
        if (user.updateUser(this, dbHelper)) {
            sendResponse(Costants.ACTION_SET_ACTIVE_USER, user);
        }

        dbHelper.close();
    }

}
