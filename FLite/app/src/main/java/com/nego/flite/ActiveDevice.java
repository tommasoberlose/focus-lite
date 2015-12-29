package com.nego.flite;

import android.content.Intent;
import android.os.Bundle;
import android.provider.*;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nego.flite.Adapter.AdapterC;

public class ActiveDevice extends AppCompatActivity {

    private RecyclerView recList;

    private int type = Costants.ALARM_TYPE_BLUETOOTH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        if (getIntent().getIntExtra(Costants.EXTRA_ACTION_TYPE, Costants.ALARM_TYPE_BLUETOOTH) == Costants.ALARM_TYPE_BLUETOOTH) {
            type = Costants.ALARM_TYPE_BLUETOOTH;
            setTitle(getString(R.string.text_active_device));
        } else {
            type = Costants.ALARM_TYPE_WIFI;
            setTitle(getString(R.string.text_active_connection));
        }

        // RECYCLER LIST
        recList = (RecyclerView) findViewById(R.id.listView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getBaseContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        AdapterC mAdapter = new AdapterC(type, this);

        recList.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose_activated, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            if (type == Costants.ALARM_TYPE_BLUETOOTH)
                startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
            else
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));

            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
