package com.qubuxing.qbx.revicer

import android.content.Context
import android.content.Intent
import com.qubuxing.qbx.MainActivity
import com.qubuxing.qbx.QBXApplication
import com.qubuxing.step.BaseClickBroadcast




class StepReciver : BaseClickBroadcast(){
    override fun onReceive(context: Context?, intent: Intent?) {
        var kotlinApplication = context!!.applicationContext as QBXApplication
        if (kotlinApplication.isForeground()){
            val mainIntent = Intent(context, MainActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(mainIntent)
        }
    }
}