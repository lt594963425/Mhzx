package com.tao.mhzx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private String url = "http://106.53.72.109:2020/";
    ProgressBar webViewProgressBar;

    WebView bridgeWebView;
    LinearLayout error_view;
    boolean loadError = false;

    private BaseLoadingDialog dialog;


    public void showPDLoading(String s) {
        if (dialog != null && dialog.isShowing()) return;
        dialog = new BaseLoadingDialog(this);
        dialog.show();
    }

    public void dismissLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadError = false;
        error_view = findViewById(R.id.error_view);
        webViewProgressBar = findViewById(R.id.webView_progressBar);
        bridgeWebView = findViewById(R.id.bridgeWebView);
        TextView refresh_tv = findViewById(R.id.refresh_tv);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int heightPixels = outMetrics.heightPixels;
        bridgeWebView.getLayoutParams().height = (int) (heightPixels * 0.88);
        bridgeWebView.requestLayout();
        requestPermissions();
        showPDLoading("");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        refresh_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bridgeWebView != null) {
                    loadError = false;
                    showPDLoading("");
                    bridgeWebView.clearCache(true);
                    bridgeWebView.reload();
                }
            }
        });

        bridgeWebView.clearCache(true);

        bridgeWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //启用支持javascript
        WebSettings settings = bridgeWebView.getSettings();
        settings.setJavaScriptEnabled(true);
//        settings.setSupportZoom(true);// 设置可以支持缩放
//        settings.setBuiltInZoomControls(true);// 设置出现缩放工具 是否使用WebView内置的缩放组件，由浮动在窗口上的缩放控制和手势缩放控制组成，默认false
//        settings.setDisplayZoomControls(false);//隐藏缩放工具
//        settings.setUseWideViewPort(true);// 扩大比例的缩放
//        settings.setDomStorageEnabled(true);//
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        bridgeWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        bridgeWebView.loadUrl(url);
        bridgeWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (webViewProgressBar != null) {
                    webViewProgressBar.setProgress(newProgress);
                    if (newProgress == 100) {
                        webViewProgressBar.setVisibility(View.GONE);
                    }
                }

            }
        });
        bridgeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //url重定向问题

                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return super.shouldInterceptRequest(view, url);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    if (bridgeWebView != null) {
                        if (!bridgeWebView.getSettings().getLoadsImagesAutomatically()) {
                            bridgeWebView.getSettings().setLoadsImagesAutomatically(true);
                        }
                    }
                    if (webViewProgressBar != null) {
                        webViewProgressBar.setVisibility(View.GONE);
                    }
                    dismissLoading();
                    if (!loadError) {
                        error_view.setVisibility(View.GONE);
                        bridgeWebView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                try {
                    webViewProgressBar.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                }
                view.setVisibility(View.INVISIBLE);
                error_view.setVisibility(View.VISIBLE);
                loadError = true;
            }
        });
        bridgeWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String paramAnonymousString1, String paramAnonymousString2, String paramAnonymousString3, String paramAnonymousString4, long paramAnonymousLong) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(paramAnonymousString1));
                startActivity(intent);
            }
        });

        bridgeWebView.setVerticalScrollBarEnabled(true);
    }

    private RxPermissions mRxPermissions;

    @SuppressLint("CheckResult")
    private void requestPermissions() {
        mRxPermissions = new RxPermissions(this);
        mRxPermissions.requestEach(
                Manifest.permission.INTERNET,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {

                        } else if (permission.shouldShowRequestPermissionRationale) {

                        } else {

                        }
                    }
                });

    }
}
