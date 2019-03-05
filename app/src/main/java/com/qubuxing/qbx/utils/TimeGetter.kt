package com.qubuxing.qbx.utils

import android.util.Log
import com.qubuxing.qbx.http.TimeRetrofitUtil
import okhttp3.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

object TimeGetter {
    var timeCall = TimeRetrofitUtil.instance.timeHelp.callBaidu()
    fun getCurrentTime(timeBack : TimeCallback){
        var timeString :Long =0
        timeCall.clone().enqueue(object : Callback<Any>{
            override fun onFailure(call: retrofit2.Call<Any>, t: Throwable) {
                timeBack.failuredGetTime()
            }

            override fun onResponse(call: retrofit2.Call<Any>, response: Response<Any>) {
                var time = response.headers().get("Date")
                time = time!!.replace("GMT", "").replace("\\(.*\\)".toRegex(), "").replace(",","")
                var simpleDateFormat = SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss",java.util.Locale.ENGLISH)
                var date = simpleDateFormat.parse(time)
                timeBack.currentTime(date.time)
            }
        })

        //调整位置，+一个回调接口，请求完成后，回调时间，继续执行相关操作。

    }
}