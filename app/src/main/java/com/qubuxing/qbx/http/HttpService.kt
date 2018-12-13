package com.qubuxing.qbx

import com.qubuxing.qbx.http.beans.*
import retrofit2.Call
import retrofit2.http.*

interface HttpService {
    @POST("customerCwral")
     fun upLoadUserMessage(@Body messages: UserMessages): Call<Any>

    @POST("face/basic/ocr")
     fun upLoadFaceImage(@Body imageJson: ImageJson) : Call<Any>

    @POST("app/upgrade")
     fun checkUpdate(@Body any: UpdateEntity) : Call<UpdateResultEntity>
    @Streaming
    @GET
     fun getApkFile(@Url url : String) : Call<Any>

    @POST("adver/list")
     fun getAdSplashs(@Body entity: GetSplashEntity) : Call<List<SplashAdEntity>>
}