package com.qubuxing.qbx.parts

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.Toast
import cn.jpush.android.api.JPushInterface
import com.baidu.mobads.AdView
import com.baidu.mobads.AdViewListener
import com.bumptech.glide.Glide
import com.bytedance.sdk.openadsdk.*

import com.google.gson.Gson
import com.iflytek.voiceads.*
import com.kf5.sdk.system.utils.SPUtils
import com.ly.adpoymer.interfaces.*
import com.ly.adpoymer.manager.*
import com.miui.zeus.mimo.sdk.MimoSdk
import com.miui.zeus.mimo.sdk.ad.AdWorkerFactory
import com.miui.zeus.mimo.sdk.ad.IAdWorker
import com.miui.zeus.mimo.sdk.listener.MimoAdListener
import com.qq.e.ads.banner.ADSize
import com.qq.e.ads.banner.AbstractBannerADListener
import com.qq.e.ads.banner.BannerView
//import com.qq.e.ads.rewardvideo.RewardVideoAD
//import com.qq.e.ads.rewardvideo.RewardVideoADListener
import com.qq.e.ads.splash.SplashADListener
import com.qq.e.comm.util.AdError
import com.qubuxing.ThridBroeserActivity
import com.qubuxing.qbx.*
import com.qubuxing.qbx.R


import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.http.RetrofitUtil

import com.qubuxing.qbx.http.beans.*
import com.qubuxing.qbx.service.StepCounterService
import com.qubuxing.qbx.service.StepDetectorService
import com.qubuxing.qbx.utils.*
import com.qubuxing.qbx.utils.KFUtils.Preference
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.*
import com.xiaomi.ad.common.pojo.AdType
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class WVWebViewClient constructor(webView: WebView,messageHandler: WVJBHandler? = null) : WVJBWebViewClient(webView,messageHandler) {
    var thread : Thread ?= null
    var gson:Gson = Gson()
    var imageGetMobile :String=""
    var imageType = ""
    var firstClickTime: Long = 0L
    var codeCallback: WVJBResponseCallback? = null
    var CAMERA_REQUEST_CODE=1110
    var READ_PHONE=10086
     var lockStep : Boolean = false
    var pageGetFinished = false
    var UUIDCallback : WVJBResponseCallback? = null
    var cameraList = ArrayList<String>()
    var ACTIVITYFOROMCLIENT = 10010
     var stepCallback: WVJBResponseCallback? = null
    var httpHelper : HttpService = RetrofitUtil.instance.help
    var backStep = false
    lateinit var videoAd : IFLYVideoAd
    var haveStepToday : Float = 0f
    lateinit var adView : ViewGroup
    var mTTAdNative : TTAdNative
    lateinit var adSlot : AdSlot
    var bannerCallback = VideoBack()
     var result = VideoBack()
    var adHelper : AdHelper
    var device : DeviceModule?
    var deviceInfo : DeviceInfo?
    lateinit var videoADDataRef :VideoADDataRef
//     var rewardVideoAd : RewardVideoAD ?= null
    var mttRewardVideoAd :TTRewardVideoAd? = null
    var timer : CountDownTimer? = null
    constructor(webView: WebView) : this(webView ,object :WVJBHandler{
        override fun request(data: Any?, callback: WVJBResponseCallback?) {
            callback!!.callback("Response for message from ObjC!")
        }
    })
    init {
//微信登录调起

        if(ActivityCompat.checkSelfPermission(webView.context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            device = DeviceModule(webView.context)
            deviceInfo = gson.fromJson<DeviceInfo>(device!!.deviceInfo,DeviceInfo::class.java)
        }else{
            device = null
            deviceInfo = null
        }
        timer = object : CountDownTimer(500, 100) {
            override fun onFinish() {
                var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                closeView.setOnClickListener {
                    binding!!.adLayout.removeAllViews()
                }
                binding!!.adLayout.addView(closeView)
                binding.adLayout.invalidate()
            }

            override fun onTick(millisUntilFinished: Long) {

            }
        }
        adHelper = AdHelper(webView)
        mTTAdNative = QBXApplication.ttAdManager.createAdNative(webView.context)
        registerHandler("submitWechatLogin", object : WVJBHandler {
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo"
                req.state = "diandi_wx_login"
                QBXApplication.instance.getWXAPI().sendReq(req)
                codeCallback = callback            }
        })
        registerHandler("jumpSetting",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                JumpSetting.jumpStartInterface(webView.context)
            }
        })
//        registerHandler("KFInitializeWithEmail", object : WVJBHandler{
//            override fun request(data: Any?, callback: WVJBResponseCallback?) {
//                var  binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
//                var kfEntity = gson.fromJson<KFentity>(data.toString(),KFentity::class.java)
//                binding!!.viewModel!!.checkKFLoginStatus(webView.context as MainActivity , kfEntity.name,kfEntity.email, kfEntity.phone, false)
//            }
//        })
        registerHandler("KFEnterChatRoom", object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var  binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var kfEntity = gson.fromJson<KFentity>(data.toString(),KFentity::class.java)
                binding!!.viewModel!!.checkKFLoginStatus(webView.context as MainActivity , kfEntity.name,kfEntity.email, kfEntity.phone, true)
            }
        })
        registerHandler("clearSp",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                SPUtils.clearSP()
            }
        })
        registerHandler("getAppStep",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                if(lockStep) return
                lockStep = true
                var intent = Intent()
                var json = data as org.json.JSONObject
                haveStepToday = json.getInt("step").toFloat()
                Log.i("TAG","haveStepToday:${haveStepToday}")
//                SharePrefenceHelper.saveFloat("LastUpdateStep",haveStepToday)
                if (!SharePrefenceHelper.getBoolean("FirstOpen")){
                    if(haveStepToday > 0){
                        var jsonEvent = JsonEvent()
                        jsonEvent.step = haveStepToday.toFloat()
                        callback!!.callback(gson.toJson(jsonEvent))
                    }else{
//                       callHandler("updateWXStep","",null)
                    }
                    SharePrefenceHelper.saveBoolean("FirstOpen",true)//打开过就记true
                }else{

                }
                intent.setClass(webView.context, StepCounterService::class.java)
                webView.context.startService(intent)
                intent.setClass(webView.context , StepDetectorService::class.java)
                webView.context.startService(intent)
                backStep = false
                stepCallback =callback
            }
        })
        registerHandler("LaunchMiniProgramCard",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var miniProgrameObj = WXMiniProgramObject()
                var entity = gson.fromJson<WXSeneEntity>(data.toString(),WXSeneEntity::class.java)
                miniProgrameObj.webpageUrl = entity.webpageUrl
                miniProgrameObj.userName = entity.userName
                miniProgrameObj.path = entity.path
                var msg = WXMediaMessage(miniProgrameObj)
                msg.title = entity.title
                msg.description = buildTransaction("webpage")
//                msg.thumbData =
                var req = SendMessageToWX.Req()
                var bitmap: Bitmap? = null
                thread = Thread(Runnable{
//                    bitmap = Glide.with(webView.context).asBitmap().load(entity.imageurl).submit(500,500).get()
                    bitmap = Glide.with(webView.context).load(entity.imageurl).asBitmap().into(600,480).get()
                    bitmap = BitmapUtils.drawableBitmapOnWhiteBg(webView.context,bitmap!!)
//                    msg.setThumbImage(bitmap)
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
        registerHandler("LaunchMiniProgram",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var req = WXLaunchMiniProgram.Req()
                var entity = gson.fromJson<WXSeneEntity>(data.toString(),WXSeneEntity::class.java)
                req.userName = entity.userName
                req.path = entity.path
                when(entity.WXMiniProgramType){
                    "release"->{
                        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
                    }
                    "test"->{
                        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_TEST
                    }
                    "preview" ->{
                        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_PREVIEW
                    }
                    else ->{
                    req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
                }
                }
                QBXApplication.api.sendReq(req)
            }
        })
        registerHandler("getVersion", object : WVJBHandler {
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                try {
                    val packageInfo = webView.context.packageManager.getPackageInfo(webView.context.packageName, 0)

                    val jsonObject = JSONObject()
                    jsonObject.put("versionName", packageInfo.versionName)
                    if (Build.VERSION.SDK_INT<= 27) jsonObject.put("versionCode", packageInfo.versionCode)
                    if (Build.VERSION.SDK_INT>= 28) jsonObject.put("versionCode", packageInfo.longVersionCode)
                    jsonObject.put("buildCode", QBXApplication.buildCode)
                    device?.let {
                        jsonObject.put("deviceInfo", device!!.deviceInfo)
                    }
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
        registerHandler("clearLocalStep",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                Log.d("TAG","clearLocalStep")
                //清除各种缓存操作
                StepHelper.clearStepHistory()
                SPUtils.clearSP()
                Preference.saveBoolLogin(webView.context , false)
            }
        })
        registerHandler("checkUpdata",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                binding!!.viewModel!!.getUpdateMessage(webView.context)
            }
        })
        registerHandler("getInitMessage",object : WVJBHandler{
            @RequiresApi(Build.VERSION_CODES.M)
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                if (ActivityCompat.checkSelfPermission(
                                webView.context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(webView.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(webView.context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(webView.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(webView.context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                ) {
                    var initMessage = InitMessage()

                    var packageManager = webView.context.packageManager
                    var applicationInfo = packageManager.getApplicationInfo(webView.context.packageName,PackageManager.GET_META_DATA)
                    var tm = webView.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    initMessage.deviceUUID = tm.imei
                    var packageInfo = packageManager.getPackageInfo(webView.getContext(). getPackageName(),PackageManager.GET_META_DATA)
                    initMessage.traffic_channel= applicationInfo.metaData.getString("JPUSH_CHANNEL")
                    initMessage.versionString = packageInfo.versionName
                    initMessage.longitude = deviceInfo!!.geoLon
                    initMessage.latitude = deviceInfo!!.geoLat
                    callback!!.callback(gson.toJson(initMessage))
                }else{
                    UUIDCallback = callback
                    cameraList.clear()
                    cameraList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    cameraList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    cameraList.add(Manifest.permission.READ_PHONE_STATE)
                    cameraList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    cameraList.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    (webView.context as Activity).requestPermissions(cameraList.toTypedArray(),READ_PHONE)
                }


            }
        })
        registerHandler("getAppStep222",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var intent = Intent()
                var json = data as org.json.JSONObject
                haveStepToday = json.getInt("step").toFloat()
                intent.setClass(webView.context, StepCounterService::class.java)
                webView.context.startService(intent)
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
//                    bitmap = Glide.with(webView.context).asBitmap().load(entity.imageurl).submit(500,500).get()
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
//                    bitmap = Glide.with(webView.context).asBitmap().load(entity.imageurl).submit(500,500).get()
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
                    override fun onAdReceived(p0: MutableList<Any?>?) {

                    }

                    override fun onAdFailed(p0: String?) {
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
                    override fun onAdClick() {

                    }

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
                    override fun onAdClick() {

                    }

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
        registerHandler("getLocation",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
               //获取当前位置信息

            }
        })
        registerHandler("showAdVideoNow",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                if(VideoManager.getInstance(webView.context).isReady){
                    VideoManager.getInstance(webView.context).showAd()
                }
            }
        })
        registerHandler("showAdVideoWithType",object : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                var jsonObject = data as org.json.JSONObject
                var videoType = jsonObject.getString("videoType")
                when(videoType){
                    "LYVideo"->{
                        if(VideoManager.getInstance(webView.context).isReady){
                            VideoManager.getInstance(webView.context).showAd()
                        }
                    }
                    "JRTTVideo" ->{
                        mttRewardVideoAd!!.showRewardVideoAd(webView.context as Activity)
                    }
                    "KDXFVideo" ->{
                        var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                        binding!!.splashLayout.addView(adView)
                        videoAd.showAd(0, 0)
                    }
                    "OPPOVideo"->{
                        adHelper.showVideoWithType("OPPOVideo")
                    }
//                    "GDTVideo"->{
//                        rewardVideoAd?.let {
//                            rewardVideoAd!!.showAD()
//                        }
//                    }
                    else ->{
                    }
                }


            }
        })
        registerHandler("removeBanner  ",object  : WVJBWebViewClient.WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
//                Toast.makeText(webView.context, "removeBanner", Toast.LENGTH_LONG).show()
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                binding!!.adLayout.removeAllViews()
                binding.adLayout.invalidate()
            }
        })
        registerHandler("showAdWithType",object : WVJBHandler{
            override fun request(data: Any?, callback: WVJBResponseCallback?) {
                if(null ==device && null == deviceInfo){
                    if(ActivityCompat.checkSelfPermission(webView.context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                        device = DeviceModule(webView.context)
                        deviceInfo = gson.fromJson<DeviceInfo>(device!!.deviceInfo,DeviceInfo::class.java)
                    }else{
                        return
                    }
                }
                var jsonObject = data as org.json.JSONObject
                var adWithTypeEntity = gson.fromJson<AdWithTypeEntity>(jsonObject.toString(),AdWithTypeEntity::class.java)
                var binding =DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var layoutParams = binding!!.adLayout.layoutParams as ConstraintLayout.LayoutParams
                if (adWithTypeEntity.local.equals("bottom")){
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                    layoutParams.bottomMargin = 0
                    binding.adLayout.layoutParams = layoutParams

                }else{
                    layoutParams.bottomMargin = ScreenUtils.dip2px(webView.context,50f)
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                    binding.adLayout.layoutParams = layoutParams
                }
                binding.adLayout.invalidate()
                if(adHelper.checkIsSupply(adWithTypeEntity , deviceInfo!!)){
                    return
                }
                when(adWithTypeEntity.supplierType){
                    "LY" ->{
                        showLYAd(adWithTypeEntity,callback)
                    }
                    "GDT" ->{
                        showGDTAd(adWithTypeEntity,callback)
                    }
                   "JRTT" ->{
                       showJRTTAd(adWithTypeEntity,callback)
                   }
                    "KDXF"->{
                        showKDXF(adWithTypeEntity,callback)
                    }
                    "BAIDU" -> {
                        showBDAD(adWithTypeEntity,callback)
                    }
                    "XIAOMI"->{
                        showXMAD(adWithTypeEntity,callback)
                    }
                    "OPPO" ->{
                        adHelper.showADOPPO(adWithTypeEntity , callback)
                    }

                    else ->{

                    }

                }
            }
        })

    }
    fun showXMAD(adWithTypeEntity: AdWithTypeEntity, callback: WVJBResponseCallback?){
        var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
        when(adWithTypeEntity.ADType){
            "banner"->{
                var mBannerAd : IAdWorker
                if(MimoSdk.isSdkReady()){
                    Log.d("TAG","MiBanneready: ${MimoSdk.isSdkReady()}")
                }
                var listener = object : MimoAdListener {
                    override fun onAdFailed(p0: String?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p0!!
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdDismissed() {
                        callHandler("bannerCallback","onAdClose",null)
                    }

                    override fun onAdPresent() {
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdClick() {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)                    }

                    override fun onStimulateSuccess() {
                        Log.d("TAG","MiBanner: onStimulateSuccess")
                    }

                    override fun onAdLoaded(p0: Int) {
                        Log.d("TAG","MiBanner: ${p0}")

                    }
                }
                binding!!.adLayout.removeAllViews()
                binding.adLayout.invalidate()
                mBannerAd = AdWorkerFactory.getAdWorker(webView.context, binding.xiaomiAdlayout ,listener, AdType.AD_BANNER)
                mBannerAd.loadAndShow(adWithTypeEntity.spaceId)
            }
            else ->{

            }
        }
    }


    fun showBDAD(adWithTypeEntity: AdWithTypeEntity, callback: WVJBResponseCallback?){
        when(adWithTypeEntity.ADType){
            "banner"->{
                var bdAdListener = object : AdViewListener{
                    override fun onAdFailed(p0: String?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p0!!
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdShow(p0: JSONObject?) {
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdClick(p0: JSONObject?) {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)                    }

                    override fun onAdReady(p0: AdView?) {
                    }

                    override fun onAdSwitch() {
                    }

                    override fun onAdClose(p0: JSONObject?) {
                        callHandler("bannerCallback","onAdClose",null)
                    }
                }
                var bdAdView = AdView(webView.context,adWithTypeEntity.spaceId)
                bdAdView.setListener(bdAdListener)
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                binding!!.adLayout.removeAllViews()
                binding!!.adLayout.invalidate()
                binding!!.adLayout.addView(bdAdView)
                var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                closeView.setOnClickListener {
                    binding.adLayout.removeAllViews()
                }
                binding.adLayout.addView(closeView)
            }
            "video"->{

            }
            "insert"->{

            }
        }
    }
    fun showKDXF(adWithTypeEntity: AdWithTypeEntity, callback: WVJBResponseCallback?){
        when(adWithTypeEntity.ADType){
            "banner" ->{
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                var bannerView = IFLYBannerAd.createBannerAd(webView.context , adWithTypeEntity.spaceId)
                if (null == bannerView) return
                bannerView.setAdSize(IFLYAdSize.BANNER)
                binding!!.adLayout.removeAllViews()
                binding.adLayout.invalidate()
                binding.adLayout.addView(bannerView)
                var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                closeView.setOnClickListener {
                    binding.adLayout.removeAllViews()
                }
                binding.adLayout.addView(closeView)
                var mAdListener = object : IFLYAdListener{
                    override fun onAdFailed(p0: com.iflytek.voiceads.AdError?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p0!!.errorDescription
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdExposure() {
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)                    }

                    override fun onCancel() {
                    }

                    override fun onConfirm() {
                    }

                    override fun onAdClick() {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdClose() {
                        callHandler("bannerCallback","onAdClose",null)
                    }

                    override fun onAdReceive() {

                        bannerView.showAd()
                    }
                }
                bannerView.loadAd(mAdListener)

            }
             "video" ->{
                 var mVideoAdListener =object :IFLYVideoAdListener{
                     override fun onAdFailed(p0: com.iflytek.voiceads.AdError?) {
                         result.result = "onAdFailed"
                         result.supplierType = adWithTypeEntity.supplierType
                         callHandler("videoCallback",gson.toJson(result),null)
                     }

                     override fun onAdPlayComplete() {
                         var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                         result.result = "onAdClose"
                         result.supplierType = adWithTypeEntity.supplierType
                         callHandler("videoCallback",gson.toJson(result),null)
                         binding!!.splashLayout.removeAllViews()
                         binding.splashLayout.invalidate()
                     }

                     override fun onAdSkip() {
                     }

                     override fun onCancel() {
                     }

                     override fun onAdPlayError() {
                         result.result = "onAdFailed"
                         result.supplierType = adWithTypeEntity.supplierType
                         result.reason= "播放错误"
                         callHandler("videoCallback",gson.toJson(result),null)
                     }

                     override fun onConfirm() {
                     }

                     override fun onAdClick() {
                     }

                     override fun onAdLoaded(p0: MutableList<VideoADDataRef>?) {
                         if (p0!!.size > 0 && videoAd != null) {
                             videoADDataRef = p0!!.get(0)
                              adView = videoAd.adView
                             adView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                             result.result = "onRewardVideoCached"
                             result.supplierType = adWithTypeEntity.supplierType
                             callHandler("videoCallback",gson.toJson(result),null)

                         }
                     }

                     override fun onAdStartPlay() {
                     }
                 }
                 videoAd = IFLYVideoAd(webView.context ,adWithTypeEntity.spaceId ,mVideoAdListener ,IFLYVideoAd.REWARDED_VIDEO_AD)
                 videoAd.loadAd(1)
             }
        }
    }

    private fun showLYAd(adWithTypeEntity: AdWithTypeEntity, callback: WVJBResponseCallback?) {
        when(adWithTypeEntity.ADType){
            //"banner" "video" "splash" "insert"
            "banner" ->{
                var binding: ActivityMainBinding? = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
                        ?: return
                binding!!.adLayout.removeAllViewsInLayout()
                binding.adLayout.invalidate()
                var bannerListener = object : BannerListener{
                    override fun onAdFailed(p0: String?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        p0?.let {
                            bannerCallback.reason = p0
                        }
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdDisplay(p0: String?) {
                        var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding.adLayout.removeAllViews()
                        }
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                        binding.adLayout.addView(closeView)
                        binding.adLayout.invalidate()
                    }

                    override fun onAdClick(p0: String?) {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdReady(p0: String?) {
                        callHandler("bannerCallback","onAdReady",null)
                        timer!!.start()
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
                    override fun onAdClick() {

                    }

                    override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
                        callback?.let {
                            callback.callback("onRewardVerify")
                        }
                    }

                    override fun onAdFailed(p0: String?) {
                        callback?.let {
                            result.result = "onAdFailed"
                            result.supplierType = adWithTypeEntity.supplierType
                            p0?.let {
                                result.reason = p0
                            }

                            callHandler("videoCallback",gson.toJson(result),null)
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
                            result.result = "onAdClose"
                            result.supplierType = adWithTypeEntity.supplierType
                            callHandler("videoCallback",gson.toJson(result),null)
                        }
                    }

                    override fun onRewardVideoCached() {
                        callback?.let {
                            callHandler("videoCallback","onRewardVideoCached",null)
                            result.result = "onRewardVideoCached"
                            result.supplierType = adWithTypeEntity.supplierType
                            callHandler("videoCallback",gson.toJson(result),null)
//                            callback.callback("onRewardVideoCached")
                        }
                    }
                }
                VideoManager.getInstance(webView.context).request(webView.context,"${adWithTypeEntity.spaceId}",adWithTypeEntity.RewardName,adWithTypeEntity.UserId,adWithTypeEntity.type.toInt(),adWithTypeEntity.amount.toInt(),listener)
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
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p0!!.errorMsg
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onADReceiv() {
                    }

                    override fun onADExposure() {
                        var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding!!.adLayout.removeAllViews()
                        }
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                        binding!!.adLayout.addView(closeView)
                        binding.adLayout.invalidate()
                    }
                    override fun onADClicked() {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
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
//            "video"->{
//                var gdtListener = object : RewardVideoADListener{
//                    override fun onADExpose() {
//
//                    }
//
//                    override fun onADClick() {
//                    }
//
//                    override fun onVideoCached() {
//                        result.result = "onRewardVideoCached"
//                        result.supplierType = adWithTypeEntity.supplierType
//                        callHandler("videoCallback",gson.toJson(result),null)
//                    }
//
//                    override fun onReward() {
//                    }
//
//                    override fun onADClose() {
//                        result.result = "onAdClose"
//                        result.supplierType = adWithTypeEntity.supplierType
//                        callHandler("videoCallback",gson.toJson(result),null)
//                    }
//
//                    override fun onADLoad() {
//                        result.result = "onRewardVideoCached"
//                        result.supplierType = adWithTypeEntity.supplierType
//                        callHandler("videoCallback",gson.toJson(result),null)
//                    }
//
//                    override fun onVideoComplete() {
//                    }
//
//                    override fun onError(p0: AdError?) {
//                        result.result = "onAdFailed"
//                        result.supplierType = adWithTypeEntity.supplierType
//                        callHandler("videoCallback",gson.toJson(result),null)
//                    }
//
//                    override fun onADShow() {
//                    }
//                }
//                 rewardVideoAd = RewardVideoAD(webView.context , config.GDTAPP_ID, adWithTypeEntity.spaceId , gdtListener)
//            }
            else ->{

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
                        var layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                        bannerView.layoutParams= layoutParams
                        bannerView?.let {
                            binding!!.adLayout.removeAllViews()
                            binding!!.adLayout.addView(bannerView)
                            var closeView = LayoutInflater.from(webView.context).inflate(R.layout.banner_close_view,null)
                            closeView.setOnClickListener {
                                binding.adLayout.removeAllViews()
                            }
                            binding.adLayout.addView(closeView)
                            binding.adLayout.invalidate()
                        }
                        p0.setBannerInteractionListener(object : TTBannerAd.AdInteractionListener{
                            override fun onAdClicked(p0: View?, p1: Int) {
                                bannerCallback.result = "onAdClick"
                                bannerCallback.supplierType = adWithTypeEntity.supplierType
                                callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                            }

                            override fun onAdShow(p0: View?, p1: Int) {
                                bannerCallback.result = "onAdShow"
                                bannerCallback.supplierType = adWithTypeEntity.supplierType
                                callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                            }
                        })
                    }

                    override fun onError(p0: Int, p1: String?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p1!!
                        callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }
                })
            }
            "video" ->{
                mTTAdNative.loadRewardVideoAd(adSlot,object : TTAdNative.RewardVideoAdListener{
                    override fun onRewardVideoAdLoad(p0: TTRewardVideoAd?) {
                        mttRewardVideoAd =p0
                        p0!!.setRewardAdInteractionListener(object : TTRewardVideoAd.RewardAdInteractionListener{
                            override fun onVideoError() {
                                result.result = "onAdFailed"
                                result.reason = "今日头条，未知错误"
                                result.supplierType = adWithTypeEntity.supplierType
                                callHandler("videoCallback",gson.toJson(result),null)
                            }

                            override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
                                result.result = "onRewardVerify"
                                result.supplierType = adWithTypeEntity.supplierType
                                callHandler("videoCallback",gson.toJson(result),null)
                            }

                            override fun onAdShow() {
                            }

                            override fun onAdVideoBarClick() {
                                result.result = "onAdVideoBarClick"
                                result.supplierType = adWithTypeEntity.supplierType
                                callHandler("videoCallback",gson.toJson(result),null)
//                                callHandler("videoCallback","onAdClick",null)
                            }

                            override fun onVideoComplete() {
                                result.result = "onVideoComplete"
                                result.supplierType = adWithTypeEntity.supplierType
                                callHandler("videoCallback",gson.toJson(result),null)
//                                callHandler("videoCallback","onVideoComplete",null)
                            }

                            override fun onAdClose() {
                                result.result = "onAdClose"
                                result.supplierType = adWithTypeEntity.supplierType
                                callHandler("videoCallback",gson.toJson(result),null)
//                                callHandler("videoCallback","onAdClose",null)
                            }
                        })
                    }

                    override fun onRewardVideoCached() {
                        result.result = "onRewardVideoCached"
                        result.supplierType = adWithTypeEntity.supplierType
                        callHandler("videoCallback",gson.toJson(result),null)
//                        callHandler("videoCallback","onRewardVideoCached",null)
                    }

                    override fun onError(p0: Int, p1: String?) {
                        result.result = "onAdFailed"
                        result.reason = "code : ${p0} 描述：${p1}"
                        result.supplierType = adWithTypeEntity.supplierType
                        callHandler("videoCallback",gson.toJson(result),null)
//                        callHandler("videoCallback","onAdFailed",null)
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
                    device =DeviceModule(webView.context)
                    deviceInfo = gson.fromJson<DeviceInfo>(device!!.deviceInfo,DeviceInfo::class.java)
                    initMessage.longitude = deviceInfo!!.geoLon
                    initMessage.latitude = deviceInfo!!.geoLat
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
        var finalStep = step

        Log.i("TAG","最终本地保存：LastUpdateStep：${step}置换serviceHasDEAD状态为：${SharePrefenceHelper.getBoolean("ServiceHasDead")}")
        ReBootHelper.saveBootOpenTime()
        if (haveStepToday > step){
            finalStep = haveStepToday
        }
//        Toast.makeText(webView.context , "最终上传给前端：${finalStep} 最终本地保存：LastUpdateStep：${step} 上次本地保存步数：${SharePrefenceHelper.getFloat("LastUpdateStep")}," , Toast.LENGTH_LONG).show()
        Log.i("TAG","最终上传给前端：${finalStep}")
        jsonEvent.step = finalStep
        SharePrefenceHelper.saveFloat("LastUpdateStep",jsonEvent.step)
        SharePrefenceHelper.saveBoolean("ServiceHasDead",false)
        lockStep = false
        stepCallback?.let {
            stepCallback!!.callback(gson.toJson(jsonEvent))
        }

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

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (url.startsWith("weixin")) {
            try {
                // 以下固定写法
                val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(url))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                webView.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // 防止没有安装的情况
                e.printStackTrace()
                Toast.makeText(webView.context, "请先安装微信！", Toast.LENGTH_LONG).show()
            }

            return true

        }
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (view!!.url.startsWith("weixin")) {
            try {
                // 以下固定写法
                val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(view.url))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                webView.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // 防止没有安装的情况
                e.printStackTrace()
                Toast.makeText(webView.context, "请先安装微信！", Toast.LENGTH_LONG).show()
            }

            return true

        }
        //抖音支持
        if (view!!.url.startsWith("snssdk143")) {
            try {
                // 以下固定写法
                val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(view.url))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                webView.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // 防止没有安装的情况
                e.printStackTrace()
                Toast.makeText(webView.context, "请先安装抖音！", Toast.LENGTH_LONG).show()
            }

            return true

        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    fun oldCallBackStep(step : Float){
        var jsonEvent = JsonEvent()
        jsonEvent.step = step
        stepCallback!!.callback(gson.toJson(jsonEvent))
    }


}