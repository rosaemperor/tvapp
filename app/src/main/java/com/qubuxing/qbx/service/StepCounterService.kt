package com.qubuxing.qbx.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.Sensor.TYPE_STEP_COUNTER
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.util.Log
import com.qubuxing.qbx.http.beans.StepGetEvent
import com.qubuxing.qbx.utils.SharePrefenceHelper
import org.greenrobot.eventbus.EventBus


class StepCounterService : IntentService("StepCounterService"){
    private var mSensorManager: SensorManager? = null
    private var mListener: SensorEventListener? = null
    private var mSensorCounter: Sensor? = null
    private var mHasSensor: Boolean = false
    override fun onHandleIntent(intent: Intent?) {
        mHasSensor = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
        if (mHasSensor) {
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorCounter = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

            mListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    var stepEvent = StepGetEvent()
                    stepEvent.setps = event.values[0]

                    EventBus.getDefault().postSticky(stepEvent)
                }

               override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

                }
            }


        } else {

        }

        if (mHasSensor) {
            mSensorManager!!.registerListener(mListener, mSensorCounter, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }
}