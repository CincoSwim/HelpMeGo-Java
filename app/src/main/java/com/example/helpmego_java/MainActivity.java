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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyApp";
    public static final Integer RecordAudioRequestCode = 1;
    private EditText editText;
    private SpeechRecognizer speechRecog;
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
        //BUILD/FILL BTGraph here!! Need to define int/beacon relationship
        //addedge
        //addedge

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
                    ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    editText.setText(data.get(0));
                    String testStr = data.get(0);

                    //int roomindex = testStr.indexOf("room") + 5;
                    String ttsTester = testStr.substring(testStr.lastIndexOf("room"));
                    testStr += "(parsed room num: " + ttsTester + ")";
                    tts.speak("You want to go to " + ttsTester + ", right?", TextToSpeech.QUEUE_ADD, null, null);

                    editText.setText(testStr);

                    /* Begin finding path*/
                    //get closest BT Beacon identity
                    //get appropriate destination beacon identity
                    //Find path between the two - findShortestPath()

                    //LinkedList<Integer> navPath = PathGraph.findShortestPath();
                    //Move to next fragment for directing


            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    speechRecog.stopListening();
                }if(event.getAction() == MotionEvent.ACTION_DOWN){
                    tts.speak("Now Listening.", TextToSpeech.QUEUE_ADD, null, null);

                    speechRecog.startListening(speechRecognizerIntent);
                }
                return false;
            }



                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            */

        });

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
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }
}