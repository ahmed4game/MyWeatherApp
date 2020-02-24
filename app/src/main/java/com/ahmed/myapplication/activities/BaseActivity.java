package com.ahmed.myapplication.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.ahmed.myapplication.Constant;
import com.ahmed.myapplication.NetworkStateReceiver;
import com.ahmed.myapplication.retrofit.APIClient;
import com.ahmed.myapplication.retrofit.APIService;
import com.ahmed.myapplication.MyApplication;
import com.ahmed.myapplication.ui.locationfinder.LocationFinderFragment;
import com.ahmed.myapplication.utils.LocationUtils;
import com.ahmed.myapplication.utils.NetworkUtils;

import static com.ahmed.myapplication.MyApplication.getInstance;

public class BaseActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    private final String TAG = BaseActivity.class.getName();
    private AlertDialog dialog;
    public APIService apiService;
    public MyApplication mInstance;
    private NetworkUtils networkUtils;
    private LocationUtils locationUtils;
    private NetworkStateReceiver networkStateReceiver;
    public Boolean isNetwrokAvailable = null;

    public LocationUtils getLocationUtils() {
        if (locationUtils == null)
            locationUtils = new LocationUtils(this);
        return locationUtils;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = APIClient.getClient(this).create(APIService.class);
        mInstance = getInstance();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }

    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG,msg);
    }

    public void showAlertDialog(String title, String msg, String posBtnName, String negBtnName, DialogInterface.OnClickListener pListener, DialogInterface.OnClickListener nListener) {
        dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(posBtnName,pListener)
                .setNegativeButton(negBtnName, nListener)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showToast("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        showToast("onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void networkAvailable() {
        showToast("Network Available");

            isNetwrokAvailable = true;
        sendBroadcast(new Intent(Constant.CONNECTIVITY_CHANGE_ACTION));
    }

    @Override
    public void networkUnavailable() {
        showToast("Network UnAvailable");
            isNetwrokAvailable = false;
        sendBroadcast(new Intent(Constant.CONNECTIVITY_CHANGE_ACTION));
    }
}
