package com.nego.flite;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Adapter.AdapterList;
import com.nego.flite.Functions.ReminderService;
import com.nego.flite.database.DbAdapter;

import java.net.URL;


public class Main extends AppCompatActivity {

    private SharedPreferences SP;
    private RecyclerView recList;
    private FloatingActionButton fab;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String query = "";
    private AdapterList mAdapter;
    private BroadcastReceiver mReceiver;
    private User accountUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.tab_intro(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowEnterTransitionOverlap(true);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        }

        recList = (RecyclerView) findViewById(R.id.listView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final Reminder to_archive = mAdapter.deleteElement(viewHolder.getAdapterPosition());
                if (to_archive != null) {
                    Snackbar.make(recList, getString(R.string.text_archived), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.action_unarchive), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ReminderService.startAction(Main.this, Costants.ACTION_UNARCHIVE, to_archive);
                                }
                            })
                            .show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recList);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent new_note = new Intent(Main.this, MyDialog.class);
                new_note.setAction(Costants.ACTION_ADD_ITEM);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Main.this,
                            Pair.create((View) fab, "action_button"));
                    startActivity(new_note, options.toBundle());
                } else {
                    startActivity(new_note);
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList(query);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);

        updateList(query);

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
                Intent i = new Intent(Main.this, Settings.class);
                i.putExtra(Costants.SECTION_SETTINGS, Costants.SECTION_NOTIFICATION_SETTINGS);
                startActivityForResult(i, Costants.CODE_REQUEST_SETTINGS);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        findViewById(R.id.action_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, About.class));
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
                updateList(query);
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
                updateList(query);
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
                updateList(query);
            }
        });
        findViewById(R.id.section_starred).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starred_check.setChecked(!starred_check.isChecked());
            }
        });

        final AppCompatCheckBox archived_check = (AppCompatCheckBox) findViewById(R.id.archived_check);
        archived_check.setChecked(SP.getBoolean(Costants.PREFERENCES_LIST_ARCHIVED, false));
        archived_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCES_LIST_ARCHIVED, isChecked).apply();
                updateList(query);
            }
        });
        findViewById(R.id.section_archived).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                archived_check.setChecked(!archived_check.isChecked());
            }
        });

        // HEADER
        updateHeader();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Costants.CODE_REQUEST_SETTINGS || requestCode == Costants.CODE_REQUEST_SIGN) {
            updateList(query);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(Costants.ACTION_UPDATE_LIST);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(Costants.EXTRA_ACTION_TYPE);
                Reminder r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                if (action != null && r != null && mAdapter != null) {
                    mAdapter.update(action, r);
                } else {
                    updateList(query);
                }
                setCount();
            }
        };
        registerReceiver(mReceiver, intentFilter);
        updateHeader();
    }

    @Override
    public void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_list, menu);
        if (mAdapter != null) {

            SearchManager searchManager = (SearchManager)
                    getSystemService(Context.SEARCH_SERVICE);
            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            if (searchMenuItem != null) {
                SearchView searchView = (SearchView) searchMenuItem.getActionView();

                searchView.setSearchableInfo(searchManager.
                        getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        query = s;
                        updateList(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        query = s;
                        updateList(query);
                        return false;
                    }
                });

                MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        findViewById(R.id.app_name).setVisibility(View.GONE);
                        updateList(query);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        findViewById(R.id.app_name).setVisibility(View.VISIBLE);
                        updateList(query);
                        invalidateOptionsMenu();
                        return true;
                    }
                });
            }

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void updateList(final String q) {

        mSwipeRefreshLayout.setRefreshing(true);

        query = q;
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                DbAdapter dbHelper = new DbAdapter(Main.this);
                dbHelper.open();

                final AdapterList adapter;
                adapter = new AdapterList(dbHelper, query, Main.this);

                dbHelper.close();

                mHandler.post(new Runnable() {
                    public void run() {
                        recList.setAdapter(adapter);
                        mAdapter = adapter;
                        setCount();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    public void setCount() {
        int c = Utils.itemsToDo(this);
        int[] cs = Utils.itemsToDoMultiple(this);
        ((TextView) findViewById(R.id.count_item_notes)).setText("" + cs[0]);
        ((TextView) findViewById(R.id.count_item_reminders)).setText("" + cs[1]);
        ((TextView) findViewById(R.id.count_item_starred)).setText("" + cs[2]);
        ((TextView) findViewById(R.id.count_item_archived)).setText("" + cs[3]);
        if (accountUser == null || accountUser.getEmail().equals("")) {
            if (c == 0) {
                ((TextView) findViewById(R.id.items_todo)).setText(getString(R.string.no_items));
            } else {
                ((TextView) findViewById(R.id.items_todo)).setText(getString(R.string.num_items_todo, c) + ".");
            }
        }

        if (mAdapter != null) {
            if (mAdapter.getItemCount() == 0) {
                if (query.equals("")) {
                    findViewById(R.id.no_notes).setVisibility(View.VISIBLE);
                    findViewById(R.id.no_notes_founded).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.no_notes).setVisibility(View.GONE);
                    findViewById(R.id.no_notes_founded).setVisibility(View.VISIBLE);
                }
            } else {
                findViewById(R.id.no_notes).setVisibility(View.GONE);
                findViewById(R.id.no_notes_founded).setVisibility(View.GONE);
            }
        }
    }

    public void updateHeader() {
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                DbAdapter dbHelper = new DbAdapter(Main.this);
                dbHelper.open();
                Cursor c = dbHelper.getActiveUser();
                accountUser = null;
                if (c.moveToFirst())
                    accountUser = new User(c);
                c.close();
                dbHelper.close();

                mHandler.post(new Runnable() {
                    public void run() {

                        if (accountUser != null) {
                            // NAME
                            ((TextView) findViewById(R.id.header_name)).setText(accountUser.getName());

                            // PHOTO
                            if (!accountUser.getPhoto().equals("")) {
                                ((ImageView) findViewById(R.id.account_photo)).setImageURI(Uri.parse(accountUser.getPhoto()));
                                findViewById(R.id.account_photo).setVisibility(View.VISIBLE);
                            } else {
                                findViewById(R.id.account_photo).setVisibility(View.GONE);
                            }

                            // EMAIL
                            ((TextView) findViewById(R.id.items_todo)).setText(accountUser.getEmail());

                            setCount();
                        } else {
                            // NAME
                            ((TextView) findViewById(R.id.header_name)).setText(Utils.getOwnerName(Main.this));

                            // PHOTO
                            findViewById(R.id.account_photo).setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();

        findViewById(R.id.header_nav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(Main.this, SignInActivity.class);
                startActivityForResult(login, Costants.CODE_REQUEST_SIGN);
            }
        });
    }

    public void recyclerGoUp() {
        recList.scrollToPosition(0);
    }
}
