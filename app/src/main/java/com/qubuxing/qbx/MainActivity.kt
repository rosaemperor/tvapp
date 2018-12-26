package com.qubuxing.qbx

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import cn.jpush.android.api.JPushInterface
import com.baidu.mobads.AdView
import com.baidu.mobads.AdViewListener
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ly.adpoymer.interfaces.BannerListener
import com.ly.adpoymer.interfaces.InsertListener
import com.ly.adpoymer.interfaces.SpreadListener
import com.ly.adpoymer.interfaces.VideoListener
import com.ly.adpoymer.manager.BannerManager
import com.ly.adpoymer.manager.InsertManager
import com.ly.adpoymer.manager.SpreadManager
import com.ly.adpoymer.manager.VideoManager
import com.miui.zeus.mimo.sdk.ad.AdWorkerFactory
import com.miui.zeus.mimo.sdk.ad.IAdWorker
import com.miui.zeus.mimo.sdk.listener.MimoAdListener
import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.databinding.ActivityThirdBrowserBinding
import com.qubuxing.qbx.http.beans.Step
import com.qubuxing.qbx.http.beans.StepGetEvent
import com.qubuxing.qbx.parts.WVWebViewClient
import com.qubuxing.qbx.service.StepCounterService
import com.qubuxing.qbx.utils.DialogUtils
import com.qubuxing.qbx.utils.JumpSetting
import com.qubuxing.qbx.utils.SharePrefenceHelper
import com.qubuxing.qbx.utils.SplashScreen
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
//        Glide.setup(GlideBuilder(this@MainActivity).setDecodeFormat(DecodeFormat.ALWAYS_ARGB_8888))
        viewModel = MainViewModel()
        viewModel.getUpdateMessage(this@MainActivity)
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
//        if (keyCode == KeyEvent.KEYCODE_BACK){
//           var mBannerAd : IAdWorker
//                var listener = object : MimoAdListener {
//                    override fun onAdFailed(p0: String?) {
//                        Log.d("TAG","MiBanner: onAdFailed")
//                    }
//
//                    override fun onAdDismissed() {
//
//                    }
//
//                    override fun onAdPresent() {
//                    }
//
//                    override fun onAdClick() {
//
//                    }
//
//                    override fun onStimulateSuccess() {
//                        Log.d("TAG","MiBanner: onAdFailed")
//                    }
//
//                    override fun onAdLoaded(p0: Int) {
//                        Log.d("TAG","MiBanner: ${p0}")
//
//                    }
//                }
//                binding.adLayout.removeAllViews()
//                binding.adLayout.invalidate()
//                mBannerAd = AdWorkerFactory.getAdWorker(webView.context, binding!!.adLayout ,listener, AdType.AD_BANNER)
//                mBannerAd.loadAndShow("7a357fc4b59ba1046c8c84763039d6b1")
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
        if (null != intent && intent.hasExtra("code")) {
            when (intent.action) {
                "qwerty" -> client.resopnseCode(intent.extras!!.get("code")!!.toString() + "")
//                "payResult" -> WVWebViewClient.responsePayResult(intent.extras!!.get("payResult")!!.toString() + "")
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stepGetEvent(event : StepGetEvent){
        if (!client.backStep) return
        var diff = 0.0f
        if(client.haveStepToday != 0){
            diff = event.setps - SharePrefenceHelper.get("stepSum").toFloat()
            if (diff <0 ) {
                diff =0.0f
                SharePrefenceHelper.save("stepSum",""+event.setps)
            }
            client.callBackStep(client.haveStepToday + diff)
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
        client.callBackStep(steps)
    }

    override fun onRestart() {
        client.callHandler("duibaCallback","active",null)
        super.onRestart()
    }

}