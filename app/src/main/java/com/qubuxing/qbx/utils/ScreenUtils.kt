package com.qubuxing.qbx.utils

import android.content.Context

object ScreenUtils {
    fun dip2px(context : Context, dp : Float) : Int{
        var scale = context.resources.displayMetrics.density
        return (dp * scale+ 0.5f ).toInt()
    }
}