package com.empatica.sample;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startRecording = (Button) findViewById(R.id.start_Recording);
        startRecording.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // Intent intent = new Intent(MainActivity.this, EntryActivity.class);
                sendMessage(v);
                //startActivity(intent);
            }
        });
    }


    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, EntryActivity.class);
        EditText participantID = (EditText) findViewById(R.id.participant_id);
        String PID = participantID.getText().toString();
        // TODO display message
        //Create the bundle
        Bundle bundle = new Bundle();
        //Add your data to bundle
        bundle.putString("PID", PID);
        //Add the bundle to the intent
        intent.putExtras(bundle);
        startActivity(intent);
    }


}