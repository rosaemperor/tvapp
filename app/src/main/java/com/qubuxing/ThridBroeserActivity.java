package com.qubuxing;

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
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding= DataBindingUtil.setContentView(this, R.layout.activity_third_browser);
        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        if(null ==bundle){
            finish();
        }
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
                finish();
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
        if(event.getAction() == KeyEvent.KEYCODE_BACK){
            Log.d("url",""+binding.mainWebview.getUrl());
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
            Timer timer = new Timer();
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
    }
}
