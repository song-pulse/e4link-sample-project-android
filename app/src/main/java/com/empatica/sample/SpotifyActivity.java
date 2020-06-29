package com.empatica.sample;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class SpotifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        this.getSpotify();
    }

    // send request for spotify and open in browser

    public void getSpotify() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String spotifyUrl = "http://130.60.24.99:8080/spotify/authorize";
        StringRequest spotifyRequest = new StringRequest(Request.Method.GET,
                spotifyUrl,
                response -> {
                    Log.d("response", String.valueOf(response));

                },
                error -> Log.d("notworking", "did not work"));
                requestQueue.add(spotifyRequest);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl));
        startActivity(browserIntent);
        SpotifyActivity.this.finish(); // finish spotifyActivity
    }
}