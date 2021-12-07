package com.example.helpmego_java;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;
/**
 * Activity Class that describes the Navigational screen for the application.
 * When a user begins navigation, they will be presented with this screen.
 * This class is the primary user of Bluetooth functionality for navigation
 * */
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
    int dest = 0;
    int start = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_second);

        // Checks if BLE is supported on device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utility_Func.toast(getApplicationContext(), "BLE not supported");
            finish();
        }
        //Create BLE scanner object - this is used to invoke BT radio through Android
        btScanner = new Scanner_BTLE(this, 1000, -75);

        btDevicesHashMap = new HashMap<>();
        btDevicesArrayList = new ArrayList<>();
        beacons = MainActivity.beacons;
        //Get room number and associated beacon ID from previous MainActivity.
        //If empty, destination is set to 4 so that dest and start are not equal.
        Bundle extras = getIntent().getExtras();
        if(extras == null){
            dest = 4;
        }else{
            dest = extras.getInt("dest");
            room = extras.getString("room");
        }
        //Debug stubs for list of beacons
        adapter = new ListAdapter_BTLE(this, R.layout.btle_device_list, btDevicesArrayList);
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        //Set functionality for this view's buttons
        btn_Scan = findViewById(R.id.Help_About_Button);
        btn_Scan.setOnClickListener(this);
        btn_Back = findViewById(R.id.Cancel_Button);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //end NavLogic thread nicely here
                MainActivity.speak("Main Menu");
                if (myThread.isAlive()){
                    myThread.interrupt();}
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
        //Vocalize what room was recognized as the destination
        MainActivity.speak("Now Navigating to room" + room);

        final TextView navText = (TextView) findViewById(R.id.Text_Directions);
        final ImageView imgView = (ImageView) findViewById(R.id.imageView);
        /**
        * This defines the Navigational thread logic to be run by the thread "myThread"
         * Navigational logic is run in a concurrent thread to prevent while loops from
         * stalling the primary thread and preventing the drawing of UI elements
        * */
        Runnable btScanRunnable = new Runnable() {
            @Override
            public void run() {
                //Run the BT scanner in 2 second bursts until a beacon is detected and logged
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
                //set start beacon to match the beacon found.
                start =MainActivity.lookupIntByBeaconID(btDevicesArrayList.get(0).getName(), beacons);

                //Calculate the current route. If Start is a bad value, use a dummy value
                if(beacons.get(start) != null){
                    currentRoute = PathGraph.findShortestPath(MainActivity.floorGraph,start, dest, 5 );
                }else{
                    currentRoute = PathGraph.findShortestPath(MainActivity.floorGraph,1, dest, 5 );
                }
                //Prep the calculated path by getting the current node isolated, and getting the next to calculate the
                //path between them, then speak it aloud.
                LocationLinkedObj currentNode = beacons.get(currentRoute.removeLast());
                LocationLinkedObj speakNext = beacons.get(currentRoute.peekLast());
                MainActivity.speak("Go " + currentNode.DirectionsTo.get(speakNext.getUniqueInt()));

                /**
                 * This acts as the primary navigational loop. When inside the loop, the program will constantly check
                 * to update what beacon is "closest" by RSSI.
                 * When the new closest beacon matches that of the "next" beacon, the directions and route are updated.
                 * When the route is complete, the loop exits.
                 * */
                while(!currentRoute.isEmpty()){
                    //get the next route node and set the appropriate Image and Text to display them.
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

                    //Start a 2 second low-latency scan to try finding the next beacon
                    if (!btScanner.isScanning()) {
                        startScan();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopScan();
                    }
                    //Check if any beacons are detected at all
                    if (btDevicesArrayList.size() > 0) {
                        //If the closest beacon matches that of the "next" in the route, update the route to reflect
                        if (btDevicesArrayList.get(0).getName().equalsIgnoreCase(nextNode.BeaconID)) {
                            currentNode = beacons.get(currentRoute.removeLast());
                            //if there's still more route to do, get the next and speak the next directions.
                            if(!currentRoute.isEmpty()){
                                nextNode = beacons.get(currentRoute.peekLast());
                                MainActivity.speak("Go " + currentNode.DirectionsTo.get(nextNode.getUniqueInt()));
                            }


                        }
                    }
                }
                //Broke out of loop, user has arrived - alert them as such.
                MainActivity.speak("You have arrived.");
                //Nav done, return to Main Menu
                finish();
                return;
            }
        };
        //This created the Thread to run btScanRunnable and starts it.
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


    /**
     * Displays an alert if Bluetooth functionality is disabled by OS (such as in settings)
     * */
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

            if(btleDevice.getName() != null){ //only add BT beacons with names to filter non-nav BT
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
     * Starts Scanner_BTLE
     */
    public void startScan(){

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

        btScanner.stop();
    }

    /**
    * Quick function to extract first word from directional string
     * This is used to determine the drawable path for the loaded image
    * */
    private String extractDirection(String input){
        int i = input.indexOf(' ');
        return input.substring(0,i);
    }
}
