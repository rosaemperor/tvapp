package com.qubuxing.qbx.revicer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TimeChangerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let{
            when(intent.action){
                Intent.ACTION_TIMEZONE_CHANGED->{
                    Log.d("TIMELooker","时间改变了")
                }
                Intent.ACTION_TIME_CHANGED->{
                    Log.d("TIMELooker","时区改变了")

                }
                else->{

                }
            }
        }

    }
}