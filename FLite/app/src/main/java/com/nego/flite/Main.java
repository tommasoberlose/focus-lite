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
    private SwitchCompat switchCompat;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.tab_intro(this);

        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);


        mViewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabview);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        findViewById(R.id.action_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent new_note = new Intent(Main.this, MyDialog.class);
                new_note.setAction(Costants.ACTION_ADD_ITEM);
                startActivity(new_note);*/

                startActivity(new Intent(Main.this, About.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        switchCompat = (SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.active_switch));

        SharedPreferences SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        switchCompat.setChecked(SP.getBoolean("enable_not_add", false));
        Utils.notification_add_update(this);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utils.notification_add_update(Main.this, isChecked);
                String text = getString(R.string.app_enabled);
                if (!isChecked)
                    text = getString(R.string.app_disabled);
                Utils.SnackbarC(Main.this, text, findViewById(R.id.action_feedback));
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FeaturesFragment(), getResources().getString(R.string.title_tab_features));
        adapter.addFrag(new SettingsFragment(), getResources().getString(R.string.title_tab_settings));
        adapter.addFrag(new PageFragment(), getResources().getString(R.string.title_tab_developer));
        viewPager.setAdapter(adapter);
    }

}
