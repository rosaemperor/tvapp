package com.qubuxing.qbx

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.baidu.mobads.AdSettings
import com.bytedance.sdk.openadsdk.TTAdManager
import com.bytedance.sdk.openadsdk.TTAdManagerFactory
import com.kf5.sdk.system.init.KF5SDKInitializer
import com.kf5.sdk.system.utils.ImageLoaderManager
import com.kf5.sdk.system.utils.SPUtils
import com.ly.adpoymer.config.AdConfig
import com.ly.adpoymer.manager.*
import com.miui.zeus.mimo.sdk.MimoSdk
import com.oppo.mobad.api.InitParams
import com.oppo.mobad.api.MobAdManager
import com.qubuxing.qbx.utils.SharePrefenceHelper
import com.qzs.sdk.ADSDK
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.stat.StatConfig
import com.tencent.stat.StatCrashCallback
import com.tencent.stat.StatCrashReporter
import com.tencent.stat.StatService
import com.tencent.stat.hybrid.StatHybridHandler

class QBXApplication : Application(){
    var appCount = 0
    companion object {
        lateinit var  instance : QBXApplication
        var buildCode = "20181015"
        lateinit var api : IWXAPI
        lateinit  var ttAdManager : TTAdManager
    }

    val tag : String = "Application"
    /**
     * 如果需要分包，请将方法置于 super.onCreate() 之前
     */
    override fun onCreate() {
        super.onCreate()
        instance = this
        initUtils()
        initSdk()
    }

    private fun initSdk() {
         registToWX()
         registJPush()
         initTenXun()
    }

    private fun registJPush() {
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
    }
    fun initTenXun(){
        StatHybridHandler.init(this)
        var applicationInfo = packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)
        var channelName = applicationInfo.metaData.getString("JPUSH_CHANNEL")
        StatConfig.setInstallChannel(this ,channelName)
        StatService.setContext(this)
        StatService.registerActivityLifecycleCallbacks(this)
        var crashReporter = StatCrashReporter.getStatCrashReporter(this)
        crashReporter.isEnableInstantReporting = true
        crashReporter.javaCrashHandlerStatus = true
        crashReporter.setJniNativeCrashLogcatOutputStatus(true)
        crashReporter.addCrashCallback(object : StatCrashCallback{
            override fun onJniNativeCrash(p0: String?) {
                Log.d("TAG","MTA StatCrashCallback onJniNativeCrash:\n $p0")
            }

            override fun onJavaCrash(p0: Thread?, ex: Throwable?) {
                Log.d("TAG","MTA StatCrashCallback onJavaCrash:\n $p0")            }
        })
    }

    private fun registToWX() {
        api = WXAPIFactory.createWXAPI(this,config.WXAPP_ID,true)
        api.registerApp(config.WXAPP_ID)
    }

    private fun initUtils() {
        //客服SDK
        KF5SDKInitializer.init(this@QBXApplication)
        ImageLoaderManager.getInstance(this@QBXApplication)
        SPUtils.getInstance(this@QBXApplication)
        //OPPO广告
        var builder = InitParams.Builder()
        if(BuildConfig.DEBUG){
            builder.setDebug(true)
        }
        var initParams = builder.build()
        MobAdManager.getInstance().init(this@QBXApplication,config.OPPO_APPID ,initParams)

        SharePrefenceHelper.initSharePreference(applicationContext)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityPaused(activity: Activity?) {

            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityStarted(activity: Activity?) {
                appCount++
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
                appCount--
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }
        })


        initLYSdk()

        //小米广告
        MimoSdk.init(this, config.miMoAPPID , config.miMoAPPKEY , config.miMoAPPTOKEN)


        //初始化今日头条SDK
        ttAdManager = TTAdManagerFactory.getInstance(this)
        ttAdManager.setAppId("5007813")
        ttAdManager.setName("趣步行")
        ttAdManager.setAllowShowNotifiFromSDK(false)

        //芝山嵌SDK初始化
        if(!config.ZSQSDKToken.equals("")){
            ADSDK.getInstance(this,config.ZSQSDKToken).init()
        }


    }
    fun getWXAPI(): IWXAPI {
        return api
    }
    fun  isForeground() : Boolean{
        return appCount > 0
    }

    fun initLYSdk(){
        //猎鹰广告SDK初始化
        FalconAdEntrance.getInstance().init(this@QBXApplication,"1665")
        var adconfig = AdConfig(this@QBXApplication).setConfigMode(AdConfig.CONFIG_EVERYTIME)
        SpreadManager.getInstance(this@QBXApplication).init(adconfig)
        InsertManager.getInstance(this@QBXApplication).init(adconfig)
        BannerManager.getInstance(this@QBXApplication).init(adconfig)
        NativeManager.getInstance(this@QBXApplication).init(adconfig)
        VideoManager.getInstance(this@QBXApplication).init(adconfig)

        AdSettings.setSupportHttps(true)
    }
}