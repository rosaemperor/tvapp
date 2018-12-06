package com.qubuxing.qbx.parts

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.Toast
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import com.bytedance.sdk.openadsdk.*

import com.google.gson.Gson
import com.ly.adpoymer.interfaces.*
import com.ly.adpoymer.manager.*
import com.qq.e.ads.banner.ADSize
import com.qq.e.ads.banner.AbstractBannerADListener
import com.qq.e.ads.banner.BannerView
import com.qq.e.ads.splash.SplashAD
import com.qq.e.ads.splash.SplashADListener
import com.qq.e.comm.util.AdError
import com.qubuxing.ThridBroeserActivity
import com.qubuxing.qbx.*
import com.qubuxing.qbx.R


import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.http.RetrofitUtil

import com.qubuxing.qbx.http.beans.*
import com.qubuxing.qbx.service.StepCounterService
import com.qubuxing.qbx.utils.*
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.*
import org.json.JSONException
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class WVWebViewClient constructor(webView: WebView,messageHandler: WVJBHandler? = null) : WVJBWebViewClient(webView,messageHandler) {
    var thread : Thread ?= null
    var gson:Gson = Gson()
    var imageGetMobile :String=""
    var imageType = ""
    var firstClickTime: Long = 0L
    var codeCallback: WVJBResponseCallback? = null
    var CAMERA_REQUEST_CODE=1110
    var READ_PHONE=10086
    var pageGetFinished = false
    var UUIDCallback : WVJBResponseCallback? = null
    var cameraList = ArrayList<String>()
    var ACTIVITYFOROMCLIENT = 10010
     var stepCallback: WVJBResponseCallback? = null
    var httpHelper : HttpService = RetrofitUtil.instance.help
    var backStep = false
    var haveStepToday : Int = 0
    var mTTAdNative : TTAdNative
    lateinit var adSlot : AdSlot

    var mttRewardVideoAd :TTRewardVideoAd? = null

    constructor(webView: WebView) : this(webView ,object :WVJBHandler{
        override fun request(data: Any?, callback: WVJBResponseCallback?) {
            callback!!.callback("Response for message from ObjC!")
        }
    })
    init {
//微信登录调起

        mTTAdNative = QBXApplication.ttAdManager.createAdNative(webView.context)
        registerHandler("submitWechatLogin", object : WVJBHandler {
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo"
                req.state = "diandi_wx_login"
                QBXApplication.instance.getWXAPI().sendReq(req)
                codeCallback = callback            }
        })
//        registerHandler("LaunchMiniProgramCard",object : WVJBWebViewClient.WVJBHandler{
//            override fun request(data: Any?, callback: WVJBResponseCallback?) {
//                var miniProgrameObj = WXMiniProgramObject()
//                var entity = gson.fromJson<WXSeneEntity>(data.toString(),WXSeneEntity::class.java)
//                miniProgrameObj.webpageUrl = entity.webpageUrl
//                miniProgrameObj.userName = entity.userName
//                miniProgrameObj.path = entity.path
//                var msg = WXMediaMessage(miniProgrameObj)
//                msg.title = entity.title
//                msg.description = buildTransaction("webpage")
////                msg.thumbData =
//                var req = SendMessageToWX.Req()
//                var bitmap: Bitmap? = null
//                thread = Thread(Runnable{
//                    //                    bitmap = Glide.with(webView.context).asBitmap().load(entity.imageurl).into(500,500).get()
//                    bitmap = Glide.with(webView.context).load(entity.imageurl).asBitmap().into(200,200).get()
//                    bitmap = BitmapUtils.drawableBitmapOnWhiteBg(webView.context,bitmap!!)
////                    msg.setThumbImage(bitmap)
//                    msg.thumbData = ByteBuffer.allocate(bitmap!!.byteCount).array()
//                    req.message = msg
//                    req.scene = SendMessageToWX.Req.WXSceneSession
//                    req.transaction = entity.webpageUrl
//                    var api = QBXApplication.api
//                    api.sendReq(req)
//                })
//                thread!!.start()
//            }
//        })
//        registerHandler("LaunchMiniProgram",object : WVJBHandler{
//            override fun request(data: Any?, callback: WVJBResponseCallback?) {
//                var req = WXLaunchMiniProgram.Req()
//                var entity = gson.fromJson<WXSeneEntity>(data.toString(),WXSeneEntity::class.java)
//                req.userName = entity.userName
//                req.path = entity.path
//                QBXApplication.api.sendReq(req)
//            }
//        })
        registerHandler("getVersion", object : WVJBHandler {
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                try {
                    val packageInfo = webView.context.packageManager.getPackageInfo(webView.context.packageName, 0)
                    val jsonObject = JSONObject()
                    jsonObject.put("versionName", packageInfo.versionName)
                    if (Build.VERSION.SDK_INT<= 27) jsonObject.put("versionCode", packageInfo.versionCode)
                    if (Build.VERSION.SDK_INT>= 28) jsonObject.put("versionCode", packageInfo.longVersionCode)
                    jsonObject.put("buildCode", QBXApplication.buildCode)
                    callback!!.callback(jsonObject.toString())
                } catch (e: Exception) {
                    Log.e("error", e.message)
                }
            }




        })
        registerHandler("postImg",object : WVJBHandler{
            @RequiresApi(Build.VERSION_CODES.M)
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var jsonObject : JSONObject = data as JSONObject
                 imageType = jsonObject.getString("num")
                imageGetMobile = jsonObject.getString("mobile")
                if (ActivityCompat.checkSelfPermission(
                                webView.context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(webView.context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                   takePhoto()
                }else{
                    cameraList.add( android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    cameraList.add(Manifest.permission.CAMERA)
                    (webView.context as Activity).requestPermissions(cameraList.toTypedArray(),CAMERA_REQUEST_CODE)
                }

            }
        })
        registerHandler("postDeviceInfo", object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                val jsonObject = JSONObject()
                if (ActivityCompat.checkSelfPermission(webView.context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    var tm = webView.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    var deviceID = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                        tm.deviceId
                    } else {
                        tm.imei
                    }
                    SharePrefenceHelper.save("deviceID",deviceID)
                    var deviceVersion = Build.MODEL
                    var systemVersion = Build.VERSION.RELEASE
                    var deviceName = Build.HOST
                    jsonObject.put("deviceOwner", deviceName)
                    jsonObject.put("deviceBrand", deviceVersion)
                    jsonObject.put("deviceImei", deviceID)
                    jsonObject.put("osVer", systemVersion)
                    var s = ChannelModule(webView.context).channel
                    val ss = s.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (ss.size == 2)
                        s = ss[1]
                    jsonObject.put("channelCode", s)

                    val location = GetGeoUtil(webView.context as Activity).geo
                    if (location != null) {
                        jsonObject.put("geoLon", location.longitude)
                        jsonObject.put("geoLat", location.latitude)
                    }

                    Log.e("devceInfo", jsonObject.toString())

                    callback!!.callback(jsonObject.toString())
                }else{
                }
            }
        })
        registerHandler("goAutoSetting",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                JumpSetting.jumpStartInterface(webView.context)
            }

        })
        registerHandler("getInitMessage",object : WVJBHandler{
            @RequiresApi(Build.VERSION_CODES.M)
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                if (ActivityCompat.checkSelfPermission(
                                webView.context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(webView.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(webView.context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

                ) {
                    var initMessage = InitMessage()

                    var packageManager = webView.context.packageManager
                    var applicationInfo = packageManager.getApplicationInfo(webView.context.packageName,PackageManager.GET_META_DATA)
                    var tm = webView.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    initMessage.deviceUUID = tm.imei
                    var packageInfo = packageManager.getPackageInfo(webView.getContext(). getPackageName(),PackageManager.GET_META_DATA)
                    initMessage.traffic_channel= applicationInfo.metaData.getString("JPUSH_CHANNEL")
                    initMessage.versionString = packageInfo.versionName
                    callback!!.callback(gson.toJson(initMessage))
                }else{
                    UUIDCallback = callback
                    cameraList.clear()
                    cameraList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    cameraList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    cameraList.add(Manifest.permission.READ_PHONE_STATE)
                    (webView.context as Activity).requestPermissions(cameraList.toTypedArray(),READ_PHONE)
                }


            }
        })
        registerHandler("getAppStep",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var intent = Intent()
//                var json = data as JsonObject
//                haveStepToday = json.get("step").asInt
                intent.setClass(webView.context, StepCounterService::class.java)
                webView.context.startService(intent)
                var stepNum = ((webView.context) as MainActivity).getStep()
                backStep = true
                stepCallback =callback
            }
        })
        registerHandler("backPress", object : WVJBHandler {
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                val s = ""
                val jsonObject = data as org.json.JSONObject
                jsonObject.toString()
                try {
                    val b = jsonObject.getBoolean("isRootPage")
                    if (b) {
                        if (System.currentTimeMillis() - firstClickTime < 2000) {
                            (webView.context as Activity).finish()
                        } else {
                            firstClickTime = System.currentTimeMillis()
                            Toast.makeText(webView.context, R.string.double_click_to_quit, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        webView.goBackOrForward(-1)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
        registerHandler("canScroll",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var scroll  = ( data as JSONObject).get("scroll") as Boolean
                binding?.let {
                    binding.swipeLayout.isEnabled = scroll
                }

            }
        })
        registerHandler("WXSceneSessionClick",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var entity = gson.fromJson<WXSeneEntity>(data.toString(),WXSeneEntity::class.java)
                var webpage = WXWebpageObject()
                webpage.webpageUrl = entity.webpageUrl
                var msg = WXMediaMessage()
                msg.mediaObject = webpage
                msg.title = entity.title
                msg.description =entity.information
                var req = SendMessageToWX.Req()
                var bitmap: Bitmap? = null
                thread = Thread(Runnable{
//                    bitmap = Glide.with(webView.context).asBitmap().load(entity.imageurl).into(500,500).get()
                    bitmap = Glide.with(webView.context).load(entity.imageurl).asBitmap().into(200,200).get()
                    bitmap = BitmapUtils.drawableBitmapOnWhiteBg(webView.context,bitmap!!)
                    msg.setThumbImage(bitmap)
                    req.message = msg
                    req.scene = SendMessageToWX.Req.WXSceneSession
                    req.transaction = entity.webpageUrl
                    var api = QBXApplication.api
                    api.sendReq(req)
                })
                thread!!.start()


            }
        })
        registerHandler("WXSceneTimelineClick",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var entity = gson.fromJson<WXSeneEntity>(data.toString(),WXSeneEntity::class.java)
                var webpage = WXWebpageObject()
                webpage.webpageUrl = entity.webpageUrl

                var msg = WXMediaMessage()
                msg.mediaObject = webpage
                msg.title = entity.title
                msg.description =entity.information
                var req = SendMessageToWX.Req()
                var bitmap: Bitmap? = null
                thread = Thread(Runnable{
//                    bitmap = Glide.with(webView.context).asBitmap().load(entity.imageurl).into(500,500).get()

                    bitmap = Glide.with(webView.context).load(entity.imageurl).asBitmap().into(200,200).get()
                    bitmap = BitmapUtils.drawableBitmapOnWhiteBg(webView.context,bitmap!!)
                    msg.setThumbImage(bitmap)
                    req.message = msg
                    req.scene = SendMessageToWX.Req.WXSceneTimeline
                    req.transaction = entity.webpageUrl
                    var api = QBXApplication.api
                    api.sendReq(req)
                })
                thread!!.start()
                         }
        })
        registerHandler("openThirdPartPage", object : WVJBHandler {
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var  entity = gson.fromJson<BackEntity>(data.toString(), BackEntity::class.java)
                val intent = Intent()
                val bundle = Bundle()
                if (null == entity) {
                    return
                }
                bundle.putString("url", entity.url)
                intent.putExtras(bundle)
                intent.setClass(webView.context, ThridBroeserActivity::class.java!!)
                webView.context.startActivity(intent)
                return
            }
        })
        registerHandler("transmitAlias",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                val jsonObject = data as org.json.JSONObject
                JPushInterface.setAlias(webView.context,1,jsonObject.getString("alias"))
            }
        })
        registerHandler("showAdOnSplash",object  : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var id  =""+ ( data as JSONObject).get("id")
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var spreadListener = object : SpreadListener{
                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdDisplay(p0: String?) {
                    }

                    override fun onAdReceived(p0: String?) {
                    }

                    override fun onAdClick() {
                        callback?.let {
                            callback.callback("onAdClick")
                        }
                    }

                    override fun onAdClose(p0: String?) {
                        callback?.let {
                            callback.callback("onAdClose")
                            binding!!.splashLayout.removeAllViews()
                            binding!!.splashLayout.invalidate()
                        }
                    }
                }

                SpreadManager.getInstance(webView.context as Activity).request(webView.context as Activity,"$id",binding!!.splashLayout,spreadListener)
            }
        })
        registerHandler("showAdOnInsert",object  : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var id  =""+ ( data as JSONObject).get("id")
                var insertListener = object : InsertListener {
                    override fun onAdDismiss(p0: String?) {
                        callback?.let {
                            callback.callback("onAdClose")
                        }
                    }

                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdDisplay(p0: String?) {
                    }

                    override fun onAdReceived(p0: String?) {
                        if (InsertManager.getInstance(webView.context).isReady) {
                            InsertManager.getInstance(webView.context).showAd()
                        }
                    }

                    override fun onAdClick(p0: String?) {
                        callback?.let {
                            callback.callback("onAdClick")
                        }
                    }

                }
                var count = 3
//                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                InsertManager.getInstance(webView.context).requestAd(webView.context,"$id",insertListener,count)
            }
        })
        registerHandler("showAdOnNative",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var nativeListener = object : NativeListener{
                    override fun onAdFailed(p0: String?) {
                    }

                    override fun onAdReceived(p0: java.util.ArrayList<*>?) {

                    }

                    override fun onAdDisplay() {
                    }

                    override fun onADClosed(p0: View?) {
                    }

                    override fun onAdClick() {
                    }

                    override fun OnAdViewReceived(p0: MutableList<out View>?) {
                    }

                }
                var count = 2
                NativeManager.getInstance(webView.context).requestAd(webView.context,"7539",count, nativeListener)
            }
        })
        registerHandler("showAdOnBanner",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                binding!!.adLayout.removeAllViewsInLayout()
                binding.adLayout.invalidate()
                var id  =""+ ( data as JSONObject).get("id")
                var bannerListener = object : BannerListener{
                    override fun onAdFailed(p0: String?) {
                        callHandler("bannerCallback","onAdFailed",null)
                        callback?.let {
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdDisplay(p0: String?) {
                        var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding.adLayout.removeAllViews()
                        }
                        binding.adLayout.addView(closeView)
                        binding.adLayout.invalidate()
                    }

                    override fun onAdClick(p0: String?) {
                        Log.d("banner","onAdClick")
                        callHandler("bannerCallback","onAdClick",null)
                        callback?.let {
                            callback.callback("onAdClick")
                        }
                    }

                    override fun onAdReady(p0: String?) {
                        callHandler("bannerCallback","onAdReady",null)
                        callback?.let {
                            callback.callback("onAdReady")
                        }
                    }

                    override fun onAdClose(p0: String?) {
                        callHandler("bannerCallback","onAdClose",null)
                        binding.adLayout.removeAllViews()
                        binding.adLayout.invalidate()
                        callback?.let {
                            callback.callback("onAdClose")
                        }
                    }
                }
                BannerManager.getInstance(webView.context).requestAd(webView.context,"$id",bannerListener,binding!!.adLayout,3)

            }
        })

        registerHandler("showAdVideo",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var id  =""+ ( data as JSONObject).get("id")
                var listener = object : VideoListener {
                    override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
                        callback?.let {
                            callback.callback("onRewardVerify")
                        }
                    }

                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            callHandler("videoCallback","onVideoComplete",null)
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdShow() {
                    }

                    override fun onAdVideoBarClick() {

                        callback?.let {
                            callback.callback("onClick")
                        }
                    }

                    override fun onVideoComplete() {
                        callback?.let {
                            callHandler("videoCallback","onVideoComplete",null)
//                            callback.callback("onVideoComplete")
                        }
                    }

                    override fun onAdClose() {
                        callback?.let {
                            callHandler("videoCallback","onAdClose",null)
//                            callback.callback("onAdClose")
                        }
                    }

                    override fun onRewardVideoCached() {
                        callback?.let {
                            callHandler("videoCallback","onRewardVideoCached",null)
//                            callback.callback("onRewardVideoCached")
                        }
                                }
                }
                VideoManager.getInstance(webView.context).request(webView.context,"$id","RewardName","123456",1,1,listener)
            }
        })
        registerHandler("showAdVideoWithParam",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {

                var jsonObject = data as org.json.JSONObject
                var videoEntity = gson.fromJson<VideoEntity>(jsonObject.toString(),VideoEntity::class.java)

                var listener = object : VideoListener {
                    override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
                        callback?.let {
                            callback.callback("onRewardVerify")
                        }
                    }

                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            callHandler("videoCallback","onAdFailed",null)
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdShow() {
                    }

                    override fun onAdVideoBarClick() {

                        callback?.let {
                            callback.callback("onClick")
                        }
                    }

                    override fun onVideoComplete() {
                        callback?.let {
                            callHandler("videoCallback","onVideoComplete",null)
//                            callback.callback("onVideoComplete")
                        }
                    }

                    override fun onAdClose() {
                        callback?.let {
                            callHandler("videoCallback","onAdClose",null)
//                            callback.callback("onAdClose")
                        }
                    }

                    override fun onRewardVideoCached() {
                        callback?.let {
                            callHandler("videoCallback","onRewardVideoCached",null)
//                            callback.callback("onRewardVideoCached")
                        }
                    }
                }
                VideoManager.getInstance(webView.context).request(webView.context,videoEntity.spaceId,videoEntity.RewardName,videoEntity.UserId,videoEntity.type.toInt(),videoEntity.amount.toInt(),listener)
            }
        })
        registerHandler("showGDTBanner",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var id  =""+ ( data as JSONObject).get("id")
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                binding!!.adLayout.removeAllViews()
                binding!!.adLayout.invalidate()
                var bannerview : BannerView?=null
                if(bannerview != null){
                    bannerview.destroy()
                }
                bannerview = BannerView(webView.context as Activity , ADSize.BANNER ,"1107985626","$id")
                bannerview.setRefresh(30)
                bannerview.setADListener(object : AbstractBannerADListener(){
                    override fun onNoAD(p0: AdError?) {
                        callHandler("bannerCallback","onAdFailed",null)
                    }

                    override fun onADReceiv() {
                    }

                    override fun onADExposure() {
                        var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding.adLayout.removeAllViews()
                        }
                        binding.adLayout.addView(closeView)
                        binding.adLayout.invalidate()
                    }
                    override fun onADClicked() {
                        callHandler("bannerCallback","onAdClick",null)
                    }

                     override fun onADClosed() {
                         binding.adLayout.removeAllViews()
                         binding.adLayout.invalidate()
                        callHandler("bannerCallback","onADClosed",null)                    }
                })
                binding!!.adLayout.addView(bannerview)
                bannerview.loadAD()
            }
        })
        registerHandler("showAdVideoNow",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                if(VideoManager.getInstance(webView.context).isReady){
                    VideoManager.getInstance(webView.context).showAd()
                }
            }
        })
//        registerHandler("showAdVideoWithType",object : WVJBWebViewClient.WVJBHandler{
//            override fun request(data: Any?, callback: WVJBResponseCallback?) {
//                var jsonObject = data as org.json.JSONObject
//                var videoType = jsonObject.getString("videoType")
//                when(videoType){
//                    "LYVideo"->{
//                        if(VideoManager.getInstance(webView.context).isReady){
//                            VideoManager.getInstance(webView.context).showAd()
//                        }
//                    }
//                    "JRTTVideo" ->{
//                        mttRewardVideoAd!!.showRewardVideoAd(webView.context as Activity)
//                    }
//                }
//
//
//            }
//        })
//        registerHandler("showAdWithType",object : WVJBHandler{
//            override fun request(data: Any?, callback: WVJBResponseCallback?) {
//                var jsonObject = data as org.json.JSONObject
//                var adWithTypeEntity = gson.fromJson<AdWithTypeEntity>(jsonObject.toString(),AdWithTypeEntity::class.java)
//                when(adWithTypeEntity.supplierType){
//                    "LY" ->{
//                        showLYAd(adWithTypeEntity,callback)
//                    }
//                    "GDT" ->{
//                        showGDTAd(adWithTypeEntity,callback)
//                    }
//                   "JRTT" ->{
//                       showJRTTAd(adWithTypeEntity,callback)
//                   }
//                }
//            }
//        })

    }

    private fun showLYAd(adWithTypeEntity: AdWithTypeEntity, callback: WVJBResponseCallback?) {
        when(adWithTypeEntity.ADType){
            //"banner" "video" "splash" "insert"
            "banner" ->{
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                binding!!.adLayout.removeAllViewsInLayout()
                binding.adLayout.invalidate()
                var bannerListener = object : BannerListener{
                    override fun onAdFailed(p0: String?) {
                        callHandler("bannerCallback","onAdFailed",null)
                        callback?.let {
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdDisplay(p0: String?) {
                        var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding.adLayout.removeAllViews()
                        }
                        binding.adLayout.addView(closeView)
                        binding.adLayout.invalidate()
                    }

                    override fun onAdClick(p0: String?) {
                        Log.d("banner","onAdClick")
                        callHandler("bannerCallback","onAdClick",null)
                        callback?.let {
                            callback.callback("onAdClick")
                        }
                    }

                    override fun onAdReady(p0: String?) {
                        callHandler("bannerCallback","onAdReady",null)
                        callback?.let {
                            callback.callback("onAdReady")
                        }
                    }

                    override fun onAdClose(p0: String?) {
                        callHandler("bannerCallback","onAdClose",null)
                        binding.adLayout.removeAllViews()
                        binding.adLayout.invalidate()
                        callback?.let {
                            callback.callback("onAdClose")
                        }
                    }
                }
                BannerManager.getInstance(webView.context).requestAd(webView.context,"${adWithTypeEntity.spaceId}",bannerListener,binding!!.adLayout,3)
            }
            "video" ->{
                var listener = object : VideoListener {
                    override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
                        callback?.let {
                            callback.callback("onRewardVerify")
                        }
                    }

                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            callHandler("videoCallback","onVideoComplete",null)
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdShow() {
                    }

                    override fun onAdVideoBarClick() {

                        callback?.let {
                            callback.callback("onClick")
                        }
                    }

                    override fun onVideoComplete() {
                        callback?.let {
                            callHandler("videoCallback","onVideoComplete",null)
//                            callback.callback("onVideoComplete")
                        }
                    }

                    override fun onAdClose() {
                        callback?.let {
                            callHandler("videoCallback","onAdClose",null)
//                            callback.callback("onAdClose")
                        }
                    }

                    override fun onRewardVideoCached() {
                        callback?.let {
                            callHandler("videoCallback","onRewardVideoCached",null)
//                            callback.callback("onRewardVideoCached")
                        }
                    }
                }
                VideoManager.getInstance(webView.context).request(webView.context,"$adWithTypeEntity.spaceId",adWithTypeEntity.RewardName,adWithTypeEntity.UserId,adWithTypeEntity.type.toInt(),adWithTypeEntity.amount.toInt(),listener)
            }
            "splash"->{
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var spreadListener = object : SpreadListener{
                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdDisplay(p0: String?) {
                    }

                    override fun onAdReceived(p0: String?) {
                    }

                    override fun onAdClick() {
                        callback?.let {
                            callback.callback("onAdClick")
                        }
                    }

                    override fun onAdClose(p0: String?) {
                        callback?.let {
                            callback.callback("onAdClose")
                            binding!!.splashLayout.removeAllViews()
                            binding.splashLayout.invalidate()
                        }
                    }
                }

                SpreadManager.getInstance(webView.context as Activity).request(webView.context as Activity,"${adWithTypeEntity.spaceId}",binding!!.splashLayout,spreadListener)
            }
            "insert"->{
                var insertListener = object : InsertListener {
                    override fun onAdDismiss(p0: String?) {
                        callback?.let {
                            callback.callback("onAdClose")
                        }
                    }

                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            callback.callback("onAdFailed")
                        }
                    }

                    override fun onAdDisplay(p0: String?) {
                    }

                    override fun onAdReceived(p0: String?) {
                        if (InsertManager.getInstance(webView.context).isReady) {
                            InsertManager.getInstance(webView.context).showAd()
                        }
                    }

                    override fun onAdClick(p0: String?) {
                        callback?.let {
                            callback.callback("onAdClick")
                        }
                    }

                }
                var count = 3
//                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                InsertManager.getInstance(webView.context).requestAd(webView.context,"${adWithTypeEntity.spaceId}",insertListener,count)
            }
        }

    }
    private fun showGDTAd(adWithTypeEntity: AdWithTypeEntity, callback: WVJBResponseCallback?) {
        when(adWithTypeEntity.ADType){
            "banner"->{
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var bannerview : BannerView?=null
                if(bannerview != null){
                    binding!!.adLayout.removeView(bannerview)
                    bannerview.destroy()
                }
                bannerview = BannerView(webView.context as Activity , ADSize.BANNER ,"1107985626","${adWithTypeEntity.spaceId}")
                bannerview.setRefresh(30)
                bannerview.setADListener(object : AbstractBannerADListener(){
                    override fun onNoAD(p0: AdError?) {
                        callHandler("bannerCallback","onAdFailed",null)
                    }

                    override fun onADReceiv() {
                    }

                    override fun onADExposure() {
                        var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding!!.adLayout.removeAllViews()
                        }
                        binding!!.adLayout.addView(closeView)
                        binding.adLayout.invalidate()
                    }
                    override fun onADClicked() {
                        callHandler("bannerCallback","onAdClick",null)
                    }

                    override fun onADClosed() {
                        binding!!.adLayout.removeAllViews()
                        binding.adLayout.invalidate()
                        callHandler("bannerCallback","onADClosed",null)
                    }
                })
                binding!!.adLayout.addView(bannerview)
                bannerview.loadAD()
            }
            "splash"->{
               var  fetchSplashADTime = System.currentTimeMillis()
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var adListener =object : SplashADListener {
                    override fun onADExposure() {
                        Log.d("Splash","广点通闪屏onADExposure")            }

                    override fun onADDismissed() {
                        Log.d("Splash","广点通闪屏点击")

                    }

                    override fun onADPresent() {
                    }

                    override fun onNoAD(p0: AdError?) {
                        Log.d("Splash","广点通没有闪屏广告")
                    }

                    override fun onADClicked() {
                        Log.d("Splash","广点通闪屏点击")

                    }

                    override fun onADTick(p0: Long) {
                        Log.d("Splash","广点通闪屏计时")               }

                }
                Log.d("TAG","广点通暂不提供开屏")
//                var splashAD = SplashAD(webView.context as Activity, binding!!.splashLayout, skipView, appId, posId, adListener, 0)
            }
        }

    }
    private fun showJRTTAd(adWithTypeEntity: AdWithTypeEntity, callback: WVJBResponseCallback?) {
        adSlot = initSlot(adWithTypeEntity)
        var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
        when(adWithTypeEntity.ADType){
            //"banner" "video" "splash" "insert"
            "banner" ->{

                mTTAdNative.loadBannerAd(adSlot, object : TTAdNative.BannerAdListener{
                    override fun onBannerAdLoad(p0: TTBannerAd?) {
                        var bannerView = p0!!.bannerView
                        bannerView?.let {
                            binding!!.adLayout.removeAllViews()
                            binding!!.adLayout.addView(bannerView)
                        }
                        p0.setBannerInteractionListener(object : TTBannerAd.AdInteractionListener{
                            override fun onAdClicked(p0: View?, p1: Int) {
                                callHandler("bannerCallback","onAdClick",null)
                            }

                            override fun onAdShow(p0: View?, p1: Int) {
                            }
                        })
                    }

                    override fun onError(p0: Int, p1: String?) {
                        callHandler("bannerCallback","onAdFailed",null)
                    }
                })
            }
            "video" ->{
                mTTAdNative.loadRewardVideoAd(adSlot,object : TTAdNative.RewardVideoAdListener{
                    override fun onRewardVideoAdLoad(p0: TTRewardVideoAd?) {
                        mttRewardVideoAd =p0
                        p0!!.setRewardAdInteractionListener(object : TTRewardVideoAd.RewardAdInteractionListener{
                            override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
                                callHandler("videoCallback","onRewardVerify",null)
                            }

                            override fun onAdShow() {
                            }

                            override fun onAdVideoBarClick() {
                                callHandler("videoCallback","onAdClick",null)
                            }

                            override fun onVideoComplete() {
                                callHandler("videoCallback","onVideoComplete",null)
                            }

                            override fun onAdClose() {
                                callHandler("videoCallback","onAdClose",null)
                            }
                        })
                    }

                    override fun onRewardVideoCached() {
                        callHandler("videoCallback","onRewardVideoCached",null)
                    }

                    override fun onError(p0: Int, p1: String?) {
                        callHandler("videoCallback","onAdFailed",null)
                    }
                })

            }
            "splash" ->{
                mTTAdNative.loadSplashAd(adSlot,object : TTAdNative.SplashAdListener{
                    override fun onSplashAdLoad(p0: TTSplashAd?) {
                        var view = p0!!.splashView
                        Log.d("TAG","开屏暂时用广点通")
                        return
                        binding!!.splashLayout.removeAllViews()
                        binding!!.splashLayout.addView(view)
                        p0.setSplashInteractionListener(object : TTSplashAd.AdInteractionListener{
                            override fun onAdClicked(p0: View?, p1: Int) {

                            }

                            override fun onAdSkip() {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onAdShow(p0: View?, p1: Int) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onAdTimeOver() {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }
                        })
                    }

                    override fun onTimeout() {
                    }

                    override fun onError(p0: Int, p1: String?) {
                    }
                })

            }
            "insert" ->{
                mTTAdNative.loadInteractionAd(adSlot, object : TTAdNative.InteractionAdListener{
                    override fun onInteractionAdLoad(p0: TTInteractionAd?) {
                        p0!!.setAdInteractionListener(object : TTInteractionAd.AdInteractionListener{
                            override fun onAdDismiss() {
                                callback!!.callback("onAdClose")
                            }

                            override fun onAdClicked() {
                                callback!!.callback("onClick")
                            }

                            override fun onAdShow() {
                            }
                        })
                        p0.showInteractionAd(webView.context as Activity)
                    }

                    override fun onError(p0: Int, p1: String?) {
                        callback!!.callback("onAdFailed")
                    }
                })

            }

        }

    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        pageGetFinished = true
        var  binding = DataBindingUtil.findBinding<ActivityMainBinding>(view)
        if(binding!!.swipeLayout.isRefreshing) binding.swipeLayout.isRefreshing = false
//        showSplashAd()
        SplashScreen.hide(view.context as Activity?)
    }


    fun onActivityResult(intent : Intent){


    }






    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        when(requestCode){
            CAMERA_REQUEST_CODE ->{
                if (grantResults[0]==0 && grantResults[1] ==0 ){
                    takePhoto()
                }
            }
            READ_PHONE ->{
                if (grantResults[0]==0 && grantResults[1] ==0 && grantResults[2] ==0){
                    var initMessage = InitMessage()
                    var packageManager = webView.context.packageManager
                    var applicationInfo = packageManager.getApplicationInfo(webView.context.packageName,PackageManager.GET_META_DATA)
                    var tm = webView.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    initMessage.deviceUUID = tm.imei
                    var packageInfo = packageManager.getPackageInfo(webView.getContext(). getPackageName(),PackageManager.GET_META_DATA)
                    initMessage.traffic_channel= applicationInfo.metaData.getString("JPUSH_CHANNEL")
                    initMessage.versionString = packageInfo.versionName
                    UUIDCallback!!.callback(gson.toJson(initMessage))
                }else{
                    var initMessage = InitMessage()
                    var packageManager = webView.context.packageManager
                    var applicationInfo = packageManager.getApplicationInfo(webView.context.packageName,PackageManager.GET_META_DATA)
                    var tm = webView.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    initMessage.deviceUUID = UUID.randomUUID().toString()
                    SharePrefenceHelper.save("deviceID",initMessage.deviceUUID)
                    var packageInfo = packageManager.getPackageInfo(webView.getContext(). getPackageName(),PackageManager.GET_META_DATA)
                    initMessage.traffic_channel= applicationInfo.metaData.getString("JPUSH_CHANNEL")
                    initMessage.versionString = packageInfo.versionName
                    UUIDCallback!!.callback(gson.toJson(initMessage))
                }
            }

        }

    }

    private fun takePhoto() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        (webView.context as Activity).startActivityForResult(intent, ACTIVITYFOROMCLIENT)
    }

    /**
     * 回传微信code给前端H5
     * @param code
     */
    fun resopnseCode(code: String) {
        if (null != codeCallback) {
            var wxCode = WXCode()
            wxCode.code=code
            codeCallback!!.callback(gson.toJson(wxCode))
        }
    }


    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        networkError(webView)
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (request.url.toString() == webView.url)
                networkError(view)
        } else {
            networkError(view)
        }
    }

    private fun networkError(view: WebView) {
        view.visibility = View.INVISIBLE
        pageGetFinished = true
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(view)
        binding!!.webView.visibility = View.GONE
        binding.llNetworkError.visibility = View.VISIBLE
        if (SplashScreen.b) {
            SplashScreen.b = false
        }

    }

    fun callBackStep(step : Float){
        var jsonEvent = JsonEvent()
        jsonEvent.step = step.toInt()
        stepCallback!!.callback(gson.toJson(jsonEvent))
    }

    private fun initSlot(adWithTypeEntity: AdWithTypeEntity) : AdSlot{
        adSlot = AdSlot.Builder().
                setCodeId("${adWithTypeEntity.spaceId}").
                setImageAcceptedSize(640,320).
                setSupportDeepLink(true)
                .setAdCount(2)
                //激励视频奖励的名称，针对激励视频参数
                .setRewardName(adWithTypeEntity.RewardName)
                //激励视频奖励个数
                .setRewardAmount(adWithTypeEntity.amount.toInt())
                //用户ID,使用激励视频必传参数
                //表来标识应用侧唯一用户；若非服务器回调模式或不需sdk透传，可设置为空字符串
                .setUserID("${adWithTypeEntity.UserId}")
                //设置期望视频播放的方向，为TTAdConstant.HORIZONTAL或TTAdConstant.VERTICAL
                .setOrientation(TTAdConstant.VERTICAL)
                //激励视频奖励透传参数，字符串，如果用json对象，必须使用序列化为String类型,可为空
                .setMediaExtra("media_extra")
                .build()
        return adSlot
    }
    /**
     * 唯一标识一个请求
     */
    private  fun buildTransaction(type : String) : String{
        return type + System.currentTimeMillis()
    }



}