package com.qubuxing.qbx.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.util.Log

class GetGeoUtil(private val mActivity: Activity) {
    private var locationManager: LocationManager? = null
    private var locationProvider: String? = null

    //获取所有可用的位置提供器
    //如果是GPS
    //如果是Network
    //获取Location
    // TODO: Consider calling
    val geo: Location?
        get() {

            try {
                if (!checkGPSisOpen()) DialogUtils.showLocationRequestDialog(mActivity)
                locationManager = mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = locationManager!!.getProviders(true)
                if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationProvider = LocationManager.GPS_PROVIDER
                } else if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationProvider = LocationManager.NETWORK_PROVIDER
                } else if (locationManager!!.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                    locationProvider = LocationManager.PASSIVE_PROVIDER
                } else {
                    return null
                }
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    return null
                }
                var location = locationManager!!.getLastKnownLocation(locationProvider)
                if(null == location && locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    locationProvider = LocationManager.NETWORK_PROVIDER
                }
                location = locationManager!!.getLastKnownLocation(locationProvider)
                return location
            } catch (e: Exception) {
                return null
            }

        }


    fun checkGPSisOpen() : Boolean{
        var locationManager = mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        var GPSIsOpen = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        var networkOpen = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
//        var passiveOpen = locationManager.isProviderEnabled(android.location.LocationManager.PASSIVE_PROVIDER)
        if(!GPSIsOpen  && !networkOpen){
            return false
        }else{
            return true
        }

    }

}