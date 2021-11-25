package com.example.helpmego_java;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class BluetoothDeviceList extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String, BTLE_Device> btDevicesHashMap;
    private ArrayList<BTLE_Device> btDevicesArrayList;
    private ArrayList<LocationLinkedObj> beacons;
    private ListAdapter_BTLE adapter;

    private Button btn_Scan;
    private Button btn_Back;

    private Scanner_BTLE btScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btle_device_main);




        // Checks if BLE is supported on device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utility_Func.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        btScanner = new Scanner_BTLE(this, 7500, -75);

        btDevicesHashMap = new HashMap<>();
        btDevicesArrayList = new ArrayList<>();
        beacons = (ArrayList<LocationLinkedObj>) getIntent().getSerializableExtra("beaconsList");

        adapter = new ListAdapter_BTLE(this, R.layout.btle_device_list, btDevicesArrayList);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        btn_Scan = findViewById(R.id.btn_scan);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);
        findViewById(R.id.btn_scan).setOnClickListener(this);

        // Sets back button to return to previous screen
        btn_Back = findViewById(R.id.backButton);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {

            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                Utility_Func.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }

    /**
     * Called when clicking the scan button.
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_scan) {
            Utility_Func.toast(getApplicationContext(), "Scan Button Pressed");

            if (!btScanner.isScanning()) {
                startScan();
            } else {
                stopScan();
            }
        }
    }

    /**
     * Adds a device to the arraylist and hashmap
     * Hashmap to determine if a device already exists, if so update the RSSI
     * Arraylist is here for now to display the devices so we know it is working
     */
    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        if (!btDevicesHashMap.containsKey(address)) {
            BTLE_Device btleDevice = new BTLE_Device(device);
            btleDevice.setRSSI(rssi);

            btDevicesHashMap.put(address, btleDevice);
            btDevicesArrayList.add(btleDevice);
        }
        else {
            btDevicesHashMap.get(address).setRSSI(rssi);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Clears the ArrayList and Hashmap the ListAdapter is keeping track of.
     * Starts Scanner_BTLE.
     * Changes the scan button text.
     */
    public void startScan(){
        btn_Scan.setText("Scanning...");

        btDevicesArrayList.clear();
        btDevicesHashMap.clear();

        adapter.notifyDataSetChanged();

        btScanner.start();
    }

    /**
     * Stops Scanner_BTLE
     * Changes the scan button text.
     */
    public void stopScan() {
        btn_Scan.setText("Scan Again");
        btScanner.stop();
    }
}
