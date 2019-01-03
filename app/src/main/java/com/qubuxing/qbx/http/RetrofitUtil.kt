package com.qubuxing.qbx.http

import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.qubuxing.qbx.HttpService
import com.qubuxing.qbx.QBXApplication
import com.qubuxing.qbx.config
import com.qubuxing.qbx.http.beans.ResponseEntity
import com.qubuxing.qbx.utils.SharePrefenceHelper
import okhttp3.*

import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class RetrofitUtil private constructor() {
    lateinit var retrofit: Retrofit
    lateinit var help: HttpService
        private set
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
                    deviceID = ""+UUID.randomUUID().toString()
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
                var responseBody: ResponseBody? = null
                var jsonObject : JSONObject = JSONObject()
                try {
                    jsonObject = JSONObject(response.body()!!.string())
                    if(jsonObject.getInt("code") == 0 ){
                        val data = jsonObject.get("data").toString() + ""
                        val type = MediaType.parse("image/jpeg; charset=utf-8")
                        Log.d("TAG", "data:$data")
                        responseBody = ResponseBody.create(type, data)
                    }else if(jsonObject.getInt("code") == 9000 ){
                        val data = jsonObject.get("data").toString() + ""
                        val type = MediaType.parse("image/jpeg; charset=utf-8")
                        Log.d("TAG", "data:$data")
                        responseBody = ResponseBody.create(type, data)
                        response = response.newBuilder().body(responseBody).build()
                        return response
                    }
                    else{

                    }
                }catch (jsone : JSONException){
                    val type = MediaType.parse("image/jpeg; charset=utf-8")
                    responseBody = ResponseBody.create(type, "")
                }




//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }

                response = response.newBuilder().body(responseBody).build()
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
        help = retrofit.let { retrofit.create(HttpService::class.java) }

    }

    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }

    companion object {
        val instance = RetrofitUtil()
    }
}