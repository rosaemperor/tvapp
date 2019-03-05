package com.qubuxing.qbx.http

import android.util.Log
import com.qubuxing.qbx.HttpService
import com.qubuxing.qbx.config
import com.qubuxing.qbx.utils.SharePrefenceHelper
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class TimeRetrofitUtil(){
    lateinit var retrofit: Retrofit
    lateinit var timeHelp: TimeInterface
    private var client: OkHttpClient? = null
    private var loggingInterceptor: HttpLoggingInterceptor? = null

    init {
        init()
    }

    private fun init() {
        loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor!!.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder().addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain?): Response {

                var requestUUID = ""+ UUID.randomUUID().toString()
                var deviceID= ""
                if (SharePrefenceHelper.get("deviceID")==""){
                    deviceID = ""+ UUID.randomUUID().toString()
                    SharePrefenceHelper.save("deviceID",deviceID)
                }else deviceID =  SharePrefenceHelper.get("deviceID")
                val request = chain!!.request().newBuilder()
                        .addHeader("version","1.0.1")
                        .addHeader("device_uuid",deviceID)
                        .addHeader("channel_code","android")
                        .addHeader("platform","android")
                        .addHeader("request_uuid","$requestUUID")
                        .addHeader("Content-Type", "text/html; charset=UTF-8")
                        .build()

                var response = chain.proceed(request)
                return response
            }
        })
                .addInterceptor(loggingInterceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build()


        retrofit = Retrofit.Builder()
                //                .baseUrl( "http://api.ih2ome.cn/")
                .baseUrl(config.HOST_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        timeHelp = retrofit.let { retrofit.create(TimeInterface::class.java) }

    }

    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }

    companion object {
        val instance = TimeRetrofitUtil()
    }
}