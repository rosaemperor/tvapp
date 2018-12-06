package com.qubuxing.qbx.parts

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*


@SuppressLint("SetJavaScriptEnabled", "NewApi")
open class WVJBWebViewClient @JvmOverloads constructor(protected var webView: WebView, private val messageHandler: WVJBHandler? = null) : WebViewClient() {

    private var startupMessageQueue: ArrayList<WVJBMessage>? = null
    private var responseCallbacks: MutableMap<String, WVJBResponseCallback>? = null
    private var messageHandlers: MutableMap<String, WVJBHandler>? = null
    private var uniqueId: Long = 0
    private val myInterface = MyJavascriptInterface()

    interface WVJBResponseCallback {
        fun callback(data: Any?)
    }

    interface WVJBHandler {
        fun request(data: Any?, callback: WVJBResponseCallback?)
    }


    init {
        this.webView.settings.javaScriptEnabled = true
        this.webView.addJavascriptInterface(myInterface, kInterface)
        this.responseCallbacks = HashMap()
        this.messageHandlers = HashMap()
        this.startupMessageQueue = ArrayList()
    }

    fun enableLogging() {
        logging = true
    }

    @JvmOverloads
    fun send(data: Any, responseCallback: WVJBResponseCallback? = null) {
        sendData(data, responseCallback, null)
    }

    @JvmOverloads
    fun callHandler(handlerName: String, data: Any? = null,
                    responseCallback: WVJBResponseCallback? = null) {
        sendData(data, responseCallback, handlerName)
    }

    fun registerHandler(handlerName: String?, handler: WVJBHandler?) {
        if (handlerName == null || handlerName.length == 0 || handler == null)
            return
        messageHandlers!![handlerName] = handler
    }

    private fun sendData(data: Any?, responseCallback: WVJBResponseCallback?,
                         handlerName: String?) {
        if (data == null && (handlerName == null || handlerName.length == 0))
            return
        val message = WVJBMessage()
        if (data != null) {
            message.data = data
        }
        if (responseCallback != null) {
            val callbackId = "objc_cb_" + ++uniqueId
            responseCallbacks!![callbackId] = responseCallback
            message.callbackId = callbackId
        }
        if (handlerName != null) {
            message.handlerName = handlerName
        }
        queueMessage(message)
    }

    private fun queueMessage(message: WVJBMessage) {
        if (startupMessageQueue != null) {
            startupMessageQueue!!.add(message)
        } else {
            dispatchMessage(message)
        }
    }

    private fun dispatchMessage(message: WVJBMessage) {
//        val messageJSON = message2JSONObject(message).toString()
//                .replace("\\\\", "\\\\\\\\").replace("\"", "\\\\\"")
//                .replace("\'", "\\\\\'").replace("\n", "\\\\\n")
//                .replace("\r", "\\\\\r").replace("\u000C", "\\\\\u000C")
        val messageJSON = message2JSONObject(message).toString()
                .replace("\\\\".toRegex(), "\\\\\\\\").replace("\"".toRegex(), "\\\\\"")
                .replace("\'".toRegex(), "\\\\\'").replace("\n".toRegex(), "\\\\\n")
                .replace("\r".toRegex(), "\\\\\r").replace("\u000C".toRegex(), "\\\\\u000C")
        log("SEND", messageJSON)

        executeJavascript("WebViewJavascriptBridge._handleMessageFromObjC('"
                + messageJSON + "');")
    }

    private fun message2JSONObject(message: WVJBMessage): JSONObject {
        val jo = JSONObject()
        try {
            if (message.callbackId != null) {
                jo.put("callbackId", message.callbackId)
            }
            if (message.data != null) {
                jo.put("data", message.data)
            }
            if (message.handlerName != null) {
                jo.put("handlerName", message.handlerName)
            }
            if (message.responseId != null) {
                jo.put("responseId", message.responseId)
            }
            if (message.responseData != null) {
                jo.put("responseData", message.responseData)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jo
    }

    private fun JSONObject2WVJBMessage(jo: JSONObject): WVJBMessage {
        val message = WVJBMessage()
        try {
            if (jo.has("callbackId")) {
                message.callbackId = jo.getString("callbackId")
            }
            if (jo.has("data")) {
                message.data = jo.get("data")
            }
            if (jo.has("handlerName")) {
                message.handlerName = jo.getString("handlerName")
            }
            if (jo.has("responseId")) {
                message.responseId = jo.getString("responseId")
            }
            if (jo.has("responseData")) {
                message.responseData = jo.get("responseData")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return message
    }

    private fun flushMessageQueue() {
        val script = "WebViewJavascriptBridge._fetchQueue()"
        executeJavascript(script, object : JavascriptCallback {
            override fun onReceiveValue(messageQueueString: String?) {
                if (messageQueueString == null || messageQueueString.length == 0)
                    return
                processQueueMessage(messageQueueString)
            }
        })
    }

    private fun processQueueMessage(messageQueueString: String?) {
        try {
            val messages : JSONArray= JSONArray(messageQueueString)
            for (i in 0 until messages.length()) {
                val jo: JSONObject = messages.getJSONObject(i)

                                log("RCVD", jo);

                val message = JSONObject2WVJBMessage(jo)
                if (message.responseId != null) {
                    val responseCallback = responseCallbacks!!
                            .remove(message.responseId.toString())
                    responseCallback?.callback(message.responseData)
                } else {
                    var responseCallback: WVJBResponseCallback? = null
                    if (message.callbackId != null) {
                        val callbackId = message.callbackId
                        responseCallback = object : WVJBResponseCallback {
                            override fun callback(data: Any?) {
                                val msg = WVJBMessage()
                                msg.responseId = callbackId
                                msg.responseData = data
                                queueMessage(msg)
                            }
                        }
                    }

                    val handler: WVJBHandler?
                    if (message.handlerName != null) {
                        handler = messageHandlers!![message.handlerName.toString()]
                    } else {
                        handler = messageHandler
                    }
                    handler?.request(message.data, responseCallback)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    internal fun log(action: String, json: Any) {
        if (!logging)
            return
        val jsonString = json.toString()
        if (jsonString.length > 500) {
            Log.i(kTag, action + ": " + jsonString.substring(0, 500) + " [...]")
        } else {
            Log.i(kTag, "$action: $jsonString")
        }
    }

    @JvmOverloads
    fun executeJavascript(script: String,
                          callback: JavascriptCallback? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script) { value ->
                var value = value
                if (callback != null) {
                    if (value != null && value.startsWith("\"")
                            && value.endsWith("\"")) {
                        value = value.substring(1, value.length - 1)
                                .replace("\\\\".toRegex(), "")
                    }
                    callback.onReceiveValue(value)
                }
            }
        } else {
            if (callback != null) {
                myInterface.addCallback((++uniqueId).toString() + "", callback)
                webView.loadUrl("javascript:window." + kInterface
                        + ".onResultForScript(" + uniqueId + "," + script + ")")
            } else {
                webView.loadUrl("javascript:$script")
            }
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        try {
            val `is` = webView.context.assets
                    .open("WebViewJavascriptBridge.js.txt")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val js = String(buffer)
            executeJavascript(js)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (startupMessageQueue != null) {
            for (i in startupMessageQueue!!.indices) {
                dispatchMessage(startupMessageQueue!![i])
            }
            startupMessageQueue = null
        }
        super.onPageFinished(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (url.startsWith(kCustomProtocolScheme)) {
            if (url.indexOf(kQueueHasMessage) > 0) {
                flushMessageQueue()
            }
            return true
        }
        return super.shouldOverrideUrlLoading(view, url)
    }

    private inner class WVJBMessage {
        internal var data: Any? = null
        internal var callbackId: String? = null
        internal var handlerName: String? = null
        internal var responseId: String? = null
        internal var responseData: Any? = null
    }

    private inner class MyJavascriptInterface {
        internal var map: MutableMap<String, JavascriptCallback> = HashMap()

        fun addCallback(key: String, callback: JavascriptCallback) {
            map[key] = callback
        }

        @JavascriptInterface
        fun onResultForScript(key: String, value: String) {
            Log.i(kTag, "onResultForScript: $value")
            val callback = map.remove(key)
            callback?.onReceiveValue(value)
        }
    }

    interface JavascriptCallback {
        fun onReceiveValue(value: String?)
    }

    companion object {

        private val kTag = "WVJB"
        private val kInterface = kTag + "Interface"
        private val kCustomProtocolScheme = "wvjbscheme"
        private val kQueueHasMessage = "__WVJB_QUEUE_MESSAGE__"

        private var logging = false
    }


}