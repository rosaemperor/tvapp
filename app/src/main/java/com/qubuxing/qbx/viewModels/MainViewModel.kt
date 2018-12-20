package com.qubuxing.qbx.viewModels

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.util.Log
import android.view.View
import android.widget.Toast
import com.qubuxing.qbx.BaseViewModel
import com.qubuxing.qbx.BuildConfig
import com.qubuxing.qbx.QBXApplication
import com.qubuxing.qbx.UrlCallback
import com.qubuxing.qbx.databinding.ActivityMainBinding
import com.qubuxing.qbx.http.RetrofitUtil
import com.qubuxing.qbx.http.beans.UpdateEntity
import com.qubuxing.qbx.http.beans.UpdateResultEntity
import com.qubuxing.qbx.utils.DialogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainViewModel : BaseViewModel(){
    var webViewVisiable : ObservableField<Int> = ObservableField()
    var errorViewVisiable :  ObservableField<Int> = ObservableField()
    var floatButtonVisibale : ObservableField<Int> = ObservableField()
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


}