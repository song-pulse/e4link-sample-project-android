package com.empatica.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        TextView participant_id;
        participant_id = (TextView) findViewById(R.id.participant_id);
        displayPID(participant_id, "Participant ID: " + EntryActivity.pId);
    }

    private void displayPID(final TextView label, final String text) {
        runOnUiThread(() -> label.setText(text));

    }
}