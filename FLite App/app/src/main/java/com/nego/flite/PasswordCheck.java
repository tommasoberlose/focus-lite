package com.nego.flite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswordCheck extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpasw);

        final Intent intent = getIntent();

        final SharedPreferences SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        final EditText pasw_text = (EditText) findViewById(R.id.pasw);
        findViewById(R.id.action_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pasw_text.getEditableText().toString().equals(SP.getString(Costants.PREFERENCE_PASSWORD, ""))) {
                    if (intent != null && intent.getAction() != null && intent.getAction().equals(Costants.ACTION_VIEW_ALL)) {
                        SharedPreferences.Editor editor = SP.edit();
                        editor.putBoolean(Costants.PREFERENCES_VIEW_ALL, true);
                        editor.apply();
                        Utils.notification_add_update(PasswordCheck.this);
                    } else {
                        setResult(Activity.RESULT_OK);
                    }
                    finish();
                } else {
                    Toast.makeText(PasswordCheck.this, R.string.error_password, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.action_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

}
