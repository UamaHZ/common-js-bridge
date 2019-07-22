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
        String url = "http://192.168.10.94:8080/#/visitor?subCode=101&subCommunityId=c7199ba9-0a1f-4a06-b8bf-0bf453c3aa31&mobileNo=862266035458821&mobileType=152&time=1561619010&defCommunityId=4829905d-cb9e-11e8-8a58-506b4b417204&token=f0d030e9-679b-48df-a179-7eea0f8c72be&defAreaCode=330106&defOrgId=306&companyCode=mh&defRoomId=16306773&mobileName=Vivo%20X7&appVersion=2.2.11_A1&defLongitude=120.124823&version=2&defLatitude%3A%2030.276971&mobileVersion=5.1.1%22";
        UamaWebSupportManager.Companion.initWebview(this,bridgeWebView);
        bridgeWebView.loadUrl(url);
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
