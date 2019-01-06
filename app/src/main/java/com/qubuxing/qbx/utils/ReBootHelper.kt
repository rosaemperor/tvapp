package com.qubuxing.qbx.utils

import android.os.SystemClock
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object ReBootHelper {

    /**
     * 记录当前开机的日期和开机的时间
     * timeString:当前开机时间（精确到秒）
     * dataString:当前日期（精确到天）
     *
     */
    var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    fun saveBootOpenTime(){
        var oldDate = Calendar.getInstance().time
        var oldDateString = simpleDateFormat.format(oldDate)
        var oldRebootTime = SystemClock.elapsedRealtimeNanos() / 1000000000
        SharePrefenceHelper.save("DateOfNow",oldDateString)
        SharePrefenceHelper.saveLong("CurrentTime",System.currentTimeMillis()/1000)
        SharePrefenceHelper.saveLong("BootOpenTime" ,oldRebootTime)
    }

    /**
     * 判断当天是否出现过重启事件
     */
    fun isReBoot() : Boolean{
        var oldDate = SharePrefenceHelper.get("DateOfNow")
        var oldCurrentTime = SharePrefenceHelper.getLong("CurrentTime")
        if(oldCurrentTime ==0L){
            Log.i("TAG","首次安装默认不是重启状态")
            SharePrefenceHelper.saveBoolean("isReBoot", false)
            return false
        }
        if(SharePrefenceHelper.getBoolean("isReBoot")){
            SharePrefenceHelper.saveBoolean("isReBoot", false)
            Log.i("TAG","有接受到系统重启")
            return true
        }
        var currentTime = System.currentTimeMillis()/1000
        var oldBootOpenSecond = SharePrefenceHelper.getLong("BootOpenTime" )
        var bootOpenSecond =  SystemClock.elapsedRealtimeNanos() / 1000000000
        var date = Calendar.getInstance().time
        var dateString = simpleDateFormat.format(date)
        if (dateString == oldDate){
             if((bootOpenSecond - oldBootOpenSecond) <0){
                return true
             }
             if((currentTime - oldCurrentTime) == (bootOpenSecond - oldBootOpenSecond)) return false
             if(((currentTime - oldCurrentTime) - (bootOpenSecond - oldBootOpenSecond)) > 5){
                 Log.i("TAG","有系统重启")
                 return true
             }
        }
        return false
    }

    /**
     * 判断上次记录步数的日期与当前日期是否未同一天
     */
    fun isTheSameDay() : Boolean{
        var oldDateString = SharePrefenceHelper.get("TodayDate")
        var todayDate = Calendar.getInstance().time
        var todayString = simpleDateFormat.format(todayDate)
        if(todayString == oldDateString){
            Log.i("TAG","有判断到两次记步之间不是同一天")
            return true
        }else{
            SharePrefenceHelper.save("TodayDate", todayString)
        }
        return false
    }

}