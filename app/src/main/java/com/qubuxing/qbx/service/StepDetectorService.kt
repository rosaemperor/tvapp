package com.qubuxing.qbx.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.qubuxing.qbx.http.beans.StepGetEvent
import com.qubuxing.qbx.utils.ReBootHelper
import com.qubuxing.qbx.utils.SharePrefenceHelper
import org.greenrobot.eventbus.EventBus

class StepDetectorService : IntentService("StepDetectorService"){
    private var mSensorManager: SensorManager? = null
    private var mListener: SensorEventListener? = null
    private var mSensorCounter: Sensor? = null
    private var mHasSensor: Boolean = false
    private var toDaySteps = 0.0f
    override fun onHandleIntent(intent: Intent?) {
        mHasSensor = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
        if (mHasSensor) {
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorCounter = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if(event.values[0] == 1.0f && ReBootHelper.isTheSameDay()){
                        if(toDaySteps < SharePrefenceHelper.getFloat("TodayServiceSteps")){
                            toDaySteps = SharePrefenceHelper.getFloat("TodayServiceSteps")
                        }
                        toDaySteps= toDaySteps++
                    }else if(event.values[0] == 1.0f && !ReBootHelper.isTheSameDay()){
                        toDaySteps = 0.0f
                        toDaySteps = toDaySteps++
                    }
                    SharePrefenceHelper.saveFloat("TodayServiceSteps",toDaySteps)
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

    override fun onDestroy() {
        SharePrefenceHelper.saveBolean("ServiceHasDead",true)
        super.onDestroy()
    }
}