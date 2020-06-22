package com.empatica.sample;

public class Run {
    boolean is_running;
    int id;
    int recording_id;
    Result[] results;

    public String getLatestSong() {
        if(results.length < 1){
            return "";
        } else {
            Result result = results[results.length - 1];
            String link = result.song.link;
            String[] split = link.split("/");
            String id = split[split.length - 1];
            return id;
        }
    }
}
