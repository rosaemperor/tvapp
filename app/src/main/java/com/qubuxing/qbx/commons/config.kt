package com.qubuxing.qbx

/**
 * 上线前检查
 */
object config {

    private var HOST_ADDRESS_PROD :String = "https://m.qubuxing.com/api/"
    private var HOST_ADDRESS_DEV : String ="https://qubuxing.lanlingdai.net/api/"
    private var HOST_ADDRESS_GETSET : String ="https://stg.qbx.lanyougroup.com/api/"

    private var WEB_UI_URL_PROD : String = "https://m.qubuxing.com"

    private var WEB_UI_URL_GETSET : String = "https://stg.qbx.lanyougroup.com/"
    private var WEB_UI_URL_DEV : String = "https://qubuxing.lanlingdai.net/"


    val WXAPP_ID ="wx2d2573d29537aede"

    //芝山嵌SDKToken
    val ZSQSDKToken=""

    //oppo广告账户
    val OPPO_APPID = "3717922"

    //小米广告账户
    val miMoAPPID = "2882303761517900333"
    val miMoAPPKEY = "fake_app_key"
    val miMoAPPTOKEN = "fake_app_token"


    //客服账号
    val KFAPPID = "0015c173d1f6df514c549a935f49fb6a3bc6a3c3a403d888"
    val KFHelpAdress ="luckybi.kf5.com"

    //快友key
    val KYKey = "SDK20191818060228rs84im4g0x8upuu"

    val GDTAPP_ID ="1107985626"
    private var HOST_ADDRESS_DEBUG : String  = if(BuildConfig.TEST) HOST_ADDRESS_GETSET else HOST_ADDRESS_DEV
    private var  WEB_UI_URL_DEBUG: String = if(BuildConfig.TEST) WEB_UI_URL_GETSET else WEB_UI_URL_DEV
    //H5前端页面地址
    var BASE_SERVER_WEBUI_URL :String = if (BuildConfig.DEBUG) WEB_UI_URL_DEBUG else WEB_UI_URL_PROD
    //后台接口地址
    var HOST_ADDRESS :String = if (BuildConfig.DEBUG) HOST_ADDRESS_DEBUG else HOST_ADDRESS_PROD




    var WXthumbData = "WXthumbData"
    var WXthumbString = "WXthumbString"


    var openOnceInit = true



    val GPS_REQUEST_CODE = 11199
}