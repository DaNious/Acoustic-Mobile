package com.example.student.inhaler_prj_v10;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/* Sensor Class to get sensor data */
/* Added on 02202019 */

public class SensorActivity implements SensorEventListener {
    private final SensorManager mSensorManager;
    private final Sensor mRotationVector;
    private final Sensor mAcceleration;
    private final MainActivity mainActivity;

    protected SensorActivity(SensorManager sm, MainActivity ma){
        mSensorManager = sm;
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.mainActivity = ma;
    }

    protected void onResume(){
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            mainActivity.rotationChanged(event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            mainActivity.accelerationChanged(event.values);
        }
    }
}
