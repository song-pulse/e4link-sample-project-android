package com.empatica.sample;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SpotifyActivity extends AppCompatActivity {

    public static String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        WebView myWebView = findViewById(R.id.webview);
        myWebView.setWebViewClient(new MyBrowser());
        String spotifyUrl = "http://130.60.24.99:8080/spotify/authorize";
        myWebView.loadUrl(spotifyUrl);
    }

    // send request for spotify
    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("spotifyurl", "Url"+url);

            if(url.contains("whoami")) {
                //onPageFinished(view, url);
                cookie = CookieManager.getInstance().getCookie(url);
                Log.d("cookieSpotify", "All the cookies in a string:" + cookie);
                // only do this if url is callback
                if(url.contains("whoami")) {
                    SpotifyActivity.this.finish(); // finish spotifyActivity
                }
            }
            return false;
        }
    }


}