package com.nego.flite;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.nego.flite.Billing.util.IabHelper;
import com.nego.flite.Billing.util.IabResult;
import com.nego.flite.Billing.util.Inventory;
import com.nego.flite.Billing.util.Purchase;

import java.util.ArrayList;
import java.util.List;

public class About extends AppCompatActivity {

    static final String TAG = "BILLING_FOCUS";
    static final String SKU_ICE_CREAM = "ice_cream";
    static final String SKU_COFFEE = "cup_of_coffee";
    static final int RC_REQUEST = 10001;
    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_develop);


        String version = "";
        try {
            version = " " + getString(R.string.text_version) + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.title_version)).setText(getString(R.string.app_name) + version);


        findViewById(R.id.action_open_focus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=nego.reminders")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=nego.reminders")));
                }
            }
        });

        findViewById(R.id.action_rate_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nego.flite")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.nego.flite")));
                }
            }
        });

        findViewById(R.id.action_community).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/100614116200820350356/stream/31109315-898b-4924-8a7f-3ed5e49c511e")));
            }
        });

        findViewById(R.id.action_gift_cafe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArRRqQld0sYyOPAzxLGyLbmYKW+LfGPMcBjpInLCy6uxAmD8YxcOuU7aredzbDr8MProNc3hg6nFak/H8ppN3C+3FNRC8ba/DbyetRF8QcwjHPI95bDEyX3Ly7uIExEqPLz1tjQM0qwBEToQvEW8ZdG7L8iSWxEqfXhXddqjmJ3CjarUB6qIHUPX7fZxETEbaUiSI/tPBhDj/fX3WTp68we/1lO91SyJi7Wd2HK4ODcQCZGwTYc/UDmEhUPnY0mxY3HBzKJsoXiN4g5+fSd7fOb25EI19/ri3ryd639bmpqv/iZ0phgzZqG+aBZd9c/M8x1lJ/AkL9jEQuq9iGxuoyQIDAQAB";


                // Create the helper, passing it our context and the public key to verify signatures with
                mHelper = new IabHelper(About.this, base64EncodedPublicKey);

                // Start setup. This is asynchronous and the specified listener
                // will be called once setup completes.
                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {
                        Log.d(TAG, "Setup finished.");

                        if (!result.isSuccess()) {
                            // Oh noes, there was a problem.
                            complain("Problem setting up in-app billing: " + result);
                            return;
                        }

                        // Have we been disposed of in the meantime? If so, quit.
                        if (mHelper == null) return;

                        // IAB is fully set up. Now, let's get an inventory of stuff we own.
                        Log.d(TAG, "Setup successful. Querying inventory.");
                        List additionalSkuList = new ArrayList();
                        additionalSkuList.add(SKU_COFFEE);
                        additionalSkuList.add(SKU_ICE_CREAM);
                        mHelper.queryInventoryAsync(true, additionalSkuList,
                                mQueryFinishedListener);
                    }
                });
            }
        });

        findViewById(R.id.action_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //getMenuInflater().inflate(R.menu., menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
    // Example with more product
    IabHelper.QueryInventoryFinishedListener
            mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Purchase coffee =
                    inventory.getPurchase(SKU_COFFEE);

            if (!inventory.hasPurchase(SKU_COFFEE)) {
                mHelper.launchPurchaseFlow(About.this,  SKU_COFFEE, RC_REQUEST,
                        mPurchaseFinishedListener, "");
            } else {
                mHelper.consumeAsync(coffee, new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        mHelper.launchPurchaseFlow(About.this, SKU_COFFEE, RC_REQUEST,
                                mPurchaseFinishedListener, "");
                    }
                });
            }

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_COFFEE)) {
                Log.d(TAG, "Purchase is coffee.");
                mHelper.consumeAsync(purchase, null);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    public void complain(String message) {
        Log.e(TAG, "Billing Error: " + message);
        //Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
    }
}
