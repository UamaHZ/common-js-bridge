package com.uama.jams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.uama.webview.UamaWebSupportManager;
import com.uama.weight.uama_webview.BridgeWebView;

import java.io.InputStream;
import java.util.List;

import cn.com.uama.imageuploader.LMImageUploader;
import okhttp3.Interceptor;


public class MainActivity extends Activity {
    BridgeWebView bridgeWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LMImageUploader.init(new cn.com.uama.imageuploader.Config() {
            @Override
            public List<Interceptor> interceptors() {
                return null;
            }

            @Override
            public String uploadUrl() {
                return "http://yztest.gttis.com.cn:6081/upload";
            }

            @Override
            public InputStream trustedCertificatesInputStream() {
                return null;
            }
        });
        bridgeWebView = findViewById(R.id.webView);
        String a= "mobileNo=862885031707566&mobileType=132&time=1561619010&defCommunityId=f3225269-8826-11e9-8a58-506b4b417204&token=579bf0e5-ca6b-4d23-b707-d5a1a73c675e&defAreaCode=330106&defOrgId=236&companyCode=yz&defRoomId=16311456&mobileName=Vivo X7&appVersion=2.2.11_A1&defLongitude=120.124823&version=2&defLatitude: 30.276971&mobileVersion=5.1.1";
        UamaWebSupportManager.Companion.initWebview(this,bridgeWebView);
        bridgeWebView.loadUrl("http://192.168.10.26:8080/#/reportInput?"+a);
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
