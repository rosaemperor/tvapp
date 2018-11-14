package com.qubuxing.qbx.widget

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.qubuxing.qbx.BuildConfig


class QBXWebView : WebView {
    internal var mContext: Context

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.mContext = context

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.mContext = context
    }

    fun initialze() {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        isVerticalFadingEdgeEnabled = false
        isHorizontalFadingEdgeEnabled = false
        webChromeClient = WebChromeClient()
        setOnLongClickListener { true }
        val settings = settings
        settings.setSupportZoom(true)
        settings.textZoom = 100
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT > 18) {
            setWebContentsDebuggingEnabled(true)
        }
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.userAgentString = settings.userAgentString + " VayNhanh-android"
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setAppCachePath(mContext.applicationContext.cacheDir.absolutePath)
        settings.allowFileAccess = true
        settings.setAppCacheEnabled(true)

    }


}