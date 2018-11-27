package com.qubuxing.qbx

import android.Manifest
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.qubuxing.qbx.QBXApplication
import com.qubuxing.qbx.http.RetrofitUtil
import org.greenrobot.eventbus.EventBus
import java.util.ArrayList


abstract class BaseActivity : AppCompatActivity(){
    lateinit var httpHelp : HttpService
    internal var PERMISSION_REQUEST_CODE = 101

    internal var needPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViewModel()
        httpHelp = RetrofitUtil.instance.help
        EventBus.getDefault().register(this)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

    }

    abstract fun initBinding()


    abstract fun initViewModel()

    //直接获取对应的viewmodel
    fun <T : ViewModel> getViewModel(activity : BaseActivity,modelClass : Class<T>) : T {
        return ViewModelProviders.of(this).get(modelClass)
    }

    fun checkPermission(permission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), PERMISSION_REQUEST_CODE)
            }
        }
    }

    fun checkPermissions( permissions: ArrayList<String>) {

        if (permissions == null) return
        val needGrant = ArrayList<String>()
        for (i in permissions.indices) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    needGrant.add(permissions[i])
                }
            }
        }
        var ps: Array<String>
        ps = needGrant.toTypedArray()
        if (ps.isNotEmpty()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(ps, PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}