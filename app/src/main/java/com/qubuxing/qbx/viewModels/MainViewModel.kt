package com.qubuxing.qbx.viewModels

import android.app.Activity
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.v7.app.AppCompatActivity
import android.text.PrecomputedText
import android.util.ArrayMap
import android.util.Log
import android.view.View
import android.widget.Toast
import com.kf5.sdk.im.ui.KF5ChatActivity
import com.kf5.sdk.system.entity.Field
import com.kf5.sdk.system.entity.ParamsKey
import com.kf5.sdk.system.init.UserInfoAPI
import com.kf5.sdk.system.internet.HttpRequestCallBack
import com.kf5.sdk.system.utils.SPUtils
import com.kf5.sdk.system.utils.SafeJson
import com.qubuxing.qbx.*
import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.http.RetrofitUtil
import com.qubuxing.qbx.http.beans.UpdateEntity
import com.qubuxing.qbx.http.beans.UpdateResultEntity
import com.qubuxing.qbx.utils.DialogUtils
import com.qubuxing.qbx.utils.KFUtils.Preference
import com.qubuxing.qbx.utils.KFUtils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.SoftReference


class MainViewModel : BaseViewModel(){
    var webViewVisiable : ObservableField<Int> = ObservableField()
    var errorViewVisiable :  ObservableField<Int> = ObservableField()
    var floatButtonVisibale : ObservableField<Int> = ObservableField()
    lateinit var activity : AppCompatActivity
    override fun initViewModel() {
        webViewVisiable.set(View.VISIBLE)
        errorViewVisiable.set(View.GONE)
        if (BuildConfig.DEBUG) floatButtonVisibale.set(View.VISIBLE)else floatButtonVisibale.set(View.GONE)
    }

    override fun initData() {

    }

    fun getUpdateMessage(context: Context){
        var updateEntity  = UpdateEntity()
        var applicationInfo = QBXApplication.instance.packageManager.getApplicationInfo(QBXApplication.instance.packageName ,
                PackageManager.GET_META_DATA)
        var packageInfo = QBXApplication.instance.packageManager.getPackageInfo(QBXApplication.instance.packageName,0)
        updateEntity.verName = packageInfo.versionName
        updateEntity.verCode = ""+packageInfo.versionCode
        updateEntity.channelCode = applicationInfo.metaData.getString("JPUSH_CHANNEL")
        var call = RetrofitUtil.instance.help.checkUpdate(updateEntity)
        call.enqueue(object : Callback<UpdateResultEntity>{
            override fun onFailure(call: Call<UpdateResultEntity>, t: Throwable) {

            }

            override fun onResponse(call: Call<UpdateResultEntity>, response: Response<UpdateResultEntity>) {
                if (response.body() != null){
                    DialogUtils.showUpdateDia(context,response.body() as UpdateResultEntity)
                }

            }
        })

    }
    fun setURL(view : View){
        Toast.makeText(view.context, "请输入本地ip加端口号",Toast.LENGTH_LONG).show()
        DialogUtils.showSetUrlDialog(view.context, object : UrlCallback{
            override fun goUrl(url: String) {
                var goUrl = url
                if(!url.startsWith("http" )){
                    goUrl = "http://$url"
                }
                var binding = DataBindingUtil.findBinding<ActivityMainBinding>(view)

                binding!!.webView.loadUrl(goUrl)
            }
        })
    }

    fun checkKFLoginStatus(activity : AppCompatActivity, userName : String , email : String){
        if(!Preference.getBoolLogin(activity)){
            this.activity = activity
            var map = ArrayMap<String , String>()
            map.put(ParamsKey.EMAIL,"${email}")
            SPUtils.saveAppID(config.KFAPPID)
            SPUtils.saveHelpAddress(config.KFHelpAdress)
            map.put(ParamsKey.USER_FIELDS , "")
            SPUtils.saveUserAgent(Utils.getAgent(SoftReference<Context>(activity)))
            UserInfoAPI.getInstance().createUser(map , object : HttpRequestCallBack{
                override fun onSuccess(p0: String?) {
                   var jsonObject = SafeJson.parseObj(p0)
                    var resultCode = SafeJson.safeInt(jsonObject, "error")
                    if(resultCode == 0){
                        Preference.saveBoolLogin(activity, true)
                        var dataObj = SafeJson.safeObject(jsonObject, Field.DATA)
                        var userObj = SafeJson.safeObject(dataObj , Field.USER)
                        if(null !=userObj){
                            var userToken = userObj.getString(Field.USERTOKEN)
                            var  id =  userObj.getInt(Field.ID)
                            SPUtils.saveUserToken(userToken)
                            SPUtils.saveUserId(id)
                            saveToken(map)
                        }
                    }else{
                        loginUser(map, activity)
                    }
                }

                override fun onFailure(p0: String?) {
                    Log.d("TAG","$p0")
                }
            })

        }else{
            var intent = Intent(activity , KF5ChatActivity::class.java)
            activity.startActivity(intent)
            Log.d("TAG","客服已登陆")
        }


    }
    fun loginUser(map : ArrayMap<String ,String>, activity : AppCompatActivity){
        UserInfoAPI.getInstance().loginUser(map, object : HttpRequestCallBack{
            override fun onSuccess(p0: String?) {
                var jsonObject = SafeJson.parseObj(p0)
                var resultCode = SafeJson.safeInt(jsonObject, "error")
                if(resultCode == 0){
                    Preference.saveBoolLogin(activity, true)
                    var dataObj = SafeJson.safeObject(jsonObject, Field.DATA)
                    var userObj = SafeJson.safeObject(dataObj , Field.USER)
                    if(null !=userObj){
                        var userToken = userObj.getString(Field.USERTOKEN)
                        var  id =  userObj.getInt(Field.ID)
                        SPUtils.saveUserToken(userToken)
                        SPUtils.saveUserId(id)
                        saveToken(map)
                    }
                }else{
                    loginUser(map,activity)
                }            }

            override fun onFailure(p0: String?) {
            }
        })
    }


    fun saveToken(map: ArrayMap<String, String>) {
        map[ParamsKey.DEVICE_TOKEN] = "123456"
        UserInfoAPI.getInstance().saveDeviceToken(map, object : HttpRequestCallBack {
            override fun onSuccess(result: String) {
                Log.i("kf5测试", "保存设备Token成功$result")
                var intent = Intent(activity , KF5ChatActivity::class.java)
                activity.startActivity(intent)
            }

            override fun onFailure(result: String) {
                Log.i("kf5测试", "保存设备Token失败$result")
            }
        })

        UserInfoAPI.getInstance().getUserInfo(map, object : HttpRequestCallBack {
            override fun onSuccess(result: String) {

            }

            override fun onFailure(result: String) {

            }
        })
    }
}