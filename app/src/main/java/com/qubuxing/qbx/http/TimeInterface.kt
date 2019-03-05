package com.qubuxing.qbx.http

import retrofit2.Call
import retrofit2.http.GET

interface TimeInterface {
    @GET("time")
    fun callBaidu() : Call<Any>
}