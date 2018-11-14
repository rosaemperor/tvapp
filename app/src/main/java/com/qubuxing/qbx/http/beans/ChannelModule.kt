package com.qubuxing.qbx.http.beans

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.webkit.JavascriptInterface

class ChannelModule(internal var mContext: Context) {

    private val umengKey = "UMENG_CHANNEL"

    var channel: String = ""
        @JavascriptInterface
        get() {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val tm = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                var channelName: String = ""
                var imei: String?
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                    imei = tm.deviceId
                } else {
                    imei = tm.imei
                }
                if (imei == null || imei == "") imei = "xx"
                val packageManager = mContext.packageManager
                try {
                    if (packageManager != null) {
                        val applicationInfo = packageManager.getApplicationInfo(mContext.packageName, PackageManager.GET_META_DATA)
                        if (applicationInfo != null) {
                            if (applicationInfo.metaData != null) {
                                channelName = applicationInfo.metaData.getString(umengKey)
                            }
                        }

                    }
                } catch (e: Exception) {
                    return ""
                }

                return channelName
            }

            return ""
        }


}
