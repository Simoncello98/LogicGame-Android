package com.example.logicgame;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.FloatMath;
import android.util.Log;

import static android.content.Context.SENSOR_SERVICE;
import static java.lang.Math.sqrt;

public class ShakeDetector  implements SensorEventListener {


    private static final float SHAKE_THRESHOLD_GRAVITY = 1.2F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }


    public interface OnShakeListener {
        public void onShake(int count);
    }

    public ShakeDetector(Context context){
        SensorManager sensorManager= (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            double gForce = sqrt(gX * gX + gY * gY + gZ * gZ);
            //Log.d("SHAKE", String.valueOf(gForce));
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {

                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    //Log.d("NOT", "CONSIDERED-timestamp");
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;
                //Log.d("cALL ", "delegate");
                mListener.onShake(mShakeCount);
            }
        }
    }
}