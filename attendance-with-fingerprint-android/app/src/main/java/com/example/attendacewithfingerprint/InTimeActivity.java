package com.example.attendacewithfingerprint;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.attendacewithfingerprint.database.DBManager;
import com.example.attendacewithfingerprint.database.DatabaseHelper;
import com.example.attendacewithfingerprint.gps.LocationManagerInterface;
import com.example.attendacewithfingerprint.sharedpreferences.PrefKeys;
import com.example.attendacewithfingerprint.sharedpreferences.PrefUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InTimeActivity extends BaseActivity implements LocationManagerInterface {

    private TextView textViewName, titleName, tanggal, info, location, infoTitle;

    String name, date, in_time, command;
    Handler messageHandler;

    public DBManager dbManager;
    String linkload, keyload, strAdd, URL, userName, idNumber;

    // Geolocation
    double latitude;
    double longitude;
    private EditText latEd, longEd;

    // Handler
    Handler handler;
    // Progress Dialog
    public ProgressDialog dialogMessage;

    // Fingerprint
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    RelativeLayout relativeLayout_in_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_out_time);

        // Init dialog
        dialogMessage = new ProgressDialog(InTimeActivity.this);
        dialogMessage.setCancelable(false);
        dialogMessage.setCanceledOnTouchOutside(false);

        latEd = findViewById(R.id.lat);
        longEd = findViewById(R.id.longt);

        relativeLayout_in_out = findViewById(R.id.relativeLayout_in_out);
        // Change background image
        changeBackground(relativeLayout_in_out, R.drawable.background_product);

        messageHandler = new Handler();

        //View objects
        textViewName = findViewById(R.id.textViewName);
        titleName = findViewById(R.id.titleName);
        location = findViewById(R.id.location);
        tanggal = findViewById(R.id.tanggal);
        info = findViewById(R.id.info);
        infoTitle = findViewById(R.id.infoTitle);

        //open databse
        dbManager = new DBManager(this);
        dbManager.open();

        // Check if url, key and name already filled in database
        checkUrlKeyName();

        // Fingerprint
        Executor executor = Executors.newSingleThreadExecutor();

        FragmentActivity activity = this;

        biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    // user clicked negative button
                    if (dialogMessage!=null && dialogMessage.isShowing()) {
                        // Dismiss dialog
                        dialogMessage.dismiss();
                        stopHandler();
                    }
                } else {
                    // Called when an unrecoverable error has been encountered and the operation is complete.
                    displayToastMessage("text", "", R.string.unrecoverable_error_has_been_encountered);
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Sent data to server
                sendDataAttendance();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Called when a biometric is valid but not recognized.
                displayToastMessage("text", "", R.string.valid_biometric_not_recognized);
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.attendance))
                .setDescription(getString(R.string.absent_with_fingerprint))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
    }

    public void sendDataAttendance() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Get Date
                @SuppressLint("SimpleDateFormat") String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                //Get Time
                Date d = new Date();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String currentDateTimeString = sdf.format(d);

                // Get data like name from json QR
                name = userName + " #" + idNumber;
                command = "in";
                date = today;
                in_time = currentDateTimeString;

                latitude = Double.parseDouble(latEd.getText().toString());
                longitude = Double.parseDouble(longEd.getText().toString());

                strAdd = "Office";
                location.setText("Office");

                new SendOTPTask().execute(keyload, command, name, date, in_time, strAdd);
            }
        });
    }

    // Function change background image
    public void changeBackground(RelativeLayout relativeLayout, int idImage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackground(ContextCompat.getDrawable(this, idImage));
        } else {
            relativeLayout.setBackgroundDrawable(ContextCompat.getDrawable(this, idImage));
        }
    }

    // Send data to database (MSQL) via asyncTask
    private class SendOTPTask extends AsyncTask<String, String, Void> {
        String responseString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogMessage.setMessage(getString(R.string.sending_attendance));
            dialogMessage.show();
        }

        @Override
        protected Void doInBackground(String... str) {

            OkHttpClient client = new OkHttpClient();

            // Param POST
            RequestBody formBody = new FormBody.Builder()
                    .add("key", str[0])
                    .add("q", str[1])
                    .add("name", str[2])
                    .add("date", str[3])
                    .add("in_time", str[4])
                    .add("location", str[5])
                    .build();

            // request
            Request request = new Request.Builder()
                    .addHeader("Accept-Encoding", "identity")
                    .url(getUrl("absent_attendance"))
                    .post(formBody)
                    .build();

            Response response;
            try {
                response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    responseString = getString(R.string.error_unexpected) + response;
                    displayToastMessage("setText", responseString, 0);
                    throw new IOException("Unexpected code " + response);
                } else {
                    responseString = response.body().string();
                    displayToastMessage("setText", responseString, 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayToastMessage("setText", getString(R.string.server_error_or_url), 0);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);

            if (dialogMessage!=null && dialogMessage.isShowing()) {
                tanggal.setText(date);
                textViewName.setText(userName);
                textViewName.setVisibility(View.VISIBLE);
                titleName.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
                infoTitle.setVisibility(View.VISIBLE);

                // Change background image
                changeBackground(relativeLayout_in_out, R.drawable.background_color_blank);

                dialogMessage.dismiss();
            }
        }
    }

    // Display message from runnable
    public void displayToastMessage(final String type, final String setText, final int theMessage) {
        Runnable doDisplayError = new Runnable() {
            public void run() {
                if (type.equals("text")) {
                    Toast.makeText(getApplicationContext(), getString(theMessage), Toast.LENGTH_LONG).show();
                } else if (setText.equals("Success!")) {
                    // Save status
                    PrefUtils.saveToPrefs(InTimeActivity.this, PrefKeys.STATUS_CHECK, "CHECK-IN");
                    dbManager.insertAttendance(userName, date, in_time, "Check-In", strAdd);
                }
                info.setText(setText);
            }
        };
        messageHandler.post(doDisplayError);
    }


    @SuppressLint("SetTextI18n")
    public void locationFetched(Location mLocal, Location oldLocation, String time, String locationProvider) {
        latEd.setText("" + mLocal.getLatitude());
        longEd.setText("" + mLocal.getLongitude());
    }

    // Handler show popup
    public void showPopupLocationHandler() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (mLocationManager.isGetAccuracyInfo()) {
                    if (dialogMessage!=null && dialogMessage.isShowing()) {
                        // Dismiss dialog
                        dialogMessage.dismiss();
                        stopHandler();

                        // Handler internet connection
                        if (isNetworkAvailable(true)) {
                            Toast.makeText(InTimeActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                        } else {
                            if (isNetworkAvailable(false)) {
                                Toast.makeText(InTimeActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();

                            } else {
                                // Check MD5 hash
                                checkMd5();
                                // is location is accurate
                                checkLocationAccurate();
                            }
                        }
                    }
                } else {
                    handler.postDelayed(this, 1000);
                    dialogMessage.setMessage(getString(R.string.trying_location));
                    dialogMessage.show();
                }
            }
        }, 50);  // The time is in milliseconds
    }

    public void checkMd5() {
        getData(getUrl("getMd5Location"), keyload, "hash");
    }

    public boolean getLocationAccurate() {
        return locationIsAccurate;
    }

    public boolean getErrorMessage() {
        return errorMessage;
    }

    public void checkLocationAccurate() {
        dialogMessage.setCancelable(true);
        dialogMessage.setCanceledOnTouchOutside(true);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {

                if (getLocationAccurate()) {
                    latitude = Double.parseDouble(latEd.getText().toString());
                    longitude = Double.parseDouble(longEd.getText().toString());
                    LatLng getCurrent = new LatLng(latitude, longitude);

                    checkInArea(checkArea(getCurrent));

                    if (dialogMessage!=null && dialogMessage.isShowing()) {
                        dialogMessage.dismiss();
                        stopHandler();
                    }

                } else if (getErrorMessage()) {
                    if (dialogMessage!=null && dialogMessage.isShowing()) {
                        dialogMessage.dismiss();
                        stopHandler();
                    }
                } else {
                    handler.postDelayed(this, 1000);
                    dialogMessage.setMessage(getString(R.string.check_office_location));
                    dialogMessage.show();
                }
            }
        }, 50);  // The time is in milliseconds
    }

    // Function stop handler
    public void stopHandler() {
        handler.removeMessages(0);
    }

    // Check if user inside office
    public void checkInArea(boolean checkArea) {
        if (checkArea) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            showDialogOutsideOffice(InTimeActivity.this);
        }
    }

    public void checkUrlKeyName() {

        //check if empty database
        if (dbManager.checkIfEmpty("DATASABSENT")) {
            Toast.makeText(InTimeActivity.this, getString(R.string.please_setting_url_key), Toast.LENGTH_LONG).show();
        }
        //check if not empty database
        else if (!dbManager.checkIfEmpty("DATASABSENT")) {
            // If DB is not empty
            Cursor cursor = dbManager.getItem();
            if (cursor != null && cursor.moveToFirst()) {
                linkload = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.LINK));
                keyload = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.KEY_API));
                userName = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.USER_NAME));
                idNumber = cursor.getString(cursor.getColumnIndex
                        (DatabaseHelper.ID_NUMBER));
                cursor.close();
            }

            //check if null database url and key
            if (linkload.equals("") && keyload.equals("")) {
                Toast.makeText(InTimeActivity.this, getString(R.string.please_setting_url_key), Toast.LENGTH_LONG).show();
            } else if (userName.equals("")) {
                Toast.makeText(InTimeActivity.this, getString(R.string.empty_name), Toast.LENGTH_LONG).show();
            } else if (idNumber.equals("")) {
                Toast.makeText(InTimeActivity.this, getString(R.string.id_number_is_empty), Toast.LENGTH_LONG).show();
            } else if (!linkload.equals("") && !keyload.equals("")) {
                // Show popup search current location
                showPopupLocationHandler();
            } else if (keyload.equals("")) {
                Toast.makeText(InTimeActivity.this, getString(R.string.setting_key), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(InTimeActivity.this, getString(R.string.setting_url), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_check_in_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.refreshAction) {
            checkUrlKeyName();
        }
        return true;
    }

    // Function get URL from database
    public String getUrl(String urlData) {
        String getLast = linkload.substring(linkload.length() - 1);
        if (getLast.equals("/")) {
            URL = linkload + "api/data/" + urlData;
        } else {
            URL = linkload + "/api/data/" + urlData;
        }

        return URL;
    }
}
