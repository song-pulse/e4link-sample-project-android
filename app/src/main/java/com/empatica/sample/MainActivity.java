package com.empatica.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class MainActivity extends AppCompatActivity {
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // start Spotify Activity
        startSpotifyActivity();
        Button startRecording = (Button) findViewById(R.id.start_Recording);
        startRecording.setOnClickListener(v -> {
            String pid = handlePID(v);
            // make HTTP request here
            sendRequest(pid);
        });

        Button startOverview = (Button) findViewById(R.id.open_Overview);
        startOverview.setOnClickListener(v -> {
            startOverviewActivity();
        });
    }


    /**
     * Called when the user taps the Send button
     */
    public String handlePID(View view) {
        Intent intent = new Intent(this, EntryActivity.class);
        EditText participantID = (EditText) findViewById(R.id.participant_id);
        String PID = participantID.getText().toString();
        //Add your data to bundle
        bundle.putString("PID", PID);
        //Add the bundle to the intent
        intent.putExtras(bundle);
        return PID;
    }

    public void sendRequest(String participant_id) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = ("http://130.60.24.99:8080/participants/" + participant_id + "/recordings/start");
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    Log.d("response", response);
                    String tmp = response.split(",")[2];
                    String recordingId = tmp.split(":")[1];
                    startEntryActivity(recordingId);
                },
                error -> Log.d("notworking", "did not work"));
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    /**
     * Called when the user taps the Send button
     */
    public void startEntryActivity(String recordingId) {
        Intent intent = new Intent(this, EntryActivity.class);
        //Add your data to bundle
        bundle.putString("recordingId", recordingId);
        //Add the bundle to the intent
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void startSpotifyActivity() {
        Intent spotifyIntent = new Intent(this, SpotifyActivity.class);
        startActivity(spotifyIntent);
    }

    public void startOverviewActivity() {
        Intent overviewIntent = new Intent(this, OverviewActivity.class);
        startActivity(overviewIntent);
    }

}