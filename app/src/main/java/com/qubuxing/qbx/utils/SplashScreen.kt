package com.qubuxing.qbx.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.ly.adpoymer.interfaces.SpreadListener
import com.ly.adpoymer.manager.SpreadManager
import com.qubuxing.qbx.MainActivity
import com.qubuxing.qbx.R
import com.qubuxing.qbx.databinding.ActivityMainBinding


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
    /**
     * 打开启动屏
     */

    @JvmOverloads
    fun show(activity: Activity?, fullScreen: Boolean = true) {
        b = true
        if (activity == null) return
        mActivity = WeakReference(activity)
        activity.runOnUiThread {
            if (!activity.isFinishing) {
                mSplashDialog = Dialog(activity, if (fullScreen) R.style.SplashScreen_Fullscreen else R.style.SplashScreen_SplashTheme)
                val view = LayoutInflater.from(activity).inflate(R.layout.dialog_splash_layout, null) as ConstraintLayout
                //                    params.setMargins(0,ScreenUtils.getStatusHeight(activity),0, 0);
                //                    view.setLayoutParams(params);
                mSplashDialog!!.setContentView(view)
                mSplashDialog!!.setCancelable(false)
                mSplashDialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                mSplashDialog!!.window!!.setDimAmount(0f)
                if (mSplashDialog != null && !mSplashDialog!!.isShowing) {
                    mSplashDialog!!.show()
                    if (Build.VERSION.SDK_INT <= 22){
                        var spreadListener = object : SpreadListener {
                            override fun onAdFailed(p0: String?) {
                                Log.d("onAdFailed","onAdFailed")
                            }

                            override fun onAdDisplay(p0: String?) {
                                Log.d("onAdDisplay","onAdDisplay")
                            }

                            override fun onAdReceived(p0: String?) {
                                Log.d("onAdReceived","onAdReceived")

                            }

                            override fun onAdClick() {
                            }

                            override fun onAdClose(p0: String?) {
                                adShowed = true
                                if((activity as MainActivity).client.pageGetFinished){
                                    hide(activity)
                                }
                            }
                        }
                        SpreadManager.getInstance(activity).request(activity,"7534",view,spreadListener)
                    }else{
                        if ( ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(activity,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED&&
                                ActivityCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED&&
                                ActivityCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        ){
                            var spreadListener = object : SpreadListener {
                                override fun onAdFailed(p0: String?) {
                                    Log.d("onAdFailed","onAdFailed")
                                }

                                override fun onAdDisplay(p0: String?) {
                                    Log.d("onAdDisplay","onAdDisplay")
                                }

                                override fun onAdReceived(p0: String?) {
                                    Log.d("onAdReceived","onAdReceived")

                                }

                                override fun onAdClick() {
                                }

                                override fun onAdClose(p0: String?) {
                                    adShowed = true
                                    if((activity as MainActivity).client.pageGetFinished){
                                        hide(activity)
                                    }
                                }
                            }
                            SpreadManager.getInstance(activity).request(activity,"7534",view,spreadListener)
                        }else{
                            adShowed = true
                            cameraList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            cameraList.add(Manifest.permission.READ_PHONE_STATE)
                            cameraList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            cameraList.add(Manifest.permission.ACCESS_FINE_LOCATION)
                            cameraList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            activity.requestPermissions(cameraList.toTypedArray(),10010)

                        }
                    }




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
}
/**
 * 打开启动屏
 */