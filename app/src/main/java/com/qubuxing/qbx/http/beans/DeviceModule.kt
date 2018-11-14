package com.qubuxing.qbx.http.beans

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.JavascriptInterface
import com.qubuxing.qbx.utils.GetGeoUtil
import org.json.JSONObject


class DeviceModule(internal var mContext: Context) {

    //设备型号
    var deviceInfo: String =""
        @JavascriptInterface
        get() {

            try {
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    val jsonObject = JSONObject()
                    val tm = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    var deviceID =""
                    deviceID = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                        tm.deviceId
                    }else{
                        tm.imei
                    }


                    val deviceVersion = Build.MODEL
                    val systemVersion = Build.VERSION.RELEASE
                    val deviceName = Build.HOST
                    jsonObject.put("deviceOwner", deviceName)
                    jsonObject.put("deviceBrand", deviceVersion)
                    jsonObject.put("deviceImei", deviceID)
                    jsonObject.put("osVer", systemVersion)
                    var s = ChannelModule(mContext).channel
                    val ss = s.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    if (ss.size == 2)
                        s = ss[1]
                    jsonObject.put("channelCode", s)

                    val location = GetGeoUtil(mContext as Activity).geo
                    if (location != null) {
                        jsonObject.put("geoLon", location!!.getLongitude()  )
                        jsonObject.put("geoLat", location!!.getLatitude() )
                    }

                    Log.e("devceInfo", jsonObject.toString())

                    return jsonObject.toString()
                }



            } catch (e: Exception) {
                e.printStackTrace()
            }

            return deviceInfo

        }
}