package com.example.pabsensor_percepatan;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private MediaPlayer soundSpeedLess;
    private MediaPlayer soundSpeedMore;
    private float lastVelocity = 0.0f;
    private long lastUpdateTime = 0;
    private final float SPEED_LESS_THRESHOLD = 20.0f ; // Kurang dari 20 km/jam dalam m/s
    private final float SPEED_MORE_THRESHOLD = 60.0f ; // Lebih dari 60 km/jam dalam m/s
    private final float MOVEMENT_THRESHOLD = 0.2f; // Ambang batas percepatan untuk deteksi gerakan (m/s^2)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        soundSpeedLess = MediaPlayer.create(this, R.raw.kurang); // Suara notifikasi untuk kecepatan kurang
        soundSpeedMore = MediaPlayer.create(this, R.raw.lebih); // Suara notifikasi untuk kecepatan lebih

        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // Metode untuk menampilkan AlertDialog
//    private void showSpeedAlert(String message) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Peringatan Kecepatan")
//                .setMessage(message)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User menekan tombol OK
//                    }
//                });
//        AlertDialog alert = builder.create();
//        alert.show();
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastUpdateTime;

            // Hitung percepatan total dari semua sumbu
            float totalAcceleration = (float) (Math.sqrt(Math.pow(event.values[0], 2) +
                                Math.pow(event.values[1], 2) +
                                Math.pow(event.values[2], 2)) - SensorManager.GRAVITY_EARTH);

            // Hanya hitung kecepatan jika ada gerakan yang cukup
            if (totalAcceleration > MOVEMENT_THRESHOLD) {
                // Integrasi percepatan untuk mendapatkan kecepatan
                lastVelocity += (totalAcceleration * deltaTime);

                if (lastVelocity < SPEED_LESS_THRESHOLD) {
                    soundSpeedLess.start(); // Mainkan suara notifikasi untuk kecepatan kurang dari 20 km/jam
//                    showSpeedAlert("Kecepatan anda kurang dari 20km/jam");
                    Toast.makeText(this, "Kecepatan anda kurang dari 20km/jam", Toast.LENGTH_SHORT).show();
                } else if (lastVelocity > SPEED_MORE_THRESHOLD) {
                    soundSpeedMore.start(); // Mainkan suara notifikasi untuk kecepatan lebih dari 60 km/jam
//                    showSpeedAlert("Kecepatan anda lebih dari 60km/jam");
                    Toast.makeText(this, "Kecepatan anda lebih dari 60km/jam", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Jika tidak ada gerakan yang cukup, reset kecepatan menjadi 0
                lastVelocity = 0.0f;
            }

            lastUpdateTime = currentTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Metode ini diperlukan oleh interface SensorEventListener
    }
}