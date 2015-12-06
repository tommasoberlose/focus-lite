package com.nego.flite.Receiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.nego.flite.Costants;
import com.nego.flite.MyDialog;
import com.nego.flite.R;

public class ShortcutReceiver extends Activity {

    public void onCreate(Bundle savedInstanceState) {

        if (getIntent().getAction().equals("android.intent.action.CREATE_SHORTCUT")) {
            Intent shortcutIntent = new Intent(getApplicationContext(), MyDialog.class);
            shortcutIntent.setAction(Costants.ACTION_ADD_ITEM);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.title_activity_add_item));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher));
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

            setResult(RESULT_OK, addIntent);
            finish();
        }

        super.onCreate(savedInstanceState);
    }
}
