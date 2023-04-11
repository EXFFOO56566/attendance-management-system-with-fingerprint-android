package com.example.attendacewithfingerprint;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.attendacewithfingerprint.gps.LocationManagerInterface;
import com.example.attendacewithfingerprint.sharedpreferences.PrefKeys;
import com.example.attendacewithfingerprint.sharedpreferences.PrefUtils;

// Main activity
public class MainActivity extends BaseActivity implements LocationManagerInterface {

    double latitude; // Latitude
    double longitude; // Longitude

    boolean hasDataStatusCheck;
    String getStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if STATUS_CHECK has data
        hasDataStatusCheck = PrefUtils.hasKey(this, PrefKeys.STATUS_CHECK);

        // Get status check in or out
        getStatus = (String) PrefUtils.getFromPrefs(this, PrefKeys.STATUS_CHECK, "");

        // Click check-in
        LinearLayout inbutton = findViewById(R.id.inbutton);
        inbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Check if status equals check-out
                if (hasDataStatusCheck && getStatus.equals("CHECK-OUT")) {
                    // can click button
                    Intent intent = new Intent(getApplicationContext(), InTimeActivity.class);
                    startActivity(intent);
                } else {
                    // Already check-in
                    Toast.makeText(MainActivity.this, getString(R.string.already_check_in), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Click check-out
        LinearLayout outbutton = findViewById(R.id.outbutton);
        outbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Check if status equals check-out
                if (hasDataStatusCheck && getStatus.equals("CHECK-IN")) {
                    Intent intent = new Intent(getApplicationContext(), OutTimeActivity.class);
                    startActivity(intent);
                } else {
                    // Already check-in
                    Toast.makeText(MainActivity.this, getString(R.string.already_check_out), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Click setting
        LinearLayout settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Click check attendance
        LinearLayout webview = findViewById(R.id.check);
        webview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CheckAttendanceActivity.class);
                startActivity(intent);
            }
        });
    }

    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Action menu
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.aandt.attendacewithfingerprint"); // url for share the app
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
                return true;
            case R.id.mail:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"abedputra@gmail.com"}); // Change email for contact dev
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                startActivity(Intent.createChooser(intent, getString(R.string.send_via)));
                return true;
            case R.id.apps:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=AbedPutra"))); // change url for other apps
                return true;
            case R.id.rate:
                // rate implementation here
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.aandt.attendacewithfingerprint"))); // rate the app
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Get location lat long
    public void locationFetched(Location mLocal, Location oldLocation, String time, String locationProvider) {
        LatLongit(mLocal.getLatitude(), mLocal.getLongitude());
    }

    // Get location for first time
    public void LatLongit(double latitudeValue, double longitudeValue) {
        latitude = latitudeValue;
        longitude = longitudeValue;
    }

    // Update status on resume
    protected void onResume() {
        super.onResume();
        getStatus = (String) PrefUtils.getFromPrefs(this, PrefKeys.STATUS_CHECK, "");
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}