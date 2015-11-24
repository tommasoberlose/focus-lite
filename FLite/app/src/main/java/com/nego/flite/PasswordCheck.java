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
        final EditText pasw_text = (EditText) findViewById(R.id.pasw);
        final Reminder r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
        findViewById(R.id.action_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pasw_text.getEditableText().toString().equals(r.getPasw())) {
                    setResult(Activity.RESULT_OK);
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
