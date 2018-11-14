package com.qubuxing.qbx.utils


import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.qubuxing.qbx.http.beans.Application

class GetApplicationUtil {
    var appTypeSystem ="system"
    var appTypeCustom = "custom"


    fun getApplications(packageManager : PackageManager) : List<Application> {
        var packages = packageManager.getInstalledPackages(0)
        var applist  = ArrayList<Application>()
        for ( i in packages){
            var type = appTypeSystem
            if (i.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0){
                type = appTypeCustom
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
                var tmpInfo = Application(i.applicationInfo.loadLabel(packageManager).toString(),
                        i.packageName,
                        i.versionName,
                        ""+i.longVersionCode
                        ,type)
                applist.add(tmpInfo)
            }
            else{
                var tmpInfo = Application(i.applicationInfo.loadLabel(packageManager).toString(),
                        i.packageName,
                        i.versionName,
                        ""+i.versionCode
                        ,type)
                applist.add(tmpInfo)
            }



        }
        return applist
    }
}