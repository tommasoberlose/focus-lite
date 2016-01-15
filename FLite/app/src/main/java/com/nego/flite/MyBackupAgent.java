package com.nego.flite;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupManager;
import android.app.backup.FileBackupHelper;
import android.app.backup.RestoreObserver;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;

import com.nego.flite.database.DbAdapter;

import java.io.File;


public class MyBackupAgent extends BackupAgentHelper {

    public void onCreate() {
        /*
        SharedPreferencesBackupHelper helper =
                new SharedPreferencesBackupHelper(this, Costants.PREFERENCES_COSTANT);
        addHelper(Costants.PREFERENCES_COSTANT_BACKUP, helper);


        TODO backup db
        FileBackupHelper dbs = new FileBackupHelper(this, DbAdapter.DATABASE_TABLE);
        addHelper("dbs", dbs);
        */
    }

    @Override
    public File getFilesDir(){
        File path = getDatabasePath(DbAdapter.DATABASE_TABLE);
        return path.getParentFile();
    }

    public static void requestBackup(Context context) {
        BackupManager bm = new BackupManager(context);
        bm.dataChanged();
    }

    public static void requestRestore(Context context) {
        BackupManager bm = new BackupManager(context);
        bm.requestRestore(new RestoreObserver() {
            @Override
            public void restoreFinished(int error) {
                super.restoreFinished(error);
            }
        });
    }
}
