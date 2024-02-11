package com.example.fitnesstrackergame;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private static final int NOTIFICATION_ID = 123; // Unique notification ID

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "step_counter_channel",
                    "Step Counter Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification to keep the service running in the foreground
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "step_counter_channel")
                .setContentTitle("Step Counter Service")
                .setContentText("Tracking steps...")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        // Initialize the sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Check if the step counter sensor is available
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor != null) {
            // Register the sensor listener
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the sensor listener and stop the foreground service
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
        }
        stopForeground(true);
    }

    public static final String STEP_COUNT_UPDATE_ACTION = "com.example.fitnesstrackergame.STEP_COUNT_UPDATE";
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Get the step count from the event
            int stepCount = (int) event.values[0];

            // Send a broadcast with the step count
            Intent intent = new Intent(STEP_COUNT_UPDATE_ACTION);
            intent.putExtra("stepCount", stepCount);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step counter sensor
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
