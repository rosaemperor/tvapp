package com.qubuxing;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.qubuxing.qbx.databinding.ActivityThirdBrowserBinding;

import com.qubuxing.qbx.BuildConfig;
import com.qubuxing.qbx.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by administrator on 2018/3/15.
 */

public class ThridBroeserActivity extends AppCompatActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener{
    private ActivityThirdBrowserBinding binding;
    private boolean canScroll = false;
    private Timer timer;
    private WebResourceResponse  responseResource;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding= DataBindingUtil.setContentView(this, R.layout.activity_third_browser);
        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        if(null ==bundle){
            finish();
        }
        timer = new Timer();
        binding.idSwipeLy.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        binding.idSwipeLy.setOnRefreshListener(this);
        binding.idSwipeLy.setEnabled(false);
        binding.llNetworkError.setVisibility(View.GONE);
        binding.mainWebview.setVisibility(View.VISIBLE);
        binding.mainWebview.setWebChromeClient(new WebChromeClient());
        binding.setEvent(this);

        binding.mainWebview.setWebViewClient(new ThridWebViewClient());
        if(BuildConfig.DEBUG && Build.VERSION.SDK_INT > 18){
            binding.mainWebview.setWebContentsDebuggingEnabled(true);
        }
        binding.mainWebview.getSettings().setSupportZoom(true);
        binding.mainWebview.getSettings().setBuiltInZoomControls(true);
        binding.mainWebview.getSettings().setDisplayZoomControls(true);
        binding.mainWebview.getSettings().setDomStorageEnabled(true);
        binding.mainWebview.getSettings().setAppCacheEnabled(true);
        binding.mainWebview.getSettings().setTextZoom(100);
        binding.mainWebview.getSettings().setAllowFileAccess(true);
        binding.mainWebview.getSettings().setJavaScriptEnabled(true);
        binding.mainWebview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        Log.d("TAG",""+bundle.getString("url"));
        binding.mainWebview.loadUrl(bundle.getString("url"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                if(binding.mainWebview.canGoBack()){
                    binding.mainWebview.goBack();
                }else {
                    finish();
                }

                break;
            case R.id.btn_reload:
                binding.mainWebview.reload();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        if (binding.mainWebview != null && binding.mainWebview.getScrollY() == 0)

            binding.mainWebview.loadUrl(binding.mainWebview.getUrl());
        else
            binding.idSwipeLy.setRefreshing(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(binding.mainWebview.canGoBack()){
                binding.mainWebview.goBack();
            }else {
                finish();
            }
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class ThridWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            binding.progressBar.setVisibility(View.GONE);
            if (url != null && url.startsWith("file")) {
                binding.llNetworkError.setVisibility(View.VISIBLE);
            } else {
                binding.llNetworkError.setVisibility(View.INVISIBLE);
            }
            if (binding.idSwipeLy != null && binding.idSwipeLy.isRefreshing())
                binding.idSwipeLy.setRefreshing(false);}
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            binding.progressBar.setVisibility(View.VISIBLE);

            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    //执行超时
                }
            };
            timer.schedule(tt, 1000 * 10);
        }



        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            networkError(view);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (request.getUrl().toString().equals(view.getUrl()))
                    networkError(view);
            } else {
                networkError(view);
            }

        }

        private void networkError(WebView view) {
            view.loadUrl("file:///android_asset/error.html");
            view.setVisibility(View.INVISIBLE);
            binding.llNetworkError.setVisibility(View.VISIBLE);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//             responseResource =shouldInterceptRequest(view, request);
//            if(responseResource.getStatusCode() == 302){
//                return true;
//            }
            if (view.getUrl().startsWith("weixin") ) {
                try {
                    // 以下固定写法
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(view.getUrl()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    view.getContext().startActivity(intent);
                    finish();
                } catch (ActivityNotFoundException e) {
                    // 防止没有安装的情况
                    e.printStackTrace();
                    Toast.makeText(view.getContext(),"请先安装手淘APP！",Toast.LENGTH_LONG).show();
                }
                return true;

            }

            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("weixin") ) {
                try {
                    // 以下固定写法
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    view.getContext().startActivity(intent);
                    finish();
                } catch (ActivityNotFoundException e) {
                    // 防止没有安装的情况
                    e.printStackTrace();
                    Toast.makeText(view.getContext(),"请先安装手淘APP！",Toast.LENGTH_LONG).show();
                }
                return true;

            }

            return super.shouldOverrideUrlLoading(view, url);
        }

//        @androidx.annotation.Nullable
//        @Nullable
//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//            return super.shouldInterceptRequest(view, request);
//        }
    }
}
