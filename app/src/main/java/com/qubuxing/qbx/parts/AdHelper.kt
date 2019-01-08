package com.qubuxing.qbx.parts

import android.app.Activity
import android.databinding.DataBindingUtil
import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson
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
                        Log.d("TAG","onAdFailed${p0}")
                        callback!!.callback("onAdFailed")
                    }

                    override fun onAdShow() {
                    }

                    override fun onAdClick() {
                        callback!!.callback("onAdClick")
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
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun onAdClick(p0: Long) {
                        Log.d("TAG","onLandingPageClose")
                    }

                    override fun onVideoPlayClose(p0: Long) {
                        result.result = "onAdClose"
                        result.supplierType = adWithTypeEntity.supplierType
                        ((binding.webView.context) as MainActivity).client.callHandler("videoCallback",gson.toJson(result),null)                    }

                    override fun onLandingPageOpen() {
                        Log.d("TAG","onLandingPageClose")
                    }

                    override fun onVideoPlayComplete() {
                        result.result = "onVideoPlayComplete"
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
                    result.result = "onAdFailed"
                    result.supplierType = adWithTypeEntity.supplierType
                    ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",result.result,null)
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
                    result.result = "onAdFailed"
                    result.supplierType = adWithTypeEntity.supplierType
                    ((binding.webView.context) as MainActivity).client.callHandler("bannerCallback",result.result,null)
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

}