package com.example.fitnesstrackergame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private EditText mSets;
    private EditText mReps;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private TextView mSteps;

    private static final int REQUEST_SENSOR_PERMISSION = 123;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSets = (EditText)findViewById(R.id.setsValue);
        mReps = (EditText)findViewById(R.id.repsValue);

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("localData", Context.MODE_PRIVATE);
        String sets = sharedPreferences.getString("Sets", "");
        String reps = sharedPreferences.getString("Reps", "");

        mSets.setText(sets);
        mReps.setText(reps);

        // If permission is not currently granted, request it to utilize the sensor
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_SENSOR_PERMISSION);
        }

        // Initialize the sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Check if the step counter sensor is available
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor != null) {
            // Register the sensor listener
            sensorManager.registerListener(stepCountSensorListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else {
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_SHORT).show();
        }

        mSteps = (TextView)findViewById(R.id.stepCount);
        mSteps.setText(String.valueOf(0));

        // Create a BroadcastReceiver to listen for step count updates
        /**stepCountReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(StepCounterService.STEP_COUNT_UPDATE_ACTION)) {
                    int stepCount = intent.getIntExtra("stepCount", 0);
                    steps.setText(stepCount);
                }
            }
        };*/

        Button gameButton = (Button)findViewById(R.id.gameButton);
        /**
         * Move to the game activity and store the current state of the main activity.
         * Give the cash amount a default value of 1000 if no prior value was given.
         */
        gameButton.setOnClickListener(
                view -> {
                    saveData();
                    Intent intent = new Intent(view.getContext(), GameActivity.class);
                    startActivity(intent);
                });
        Button upgradeButton = (Button)findViewById(R.id.upgradeButton);
        /**
         * Move to the upgrade activity and store the current state of the main activity.
         * Give the cash amount a default value of 1000 if no prior value was given.
         */
        upgradeButton.setOnClickListener(
                view -> {
                    saveData();
                    Intent intent = new Intent(view.getContext(), UpgradeActivity.class);
                    startActivity(intent);
                });
    }

    private final SensorEventListener stepCountSensorListener = new
            SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

                        if(event.values[0] > Integer.MAX_VALUE || event.values[0] < 0) {
                            return;
                        }

                        // Get the step count from the event
                        int stepCount = (int) event.values[0];
                        mSteps.setText(String.valueOf(stepCount));
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
    };

    private void saveData() {
        // Save data using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("localData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("Sets", mSets.getText().toString());
        editor.putString("Reps", mReps.getText().toString());

        editor.apply();

        SharedPreferences loginPreferences = getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
        String username = loginPreferences.getString("username", "");
        String password = loginPreferences.getString("password", "");
        SharedPreferences gamedata = getSharedPreferences("localData", Context.MODE_PRIVATE);
        String cash = gamedata.getString("Cash", "1000");
        new SendDataToServerTask().execute(username, password, cash);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        saveData();

        sensorManager.unregisterListener(stepCountSensorListener);

        SharedPreferences loginPreferences = getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
        String username = loginPreferences.getString("username", "");
        String password = loginPreferences.getString("password", "");
        SharedPreferences gamedata = getSharedPreferences("localData", Context.MODE_PRIVATE);
        String cash = gamedata.getString("Cash", "1000");
        new SendDataToServerTask().execute(username, password, cash);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("Sets", mSets.getText().toString());
        savedInstanceState.putString("Reps", mReps.getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSets.setText(savedInstanceState.getString("Sets"));
        mReps.setText(savedInstanceState.getString("Reps"));
    }
}

