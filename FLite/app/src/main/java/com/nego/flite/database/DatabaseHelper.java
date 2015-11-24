package com.nego.flite.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reminderdb";
    public static final int DATABASE_VERSION = 5;

    private static final String DATABASE_CREATE = "create table IF NOT EXISTS reminders (id integer primary key autoincrement, title text not null, content text default '', action_type text not null, action_info text not null, img text not null, pasw default '', date_create long not null, alarm long default 0, alarm_repeat text default '');";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {

        if (oldVersion == 3) {
            database.execSQL("ALTER TABLE reminders ADD COLUMN alarm long DEFAULT 0");
            database.execSQL("ALTER TABLE reminders ADD COLUMN alarm_repeat text DEFAULT ''");
            onUpgrade(database, 4, DATABASE_VERSION);
        }
        if (oldVersion == 4) {
            database.execSQL("ALTER TABLE reminders ADD COLUMN content text DEFAULT ''");
            database.execSQL("ALTER TABLE reminders ADD COLUMN pasw text DEFAULT ''");
            onUpgrade(database, 5, DATABASE_VERSION);
        }
        //database.execSQL("DROP TABLE IF EXISTS reminders");
        onCreate(database);

    }
}