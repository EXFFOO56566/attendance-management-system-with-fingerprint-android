package com.example.attendacewithfingerprint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.attendacewithfingerprint.database.DBManager;
import com.example.attendacewithfingerprint.database.DatabaseHelper;
import com.example.attendacewithfingerprint.gps.LocationManagerInterface;
import com.example.attendacewithfingerprint.gps.SmartLocationManager;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.example.attendacewithfingerprint.database.DatabaseHelper.LAT;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.LONGT;

public abstract class BaseActivity extends AppCompatActivity implements LocationManagerInterface {

    // Check permission
    private void checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int locationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int locationPermission1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (locationPermission1 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    Context mContext;
    LocationManager manager;

    SmartLocationManager mLocationManager;

    public DBManager dbManager;
    List<LatLng> points = new ArrayList<>();

    Handler messageHandler;
    // Indicate location is accurate
    public boolean locationIsAccurate;
    // Indicate there is any error
    public boolean errorMessage = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        messageHandler = new Handler();

        //open database sqlite
        dbManager = new DBManager(this);
        dbManager.open();

        // Init location manager
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager = new SmartLocationManager(mContext, this, this, SmartLocationManager.ALL_PROVIDERS, LocationRequest.PRIORITY_HIGH_ACCURACY, 10 * 1000, 1000); // init location manager

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermissions();
        } else {
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.displayLocationSettingsRequest(mContext, this);
            }
        }
    }

    // get address location
    @SuppressLint("LongLogTag")
    public String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        //Set Address
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                //Get String Address
                strAdd = address + ".";

            } else {
                strAdd = "No Address returned" + getString(R.string.n);
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            strAdd = "Cannot get Address." + getString(R.string.n) + "note: we need internet access" + getString(R.string.n) + "(try rebooting your device)" + getString(R.string.n);
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }

    //-----location
    // After user give permissions will handle to this
    // The result code is 50 mean user allow permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mLocationManager.displayLocationSettingsRequest(mContext, this);
                }
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(mContext, getString(R.string.need_grant_permission), Toast.LENGTH_SHORT).show();

                // show stale location if not grant permission
                mLocationManager.getStaleLocation();
            }
        }
    }

    public void locationFetched(Location mLocal, Location oldLocation, String time, String locationProvider) {
        mLocal.getLatitude();
        mLocal.getLongitude();
    }

    protected void onStart() {
        super.onStart();
//        mLocationManager.startLocationFetching();
    }

    protected void onResume() {
        super.onResume();
        mLocationManager.startLocationFetching();
    }

    protected void onStop() {
        super.onStop();
        mLocationManager.abortLocationFetching();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mLocationManager.pauseLocationFetching();
    }
    //-----location

    // Hash location polygon
    public static String MD5_Hash(String s) {
        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        assert m != null;
        m.update(s.getBytes(), 0, s.length());
        return new BigInteger(1, m.digest()).toString(16);
    }

    // Check if current location inside the area
    public boolean checkArea(LatLng currentLocationLatLng) {
        return PolyUtil.containsLocation(currentLocationLatLng, points, true);
    }

    // Get Json from http
    void getData(String URL, final String key, final String typeData) {
        OkHttpClient client = new OkHttpClient();

        // Param POST
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", key)
                .build();

        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "identity")
                .url(URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Get error message
                String mMessage = e.getMessage();

                // Set error message
                errorMessage = true;
                setMessage("onFailure: Error " + mMessage);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                ResponseBody body = response.body();
                if (body != null)
                    try {
                        String returnData = null;
                        if (response.body() != null) {
                            returnData = body.string();
                        }
                        // Parse Json
                        parseResult(returnData, typeData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    // Parse result json
    private void parseResult(String result, String data) {

        if (data.equals("location")) {
            // Delete first if any change location area
            dbManager.deleteAllLocation();

            try {
                // Create a JSONObject from the JSON response string
                JSONObject baseJsonResponse = new JSONObject(result);

                //Create the JSONObject with the key "response"
                String message = baseJsonResponse.getString("message");
                switch (message) {
                    case "success":

                        // Extract the JSONArray associated with the key called "results",
                        // which represents a list of news stories.
                        JSONArray newsStoryArray = baseJsonResponse.getJSONArray("data");

                        // For each newsStory in the newsStoryArray, create an NewsStory object
                        for (int i = 0; i < newsStoryArray.length(); i++) {

                            // Get a single newsStory at position i within the list of news stories
                            JSONObject currentStory = newsStoryArray.getJSONObject(i);

                            // Extract the value for the key called "webTitle"
                            String lat = currentStory.getString("lat");

                            // Extract the value for the key called "sectionName"
                            String longt = currentStory.getString("longt");

                            dbManager.insertLocation(Double.parseDouble(lat), Double.parseDouble(longt));

                        }

                        // DB is not empty
                        Cursor cursor = dbManager.getLocation();
                        if (cursor.moveToFirst()) {
                            points.clear();
                            do {
                                String lat = cursor.getString(cursor.getColumnIndex(LAT));
                                String longt = cursor.getString(cursor.getColumnIndex(LONGT));
                                points.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(longt)));

                            } while (cursor.moveToNext());
                        }
                        locationIsAccurate = true;
                        break;
                    case "empty":
                        errorMessage = true;
                        setMessage(getString(R.string.ask_admin));
                        break;
                    case "Your key is wrong":
                        errorMessage = true;
                        setMessage(getString(R.string.key_wrong));
                        break;
                    default:
                        errorMessage = true;
                        setMessage(getString(R.string.insert_key_first));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage = true;
                setMessage(getString(R.string.url_incorrect));
            }
        } else if (data.equals("hash")) {
            try {
                // Create a JSONObject from the JSON response string
                JSONObject baseJsonResponse = new JSONObject(result);

                //Create the JSONObject with the key "response"
                String message = baseJsonResponse.getString("message");
                switch (message) {
                    case "success":

                        // Extract the JSONArray associated with the key called "results",
                        // which represents a list of news stories.
                        JSONArray newsStoryArray = baseJsonResponse.getJSONArray("data");
                        String getMd5 = null;
                        // For each newsStory in the newsStoryArray, create an NewsStory object
                        for (int i = 0; i < newsStoryArray.length(); i++) {

                            // Get a single newsStory at position i within the list of news stories
                            JSONObject currentStory = newsStoryArray.getJSONObject(i);

                            // Extract the value for the key called "webTitle"
                            getMd5 = currentStory.getString("md5");
                        }

                        if (points.size() == 0) {
                            // DB is not empty
                            Cursor cursor = dbManager.getLocation();
                            if (cursor.moveToFirst()) {
                                do {
                                    String lat = cursor.getString(cursor.getColumnIndex(LAT));
                                    String longt = cursor.getString(cursor.getColumnIndex(LONGT));
                                    points.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(longt)));

                                } while (cursor.moveToNext());
                            }
                        }

                        // Get has form database
                        String hash = MD5_Hash(String.valueOf(points));

                        // if Md5 on server different on app
                        assert getMd5 != null;
                        if (!getMd5.equals(hash)) {
                            locationIsAccurate = false;

                            // If DB is not empty
                            Cursor cursorGetItem = dbManager.getItem();
                            String URL;
                            String linkload = null;
                            String keyload = null;
                            if (cursorGetItem != null && cursorGetItem.moveToFirst()) {
                                linkload = cursorGetItem.getString(cursorGetItem.getColumnIndex
                                        (DatabaseHelper.LINK));
                                keyload = cursorGetItem.getString(cursorGetItem.getColumnIndex
                                        (DatabaseHelper.KEY_API));
                                cursorGetItem.close();
                            }

                            // Check if the end is or not "/" (Slash)
                            assert linkload != null;
                            String getLast = linkload.substring(linkload.length() - 1);
                            if (getLast.equals("/")) {
                                URL = linkload + "api/data/index";
                            } else {
                                URL = linkload + "/api/data/index";
                            }

                            // Get data office location
                            getData(URL, keyload, "location");

                        } else {
                            locationIsAccurate = true;
                        }
                        break;
                    case "empty":
                        errorMessage = true;
                        setMessage(getString(R.string.ask_admin));
                        break;
                    case "Your key is wrong":
                        errorMessage = true;
                        setMessage(getString(R.string.key_wrong));
                        break;
                    default:
                        errorMessage = true;
                        setMessage(getString(R.string.insert_key_first));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage = true;
                setMessage(getString(R.string.url_incorrect));
            }
        } else {
            errorMessage = true;
            setMessage("Something error, with type data..");
        }
    }

    // function set the message on Runnable
    public void setMessage(final String theMessage) {
        Runnable doDisplayError = new Runnable() {
            public void run() {
                Toast.makeText(mContext, theMessage, Toast.LENGTH_LONG).show();
            }
        };
        messageHandler.post(doDisplayError);
    }

    // Show popup when user outside attendance from office
    public void showDialogOutsideOffice(final Activity activity) {

        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.out_of_working))
                .setMessage(getString(R.string.location_outside_office))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        // Go to MainActivity
                        Intent mainIntent = new Intent(activity, MainActivity.class);
                        activity.startActivity(mainIntent);
                        activity.finish();
                    }

                })
                .create();

        builder.show();
    }

    // Check internet connection
    public boolean isNetworkAvailable(boolean SlowButMoreReliable) {
        boolean Result = false;
        try {
            if (SlowButMoreReliable) {
                ConnectivityManager MyConnectivityManager;
                MyConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo MyNetworkInfo;
                MyNetworkInfo = MyConnectivityManager.getActiveNetworkInfo();

                Result = MyNetworkInfo != null && MyNetworkInfo.isConnected();

            } else {
                Runtime runtime = Runtime.getRuntime();
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                int i = ipProcess.waitFor();
                Result = i == 0;
            }

        } catch (Exception ex) {
            //Common.Exception(ex);
        }
        return !Result;
    }
}