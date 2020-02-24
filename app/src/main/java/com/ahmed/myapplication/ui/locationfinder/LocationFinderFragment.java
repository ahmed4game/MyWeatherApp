package com.ahmed.myapplication.ui.locationfinder;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahmed.myapplication.Constant;
import com.ahmed.myapplication.models.LocationModel;
import com.ahmed.myapplication.R;
import com.ahmed.myapplication.activities.ScrollingActivity;
import com.ahmed.myapplication.utils.NetworkUtils;
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
import java.text.DateFormat;
import java.util.Date;

import static com.ahmed.myapplication.MyApplication.*;

public class LocationFinderFragment extends Fragment {

    private LocationFinderViewModel mViewModel;
    private boolean isUserPromptedForGPS;

    private ScrollingActivity activity;
    public static String BROADCAST_ACTION = "com.action.activity.data";

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

    private TextView area,lastUpdatedAt;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ScrollingActivity) context;
    }

    private void initLocationSetup() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                activity.showToast("onLocationResult::Provider = "+mCurrentLocation.getProvider() +" LatLng: " +
                        mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude());
                Address address = activity.getLocationUtils().updateLocationUI(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());

                if (address != null) {
                    mViewModel.setLocationModelMutableLiveData(new LocationModel(address.getSubLocality(),
                            address.getLocality(), address.getAdminArea(), DateFormat.getTimeInstance().format(new Date())));
                }else{
                    throw new UnsupportedOperationException("Google's GeoCoder API is not Stable");
                }
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


    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        activity.mInstance = getInstance();
        View view = inflater.inflate(R.layout.location_finder_fragment, container, false);

        area = view.findViewById(R.id.area);
        lastUpdatedAt = view.findViewById(R.id.last_updated_at);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(LocationFinderViewModel.class);
        mViewModel.getCurrentWeatherModelMutableLiveData().observe(getActivity(), locationModel -> {
            // TODO: 2020-02-21 Update your UI
            activity.showToast("ViewModel Observer triggered");
        });
        mViewModel.getLocationModelMutableLiveData().observe(this, locationModel -> {
            // TODO: 2020-02-24 Update Location UI (Header)
            activity.showToast("From ViewModel: "+locationModel.toString());
            area.setText(locationModel.getAreaName());
            lastUpdatedAt.setText("Last Update At: "+DateFormat.getTimeInstance().format(new Date()));
        });

        initLocationSetup();
        checkDependencies();
    }

    private void checkDependencies() {
        if (checkInternetConnection()) {
            checkPermissionDependencies();
        }
    }

    private void checkPermissionDependencies() {
        if (checkPermissions()) {
            activity.showToast("Location permission granted");
            // TODO: 2020-02-19 Start your flow here
//            testingNetworkProvider(); Depends on GPS
            startLocationUpdates();
        } else {
            activity.showToast("Location permission needed");
            permission();
        }
    }

    //region BroadCast Reciever responsible for Connectivity Change and Broadcast action
    private BroadcastReceiver activityDataReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(BROADCAST_ACTION)) {
                activity.showToast("onRestart LifeCycle called from Activity to Fragment");
                checkDependencies();
            }else if (intent.getAction().equals(Constant.CONNECTIVITY_CHANGE_ACTION)) {
                activity.showToast("Connectivity Changed");
                if (activity.isNetwrokAvailable)
                    activity.showToast("Internet turned ON");
                else {
                    activity.showToast("Internet turned OFF");
                    activity.showAlertDialog("Internet is Down", "With internet connection, we can provide better Data, Do you want turn on Internet?",
                            "SURE", "DISMISS",
                            (dialog, which) -> {
                                // TODO: 2020-02-24 Positive
                                if (!NetworkUtils.isInternetAvailable(activity))
                                    NetworkUtils.openSettings(activity);
                            },
                            (dialog, which) -> {
                                // TODO: 2020-02-24 Negative
                                dialog.dismiss();
                            });
                }
            }
        }
    };
    //endregion

    @Override
    public void onResume() {
        super.onResume();
        activity.showToast("onResume");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION);
        filter.addAction(Constant.CONNECTIVITY_CHANGE_ACTION);
        getActivity().registerReceiver(activityDataReciever, filter);

        if (isUserPromptedForGPS){
            if (!activity.getLocationUtils().isGpsOn()){
                activity.showAlertDialog("Location Alert",
                        "Your GPS provider is Off, Please Turn ON to continue",
                        "SURE", "DISMISS",
                        (dialog, which) -> {
                            startLocationUpdates();
                        },
                        (dialog, which) -> {
                            // TODO: 2020-02-21 handle user rejection to turn On GPS
                        });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(activityDataReciever);
    }

    private boolean checkInternetConnection() {
        if (NetworkUtils.isInternetAvailable(activity)){
            return true;
        }else {
            activity.showAlertDialog(null, "Please check your Internet connection.", "OK", "DISMISS",  (dialog, which) -> {
                if (!NetworkUtils.isInternetAvailable(activity)) {
                    NetworkUtils.openSettings(activity);
                } else {
                    checkPermissionDependencies();
                }
            }, (dialog,which) -> {
                if (!NetworkUtils.isInternetAvailable(activity)) {
                    activity.finish();
                } else {
                    checkPermissionDependencies();
                }
            });
            return false;
        }
    }

    private void permission() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted
                        activity.showToast("Permission granted");
                        // TODO: 2020-02-19 Start your flow here
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission

                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            activity.showToast("Permission permenantly denied");
                            activity.showAlertDialog("Permission Permenantl Denied Alert", "Without Location Permission Application can't continue.",
                                    "ALLOW", "I'M SURE", (dialog, which) -> {
                                        // positive button listener
                                        activity.getLocationUtils().openSettings();
                                    }, (dialog, which) -> {
                                        // negative button listener
                                        getActivity().finish();
                                    });

                        } else {
                            activity.showToast("Permission Denied");
                            activity.showAlertDialog("Permission Alert", "Without Location Permission Application can't continue.\n Do you want to continue?",
                                    "OK", "CANCEL", (dialog, which) -> {
                                        // positive button listener
                                        permission();
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
                                rae.startResolutionForResult(getActivity(), LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                                isUserPromptedForGPS = true;
                            } catch (IntentSender.SendIntentException sie) {
                                activity.showToast("PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings.";
                            activity.showToast("Error Message:- "+errorMessage);
                            activity.showToast(errorMessage);
                    }
                });
    }
}