package com.nego.flite;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        findViewById(R.id.section_notification_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                startActivity(i);
            }
        });

        findViewById(R.id.section_alarm_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_ALARM_SETTINGS);
                startActivity(i);
            }
        });

        findViewById(R.id.section_style_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_STYLE_SETTINGS);
                startActivity(i);
            }
        });

        findViewById(R.id.section_application_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainList.this, Settings.class);
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
                startActivity(new Intent(MainList.this, About.class));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i_settings = new Intent(MainList.this, Settings.class);

        switch (id) {
            case R.id.section_notification_settings:
                i_settings.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                startActivity(i_settings);
                break;
            case R.id.section_alarm_settings:
                i_settings.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                startActivity(i_settings);
                break;
            case R.id.section_style_settings:
                i_settings.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                startActivity(i_settings);
                break;
            case R.id.section_application_settings:
                i_settings.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                startActivity(i_settings);
                break;
            case R.id.action_about:
                startActivity(new Intent(MainList.this, About.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
