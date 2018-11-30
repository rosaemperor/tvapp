package com.qubuxing.qbx.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import com.qubuxing.qbx.R

class BitmapUtils {
    companion object {
        fun drawableBitmapOnWhiteBg(context : Context , bitmap: Bitmap) : Bitmap{


        var newBitmap = Bitmap.createBitmap(bitmap.width,bitmap.height,Bitmap.Config.ARGB_8888)
        var canvas = Canvas(newBitmap)
        var paint = Paint()
            canvas.drawColor(ContextCompat.getColor(context,R.color.white))
            canvas.drawBitmap(bitmap,0f,0f,paint)
            return newBitmap
        }
    }
}