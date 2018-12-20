package com.qubuxing.qbx.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.CountDownTimer
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.androidquery.AQuery
import com.baidu.mobads.SplashAd
import com.baidu.mobads.SplashAdListener
import com.bumptech.glide.Glide
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTSplashAd
import com.iflytek.voiceads.AdKeys
import com.iflytek.voiceads.IFLYNativeAd
import com.iflytek.voiceads.IFLYNativeListener
import com.iflytek.voiceads.NativeADDataRef
import com.ly.adpoymer.interfaces.SpreadListener
import com.ly.adpoymer.manager.SpreadManager
import com.qq.e.ads.splash.SplashAD
import com.qq.e.ads.splash.SplashADListener
import com.qq.e.comm.util.AdError
import com.qubuxing.qbx.MainActivity
import com.qubuxing.qbx.QBXApplication
import com.qubuxing.qbx.R
import com.qubuxing.qbx.http.RetrofitUtil
import com.qubuxing.qbx.http.beans.AdWithTypeEntity
import com.qubuxing.qbx.http.beans.GetSplashEntity
import com.qubuxing.qbx.http.beans.SplashAdEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


import java.lang.ref.WeakReference

/**
 * SplashScreen
 * 启动屏
 * from：http://www.devio.org
 * Author:CrazyCodeBoy
 * GitHub:https://github.com/crazycodeboy
 * Email:crazycodeboy@gmail.com
 */
object SplashScreen {
    private var mSplashDialog: Dialog? = null
    private var mActivity: WeakReference<Activity>? = null
    var b = false
    var cameraList = ArrayList<String>()
    var adShowed = false
    var activity: Activity? = null
    var SKIP_TEXT = "点击跳过 %d"
    lateinit var skipView: TextView
    lateinit var view: ConstraintLayout
    lateinit var containLayout: FrameLayout
    lateinit var mTTAdNative: TTAdNative
    lateinit var fullImageView : ImageView
    lateinit var timer: CountDownTimer
    /**
     * 打开启动屏
     */

    @JvmOverloads
    fun show(activity: Activity?, fullScreen: Boolean = true) {
        b = true
        this.activity = activity
        if (activity == null) return
        mActivity = WeakReference(activity)
        timer = object : CountDownTimer(5000, 1000) {
            override fun onFinish() {
                adShowed = true
                if ((SplashScreen.activity as MainActivity).client.pageGetFinished) {
                    hide(SplashScreen.activity)
                }
            }

            override fun onTick(millisUntilFinished: Long) {

            }
        }
        mTTAdNative = QBXApplication.ttAdManager.createAdNative(activity)
        activity.runOnUiThread {
            if (!activity.isFinishing) {
                mSplashDialog = Dialog(activity, if (fullScreen) R.style.SplashScreen_Fullscreen else R.style.SplashScreen_SplashTheme)
                view = LayoutInflater.from(activity).inflate(R.layout.dialog_splash_layout, null) as ConstraintLayout
                //                    params.setMargins(0,ScreenUtils.getStatusHeight(activity),0, 0);
                containLayout = view.findViewById<FrameLayout>(R.id.splash_container)
                //                    view.setLayoutParams(params);
                mSplashDialog!!.setContentView(view)
                mSplashDialog!!.setCancelable(false)
                skipView = view.findViewById<TextView>(R.id.skip_view)
                fullImageView = view.findViewById(R.id.fullscreen_img)
                mSplashDialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                mSplashDialog!!.window!!.setDimAmount(0f)
                if (mSplashDialog != null && !mSplashDialog!!.isShowing) {
                    mSplashDialog!!.show()
                    var getSplash = GetSplashEntity()
                    var call = RetrofitUtil.instance.help.getAdSplashs(getSplash)
                    timer.start()
                    call.enqueue(object : Callback<List<SplashAdEntity>> {
                        override fun onFailure(call: Call<List<SplashAdEntity>>, t: Throwable) {

                        }

                        override fun onResponse(call: Call<List<SplashAdEntity>>, response: Response<List<SplashAdEntity>>) {
                            var splashs = response.body()
                            timer.cancel()
                            if (Build.VERSION.SDK_INT <= 22) {

                                callAdWithType(splashs!![0])
                            } else {
                                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                ) {
//                                    Log.d("TAG","${splashs[1].adSrc}")

                                    callAdWithType(splashs!![0])
                                } else {
                                    adShowed = true
                                    cameraList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    cameraList.add(Manifest.permission.READ_PHONE_STATE)
                                    cameraList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    cameraList.add(Manifest.permission.ACCESS_FINE_LOCATION)
                                    cameraList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    cameraList.add(Manifest.permission.RECORD_AUDIO)
                                    activity.requestPermissions(cameraList.toTypedArray(), 10010)
                                }
                            }
                        }

                    })

                }
            }
        }
    }

    /**
     * 关闭启动屏
     */
    fun hide(activity: Activity?) {
        var activity = activity
        if (activity == null) activity = mActivity!!.get()
        if (activity == null) return

        activity.runOnUiThread {
            if (mSplashDialog != null && mSplashDialog!!.isShowing && adShowed) {
                try {
                    mSplashDialog!!.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }


    fun callAdWithType(splashAdEntity: SplashAdEntity) {
        when (splashAdEntity.adSrc) {
            "ad-lieying" -> {
                var spreadListener = object : SpreadListener {
                    override fun onAdFailed(p0: String?) {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }

                    override fun onAdDisplay(p0: String?) {
                        Log.d("onAdDisplay", "onAdDisplay")
                    }

                    override fun onAdReceived(p0: String?) {
                        Log.d("onAdReceived", "onAdReceived")

                    }

                    override fun onAdClick() {
                    }

                    override fun onAdClose(p0: String?) {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }
                }
                SpreadManager.getInstance(activity).request(activity, splashAdEntity.adSrcId, view, spreadListener)
            }
            "ad-gdt" -> {
                skipView.visibility = View.VISIBLE
                var adListener = object : SplashADListener {
                    override fun onADExposure() {
                        Log.d("Splash", "广点通闪屏onADExposure")
                    }

                    override fun onADDismissed() {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }

                    }

                    override fun onADPresent() {
                    }

                    override fun onNoAD(p0: AdError?) {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }

                    override fun onADClicked() {
                        Log.d("Splash", "广点通闪屏点击")

                    }

                    override fun onADTick(p0: Long) {
                        skipView.text = String.format(SKIP_TEXT, Math.round(p0 / 1000f))
                    }

                }

                var splashAD = SplashAD(activity, containLayout, skipView, "1107985626", splashAdEntity.adSrcId, adListener, 0)
            }
            "ad-toutiao" -> {
                mTTAdNative.loadSplashAd(initSlot(splashAdEntity), object : TTAdNative.SplashAdListener {
                    override fun onSplashAdLoad(p0: TTSplashAd?) {
                        var view = p0!!.splashView

                        p0.setSplashInteractionListener(object : TTSplashAd.AdInteractionListener {
                            override fun onAdClicked(p0: View?, p1: Int) {

                            }

                            override fun onAdSkip() {
                                adShowed = true
                                if ((activity as MainActivity).client.pageGetFinished) {
                                    hide(activity)
                                }
                            }

                            override fun onAdShow(p0: View?, p1: Int) {

                            }

                            override fun onAdTimeOver() {
                                adShowed = true
                                if ((activity as MainActivity).client.pageGetFinished) {
                                    hide(activity)
                                }

                            }
                        })
                        containLayout.addView(view)
                    }

                    override fun onTimeout() {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }

                    override fun onError(p0: Int, p1: String?) {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }
                })
            }
            "ad-kdxf" -> {
//                adShowed = true
//                if ((activity as MainActivity).client.pageGetFinished) {
//                    hide(activity)
//                }
                var nativeAd : IFLYNativeAd? = null
                var adItem : NativeADDataRef? = null
                var aquery = AQuery(containLayout.context)
                var mListener = object : IFLYNativeListener{
                    override fun onAdFailed(p0: com.iflytek.voiceads.AdError?) {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }

                    override fun onCancel() {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }

                    override fun onADLoaded(p0: MutableList<NativeADDataRef>?) {
                        if (p0!!.size > 0){
                            adItem = p0[0]
                            if (adItem!!.imgUrls != null && adItem!!.imgUrls.size > 0){
//                                aquery.id(R.id.fullscreen_img).image(adItem!!.imgUrls[0], false ,true)
                                Glide.with(fullImageView.context).load(adItem!!.imgUrls[0]).into(fullImageView)
                            }else{
//                                aquery.id(R.id.fullscreen_img).image(adItem!!.image , false, true)
                                Glide.with(fullImageView.context).load(adItem!!.image).into(fullImageView)
                                Log.d("Splash","${adItem!!.image}")
                            }
                            fullImageView.visibility = View.VISIBLE
                            aquery.id(R.id.fullscreen_img).clicked(object : View.OnClickListener{
                                override fun onClick(v: View?) {
                                    adItem!!.onClicked(view)
                                }
                            })
                            fullImageView.setOnTouchListener(object : View.OnTouchListener{
                                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                                    when(event!!.action){
                                        MotionEvent.ACTION_DOWN->{
                                            nativeAd!!.setParameter(AdKeys.CLICK_POS_DX, "${event.x}")
                                            nativeAd!!.setParameter(AdKeys.CLICK_POS_DY, "${event.y}")

                                        }
                                        MotionEvent.ACTION_UP ->{
                                            nativeAd!!.setParameter(AdKeys.CLICK_POS_UX, "${event.x}")
                                            nativeAd!!.setParameter(AdKeys.CLICK_POS_UY, "${event.y}")
                                        }
                                    }
                                    return false
                                }
                            })
                            timer.start()
                            if (adItem!!.onExposured(fullImageView)){
                                Log.d("Splash","KDXF曝光成功")
                            }
                        }
                    }

                    override fun onConfirm() {
                    }
                }
                 nativeAd = IFLYNativeAd(activity ,splashAdEntity.adSrcId , mListener)
                nativeAd.setParameter(AdKeys.DOWNLOAD_ALERT,"true")
                nativeAd.setParameter(AdKeys.DEBUG_MODE , "true")
                nativeAd.loadAd(1)

            }

            "ad-baidu"->{
                Log.d("Splash","百度")
                var bdSplashListener = object : SplashAdListener{
                    override fun onAdFailed(p0: String?) {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }

                    override fun onAdDismissed() {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }

                    override fun onAdPresent() {
                    }

                    override fun onAdClick() {
                        adShowed = true
                        if ((activity as MainActivity).client.pageGetFinished) {
                            hide(activity)
                        }
                    }
                }
                var splashAd = SplashAd(activity, containLayout ,bdSplashListener , splashAdEntity.adSrcId , true)
            }
            else ->{
                adShowed = true
                if ((activity as MainActivity).client.pageGetFinished) {
                    hide(activity)
                }
            }
        }
    }

    private fun initSlot(adWithTypeEntity: SplashAdEntity): AdSlot {
        var adSlot = AdSlot.Builder().setCodeId("${adWithTypeEntity.adSrcId}").setImageAcceptedSize(640, 320).setSupportDeepLink(true)
                .setAdCount(2)
                //激励视频奖励的名称，针对激励视频参数
                .setRewardName("rewardName")
                //激励视频奖励个数
                .setRewardAmount(1)
                //用户ID,使用激励视频必传参数
                //表来标识应用侧唯一用户；若非服务器回调模式或不需sdk透传，可设置为空字符串
                .setUserID("123455")
                //设置期望视频播放的方向，为TTAdConstant.HORIZONTAL或TTAdConstant.VERTICAL
                .setOrientation(TTAdConstant.VERTICAL)
                //激励视频奖励透传参数，字符串，如果用json对象，必须使用序列化为String类型,可为空
                .setMediaExtra("media_extra")
                .build()
        return adSlot
    }
}
/**
 * 打开启动屏
 */