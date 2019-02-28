package com.qubuxing.qbx.parts

import android.app.Activity
import android.content.pm.ActivityInfo
import android.databinding.DataBindingUtil
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import com.baidu.mobads.AdView
import com.baidu.mobads.AdViewListener
import com.google.gson.Gson
import com.kuaiyou.loader.AdViewBannerManager
import com.kuaiyou.loader.AdViewVideoManager
import com.kuaiyou.loader.loaderInterface.AdViewBannerListener
import com.kuaiyou.loader.loaderInterface.AdViewVideoListener
import com.oppo.mobad.api.ad.BannerAd
import com.oppo.mobad.api.ad.RewardVideoAd
import com.oppo.mobad.api.listener.IBannerAdListener
import com.oppo.mobad.api.listener.IRewardVideoAdListener
import com.oppo.mobad.api.params.RewardVideoAdParams
import com.qubuxing.qbx.MainActivity
import com.qubuxing.qbx.R
import com.qubuxing.qbx.config
import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.http.beans.AdWithTypeEntity
import com.qubuxing.qbx.http.beans.DeviceInfo
import com.qubuxing.qbx.http.beans.VideoBack
import org.json.JSONObject

class AdHelper {
    var webview : WebView
    var binding : ActivityMainBinding
    lateinit var oppoVideoAd : RewardVideoAd
     var result : VideoBack
    var gson : Gson
    var bannerCallback = VideoBack()
    var videoManager: AdViewVideoManager? =null
    lateinit var mRewardVideoAd : com.baidu.mobads.rewardvideo.RewardVideoAd
    constructor(webview: WebView){
        this.webview = webview
        binding = DataBindingUtil.findBinding<ActivityMainBinding>(this.webview)!!
        result = VideoBack()
        gson = Gson()
    }
    fun showADOPPO(adWithTypeEntity: AdWithTypeEntity , callback: WVJBWebViewClient.WVJBResponseCallback?){
        when (adWithTypeEntity.ADType){
            "banner" ->{
                var oppoBanner = BannerAd((webview.context) as Activity , adWithTypeEntity.spaceId)
                var oppoListener = object : IBannerAdListener{
                    override fun onAdFailed(p0: String?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p0!!
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdShow() {
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdClick() {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdReady() {
                        Log.d("TAG","")
                    }

                    override fun onAdClose() {
                        callback!!.callback("onAdClose")
                    }
                }
                oppoBanner.setAdListener(oppoListener)
                binding.adLayout.removeAllViews()
                if (null !=oppoBanner.adView){
                    binding.adLayout.addView(oppoBanner.adView)
                    binding.adLayout.invalidate()

                }
                oppoBanner.loadAd()
            }
            "video" ->{
                var oppoVideoListener = object : IRewardVideoAdListener {
                    override fun onLandingPageClose() {
                        Log.d("TAG","onLandingPageClose")
                    }

                    override fun onAdFailed(p0: String?) {
                        result.result = "onAdFailed"
                        result.supplierType = adWithTypeEntity.supplierType
                        p0?.let {
                            result.reason = p0
                        }
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    }

                    override fun onAdSuccess() {
                        result.result = "onRewardVideoCached"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun onVideoPlayStart() {
                        Log.d("TAG","onLandingPageClose")
                    }

                    override fun onVideoPlayError(p0: String?) {
                        result.result = "onAdFailed"
                        result.supplierType = adWithTypeEntity.supplierType
                        result.reason = "播放错误"
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun onAdClick(p0: Long) {
                        result.result = "onAdClick"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    }

                    override fun onVideoPlayClose(p0: Long) {
                        result.result = "onAdClose"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    }

                    override fun onLandingPageOpen() {
                        Log.d("TAG","onLandingPageClose")
                    }

                    override fun onVideoPlayComplete() {
                        result.result = "onRewardVideoCached"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }
                }
                oppoVideoAd = RewardVideoAd(webview.context , adWithTypeEntity.spaceId ,oppoVideoListener )
                var paramsBuilder = RewardVideoAdParams.Builder()
                var rewardVideoAdParams = paramsBuilder.setFetchTimeout(3000).build()
                oppoVideoAd.loadAd(rewardVideoAdParams)
            }
            else ->{

            }


        }
    }
    fun showVideoWithType(type : String){
        when(type){
            "OPPOVideo"->{
                oppoVideoAd.showAd()
            }
            "KYVideo" ->{
                videoManager?.let {
                    videoManager!!.playVideo(webview.context)
                }
            }
            "BAIDUVideo" ->{
                if (mRewardVideoAd.isReady){
                    mRewardVideoAd.show()
                }
            }
            else ->{

            }
        }

    }
    fun checkIsSupply(adWithTypeEntity: AdWithTypeEntity ,device : DeviceInfo): Boolean{

        if(adWithTypeEntity.supplierType == "OPPO"  && !device.deviceBrand.contains("OPPO")){
            when(adWithTypeEntity.ADType){
                "banner" ->{
                    bannerCallback.result = "onAdFailed"
                    bannerCallback.supplierType = adWithTypeEntity.supplierType
                    ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    return true
                }
                "video" ->{
                    result.result = "onAdFailed"
                    result.supplierType = adWithTypeEntity.supplierType

                    ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    return true
                }
            }

        }
        if(adWithTypeEntity.supplierType == "XIAOMI"  && !(device.deviceBrand.contains("MI") || device.deviceBrand.contains("Redmi"))){
            when(adWithTypeEntity.ADType){
                "banner" ->{
                    bannerCallback.result = "onAdFailed"
                    bannerCallback.supplierType = adWithTypeEntity.supplierType
                    ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    return true
                }
                "video" ->{
                    result.result = "onAdFailed"
                    result.supplierType = adWithTypeEntity.supplierType
                    ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    return true
                }
            }

        }
        return false
    }


    fun showAdKY(adWithTypeEntity: AdWithTypeEntity , callback: WVJBWebViewClient.WVJBResponseCallback?){
        when (adWithTypeEntity.ADType) {
            "banner" -> {
                var adViewBIDView = AdViewBannerManager(webview.context , config.KYKey,AdViewBannerManager.BANNER_AUTO_FILL , true)
                adViewBIDView.setShowCloseBtn(true)
                adViewBIDView.setRefreshTime(15)
                adViewBIDView.setOpenAnim(true)
                adViewBIDView.setOnAdViewListener(object : AdViewBannerListener {
                    override fun onAdFailedReceived(p0: String?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p0!!
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdReceived() {
                        binding.adLayout.removeAllViews()
                        binding.adLayout.addView(adViewBIDView.adViewLayout)

                        var closeView = LayoutInflater.from(binding.webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding!!.adLayout.removeAllViews()
                        }
                        binding.adLayout.addView(closeView)
                        binding.adLayout.invalidate()
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdClicked() {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdDisplayed() {

                    }

                    override fun onAdClosed() {
                        result.result = "onAdClose"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(result),null)
                    }
                })
            }
            "video"  ->{

                var adViewVideoInterface = object : AdViewVideoListener{
                    override fun onVideoReady() {
                        result.result = "onRewardVideoCached"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
//                        videoManager?.let {
//                            videoManager!!.playVideo(webview.context)
//                        }
                    }

                    override fun onVideoStartPlayed() {
                    }

                    override fun onFailedReceivedVideo(p0: String?) {
                        result.result = "onAdFailed"
                        result.supplierType = adWithTypeEntity.supplierType
                        p0?.let {
                            result.reason = p0
                        }
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    }

                    override fun onPlayedError(p0: String?) {
                        result.result = "onAdFailed"
                        result.supplierType = adWithTypeEntity.supplierType
                        result.reason = "播放错误"
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    }

                    override fun onVideoClosed() {
                        result.result = "onAdClose"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    }

                    override fun onVideoFinished() {
//                        result.result = "onAdClose"
//                        result.supplierType = adWithTypeEntity.supplierType
//                        result.reason = "播放错误"
//                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)
                    }

                    override fun onReceivedVideo(p0: String?) {
                    }
                }
                videoManager= AdViewVideoManager(webview.context  , config.KYKey ,adWithTypeEntity.spaceId , adViewVideoInterface , false)
                videoManager?.let {
                    videoManager!!.setVideoOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                }
            }
        }
    }


    fun showBDAD(adWithTypeEntity: AdWithTypeEntity, callback: WVJBWebViewClient.WVJBResponseCallback?){
        when(adWithTypeEntity.ADType){
            "banner"->{
                var bdAdListener = object : AdViewListener {
                    override fun onAdFailed(p0: String?) {
                        bannerCallback.result = "onAdFailed"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        bannerCallback.reason = p0!!
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                    }

                    override fun onAdShow(p0: JSONObject?) {
                        var closeView = LayoutInflater.from(binding.webView.context).inflate(R.layout.banner_close_view,null)
                        closeView.setOnClickListener {
                            binding!!.adLayout.removeAllViews()
                        }
                        bannerCallback.result = "onAdShow"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)
                        binding.adLayout.addView(closeView)
                    }

                    override fun onAdClick(p0: JSONObject?) {
                        bannerCallback.result = "onAdClick"
                        bannerCallback.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",gson.toJson(bannerCallback),null)                    }

                    override fun onAdReady(p0: AdView?) {
                    }

                    override fun onAdSwitch() {
                    }

                    override fun onAdClose(p0: JSONObject?) {
                        ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback","onAdClose",null)
                    }
                }
                var bdAdView = AdView(webview.context,adWithTypeEntity.spaceId)
                bdAdView.setListener(bdAdListener)
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webview)
                binding!!.adLayout.removeAllViews()
                binding!!.adLayout.invalidate()
                binding!!.adLayout.addView(bdAdView)
                var closeView = LayoutInflater.from(webview.context).inflate(R.layout.banner_close_view,null)
                closeView.setOnClickListener {
                    binding.adLayout.removeAllViews()
                }
                binding.adLayout.addView(closeView)
            }
            "video"->{
                var videoListener =object :com.baidu.mobads.rewardvideo.RewardVideoAd.RewardVideoAdListener{
                    override fun onAdFailed(p0: String?) {
                        result.result = "onAdFailed"
                        result.supplierType = adWithTypeEntity.supplierType
                        result.reason = "${p0}"
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun playCompletion() {
                        result.result = "onAdClose"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun onAdShow() {
                    }

                    override fun onAdClick() {
                    }

                    override fun onAdClose(p0: Float) {
                        result.result = "onAdClose"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun onVideoDownloadSuccess() {
                        result.result = "onRewardVideoCached"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun onVideoDownloadFailed() {
                        result.result = "onAdFailed"
                        result.supplierType = adWithTypeEntity.supplierType
                        result.reason = "播放错误"
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }
                }
                mRewardVideoAd = com.baidu.mobads.rewardvideo.RewardVideoAd((webview.context as Activity) ,adWithTypeEntity.spaceId , videoListener)
            }
            "insert"->{

            }
        }
    }


}