package com.nego.flite;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.nego.flite.database.DbAdapter;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private CardView signInButton;

    private RecyclerView recList;


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

        signInButton = (CardView) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar(true);
                signIn();
            }
        });

        recList = (RecyclerView) findViewById(R.id.listView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        updateList();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
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

    public void setUserInfo(GoogleSignInAccount account) {
        User user = new User(account.getId(), account.getDisplayName(), account.getEmail(), account.getPhotoUrl().toString(), 0);
        DbAdapter dbHelper = new DbAdapter(SignInActivity.this);
        dbHelper.open();
        Cursor c = dbHelper.getUserById(user.getId());
        if (c.moveToFirst()) {
            Toast.makeText(this, getString(R.string.error_user_signed), Toast.LENGTH_LONG).show();
        } else {
            user.createUser(SignInActivity.this, dbHelper);
        }
        c.close();
        dbHelper.close();
        revokeAccess();
        updateList();
        showProgressBar(false);
        Toast.makeText(this, getString(R.string.text_account_added), Toast.LENGTH_SHORT).show();
    }

    public void deleteUserInfo(User user) {
        DbAdapter dbHelper = new DbAdapter(SignInActivity.this);
        dbHelper.open();
        user.deleteUser(this, dbHelper);
        dbHelper.close();
        updateList();
        Toast.makeText(this, getString(R.string.text_account_removed), Toast.LENGTH_SHORT).show();
    }

    public void showProgressBar(boolean b) {
        findViewById(R.id.progress_bar).setVisibility(b ? View.VISIBLE : View.GONE);
        findViewById(R.id.text_signin).setVisibility(!b ? View.VISIBLE : View.GONE);
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
                    }
                });
            }
        }).start();
    }
}
