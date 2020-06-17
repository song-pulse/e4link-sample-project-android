package com.empatica.sample;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestHelper {
    // TODO: Handle HttpRequest to BE song-pulse in here

    public void startNewRunForParticipant(String participant_id) throws IOException {
        URL url = new URL("http://130.60.24.99:8080/participants/" + participant_id + "/recordings/start");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
    }
}

