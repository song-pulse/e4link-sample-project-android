package com.empatica.sample;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.google.gson.Gson;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EntryActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;


    private static final String EMPATICA_API_KEY = "526b747d081044a1a991cf078daedfbd";


    private EmpaDeviceManager deviceManager = null;

    private TextView accel_xLabel;

    private TextView accel_yLabel;

    private TextView accel_zLabel;

    private TextView bvpLabel;

    private TextView edaLabel;

    private TextView ibiLabel;

    private TextView temperatureLabel;

    private TextView batteryLabel;

    private TextView statusLabel;

    private TextView deviceNameLabel;

    private TextView part_id;

    private LinearLayout dataCnt;


    // save data from E4
    private float Accx;
    private float Accy;
    private float Accz;
    private int globaltimestamp;

    private float globalbvp;
    private float globalibi;
    private float globaltemp;
    private float globaleda;

    private String pId;
    private String rId;

    private static final String CLIENT_ID = "6550a6a412b5465c95c32dedf34ac18d";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private SpotifyAppRemote mSpotifyAppRemote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Get the bundle in order to get PID from mainactivity
        Bundle bundle = getIntent().getExtras();
        //Extract the data…
        String participant_id = bundle.getString("PID");
        pId = participant_id;

        rId = bundle.getString("recordingId");

        setContentView(R.layout.activity_entry);

        // Initialize vars that reference UI components
        statusLabel = (TextView) findViewById(R.id.status);

        dataCnt = (LinearLayout) findViewById(R.id.dataArea);

        accel_xLabel = (TextView) findViewById(R.id.accel_x);

        accel_yLabel = (TextView) findViewById(R.id.accel_y);

        accel_zLabel = (TextView) findViewById(R.id.accel_z);

        bvpLabel = (TextView) findViewById(R.id.bvp);

        edaLabel = (TextView) findViewById(R.id.eda);

        ibiLabel = (TextView) findViewById(R.id.ibi);

        temperatureLabel = (TextView) findViewById(R.id.temperature);

        batteryLabel = (TextView) findViewById(R.id.battery);

        deviceNameLabel = (TextView) findViewById(R.id.deviceName);

        part_id = (TextView) findViewById(R.id.participant_id);

        // Displays participant id
        displayPID(part_id, "Participant ID: " + participant_id);

        final Button disconnectButton = findViewById(R.id.disconnectButton);

        disconnectButton.setOnClickListener(v -> {
            // pause spotify song and close connection
            mSpotifyAppRemote.getPlayerApi().pause();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            if (deviceManager != null) {
                deviceManager.disconnect();
            }
        });

        initEmpaticaDeviceManager();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, yay!
                initEmpaticaDeviceManager();
            } else {
                // Permission denied, boo!
                final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                new AlertDialog.Builder(this)
                        .setTitle("Permission required")
                        .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                        .setPositiveButton("Retry", (dialog, which) -> {
                            // try again
                            if (needRationale) {
                                // the "never ask again" flash is not set, try again with permission request
                                initEmpaticaDeviceManager();
                            } else {
                                // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Exit application", (dialog, which) -> finish())
                        .show();
            }
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", (dialog, which) -> finish())
                        .show();
                return;
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(EntryActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didFailedScanning(int errorCode) {

    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothStateChanged() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {
        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
            // The device manager has established a connection
            hide();
        } else if (status == EmpaStatus.CONNECTED) {
            show();
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            updateLabel(deviceNameLabel, "");
            hide();
        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        Accx = x;
        Accy = y;
        Accz = z;
        globaltimestamp = (int)timestamp;
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        globalbvp = bvp;
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        globaleda = gsr;
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        globalibi = ibi;
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        globaltemp = temp;
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(() -> label.setText(text));
    }

    private void displayPID(final TextView label, final String text) {
        runOnUiThread(() -> label.setText(text));
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {
        show();
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {
        runOnUiThread(() -> {
            if (status == EmpaSensorStatus.ON_WRIST) {
                ((TextView) findViewById(R.id.wrist_status_label)).setText("ON WRIST");
            } else {
                ((TextView) findViewById(R.id.wrist_status_label)).setText("NOT ON WRIST");
            }
        });
    }

    void show() {
        spotifyStart();
        // send E4 data all minutes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> sendE4data(pId, rId), 0, 30, TimeUnit.SECONDS);

        runOnUiThread(() -> dataCnt.setVisibility(View.VISIBLE));
    }

    void hide() {
        runOnUiThread(() -> dataCnt.setVisibility(View.INVISIBLE));
    }

    public void sendE4data(String participantId, String recordingId) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://130.60.24.99:8080/participants/" + participantId + "/recordings/" + recordingId + "/values/timestamps";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    Log.d("response", response);
                    // take newest result and queue it in spotify
                    Gson gson = new Gson();
                    Run run = gson.fromJson(response, Run.class);
                    String latestSong = run.getLatestSong();
                    if (!latestSong.equals("")){
                        mSpotifyAppRemote.getPlayerApi().queue("spotify:track:"+ latestSong);
                    }
                },
                error -> Log.d("notworking", "did not work"))
        {
            @Override
            public byte[] getBody() {
                final JSONObject body = new JSONObject();
                try {
                    body.put("timestamp", String.valueOf(globaltimestamp));
                    body.put("eda", String.valueOf(globaleda));
                    body.put("ibi", String.valueOf(globalibi));
                    body.put("temp", String.valueOf(globaltemp));
                    body.put("acc_x", String.valueOf(Accx));
                    body.put("acc_y", String.valueOf(Accy));
                    body.put("acc_z", String.valueOf(Accz));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return body.toString().getBytes();
            }
        };

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    public void spotifyStart() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                    }
                });
    }

}
