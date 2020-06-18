package com.empatica.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class MainActivity extends AppCompatActivity {
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startRecording = (Button) findViewById(R.id.start_Recording);
        startRecording.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Intent intent = new Intent(MainActivity.this, EntryActivity.class);
                String pid = handlePID(v);
                // make HTTP request here
                sendRequest(pid);
                // HttpRequestHelper.startNewRunForParticipant(pid);
                //startActivity(intent);
            }
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
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // textView.setText("Response is: "+ response.substring(0,500));
                        Log.d("response", response);
                        String tmp = response.split(",")[2];
                        String recordingId = tmp.split(":")[1];
                        startEntryActivity(recordingId);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //textView.setText("That didn't work!");
                        Log.d("notworking", "did not work");
                    }
                });
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

}