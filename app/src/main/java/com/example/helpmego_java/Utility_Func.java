package com.example.helpmego_java;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;
/**
 * Class of utility functions that can be called from anywhere in the project
 * */
public class Utility_Func {

    public interface DelayCallback{
        void afterDelay();
    }

    public static void delay(int secs, final DelayCallback delayCallback){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayCallback.afterDelay();
            }
        }, secs * 1000); // afterDelay will be executed after (secs*1000) milliseconds.
    }

    public static boolean checkBluetooth(BluetoothAdapter bluetoothAdapter) {

        // Check if bluetooth is enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }
        else {
            return true;
        }
    }

    public static void requestUserBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, BluetoothDeviceList.REQUEST_ENABLE_BT);
    }

    public static void toast(Context context, String string) {

        Toast toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
        toast.show();
    }

}