package com.qubuxing.qbx.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.qubuxing.qbx.MainActivity
import com.qubuxing.qbx.QBXApplication
import com.qubuxing.qbx.config
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage



class WXEntryActivity : Activity(),IWXAPIEventHandler{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QBXApplication.instance.getWXAPI().handleIntent(intent,this)
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {

    }

    override fun onReq(baseReq: BaseReq) {
    }

    override fun onResp(baseResp: BaseResp) {
//        val resp = baseResp as SendAuth.Resp
        if (baseResp.errCode == 0 ){
            val intent = Intent("qwerty")
            intent.setClass(this@WXEntryActivity, MainActivity::class.java!!)
            val bundle = Bundle()
            if(baseResp.type ==1){
                bundle.putString("code", "" + (baseResp as SendAuth.Resp).code)
            }
            if(baseResp.type == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM){
                var launchMiniProResq = baseResp as WXLaunchMiniProgram.Resp
                bundle.putString(config.WXthumbString, launchMiniProResq.extMsg)
            }
            intent.putExtras(bundle)
            startActivity(intent)
            finish()
        }else{
            finish()
        }



    }

    override fun onPause() {
        super.onPause()
    }
    fun goToShowMsg(showReq :ShowMessageFromWX.Req ){
        val wxMsg = showReq.message
        val obj = wxMsg.mediaObject as WXAppExtendObject

        val msg = StringBuffer()
        msg.append("description: ")
        msg.append(wxMsg.description)
        msg.append("\n")
        msg.append("extInfo: ")
        msg.append(obj.extInfo)
        msg.append("\n")
        msg.append("filePath: ")
        msg.append(obj.filePath)

        val intent = Intent(this, MainActivity::class.java)
        var bundle = Bundle()
        bundle.putString(config.WXthumbString , msg.toString())
        bundle.putByteArray(config.WXthumbData, wxMsg.thumbData)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }


}