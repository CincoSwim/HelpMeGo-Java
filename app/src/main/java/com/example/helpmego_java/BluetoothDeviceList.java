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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class BluetoothDeviceList extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String, BTLE_Device> btDevicesHashMap;
    private ArrayList<BTLE_Device> btDevicesArrayList;
    private ArrayList<LocationLinkedObj> beacons;
    private LinkedList<Integer> currentRoute;

    private ListAdapter_BTLE adapter;

    private Button btn_Scan;
    private Button btn_Back;

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

        btScanner = new Scanner_BTLE(this, 7500, -75);

        btDevicesHashMap = new HashMap<>();
        btDevicesArrayList = new ArrayList<>();
        beacons = MainActivity.beacons;
        Bundle extras = getIntent().getExtras();
        if(extras == null){
            dest = 4;
        }else{
            dest = extras.getInt("dest");
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
                finish();
            }
        });
        //((ScrollView) findViewById(R.id.scrollView)).addView(listView);
        //start = findClosestBeacon();


        //<test
        start = 0;
        //endtest>



        /*
        while(!currentRoute.isEmpty()){
            LocationLinkedObj currentNode = beacons.get(currentRoute.peek());
            navText.setText(currentNode.DirectionsTo.get(currentRoute.get(1)));
            //need to set imageView here, potentially flip it
            recheckClosestBeacon();
        }
        if(currentRoute.isEmpty()){
            setContentView(R.layout.fragment_first);
        }*/

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.speak("Now navigating....");
        currentRoute = PathGraph.findShortestPath(MainActivity.floorGraph,start, dest, 5 );

        // Sets back button to return to previous screen
        /*
        btn_Back = findViewById(R.id.backButton);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
         */

        //ENTER NAVIGATION LOOP HERE SOMEHOW
        final TextView navText = (TextView) findViewById(R.id.Text_Directions);

        Runnable btScanRunnable = new Runnable() {
            @Override
            public void run() {
                LocationLinkedObj currentNode = beacons.get(currentRoute.removeLast());
                while(!currentRoute.isEmpty()){

                    LocationLinkedObj nextNode = beacons.get(currentRoute.peekLast());
                    final String navUpdate = currentNode.DirectionsTo.get(nextNode.getUniqueInt());
                    navText.post(new Runnable() {
                        @Override
                        public void run() {
                            navText.setText(navUpdate);
                        }
                    });
                    //startScan();

                    if (!btScanner.isScanning()) {
                        startScan();
                        Utility_Func.delay(1, new Utility_Func.DelayCallback() {
                            @Override
                            public void afterDelay() {
                                System.out.println("testprint");
                                stopScan();
                                System.out.println("afterStopScan");
                            }
                        });

                    }

                    if (btDevicesArrayList.size() > 0) {
                        String vibeCheck = btDevicesArrayList.get(0).getAddress();
                        String feelsCheck = btDevicesArrayList.get(0).getName();
                        BTLE_Device first = btDevicesArrayList.get(0);
                        feelsCheck = first.getName();


                        if (btDevicesArrayList.get(0).getName().equalsIgnoreCase(nextNode.BeaconID)) {
                            currentNode = beacons.get(currentRoute.removeLast());
                        }
                    }
                }
            }
        };

        Thread myThread = new Thread(btScanRunnable);
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
}
