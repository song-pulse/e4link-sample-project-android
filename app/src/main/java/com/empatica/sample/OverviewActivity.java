package com.empatica.sample;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class OverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        TextView participant_id;
        participant_id = (TextView) findViewById(R.id.participant_id);
        displayPID(participant_id, "Participant ID: " + EntryActivity.pId);
        displayGraph();
    }

    private void displayPID(final TextView label, final String text) {
        runOnUiThread(() -> label.setText(text));

    }

    public void displayGraph() {
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        series.setColor(Color.RED);
        graph.addSeries(series);
    }


}