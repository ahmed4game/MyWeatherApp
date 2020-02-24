package com.ahmed.myapplication.ui.location;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ahmed.myapplication.BuildConfig;
import com.ahmed.myapplication.MyApplication;
import com.ahmed.myapplication.R;
import com.ahmed.myapplication.activities.ScrollingActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class LocationFragment extends Fragment {

    private LocationViewModel mViewModel;
    public static String BROADCAST_ACTION = "com.action.activity.data";
    private MyApplication mInstance;
    private static final String TAG = LocationFragment.class.getSimpleName();

    // location last updated time
    private String mLastUpdateTime;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30 * 60 * 1000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 15 * 60 * 1000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private ScrollingActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ScrollingActivity) context;
    }

    private BroadcastReceiver activityDataReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            activity.showToast("onRestart LifeCycle called from fragment");
        }
    };

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                activity.showToast("LocationCallBack onLocationResult()");
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void testingNetworkProvider(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        Location location = null;
        double latitude = 0d, longitude = 0d;

        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    15000,
                    20, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            activity.showToast("onLocationChanged");
                            activity.showToast("Lat:"+location.getLatitude()+" Long:"+location.getLongitude()+" Provider:"+ location.getProvider());
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            activity.showToast("onStatusChanged "+provider);
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            activity.showToast("onProviderEnabled "+provider);
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            activity.showToast("onProviderDisabled "+provider);
                        }
                    });

            activity.showToast("Location");


            if (isNetworkEnabled){
                location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Toast.makeText(
                            getActivity(),
                            "Mobile Location (NW): \nLatitude: " + latitude
                                    + "\nLongitude: " + longitude,
                            Toast.LENGTH_LONG).show();
                }
            }




    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mInstance = MyApplication.getInstance();
        return inflater.inflate(R.layout.location_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
        permissionCheck();
        mViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
        mViewModel.getLocationLiveData().observe(this, locationModel -> {
            activity.showToast("Observer Data Called");
        });
    }

    private void permissionCheck() {
        // Resuming location updates depending on button state and
        // allowed permissions
        if (checkPermissions()) {
            activity.showToast("Location askPermission granted");
            startLocationUpdates();
        } else {
            activity.showToast("Location askPermission needed");
            askPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.showToast("onResume");
        getActivity().registerReceiver(activityDataReciever, new IntentFilter(BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(activityDataReciever);
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {

            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

            String result = null;
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                activity.showToast(e.getLocalizedMessage());
            }
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);


                StringBuilder sb = new StringBuilder();

                // Area
                sb.append(address.getSubLocality()).append("\n");

                sb.append(address.getLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName());
                result = sb.toString();

                // location last updated time
                activity.showToast("Last updated on: " + mLastUpdateTime);
                activity.showToast(result);
            }

        }

//        testingNetworkProvider(); //  working
    }

    private void askPermission() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // askPermission is granted
                        activity.showToast("Permission granted");
                        updateLocationUI();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of askPermission

                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            activity.showToast("Permission permenantly denied");
                            activity.showAlertDialog("Permission Alert", "Without Location Permission Application can't continue.\n Do you want to continue?",
                                    "OK", "CANCEL", (dialog, which) -> {
                                        // positive button listener
                                        openSettings();
                                    }, (dialog, which) -> {
                                        // negative button listener
                                        getActivity().finish();
                                    });

                        } else {
                            activity.showToast("Permission Denied");
                            activity.showAlertDialog("Permission Alert", "Without Location Permission Application can't continue.\n Do you want to continue?",
                                    "OK", "CANCEL", (dialog, which) -> {
                                        // positive button listener
                                        askPermission();
                                    }, (dialog, which) -> {
                                        // negative button listener
                                        getActivity().finish();
                                    });
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                        activity.showToast("Permission rationale");
                    }
                }).check();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent,REQUEST_CHECK_SETTINGS);
    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), locationSettingsResponse -> {
                    activity.showToast("All location settings are satisfied.");
                    activity.showToast("Started location updates!");



                    //noinspection MissingPermission
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, Looper.myLooper());

                    updateLocationUI();
                })
                .addOnFailureListener(getActivity(), e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            activity.showToast("Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
//                                startIntentSenderForResult(rae.getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                            } catch (IntentSender.SendIntentException sie) {
                                activity.showToast("PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings.";
                            Log.e(TAG, errorMessage);
                            activity.showToast(errorMessage);
                    }

                    updateLocationUI();
                });
    }

    @Override
    public void onActivityResult ( int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        activity.showToast("User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        activity.showToast("User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }


}
