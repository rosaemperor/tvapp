package com.qubuxing.qbx.http;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.qubuxing.qbx.utils.DownCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownHelper {
    private String m_appNameStr = "temp_qubuxing.apk" ;
    private File file;
    public void helpDown(String url, Activity activity ,DownCallback callback){
        OkHttpClient client = new OkHttpClient() ;
        Request request = new Request.Builder().url(url).get().build() ;
        try {
            Response response = client.newCall(request).execute() ;

            if(response.code()!=200)
                throw  new IOException("not 200") ;
            ResponseBody body = response.body() ;
            long length = body.contentLength() ;
            InputStream inputStream  = body.byteStream() ;
            FileOutputStream fileOutputStream = null;
            if (inputStream != null) {
                file = new File(
                        Environment.getExternalStorageDirectory(),
                        m_appNameStr);
                if(file.exists()) file.delete();
                file.createNewFile() ;
                fileOutputStream = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int ch = -1;
                long count = 0;
                while ((ch = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, ch);
                    count += ch;
                    if (length > 0) {
                        final  long rate = (count*100)/length ;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onProgress(""+rate);
                            }
                        });

                    }
                }
                fileOutputStream.flush();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(file);
                    }
                });
            }

            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e) {

            //下载失败，关闭应用
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity,"下载安装包失败，请稍后再试"+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }

    }
    public void down(String url, Activity activity ,DownCallback callback){
        OkHttpClient client = new OkHttpClient() ;
        Request request = new Request.Builder().url(url).get().build() ;
        try {
            Response response = client.newCall(request).execute() ;

            if(response.code()!=200)
                throw  new IOException("not 200") ;
            ResponseBody body = response.body() ;
            long length = body.contentLength() ;
            InputStream inputStream  = body.byteStream() ;
            FileOutputStream fileOutputStream = null;
            if (inputStream != null) {
                File file = new File(
                        activity.getFilesDir(),
                        m_appNameStr);
                if(file.exists()) file.delete();
                if (ActivityCompat.checkSelfPermission(
                        activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                    file.createNewFile() ;
                }else {
                    throw new IOException("have not permission ");
                }

                fileOutputStream = new FileOutputStream(file);
                byte[] buf = new byte[2048];
                int ch = -1;
                long count = 0;
                while ((ch = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, ch);
                    count += ch;
                    if (length > 0) {
                        final  long rate = (count*100)/length ;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                layout.setProgress(rate);
                            }
                        });

                    }
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TAG","done");
                    }
                });
            }
            fileOutputStream.flush();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e) {

            //下载失败，关闭应用
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG","error");

                }
            });
        }
    }
}
