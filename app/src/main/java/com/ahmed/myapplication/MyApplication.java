package com.ahmed.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class MyApplication extends Application {

    private final String TAG = MyApplication.class.getName();
    private static MyApplication INSTANCE;
    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public static MyApplication getInstance(){
        return INSTANCE;
    }

    public void showToast(String msg){
        Toast.makeText(MyApplication.this, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG,msg);
    }

    public void showAlertDialog(Context c,String title, String msg, String posBtnName, String negBtnName, DialogInterface.OnClickListener pListener, DialogInterface.OnClickListener nListener) {
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(posBtnName,pListener)
                .setNegativeButton(negBtnName, nListener)
                .show();
    }

}
