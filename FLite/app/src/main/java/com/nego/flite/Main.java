package com.nego.flite;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class Main extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.tab_intro(this);

        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        findViewById(R.id.section_notification_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                startActivity(i);
            }
        });

        findViewById(R.id.section_alarm_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_ALARM_SETTINGS);
                startActivity(i);
            }
        });

        findViewById(R.id.section_style_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_STYLE_SETTINGS);
                startActivity(i);
            }
        });

        findViewById(R.id.section_application_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_APPLICATION_SETTINGS);
                startActivity(i);
            }
        });

        findViewById(R.id.action_feedback).setVisibility(View.GONE);
        /* TODO HELP

        findViewById(R.id.action_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        */

        findViewById(R.id.action_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, About.class));
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.action_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent new_note = new Intent(Main.this, MyDialog.class);
                new_note.setAction(Costants.ACTION_ADD_ITEM);
                startActivity(new_note);*/
                startActivity(new Intent(Main.this, MainList.class));
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
