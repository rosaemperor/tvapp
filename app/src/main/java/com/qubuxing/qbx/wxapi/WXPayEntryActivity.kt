package com.qubuxing.qbx.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.qubuxing.qbx.QBXApplication
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

class WXPayEntryActivity : Activity(), IWXAPIEventHandler{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QBXApplication.instance.getWXAPI().handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq) {

    }

    override fun onResp(baseResp: BaseResp) {

        val intent = Intent("payResult")
        val bundle = Bundle()
        bundle.putString("payResult", "" + baseResp.errCode)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
        Log.d("PAYTAG", "" + baseResp.errCode + "信息：" + baseResp.errStr)
    }
}