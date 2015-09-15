package com.appavate.motoparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appavate.motoparking.util.IabHelper;
import com.appavate.motoparking.util.IabResult;
import com.appavate.motoparking.util.Purchase;


public class About extends ActionBarActivity {


    // General Related Variables
    private final String Email = "mailto:support@appavate.com";
    private final String AppName = "Auckland Moto Parking";
    private final String MarketName = "market://details?id=com.appavate.motoparking";
    private final String Website = "http://www.appavate.com";

    // Payment related variables
    IabHelper mHelper;
    private static final String TAG = "com.appavate.motoparking"; // For debugging really
    static final String ITEM_SKU = "motoparking.support"; // Same name as the dev console
    private String base64EncodedPublicKey = "";
    Button _support;
    TextView _donateDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        SetupBilling();
        // Register the buttons
        ImageView _logo = (ImageView)findViewById( R.id._logo);
        Button _rate = (Button)findViewById( R.id._rate);
        Button _email = (Button)findViewById( R.id._email );
        _support = (Button)findViewById( R.id._supportdev );

        // RATE the application
        _rate.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(MarketName));
                startActivity(intent);
            }

        });

        // Email Developer
        _email.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, AppName);
                intent.putExtra(Intent.EXTRA_TEXT, "");
                intent.setData(Uri.parse(Email)); // or just "mailto:" for blank
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                startActivity(intent);

                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(About.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        // When the Logo button is pressed
        _logo.setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Website));
                startActivity(browserIntent);
            }
        });


        // When the support dev option is pressed if it exists
        _support.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                BuyItem();

            }
        });
    }










//////////  IN APP PAYMENTS ///////////////

    // Set up the billing. Check if they've already purchased it locally.
    private void SetupBilling()
    {
        SharedPreferences myPrefs = getSharedPreferences("Pref", MODE_PRIVATE);
        Boolean paid = myPrefs.getBoolean("paid", false);
        if(paid == true)
        {
            _donateDescription.setText("Thanks for donating! We appreciate it greatly.");
            _support.setVisibility(View.GONE);
        }
        else
        {
            try
            {
                mHelper = new IabHelper(this, base64EncodedPublicKey);
                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result)
                    {
                        if (!result.isSuccess()) {
                            Log.d(TAG, "In-app Billing setup failed: " + result);
                        } else {
                            Log.d(TAG, "In-app Billing is set up OK");
                        }
                    }
                });
            }
            catch (Exception e) { }

        }
    }


    private void BuyItem()
    {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,  mPurchaseFinishedListener, "mypurchasetoken");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener  = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            int response = result.getResponse();
            if(response == 7)
            {
                UnlockPaid();
                Toast.makeText(getApplicationContext(), "You have donated previously. Thanks!", Toast.LENGTH_LONG).show();
            }

            else if (result.isFailure()) {
                Toast.makeText(getApplicationContext(), "Something went wrong. You were not charged. Please let us know if there's a problem.", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {

                // PURCHASE COMPLETED
                UnlockPaid();
                Toast.makeText(getApplicationContext(), "PURCHASE COMPLETED. You may need to restart the app.", Toast.LENGTH_LONG).show();
            }

        }
    };

    // Change the settings to display as paid
    private void UnlockPaid()
    {
        SharedPreferences myPrefs = getSharedPreferences("Pref", MODE_PRIVATE);
        SharedPreferences.Editor e = myPrefs.edit();
        e.putBoolean("paid", true);
        e.commit(); // this saves to disk and notifies observers

        //   _donateDescription.setText("Thanks for donating! We appreciate it greatly.");
        _support.setVisibility(View.GONE);
    }


    // Destroy the helper when done
    @Override
    public void onDestroy() {
        super.onDestroy();

        try
        {
            if (mHelper != null)
            {
                mHelper.dispose();
            }
            mHelper = null;
        }
        catch (Exception e)
        {

        }

    }







}
