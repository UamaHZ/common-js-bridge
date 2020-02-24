package com.uama.jams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.uama.webview.UamaWebSupportManager;
import com.uama.weight.uama_webview.BridgeWebView;

import uama.share.ShareDialog;
import uama.share.UamaShare;


public class MainActivity extends Activity {
    BridgeWebView bridgeWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UamaShare.setLogEnabled(BuildConfig.DEBUG);
        // 注册微信
        UamaShare.registerWechat(this, "wxc426a5ff52b2bfde");
// 注册 QQ
        UamaShare.registerQQ(this, "1109779379");

//        LMImageUploader.init(new cn.com.uama.imageuploader.Config() {
//            @Override
//            public List<Interceptor> interceptors() {
//                return null;
//            }
//
//            @Override
//            public String uploadUrl() {
//                return "http://yztest.gttis.com.cn:6081/upload";
//            }
//
//            @Override
//            public InputStream trustedCertificatesInputStream() {
//                return null;
//            }
//        });
        bridgeWebView = findViewById(R.id.webView);
        String url = "http://192.168.10.26:8080/";
        UamaWebSupportManager.Companion.initWebview(this,bridgeWebView);
        bridgeWebView.loadUrl(url);

        findViewById(R.id.tx_hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShareDialog(MainActivity.this)
                        // 设置纯图资源 id
                        // 设置网页分享标题
                        .setTitle("title")
                        // 设置网页分享描述
                        .setDescription("des")
                        .showQQ()
                        // 设置网页分享网址
                        .show();

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UamaWebSupportManager.Companion.onActivityResult(requestCode,resultCode,data,this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UamaWebSupportManager.Companion.destroyWebView(bridgeWebView);
    }
}
