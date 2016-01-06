package com.nego.flite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

public class MainList extends AppCompatActivity {

    private SharedPreferences SP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        // SETTINGS

        findViewById(R.id.action_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View container = findViewById(R.id.container_menu_settings);
                if (container.getVisibility() == View.VISIBLE) {
                    Utils.collapse(container);
                } else {
                    Utils.expand(container);
                }
                findViewById(R.id.arrow_settings).animate().rotation(findViewById(R.id.arrow_settings).getRotation() + 180).start();
            }
        });

        findViewById(R.id.section_notification_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(i);
            }
        });

        findViewById(R.id.section_alarm_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_ALARM_SETTINGS);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(i);
            }
        });

        findViewById(R.id.section_style_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_STYLE_SETTINGS);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(i);
            }
        });

        findViewById(R.id.section_application_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_APPLICATION_SETTINGS);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(i);
            }
        });

        // FEEDBACK AND HELP

        findViewById(R.id.action_feedback).setVisibility(View.GONE);
        /* TODO HELP

        findViewById(R.id.action_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawer.closeDrawer(GravityCompat.START);
            }
        });

        */

        findViewById(R.id.action_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainList.this, About.class));
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        // CATEGORIES

        final AppCompatCheckBox notes_check = (AppCompatCheckBox) findViewById(R.id.check_main_list);
        notes_check.setChecked(SP.getBoolean(Costants.PREFERENCES_LIST_NOTE, true));
        notes_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCES_LIST_NOTE, isChecked).apply();
            }
        });
        findViewById(R.id.section_main_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notes_check.setChecked(!notes_check.isChecked());
            }
        });

        final AppCompatCheckBox reminders_check = (AppCompatCheckBox) findViewById(R.id.check_reminders);
        reminders_check.setChecked(SP.getBoolean(Costants.PREFERENCES_LIST_REMINDERS, true));
        reminders_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCES_LIST_REMINDERS, isChecked).apply();
            }
        });
        findViewById(R.id.section_reminded).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reminders_check.setChecked(!reminders_check.isChecked());
            }
        });

        final AppCompatCheckBox starred_check = (AppCompatCheckBox) findViewById(R.id.starred_check);
        starred_check.setChecked(SP.getBoolean(Costants.PREFERENCES_LIST_STARRED, true));
        starred_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCES_LIST_STARRED, isChecked).apply();
            }
        });
        findViewById(R.id.section_starred).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starred_check.setChecked(!starred_check.isChecked());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
