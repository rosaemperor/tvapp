package com.qubuxing.qbx.viewModels

import android.databinding.DataBindingUtil
import android.view.View
import com.ly.adpoymer.interfaces.InsertListener
import com.ly.adpoymer.interfaces.SpreadListener
import com.ly.adpoymer.interfaces.VideoListener
import com.ly.adpoymer.manager.InsertManager
import com.ly.adpoymer.manager.SpreadManager
import com.ly.adpoymer.manager.VideoManager
import com.qubuxing.qbx.BaseViewModel
import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.databinding.TestLayoutBinding

class TestViewModel : BaseViewModel(){
    override fun initViewModel() {

    }

    override fun initData() {
    }
    fun showOnSplash(view : View){
        var spreadListener = object : SpreadListener {
            override fun onAdFailed(p0: String?) {

            }

            override fun onAdDisplay(p0: String?) {
            }

            override fun onAdReceived(p0: String?) {
            }

            override fun onAdClick() {
            }

            override fun onAdClose(p0: String?) {
            }
        }
        var binding = DataBindingUtil.findBinding<TestLayoutBinding>(view)
        SpreadManager.getInstance(view.context).request(view.context,"7534",binding!!.parentLayout,spreadListener)
    }

    fun showOnInsert(view: View){
        var insertListener = object : InsertListener {
            override fun onAdDismiss(p0: String?) {
            }

            override fun onAdFailed(p0: String?) {
            }

            override fun onAdDisplay(p0: String?) {
            }

            override fun onAdReceived(p0: String?) {
                if (InsertManager.getInstance(view.context).isReady) {
                    InsertManager.getInstance(view.context).showAd()
                }
            }

            override fun onAdClick(p0: String?) {
            }

        }
        var count = 3
//                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(webView)
        InsertManager.getInstance(view.context).requestAd(view.context,"",insertListener,count)
    }

    fun showStoreVideo(view : View){
        var listener = object : VideoListener {
            override fun onAdClick() {

            }

            override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
            }

            override fun onAdFailed(p0: String?) {
            }

            override fun onAdShow() {
            }

            override fun onAdVideoBarClick() {
            }

            override fun onVideoComplete() {
            }

            override fun onAdClose() {
            }

            override fun onRewardVideoCached() {
                if(VideoManager.getInstance(view.context).isReady){
                    VideoManager.getInstance(view.context).showAd()
                }            }
        }
        VideoManager.getInstance(view.context).request(view.context,"7536","RewardName","userId",2,3,listener)
    }

    fun showInvadate(view : View){

    }
}