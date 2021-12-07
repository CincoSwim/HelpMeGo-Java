package com.example.helpmego_java;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.*;
/**
 * Activity Class that describes the logic and functionality of the Main Menu Screen.
 * When first started, the app presents this screen to the user.
 * From this screen, the user can detail what room they'd like to navigate to, at which point the app will transition
 * to the BluetoothDeviceList activity.
 * */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MyApp"; //debug tag
    public static final Integer RecordAudioRequestCode = 1;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    protected static ArrayList<ArrayList<Integer>> floorGraph = new ArrayList<ArrayList<Integer>>(); //initialize Graph
    protected static ArrayList<LocationLinkedObj> beacons = new ArrayList<LocationLinkedObj>(); //initialize Node list
    protected static LinkedList<Integer> currentRoute;
    private Spinner spinner;
    List<String> rooms;
    protected static String STT_STRING = "";
    int start = 1;
    int dest = 4;

    public static TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        //Fill spinner object with possible room numbers and detail Listener method
        spinner = (Spinner) findViewById(R.id.spinner);
        rooms = new ArrayList<String>();
        rooms.add("Pick a room number!");
        rooms.add("203");
        rooms.add("204");
        rooms.add("205");
        rooms.add("206");
        rooms.add("207");
        rooms.add("208");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, rooms);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);


        //Initialize and fill in static maps and graph data
        for (int i=0; i<=5; i++){
            floorGraph.add(i, new ArrayList<Integer>());
        }
        makeTestGraph();

        //check for Audio Recording permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        //Define button in the center of the screen and set it's OnClickListener action
        Button mainButton = findViewById(R.id.button_first);
        mainButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String parsedRoom;
                promptSpeechInput(); //this continues before getting the STT input!

            }
        });

        // Added button to goto bluetooth device screen for debug tessting - stub
        Button toBTDevices = (Button) findViewById(R.id.BTButton);
        toBTDevices.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), BluetoothDeviceList.class);

                intent.putExtra("dest", dest);
                startActivity(intent);
            }
        });

    }





    @Override
    protected void onStart(){
        super.onStart();
        //initialize TextToSpeech object tts to use phone language and US locale
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS ERROR", "Lang not Supported");

                    }else{
                        //TTS success, test it by speaking the intro string
                        tts.speak("Welcome to Help Me Go. To start, press the button in the center of the screen, and speak where you'd like to go.", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    private void checkPermission(){
        //prompt to allow Audio Recording
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Method to call tts.speak() with proper parameters
     * */
    public static void speak(String text){

        Log.d(TAG, "TTS Speak() called - Speaking: " + text );
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }
    /**
     * Function to fill floorGraph and beacons with static map data.
     * In a future release candidate, this can be replaced with something like a JSON parser or similar
     * */
    private void makeTestGraph(){
        Log.d(TAG, "filling adj graph");

        PathGraph.addEdge(floorGraph, 0, 1);
        PathGraph.addEdge(floorGraph, 1, 2);
        PathGraph.addEdge(floorGraph, 2, 3);
        PathGraph.addEdge(floorGraph, 3, 4);
        Log.d(TAG, "graph filled");
        Log.d(TAG, "filling LocationLinkedObjs");

        beacons.add(0, new LocationLinkedObj("Beacon1", 0));
        beacons.add(1, new LocationLinkedObj("Beacon2", 1));
        beacons.add(2, new LocationLinkedObj("Beacon3", 2));
        beacons.add(3, new LocationLinkedObj("Beacon4", 3));
        beacons.add(4, new LocationLinkedObj("5", 4));
        //set beacon 1 data
        beacons.get(0).addRoomID("203");
        beacons.get(0).addRoomID("204");
        beacons.get(0).addDirection(1, "forward to Beacon 2");
        //set beacon 2 data
        beacons.get(1).addDirection(0, "back to Beacon 1");
        beacons.get(1).addDirection(2, "forward to Beacon 3");
       // beacons.get(1).addDirection(4, "right");
        //set beacon 3 data
        beacons.get(2).addRoomID("205");
        beacons.get(2).addDirection(1, "back to Beacon 2");
        beacons.get(2).addDirection(3, "right to Beacon 4");
        //beacons.get(2).addDirection(3, "right");
        //set beacon 4 data
        beacons.get(3).addRoomID("206");
        beacons.get(3).addDirection(2, "left to Beacon 3");
        beacons.get(3).addDirection(4, "back to Beacon 5");
        //set beacon 5 data
        beacons.get(4).addRoomID("207");
        beacons.get(4).addRoomID("208");
        beacons.get(4).addDirection(3, "forward to Beacon 4");
       // beacons.get(4).addDirection(1, "left");
        Log.d(TAG, "list of LocationLinkedObjs (beacons) filled");
    };

    //Takes in a beacon's name and returns its index if contained in the beacon list
    public static int lookupIntByBeaconID(String BUID, ArrayList<LocationLinkedObj> bList){
        //for each object in bList
        Log.d(TAG, "lookup by BeaconID Started");
        for (LocationLinkedObj beac: bList) {
            if(beac.getBeaconID().equals(BUID)){
                Log.d(TAG, "ID found: " + beac.getUniqueInt());
                return beac.getUniqueInt();
            }
        }
        //no match, return dummy val
        Log.d(TAG, "No ID found, defaulting");
        return 99;
    }
    //Takes in a room number and returns the index of the beacon assigned to that room if it exists
    private int lookupIntByRoomNum(String Room, ArrayList<LocationLinkedObj> bList){
        for (LocationLinkedObj beac: bList) {
            if(beac.roomIDs.contains(Room)){
                Log.d(TAG, "ID found: " + beac.getUniqueInt());
                return beac.getUniqueInt();
            }
        }
        //no match, return dummy val
        Log.d(TAG, "No ID found, defaulting");
        return 99;
    }

    /**
     * Details actions taken when an option is selected in the spinner, then transitions to BluetoothDeviceList
     * activity if a user selected a room.
     * */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        if(item.equals("Pick a room number!")) return; //Don't want to transition because of the dummy val

        //lookup by room num
        dest = lookupIntByRoomNum(item, beacons);
        if(dest < 0) dest = 0; //catch bad values

        Log.d(TAG, "onResults: dest parsed, moving to BluetoothDeviceList activity");
        /*end finding path, move to nav*/
        Log.d(TAG, "making malicious intent"); //just a joke
        Intent intent = new Intent(MainActivity.this, BluetoothDeviceList.class);
        //store relevant data for retrieval
        intent.putExtra("dest", dest);
        intent.putExtra("room", item);
        Log.d(TAG, "intent filled, moving to activity");
        startActivity(intent); //Start BluetoothDeviceList.Class activity
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //TODO Auto generated stub
    }
    /**
     * Creates the Text-to-Speech popup, prompting user for Speech Recog input.
     * */
    private void promptSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try{ startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);}
        catch (ActivityNotFoundException a){
            Toast.makeText(getApplicationContext(), "Speech not supported", Toast.LENGTH_SHORT).show();
        }

        return;
    }

    /**
     * Runs after successful completion of the Speech Recog popup.
     * If a room number is parsed, transitions activity to the navigation view.
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        String parsedRoom;

        switch(requestCode){
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //get STT string
                    STT_STRING = result.get(0);
                    int lastindex = STT_STRING.lastIndexOf("room"); //parse index of 'room'
                    if(lastindex == -1){
                        parsedRoom = "heard nothing!";
                        Log.e(TAG, "no room parsed - check voice input");
                        Toast.makeText(getApplicationContext(), "nothing heard", Toast.LENGTH_SHORT).show();
                    }else {
                        //isolate room number, find corresponding beacon, and go to BluetoothDeviceList class
                        parsedRoom = STT_STRING.substring(lastindex + 5);
                        dest = lookupIntByRoomNum(parsedRoom, beacons);
                        if (dest == 99) return;
                        Intent intent = new Intent(MainActivity.this, BluetoothDeviceList.class);

                        intent.putExtra("dest", dest);
                        intent.putExtra("room", parsedRoom);
                        Log.d(TAG, "intent filled, moving to activity");
                        STT_STRING = "";
                        startActivity(intent);
                    }


                }
                break;
            }
        }
    }
}