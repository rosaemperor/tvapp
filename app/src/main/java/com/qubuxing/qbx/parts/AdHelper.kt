package com.qubuxing.qbx.parts

import android.app.Activity
import android.databinding.DataBindingUtil
import android.util.Log
import android.view.View
import android.webkit.WebView
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
import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.http.beans.AdWithTypeEntity
import com.qubuxing.qbx.http.beans.DeviceInfo
import com.qubuxing.qbx.http.beans.VideoBack

class AdHelper {
    var webview : WebView
    var binding : ActivityMainBinding
    lateinit var oppoVideoAd : RewardVideoAd
     var result : VideoBack
    var gson : Gson
    var bannerCallback = VideoBack()
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
                        result.result = "onAdClose"
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


    fun showAdYY(adWithTypeEntity: AdWithTypeEntity , callback: WVJBWebViewClient.WVJBResponseCallback?){
        when (adWithTypeEntity.ADType) {
            "banner" -> {
                var adViewBIDView = AdViewBannerManager(webview.context , "",200 , true)
                adViewBIDView.setShowCloseBtn(true)
                adViewBIDView.setRefreshTime(15)
                adViewBIDView.setOpenAnim(true)
                adViewBIDView.setOnAdViewListener(object : AdViewBannerListener {
                    override fun onAdFailedReceived(p0: String?) {

                    }

                    override fun onAdReceived() {
                    }

                    override fun onAdClicked() {
                    }

                    override fun onAdDisplayed() {

                    }

                    override fun onAdClosed() {
                    }
                })
            }
            "video"  ->{
                var adViewVideoInterface = object : AdViewVideoListener{
                    override fun onVideoReady() {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onVideoStartPlayed() {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onFailedReceivedVideo(p0: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onPlayedError(p0: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onVideoClosed() {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onVideoFinished() {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onReceivedVideo(p0: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                }
                var videoManager = AdViewVideoManager(webview.context  , "" ,adWithTypeEntity.spaceId , adViewVideoInterface , false)
            }
        }
    }


}