package com.nego.flite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.nego.flite.Adapter.ViewPagerAdapter;
import com.nego.flite.Pages.FeaturesFragment;
import com.nego.flite.Pages.PageFragment;
import com.nego.flite.Pages.SettingsFragment;


public class Main extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.tab_intro(this);

        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.action_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent new_note = new Intent(Main.this, MyDialog.class);
                new_note.setAction(Costants.ACTION_ADD_ITEM);
                startActivity(new_note);
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

        if (id == R.id.action_settings) {
            startActivity(new Intent(Main.this, Settings.class));
        }

        if (id == R.id.action_about) {
            startActivity(new Intent(Main.this, About.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
