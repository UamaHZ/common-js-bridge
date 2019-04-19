package com.uama.jams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.uama.webview.UamaWebSupportManager;
import com.uama.weight.uama_webview.BridgeWebView;


public class MainActivity extends Activity {
    BridgeWebView bridgeWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bridgeWebView = findViewById(R.id.webView);
        UamaWebSupportManager.Companion.initWebview(this,bridgeWebView);
        bridgeWebView.loadUrl("http://192.168.10.101:8080/#/");
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
