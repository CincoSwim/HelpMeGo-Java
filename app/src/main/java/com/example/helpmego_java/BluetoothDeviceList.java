package com.example.helpmego_java;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class BluetoothDeviceList extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String, BTLE_Device> btDevicesHashMap;
    private ArrayList<BTLE_Device> btDevicesArrayList;
    private ArrayList<LocationLinkedObj> beacons;
    

    private ListAdapter_BTLE adapter;
    private static LinkedList<Integer> currentRoute;
    private String room;

    private Button btn_Scan;
    private Button btn_Back;

    private Thread myThread;

    private Scanner_BTLE btScanner;
    int dest;
    int start = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_second);




        // Checks if BLE is supported on device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utility_Func.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        btScanner = new Scanner_BTLE(this, 1000, -75);

        btDevicesHashMap = new HashMap<>();
        btDevicesArrayList = new ArrayList<>();
        beacons = MainActivity.beacons;
        Bundle extras = getIntent().getExtras();
        if(extras == null){
            dest = 4;
        }else{
            dest = extras.getInt("dest");
            room = extras.getString("room");
        }

        adapter = new ListAdapter_BTLE(this, R.layout.btle_device_list, btDevicesArrayList);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        btn_Scan = findViewById(R.id.Help_About_Button);
        btn_Scan.setOnClickListener(this);
        btn_Back = findViewById(R.id.Cancel_Button);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //need to end thread nicely here
                MainActivity.speak("Main Menu");
                finish();
            }
        });



        //<test
        start = 0;
        //endtest


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MainActivity.speak("Now Navigating to room" + room);


        final TextView navText = (TextView) findViewById(R.id.Text_Directions);
        final ImageView imgView = (ImageView) findViewById(R.id.imageView);
        Runnable btScanRunnable = new Runnable() {
            @Override
            public void run() {
                do{
                    if (!btScanner.isScanning()) {
                        startScan();

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopScan();

                    }

                }while(btDevicesArrayList.isEmpty());
                start =MainActivity.lookupIntByBeaconID(btDevicesArrayList.get(0).getName(), beacons);
                if(beacons.get(start) != null){
                    currentRoute = PathGraph.findShortestPath(MainActivity.floorGraph,start, dest, 5 );
                }else{
                    currentRoute = PathGraph.findShortestPath(MainActivity.floorGraph,1, dest, 5 );
                }
                LocationLinkedObj currentNode = beacons.get(currentRoute.removeLast());
                LocationLinkedObj speakNext = beacons.get(currentRoute.peekLast());
                MainActivity.speak("Go " + currentNode.DirectionsTo.get(speakNext.getUniqueInt()));
                while(!currentRoute.isEmpty()){

                    LocationLinkedObj nextNode = beacons.get(currentRoute.peekLast());
                    final String navUpdate = currentNode.DirectionsTo.get(nextNode.getUniqueInt());
                    final String parsedDirection = extractDirection(navUpdate);
                    imgView.post(new Runnable() {
                        @Override
                        public void run() {
                            String mDrawableName = parsedDirection;
                            int resID = getResources().getIdentifier(mDrawableName, "drawable", getPackageName());
                            imgView.setImageResource(resID);
                        }
                    });
                    navText.post(new Runnable() {
                        @Override
                        public void run() {
                            navText.setText("Go " + navUpdate);
                        }
                    });


                    if (!btScanner.isScanning()) {
                        startScan();

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopScan();


                    }

                    if (btDevicesArrayList.size() > 0) {

                        if (btDevicesArrayList.get(0).getName().equalsIgnoreCase(nextNode.BeaconID)) {
                            currentNode = beacons.get(currentRoute.removeLast());
                            nextNode = beacons.get(currentRoute.peekLast());
                            MainActivity.speak("Go " + currentNode.DirectionsTo.get(nextNode.getUniqueInt()));

                        }
                    }
                }
                MainActivity.speak("You have arrived.");

                return;
            }
        };

        myThread = new Thread(btScanRunnable);
        myThread.start();

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

        if (v.getId() == R.id.Help_About_Button) {
            Utility_Func.toast(getApplicationContext(), "Scan Button Pressed");

            if (!btScanner.isScanning()) {
                startScan();
                Utility_Func.delay(5, new Utility_Func.DelayCallback() {
                    @Override
                    public void afterDelay() {
                        stopScan();
                    }
                });

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

            if(btleDevice.getName() != null){
                btDevicesArrayList.add(btleDevice);
            }
        }
        else {
            btDevicesHashMap.get(address).setRSSI(rssi);
        }
        adapter.notifyDataSetChanged();
        Collections.sort(btDevicesArrayList, new Comparator<BTLE_Device>(){
            public int compare(BTLE_Device o1, BTLE_Device o2){
                if(o1.getRSSI() == o2.getRSSI())
                    return 0;
                return o1.getRSSI() > o2.getRSSI() ? -1 : 1;
            }
        });
    }


    /**
     * Clears the ArrayList and Hashmap the ListAdapter is keeping track of.
     * Starts Scanner_BTLE.
     * Changes the scan button text.
     */
    public void startScan(){
        //btn_Scan.setText("Scanning...");

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
        //btn_Scan.setText("Scan Again");

        //get closest beacon

        btScanner.stop();
    }
    public void recheckClosestBeacon(){
        if(btDevicesArrayList.isEmpty()){
            return;
        }
        BTLE_Device closest = btDevicesArrayList.get(0);
        for (BTLE_Device btDev:btDevicesArrayList) {
            if(btDev.getRSSI() < closest.getRSSI()){
                closest = btDev;
            }
        }
        if(closest.getName().equals(beacons.get(MainActivity.currentRoute.peek()).getBeaconID() )){
            //change to next one
            MainActivity.currentRoute.pop(); //this'll probably throw a null pointer if we get to the end of the route...
        }
    }
    public int findClosestBeacon(){
        BTLE_Device closest = btDevicesArrayList.get(0);
        for (BTLE_Device btDev:btDevicesArrayList) {
            if(btDev.getRSSI() < closest.getRSSI()){
                closest = btDev;
            }
        }
        int closestNode = MainActivity.lookupIntByBeaconID(closest.getName(), beacons);
        return closestNode;
    }

    private String extractDirection(String input){
        int i = input.indexOf(' ');
        return input.substring(0,i);
    }
}
