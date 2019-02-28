package com.qubuxing.qbx.utils

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.provider.SyncStateContract.Helpers.update
import android.support.constraint.ConstraintLayout
import android.support.v4.content.FileProvider
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.qubuxing.qbx.R
import com.qubuxing.qbx.UrlCallback
import com.qubuxing.qbx.config
import com.qubuxing.qbx.databinding.DialogLocationRequestBinding
import com.qubuxing.qbx.http.DownHelper
import com.qubuxing.qbx.http.RetrofitUtil
import com.qubuxing.qbx.http.beans.UpdateResultEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class DialogUtils {

    companion object {
        var hasDialogShowing = false
//          var locationDialog : Dialog? = null
        var updateDialog : Dialog? = null
        lateinit var mContext : Context
        var file : File? =null
        var m_appNameStr = "temp_qubuxing.apk"
        fun showUpdateDia( context: Context , updateResult : UpdateResultEntity) {
            mContext = context
            var rate : Long = 0
            updateDialog = Dialog(context,R.style.update_dialog)
            val layout = LayoutInflater.from(context).inflate(R.layout.dia_update_layout, null) as ConstraintLayout
            var description = layout.findViewById<TextView>(R.id.description)
            var updateView = layout.findViewById<TextView>(R.id.update_view)
            var progressBar = layout.findViewById<ProgressBar>(R.id.progress_bar)
            var downLayout = layout.findViewById<LinearLayout>(R.id.down_layout)
            var pers = layout.findViewById<TextView>(R.id.pers)
            var startNow = layout.findViewById<TextView>(R.id.start)
            description.text = updateResult.description
            updateView.setOnClickListener {
                updateView.visibility = View.GONE
                downLayout.visibility = View.VISIBLE
                var thread = Thread(Runnable {
                    var downHelper = DownHelper()
                    downHelper.helpDown(updateResult.releasePackageUrl, mContext as Activity , object : DownCallback{
                        override fun onProgress(currentLength: String?) {
                            progressBar.progress = currentLength!!.toInt()
                                    pers.text = "$currentLength%"
       }

                        override fun onFinish(file: File?) {
                            startNow.visibility = View.VISIBLE
                            startNow.setOnClickListener {
                                junmUpdate(file!!)
                            }
                            junmUpdate(file!!)
                        }

                        override fun onFailure() {

                        }
                    })



//                    var client = OkHttpClient()
//                    var request = Request.Builder().url("http://p.minrui.zj.cn/static/release/ybqb.apk").get().build()
//                    var response = client.newCall(request).execute()
//
//                    if(response.code() != 200){
//                        throw IOException("this is a exception from native")
//                    }
//                    val body = response.body() as ResponseBody
//                    val length = body!!.contentLength()
//                    val inputStream = body.byteStream()
//                    var fileOutputStream: FileOutputStream? = null
//                    if (inputStream != null) {
//                        file = File(
//                                context.filesDir,
//                                m_appNameStr)
//                        if (file!!.exists()) file!!.delete()
//                        file!!.createNewFile()
//                        fileOutputStream = FileOutputStream(file)
//                        val buf = ByteArray(1024)
//                        var ch = -1
//                        var count: Long = 0
//                        inputStream.read(buf)
//
//                        do {
//                            ch = inputStream!!.read(buf)
////                            if(ch ==-1) break
//                            fileOutputStream.write(buf, 0, ch)
//                            count += ch.toLong()
//                            if (length > 0) {
//                                rate = count * 100 / length
//                                (context as Activity).runOnUiThread {
//                                    progressBar.progress = rate.toInt()
//                                    pers.text = "$rate%"}
//
//                            }
//                        }while (inputStream.read(buf) != -1)
////                        while ( inputStream.read(buf) != -1) {
////                            fileOutputStream.write(buf, 0, ch)
////                            count += ch.toLong()
////                            if (length > 0) {
////                                val rate = count * 100 / length
////                                (context as Activity).runOnUiThread {
////                                    progressBar.progress = rate.toInt()
////                                    pers.text = "$rate%"}
////
////                            }
////                        }
//                        (context as Activity).runOnUiThread{
//                            downLayout.visibility = View.GONE
//                            startNow.visibility = View.VISIBLE
//                            startNow.setOnClickListener {
//                            junmUpdate()
//                        }}
//                    }
//                    fileOutputStream!!.flush()
                })
                thread.start()
//                Toast.makeText(updateView.context,"更新更新",Toast.LENGTH_LONG).show()



            }
//
            updateDialog!!.setContentView(layout)
            updateDialog!!.setCancelable(false)
            if ( !updateDialog!!.isShowing) {
//                locationDialog?.let {
//                    if (locationDialog!!.isShowing) locationDialog!!.dismiss()
//                }
                updateDialog!!.show()
                hasDialogShowing = false
            }


        }
        private fun junmUpdate() {
            val intent = Intent(Intent.ACTION_VIEW)
            var fileUri : Uri
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(mContext, "com.qubuxing.qbx.fileprovider", file!!)
            } else {
                fileUri = Uri.fromFile(file)
            }
            intent.setDataAndType(fileUri,
                    "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            (mContext as Activity).startActivity(intent)
        }
        private fun junmUpdate(downFile : File) {
            val intent = Intent(Intent.ACTION_VIEW)
            var fileUri : Uri
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(mContext, "com.qubuxing.qbx.fileprovider", downFile!!)
                intent.data = fileUri
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            } else {
                fileUri = Uri.fromFile(downFile)
                intent.setDataAndType(fileUri,
                        "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            Log.d("Uri","${fileUri}")

            (mContext as Activity).startActivity(intent)
        }



        fun showSetUrlDialog(context: Context, click : UrlCallback){
            val urlDialog = Dialog(context,R.style.update_dialog)
            val layout = LayoutInflater.from(context).inflate(R.layout.layout_set_url_dialog, null) as ConstraintLayout
            urlDialog.setContentView(layout)
            urlDialog.setCancelable(true)
            var goUrl = layout.findViewById<TextView>(R.id.update_view)
            var urlEdit = layout.findViewById<EditText>(R.id.edit_url)

            goUrl.setOnClickListener{
                var url = urlEdit.text.toString()
                if(url.equals("")){
                    Toast.makeText(context, "请输入本地ip加端口号",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                Log.d("TAG","$url")
                click.goUrl(url)
                urlDialog.dismiss()
                hasDialogShowing = false
            }
            if(!urlDialog.isShowing &&!hasDialogShowing){
                urlDialog.show()
                hasDialogShowing = true
            }
        }


        fun showLocationRequestDialog(context: Context){
           var  locationDialog= Dialog(context,R.style.update_dialog)
            val layout = LayoutInflater.from(context).inflate(R.layout.dialog_location_request, null) as ConstraintLayout
            locationDialog!!.setContentView(layout)
            locationDialog!!.setCancelable(false)
            val cancelView = layout.findViewById<TextView>(R.id.cancel_view)
            val clickView  = layout.findViewById<TextView>(R.id.click_view)
            cancelView.setOnClickListener {
                locationDialog!!.dismiss()
                hasDialogShowing = false
            }
            clickView.setOnClickListener {
                var locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                (context as Activity).startActivityForResult(locationIntent , config.GPS_REQUEST_CODE)
            }

            if(!locationDialog!!.isShowing && !hasDialogShowing){
                locationDialog!!.show()
                hasDialogShowing = true
            }
        }

    }





}