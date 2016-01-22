package com.nego.flite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.nego.flite.Adapter.AdapterAccounts;
import com.nego.flite.Adapter.AdapterList;
import com.nego.flite.Functions.UserService;
import com.nego.flite.database.DbAdapter;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private RecyclerView recList;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.common_signin_button_text_long));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        recList = (RecyclerView) findViewById(R.id.listView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        updateList();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signin_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_add_account:
                showProgressBar(true);
                signIn();
                break;
        }



        return super.onOptionsItemSelected(item);
    }

    private void signIn() {
        if (mGoogleApiClient.isConnected()) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    private void signOut() {
        if (mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                        }
                    });
        }
    }

    private void revokeAccess() {
        if (mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            setUserInfo(acct);
        } else {
            Toast.makeText(this, getString(R.string.common_google_play_services_sign_in_failed_title), Toast.LENGTH_LONG).show();
            showProgressBar(false);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.common_google_play_services_sign_in_failed_title), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(Costants.ACTION_UPDATE_LIST_ACCOUNT);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(Costants.EXTRA_ACTION_TYPE);
                switch (action) {
                    case Costants.ACTION_CREATE:
                        Toast.makeText(SignInActivity.this, getString(R.string.text_account_added), Toast.LENGTH_SHORT).show();
                        break;
                    case Costants.ACTION_DELETE:
                        Toast.makeText(SignInActivity.this, getString(R.string.text_account_removed), Toast.LENGTH_SHORT).show();
                        break;
                    case Costants.GENERAL_ERROR:
                        Toast.makeText(SignInActivity.this, getString(R.string.error_user_signed), Toast.LENGTH_SHORT).show();
                        break;
                }
                updateList();
            }
        };
        registerReceiver(mReceiver, intentFilter);

        updateList();
    }

    @Override
    public void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    private User userToSave = null;
    public void setUserInfo(GoogleSignInAccount account) {
        userToSave = new User(account.getId(), account.getDisplayName(), account.getEmail(), account.getPhotoUrl().toString(), 0);
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                final String photo = Utils.savePhoto(SignInActivity.this, userToSave.getPhoto(), userToSave.getId());

                mHandler.post(new Runnable() {
                    public void run() {
                        userToSave.setPhoto(photo);
                        UserService.startAction(SignInActivity.this, Costants.ACTION_CREATE, userToSave);
                        revokeAccess();
                    }
                });
            }
        }).start();
    }

    public void showProgressBar(boolean b) {
        findViewById(R.id.progress_bar).setVisibility(b ? View.VISIBLE : View.GONE);
        findViewById(R.id.no_account).setVisibility(!b ? View.VISIBLE : View.GONE);
        if (b)
            Utils.collapse(recList);
        else
            Utils.expand(recList);
    }

    public void updateList() {
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                DbAdapter dbHelper = new DbAdapter(SignInActivity.this);
                dbHelper.open();

                final AdapterAccounts adapter = new AdapterAccounts(SignInActivity.this, dbHelper);

                dbHelper.close();

                mHandler.post(new Runnable() {
                    public void run() {
                        recList.setAdapter(adapter);
                        showProgressBar(false);
                        try {
                            findViewById(R.id.no_account).setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

}
