package com.qubuxing.qbx.utils

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.qubuxing.qbx.R


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
                val view = LayoutInflater.from(activity).inflate(R.layout.dialog_splash_layout, null)
                //                    params.setMargins(0,ScreenUtils.getStatusHeight(activity),0, 0);
                //                    view.setLayoutParams(params);
                mSplashDialog!!.setContentView(view)
                mSplashDialog!!.setCancelable(false)
                mSplashDialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                mSplashDialog!!.window!!.setDimAmount(0f)
                if (mSplashDialog != null && !mSplashDialog!!.isShowing) {
                    mSplashDialog!!.show()
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
            if (mSplashDialog != null && mSplashDialog!!.isShowing) {
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