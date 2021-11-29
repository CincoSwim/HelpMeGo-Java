package com.example.helpmego_java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyApp";
    public static final Integer RecordAudioRequestCode = 1;
    protected static ArrayList<ArrayList<Integer>> floorGraph = new ArrayList<ArrayList<Integer>>();
    protected static ArrayList<LocationLinkedObj> beacons = new ArrayList<LocationLinkedObj>(); //there's gotta be a cleaner way for this
    protected static LinkedList<Integer> currentRoute;
    private EditText editText;
    private SpeechRecognizer speechRecog;
    int start = 1;
    int dest = 4;
    ArrayList<ArrayList<Integer>> BTGraph;
    public static TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        editText = findViewById(R.id.edit_text);
        editText.setHint("Please input a destination.");
        setSupportActionBar(toolbar);


        //Load in static maps?
        for (int i=0; i<=5; i++){
            floorGraph.add(i, new ArrayList<Integer>());
        }
        makeTestGraph();


        //end load of static maps


        //Implement TTS Here
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS ERROR", "Lang not Supported");

                    }else{
                        Log.e("TTS ERROR", "Init failure");
                    }
                }
            }
        });

        tts.speak("Welcome to HelpMeGo!", TextToSpeech.QUEUE_ADD, null, null);


        //pucko added here
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        speechRecog = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecog.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Listening to input on button press");
                editText.setText("");
                editText.setHint("Listening...");

                Log.d(TAG, "Listening to input on button press");

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                    Log.d(TAG, "STT onResults() starting...");
                    String ttsTester = "";
                    ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    editText.setText(data.get(0));
                    String testStr = data.get(0);
                    int lastindex = testStr.lastIndexOf("room");
                    if(lastindex == -1){
                        testStr = "heard nothing!";
                    }else{
                        ttsTester = testStr.substring(lastindex);
                        testStr += "(parsed room num: " + ttsTester + ")";
                    }
                    //int roomindex = testStr.indexOf("room") + 5;

                    tts.speak("You want to go to " + ttsTester + ", right?", TextToSpeech.QUEUE_ADD, null, null);

                    Log.d(TAG, "onResults: string manip finished");
                    editText.setText(testStr);

                    /* Begin finding path*/
                    Log.d(TAG, "onResults: finding path...");
                    //get closest BT Beacon identity


                    //get appropriate destination beacon identity
                    dest = lookupIntByRoomNum(ttsTester, beacons);
                    if(dest < 0) dest = 0;

                    //Find path between the two - findShortestPath()
                    //^ this is now done in the new activity!

                    Log.d(TAG, "onResults: dest parsed, moving to BluetoothDeviceList activity");
                    /*end finding path, move to nav*/
                    Log.d(TAG, "making murderous intent");
                    Intent intent = new Intent(MainActivity.this, BluetoothDeviceList.class);

                    intent.putExtra("dest", dest);
                    Log.d(TAG, "intent filled, moving to activity");
                    startActivity(intent);


            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
        Button mainButton = findViewById(R.id.button_first);
        mainButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    Log.d(TAG, "mainButton sees UP");
                    speechRecog.stopListening();
                    Log.d(TAG, "stopped listening");
                }if(event.getAction() == MotionEvent.ACTION_DOWN){

                    Log.d(TAG, "mainButton sees DOWN");
                    tts.speak("Now Listening.", TextToSpeech.QUEUE_ADD, null, null);


                    speechRecog.startListening(speechRecognizerIntent);
                    Log.d(TAG, "started listening");
                }
                return false;
            }

        });

        // Added button to goto bluetooth device screen for testing
        Button toBTDevices = (Button) findViewById(R.id.BTButton);
        toBTDevices.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), BluetoothDeviceList.class);

                intent.putExtra("dest", dest);
                startActivity(intent);
            }
        });


        tts.speak("Welcome to Help Me Go. To begin, please press the button in the center of the screen, and ask for directions to a room.", TextToSpeech.QUEUE_ADD, null, null);
   
    }



    private void startListen(View view){
        //do something
    }

    private void checkPermission(){
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void speak(String text){

        Log.d(TAG, "TTS Speak() called");
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }

    private void makeTestGraph(){
        Log.d(TAG, "filling adj graph");

        PathGraph.addEdge(floorGraph, 0, 1);
        PathGraph.addEdge(floorGraph, 1, 2);
        PathGraph.addEdge(floorGraph, 2, 3);
        PathGraph.addEdge(floorGraph, 3, 4);
        PathGraph.addEdge(floorGraph, 4, 1);
        Log.d(TAG, "graph filled");
        Log.d(TAG, "filling LocationLinkedObjs");

        beacons.add(0, new LocationLinkedObj("1", 0));
        beacons.add(1, new LocationLinkedObj("2", 1));
        beacons.add(2, new LocationLinkedObj("3", 2));
        beacons.add(3, new LocationLinkedObj("4", 3));
        beacons.add(4, new LocationLinkedObj("5", 4));
        //set beacon 1 data
        beacons.get(0).addRoomID("203");
        beacons.get(0).addRoomID("204");
        beacons.get(0).addDirection(1, "forward");
        //set beacon 2 data
        beacons.get(1).addDirection(0, "back");
        beacons.get(1).addDirection(2, "forward");
        beacons.get(1).addDirection(4, "right");
        //set beacon 3 data
        beacons.get(2).addRoomID("205");
        beacons.get(2).addDirection(1, "back");
        beacons.get(2).addDirection(3, "right");
        //set beacon 4 data
        beacons.get(3).addRoomID("206");
        beacons.get(3).addDirection(2, "left");
        beacons.get(3).addDirection(4, "back");
        //set beacon 5 data
        beacons.get(4).addRoomID("207");
        beacons.get(4).addRoomID("208");
        beacons.get(4).addDirection(3, "forward");
        beacons.get(4).addDirection(1, "left");
        Log.d(TAG, "list of LocationLinkedObjs (beacons) filled");
    };
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

}