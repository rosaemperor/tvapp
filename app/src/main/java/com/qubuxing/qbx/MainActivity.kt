package com.qubuxing.qbx

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.widget.RelativeLayout
import com.kuaiyou.loader.AdViewBannerManager
import com.kuaiyou.loader.AdViewVideoManager
import com.kuaiyou.loader.loaderInterface.AdViewBannerListener
import com.kuaiyou.loader.loaderInterface.AdViewVideoListener

import com.oppo.mobad.api.InitParams
import com.oppo.mobad.api.MobAdManager
import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.databinding.ActivityThirdBrowserBinding
import com.qubuxing.qbx.http.beans.Step
import com.qubuxing.qbx.http.beans.StepGetEvent
import com.qubuxing.qbx.parts.WVWebViewClient
import com.qubuxing.qbx.service.StepCounterService
import com.qubuxing.qbx.utils.*
import com.qubuxing.qbx.utils.KFUtils.Preference
import com.qubuxing.qbx.viewModels.MainViewModel
import com.qubuxing.step.IStepGetAidlInterface
import com.qubuxing.step.TodayStepManager
import com.qubuxing.step.TodayStepService
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.xiaomi.ad.common.pojo.AdType
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.activity_main.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var client: WVWebViewClient
    lateinit var activityThirdBrowserBinding : ActivityThirdBrowserBinding
    lateinit var viewModel : MainViewModel
    lateinit var iSportStepInterface: IStepGetAidlInterface

    var mStepSum = 0
    var cameraList = ArrayList<String>()

    var dontGrantedPermissions: MutableList<String> = ArrayList()
    override fun initBinding() {
        binding = DataBindingUtil.setContentView(this@MainActivity,R.layout.activity_main)
        SplashScreen.show(this@MainActivity)
    }

    @SuppressLint("ResourceAsColor")
    override fun initViewModel() {
        viewModel = MainViewModel()
//        viewModel.getUpdateMessage(this@MainActivity)//改为等待前端吊起更新事件
        binding.swipeLayout.isEnabled = false
        binding.swipeLayout.setColorSchemeColors(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        binding.viewModel = viewModel
        viewModel.setLifecycle(lifecycle)

        binding.swipeLayout.setOnRefreshListener { binding.webView.reload() }
        binding.webView.initialze()
        binding.webView.webChromeClient = WebChromeClient()
        client = WVWebViewClient(binding.webView)
        binding.webView.webViewClient = client
        var link : Uri = Uri.parse(config.BASE_SERVER_WEBUI_URL)
        binding.webView.loadUrl(link.toString())
        initStepGet()
        binding.btnReload.setOnClickListener{
            binding.llNetworkError.visibility = View.GONE
            binding.webView.loadUrl(config.BASE_SERVER_WEBUI_URL)
        }
//        binding.splashLayout.setOnClickListener {
//            Toast.makeText(this@MainActivity,"wo sjdfajsdklf",Toast.LENGTH_LONG).show()
//        }
        binding.delete.setOnClickListener {
            binding.adLayout.removeAllViewsInLayout()
            binding.adLayout.invalidate()
        }
    }











    //按键处理
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            var videoManager : AdViewVideoManager? =null
//            var adViewVideoInterface = object : AdViewVideoListener {
//                override fun onVideoReady() {
//                    videoManager?.let {
//                        videoManager!!.playVideo(this@MainActivity)
//                    }
//
//                }
//
//                override fun onVideoStartPlayed() {
//                }
//
//                override fun onFailedReceivedVideo(p0: String?) {
//                    Log.d("TAG","${p0}")
//                }
//
//                override fun onPlayedError(p0: String?) {
//                }
//
//                override fun onVideoClosed() {
//                }
//
//                override fun onVideoFinished() {
//                }
//
//                override fun onReceivedVideo(p0: String?) {
//                }
//            }
//            videoManager = AdViewVideoManager(this@MainActivity , config.KYKey ,"POSID6em3hj6lajyt" , adViewVideoInterface , false)
//            videoManager.setVideoOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
//            return true
//        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        client.callHandler("checkIsRootPage")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data ==null || resultCode != RESULT_OK) return
        if (requestCode == client.ACTIVITYFOROMCLIENT){
            client.onActivityResult(data)
        }
    }

    /**
     * 谁请求的谁处理
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode){
            client.CAMERA_REQUEST_CODE -> client.onRequestPermissionsResult(requestCode,permissions,grantResults)
            client.READ_PHONE -> client.onRequestPermissionsResult(requestCode,permissions,grantResults)
            10010 ->{
                //OPPO广告
                var builder = InitParams.Builder()
                if(BuildConfig.DEBUG){
                    builder.setDebug(true)
                }
                var initParams = builder.build()
                MobAdManager.getInstance().init(this@MainActivity,config.OPPO_APPID ,initParams)
            }

        }



    }

    fun initStepGet(){
        TodayStepManager.init(application)
        var intent = Intent(this@MainActivity, TodayStepService::class.java)

        startService(intent)

        bindService(intent , object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                iSportStepInterface = IStepGetAidlInterface.Stub.asInterface(service) as IStepGetAidlInterface
                if (null == iSportStepInterface) return
                mStepSum = iSportStepInterface.currentTimeSportStep
                Log.d("TAG","$mStepSum")

            }
        }, Context.BIND_AUTO_CREATE)
        intent.setClass(this@MainActivity, StepCounterService::class.java)
        startService(intent)
    }


     override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
         if(null != intent && intent.hasExtra(config.WXthumbString)){
             when(intent.extras.getString(config.WXthumbString)){
                 "update"->{
                     binding.webView.reload()
                 }
                 else->{

                 }
             }
         }
        if (null != intent && intent.hasExtra("code")) {
            when (intent.action) {
                "qwerty" -> client.resopnseCode(intent.extras!!.get("code")!!.toString() + "")
//                "payResult" -> WVWebViewClient.responsePayResult(intent.extras!!.get("payResult")!!.toString() + "")
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun androidStepEvent(event : StepGetEvent){
        if(ReBootHelper.isReBoot()){
            if(!ReBootHelper.isTheSameDay()){
                SharePrefenceHelper.saveFloat("LastUpdateStep" , 0f)
                SharePrefenceHelper.saveFloat("LastSensorStep",event.setps)
            }
            var updateStep  = SharePrefenceHelper.getFloat("LastUpdateStep") + event.setps
            Log.i("TAG","检测到系统重启，当前数据：LastUpdateStep：${SharePrefenceHelper.getFloat("LastUpdateStep")}" +
                    "当前sensor步数为：${event.setps}")
//            Toast.makeText(webView.context , "检测到系统存在重启行为", Toast.LENGTH_LONG).show()
            Log.i("TAG","正常状态4")
            client.callBackStep(updateStep)
        }else{
            if(!ReBootHelper.isTheSameDay()){
                Log.i("TAG","两次取步数之间跨天，置初始状态")
                SharePrefenceHelper.saveFloat("LastSensorStep",event.setps)
                SharePrefenceHelper.saveFloat("LastUpdateStep",0f)
                client.callBackStep(0f)
            }else{
                if(SharePrefenceHelper.getBoolean("ServiceHasDead")){
                    Log.i("TAG","检测到系统没有重启，当前数据：LastUpdateStep：${SharePrefenceHelper.getFloat("LastUpdateStep")}" +
                            "当前sensor步数为：${event.setps}")
                    Log.i("TAG","检测到记步服务被杀死")
//                    Toast.makeText(this@MainActivity , "检测到记步服务被杀死" , Toast.LENGTH_LONG).show()
                    var lastUpdateStep = SharePrefenceHelper.getFloat("LastUpdateStep")
                    var lastSensorStep = SharePrefenceHelper.getFloat("LastSensorStep")
                    Log.i("TAG","lastUpdateStep:${lastUpdateStep} lastSensorStep:${lastSensorStep}")
                    SharePrefenceHelper.saveFloat("LastSensorStep" , event.setps)
                    if(event.setps > lastSensorStep && lastSensorStep!= 0f){
                        Log.i("TAG","正常状态1")
                        client.callBackStep(event.setps-lastSensorStep + lastUpdateStep)
                    }else{
                        Log.i("TAG","正常状态2")
                        client.callBackStep(lastUpdateStep)
                    }

                }else{
                    Log.i("TAG","没有检测到记步服务被杀死")
                    var  lastUpdateStep = SharePrefenceHelper.getFloat("LastUpdateStep")
                    var lastSensorStep = SharePrefenceHelper.getFloat("LastSensorStep")
                    Log.i("TAG","正常状态3")
                    var dayServiceStep =  SharePrefenceHelper.getFloat("TodayServiceSteps")
                    SharePrefenceHelper.saveFloat("TodayServiceSteps",0f)
                    SharePrefenceHelper.saveFloat("LastSensorStep" , event.setps)
                    client.callBackStep(lastUpdateStep+(event.setps -lastSensorStep -  dayServiceStep))
                    Log.i("TAG","lastUpdateStep:${lastUpdateStep} TodayServiceSteps : ${SharePrefenceHelper.getFloat("TodayServiceSteps")}")
                }
            }


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stepGetEvent(event : StepGetEvent){
        Log.d("TAG","stepGetEvent")
        if (!client.backStep) return
        var diff = 0.0f
        var str = ""
        if(client.haveStepToday != 0f){
            var lastSteps = 0.0f
            if ( "" != SharePrefenceHelper.get("stepSum")){
                lastSteps = SharePrefenceHelper.get("stepSum").toFloat()
            }
            diff = event.setps - lastSteps
            if (diff <0 ) {
                diff =0.0f

            }
            SharePrefenceHelper.save("stepSum",""+event.setps)
            client.oldCallBackStep(client.haveStepToday + diff)
            return
        }else{
            SharePrefenceHelper.save("stepSum",""+event.setps)
            var long =  SystemClock.elapsedRealtimeNanos() / 1000000000
//            Log.d("TAGS","currentTimeMillis:${System.currentTimeMillis()}")
//            Log.d("TAGS","elapsedRealtimeNanos:${SystemClock.elapsedRealtimeNanos()}")
//            Log.d("TAGS","${long}")
//            Log.d("TAGS","${long/(60 * 60)}")
            var spendHour = long/(60 * 60)
            if (spendHour<= 0) spendHour =1
            var dayStep = event.setps/(spendHour)
            SharePrefenceHelper.save("dayStep","$dayStep")
            makeStepNumber()
        }
    }

    fun makeStepNumber(){
        var dayStep = SharePrefenceHelper.get("dayStep").toFloat()
        var calendar = Calendar.getInstance()
        var hours : Float = (calendar.get(Calendar.HOUR_OF_DAY)-6).toFloat()
        var mins = calendar.get(Calendar.MINUTE).toFloat()
        if(mins in 1..58){
            hours += mins / 60
            Log.d("TAGS","hours :$hours")
        }

        if (hours <0f ) hours = 0f
        var steps = dayStep* hours
        if (steps.isNaN()) steps =0f
        Log.d("TAGS","dayStep:$dayStep hours:$hours ")
        if (steps < mStepSum)
            steps =  mStepSum.toFloat()
        client.oldCallBackStep(steps)
    }

    override fun onRestart() {
        client.callHandler("duibaCallback","active",null)
        super.onRestart()
    }


}