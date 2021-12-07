package com.example.helpmego_java;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
/**
 * Class used to implement scanning capabilities for Bluetooth LE and make them available as more abstract calls
 * This class is what initializes the BT Radio directly.
 * */
public class Scanner_BTLE {

    private BluetoothDeviceList btdl;

    private BluetoothAdapter btadap;
    private boolean scanning;
    private Handler mHandler;
    ScanSettings settings;

    private long scanPeriod;
    private int signalStrength;

    public Scanner_BTLE(BluetoothDeviceList btl, long scanPeriod, int signalStrength) {
        btdl = btl;

        mHandler = new Handler(Looper.getMainLooper());

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) btdl.getSystemService(Context.BLUETOOTH_SERVICE);
        btadap = bluetoothManager.getAdapter();
    }

    public boolean isScanning() {
        return scanning;
    }

    public void start() {
        if (!Utility_Func.checkBluetooth(btadap)) {
            Utility_Func.requestUserBluetooth(btdl);
            btdl.stopScan();
        } else {
            scanLeDevice(true);
        }
    }

    public void stop() {
        scanLeDevice(false);
    }

    /**
     * Scans for Bluetooth devices and stops after a certain time period.
     * Can be adjusted depending on power consumption and how we want to manage scan usage
     */
    private void scanLeDevice(final boolean enable) {

        final BluetoothLeScanner bluetoothLeScanner = btadap.getBluetoothLeScanner();

        if (enable && !scanning) {
            //Utility_Func.toast(btdl.getApplicationContext(), "Starting BLE scan...");

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Utility_Func.toast(btdl.getApplicationContext(), "Stopping BLE scan...");

                    scanning = false;
                    bluetoothLeScanner.stopScan(mLeScanCallback);

                    btdl.stopScan();
                }
            }, scanPeriod);

            scanning = true;
            ScanSettings.Builder settingBuilder = new ScanSettings.Builder();
            settingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            settings = settingBuilder.build();
            bluetoothLeScanner.startScan(null, settings, mLeScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {

            final int new_rssi = result.getRssi();
            if (result.getRssi() > signalStrength) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                           btdl.addDevice(result.getDevice(), new_rssi);

                    }
                });
            }
        }
    };
}
