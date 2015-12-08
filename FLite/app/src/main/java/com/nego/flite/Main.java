package com.nego.flite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;
import com.nego.flite.Adapter.ViewPagerAdapter;
import com.nego.flite.Pages.PageFragment;
import com.nego.flite.Pages.SettingsFragment;


public class Main extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager stack;

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

        ViewPagerAdapter mPageAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPageAdapter.addFrag(getString(R.string.title_action_help), getString(R.string.subtitle_action_help), R.drawable.ic_action_communication_call);
        mPageAdapter.addFrag(getString(R.string.title_feature_widget), getString(R.string.subtitle_feature_widget), R.drawable.ic_action_widget);
        mPageAdapter.addFrag(getString(R.string.title_feature_hide), getString(R.string.subtitle_features_hide), R.drawable.ic_action_hide_all);
        mPageAdapter.addFrag(getString(R.string.title_feature_url), getString(R.string.subtitle_feature_url), R.drawable.ic_action_explore);
        mPageAdapter.addFrag(getString(R.string.title_feature_image), getString(R.string.subtitle_feature_image), R.drawable.ic_action_camera);
        mPageAdapter.addFrag(getString(R.string.title_lock), getString(R.string.subtitle_add_pasw), R.drawable.ic_action_lock_big);
        mPageAdapter.addFrag(getString(R.string.title_feature_alarm), getString(R.string.subtitle_feature_alarm), R.drawable.ic_action_alarm_on);

        stack = (ViewPager) findViewById(R.id.stack);
        stack.setAdapter(mPageAdapter);

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
