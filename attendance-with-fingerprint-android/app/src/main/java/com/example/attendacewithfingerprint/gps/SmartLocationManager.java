package com.example.attendacewithfingerprint.gps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;


public class SmartLocationManager implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = SmartLocationManager.class.getSimpleName();

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private String locationProvider;                                                                // source of fetched location

    private Location mLastLocationFetched;                                                          // location fetched
    private Location mLocationFetched;                                                              // location fetched
    private Location networkLocation;
    private Location gpsLocation;

    private int mLocationPiority;
    private long mLocationFetchInterval;
    private long mFastestLocationFetchInterval;

    private Context mContext;                                                                       // application context
    private Activity mActivity;                                                                     // activity context
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationManagerInterface mLocationManagerInterface;

    private android.location.LocationManager locationManager;
    private android.location.LocationListener locationListener;

    private int mProviderType;
    private static final int NETWORK_PROVIDER = 1;
    public static final int ALL_PROVIDERS = 0;
    private static final int GPS_PROVIDER = 2;

    private boolean getAccuracyInfo = false;
    private String accuracyMeters;

    public SmartLocationManager(Context context, Activity activity, LocationManagerInterface locationInterface, int providerType, int locationPiority, long locationFetchInterval, long fastestLocationFetchInterval) {
        mContext = context;
        mActivity = activity;
        mProviderType = providerType;

        mLocationPiority = locationPiority;
        mLocationFetchInterval = locationFetchInterval;
        mFastestLocationFetchInterval = fastestLocationFetchInterval;

        mLocationManagerInterface = locationInterface;

        initSmartLocationManager();
    }


    private void initSmartLocationManager() {
        if (isGooglePlayServicesAvailable())                // if googleplay services available
            initLocationObjts();                            // init obj for google play service and start fetching location
        else
            getLocationUsingAndroidAPI();                   // otherwise get location using Android API
    }

    private void initLocationObjts() {
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(mLocationPiority)
                .setInterval(mLocationFetchInterval)                    // 10 seconds, in milliseconds
                .setFastestInterval(mFastestLocationFetchInterval);     // 1 second, in milliseconds

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        startLocationFetching();                                        // connect google play services to fetch location
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            getLocationUsingAndroidAPI();
        } else {
            setNewLocation(getBetterLocation(location, mLocationFetched), mLocationFetched);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            getLastKnownLocation();
        } else {
            int suitableMeter = 100; // adjust getAccuracy
            if (location.hasAccuracy() && location.getAccuracy() <= suitableMeter) {
                getAccuracyInfo = true;
            } else {
                getAccuracyInfo = false;
                accuracyMeters = "" + location.getAccuracy();
            }

            setNewLocation(getBetterLocation(location, mLocationFetched), mLocationFetched);
        }
    }

    public boolean isGetAccuracyInfo() {
        return getAccuracyInfo;
    }

    public String getAccuracyMeters() {
        return accuracyMeters;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mActivity, CONNECTION_FAILURE_RESOLUTION_REQUEST); // Start an Activity that tries to resolve the error
                getLocationUsingAndroidAPI();                                                                // try to get location using Android API locationManager
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void setNewLocation(Location location, Location oldLocation) {
        if (location != null) {
            mLastLocationFetched = oldLocation;
            mLocationFetched = location;
            // default value is false but user can change it
            // fetched location time
            String mLastLocationUpdateTime = DateFormat.getTimeInstance().format(new Date());
            locationProvider = location.getProvider();
            mLocationManagerInterface.locationFetched(location, mLastLocationFetched, mLastLocationUpdateTime, location.getProvider());
        }
    }

    private void getLocationUsingAndroidAPI() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        setLocationListner();
        captureLocation();
    }

    private void captureLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            if (mProviderType == SmartLocationManager.GPS_PROVIDER) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else if (mProviderType == SmartLocationManager.NETWORK_PROVIDER) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setLocationListner() {
        // Define a listener that responds to location updates
        locationListener = new android.location.LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location == null) {
                    getLastKnownLocation();
                } else {
                    setNewLocation(getBetterLocation(location, mLocationFetched), mLocationFetched);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    public Location getAccurateLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        try {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location newLocalGPS, newLocalNetwork;
            if (gpsLocation != null || networkLocation != null) {
                newLocalGPS = getBetterLocation(mLocationFetched, gpsLocation);
                newLocalNetwork = getBetterLocation(mLocationFetched, networkLocation);
                setNewLocation(getBetterLocation(newLocalGPS, newLocalNetwork), mLocationFetched);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return mLocationFetched;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void startLocationFetching() {
        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    public void pauseLocationFetching() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    public void abortLocationFetching() {
        mGoogleApiClient.disconnect();

        // Remove the listener you previously added
        if (locationManager != null && locationListener != null) {
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            try {
                locationManager.removeUpdates(locationListener);
                locationManager = null;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());

            }
        }
    }

    // Reset location
    public void resetLocation() {
        mLocationFetched = null;
        mLastLocationFetched = null;
        networkLocation = null;
        gpsLocation = null;
    }

    public void displayLocationSettingsRequest(Context context, final Activity newContext) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(newContext, 1);
                        } catch (IntentSender.SendIntentException ignored) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }


    private Location getLastKnownLocation() {
        locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation;
        // Or use LocationManager.GPS_PROVIDER
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        try {
            lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            return lastKnownLocation;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        return status == ConnectionResult.SUCCESS;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    private Location getBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return location;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return location;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return location;
        } else if (isNewer && !isLessAccurate) {
            return location;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return location;
        }
        return currentBestLocation;
    }

    /**
     * Checks whether two providers are the same
     */

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public Location getStaleLocation() {
        if (mLastLocationFetched != null) {
            return mLastLocationFetched;
        }
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if (mProviderType == SmartLocationManager.GPS_PROVIDER) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (mProviderType == SmartLocationManager.NETWORK_PROVIDER) {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            return getBetterLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER), locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
    }
}
