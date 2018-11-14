package com.qubuxing.qbx

import com.qubuxing.qbx.http.beans.ImageJson
import com.qubuxing.qbx.http.beans.UpdateEntity
import com.qubuxing.qbx.http.beans.UpdateResultEntity
import com.qubuxing.qbx.http.beans.UserMessages
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
}