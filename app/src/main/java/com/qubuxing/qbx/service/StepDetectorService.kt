package com.qubuxing.qbx.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import com.qubuxing.qbx.BuildConfig
import com.qubuxing.qbx.utils.ReBootHelper
import com.qubuxing.qbx.utils.SharePrefenceHelper

class StepDetectorService : IntentService("StepDetectorService"){
    private var mSensorManager: SensorManager? = null
    private var mListener: SensorEventListener? = null
    private var mSensorCounter: Sensor? = null
    private var mHasSensor: Boolean = false
    private var toDaySteps = 0.0f
    override fun onHandleIntent(intent: Intent?) {
//        startForeground(9001,null)
        mHasSensor = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
        if (mHasSensor) {
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mSensorCounter = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    toDaySteps = SharePrefenceHelper.getFloat("TodayServiceSteps")

                    Log.i("TAG","记步：${event.values[0]}")
                    if(event.values[0] == 1.0f && ReBootHelper.isTheSameDay()){
                        if(toDaySteps < SharePrefenceHelper.getFloat("TodayServiceSteps")){
                            toDaySteps = SharePrefenceHelper.getFloat("TodayServiceSteps")
                        }
                        toDaySteps= toDaySteps++
                        Log.i("TAG","记步service状态1：toDaySteps：${toDaySteps}")
                    }else if(event.values[0] == 1.0f && !ReBootHelper.isTheSameDay()){
                        toDaySteps = 0.0f
                        toDaySteps = toDaySteps++
                        Log.i("TAG","记步service状态2：toDaySteps：${toDaySteps}")
                    }
//                    Log.i("TAG","toDaySteps:${toDaySteps}")
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
        Log.i("TAG","记步服务被杀死")
        if(BuildConfig.DEBUG){
            Toast.makeText(this@StepDetectorService , "记步服务被杀死",Toast.LENGTH_LONG).show()
        }
        SharePrefenceHelper.saveBoolean("ServiceHasDead",true)
        SharePrefenceHelper.saveFloat("TodayServiceSteps",0.0f)
        super.onDestroy()
    }
}