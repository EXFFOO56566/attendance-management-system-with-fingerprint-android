package com.example.attendacewithfingerprint;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendacewithfingerprint.database.DBManager;
import com.example.attendacewithfingerprint.sharedpreferences.PrefKeys;
import com.example.attendacewithfingerprint.sharedpreferences.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This is Splash Class
 */

public class SplashActivity extends AppCompatActivity {

    boolean hasDataStatusCheck;
    public DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //open database sqlite
        dbManager = new DBManager(this);
        dbManager.open();

        //--------------Checking for prevent twice check-in or check-out----------------------------
        //Get Date today
        @SuppressLint("SimpleDateFormat") String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        boolean hasDataCheckDate = PrefUtils.hasKey(this, PrefKeys.CHECK_DATE);
        hasDataStatusCheck = PrefUtils.hasKey(this, PrefKeys.STATUS_CHECK);

        // For first time use the app for settings
        if (!PrefUtils.hasKey(this, PrefKeys.IS_FIRST_TIME)) {
            PrefUtils.saveToPrefs(this, PrefKeys.IS_FIRST_TIME, "YES");
        }

        // For first time use the app for check date
        if (!hasDataCheckDate) {
            PrefUtils.saveToPrefs(this, PrefKeys.CHECK_DATE, today);
        }

        // For first time use the app for check status
        if (!hasDataStatusCheck) {
            // If STATUS_CHECK has no data will save with value check-out
            PrefUtils.saveToPrefs(this, PrefKeys.STATUS_CHECK, "CHECK-OUT");
        }

        // Get status check in or out
        String getDate = (String) PrefUtils.getFromPrefs(this, PrefKeys.CHECK_DATE, "");

        if (!getDate.equals(today)) {
            PrefUtils.saveToPrefs(this, PrefKeys.STATUS_CHECK, "CHECK-OUT");
        }
        //--------------Checking for prevent twice check-in or check-out----------------------------

        //Time Splash Screen
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // Check if setting already filled
                    if (!checkIfDoSetting()) {
                        Intent settingActivity = new Intent(SplashActivity.this, SettingsActivity.class);
                        SplashActivity.this.startActivity(settingActivity);
                        SplashActivity.this.finish();
                    } else {
                        PrefUtils.saveToPrefs(SplashActivity.this, PrefKeys.IS_FIRST_TIME, "NO");
                        // After finnish time go to next class
                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                        SplashActivity.this.startActivity(mainIntent);
                        SplashActivity.this.finish();
                    }

                }
            }
        };
        timerThread.start();
        //Time Splash Screen
    }

    public boolean checkIfDoSetting() {
        //check if empty database
        return !dbManager.checkIfEmpty("DATASABSENT");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }

}
