package com.uama.webview

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.PhoneUtils
import com.google.gson.Gson
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.export.external.interfaces.WebResourceResponse
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.uama.webview.extension.toJsonStringByGson
import com.uama.webview.matisse.GifSizeFilter
import com.uama.webview.matisse.Glide4Engine
import com.uama.webview.matisse.ImagePreViewActivity
import com.uama.weight.uama_webview.*
import com.uuzuche.lib_zxing.activity.CaptureActivity
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import com.zhihu.matisse.internal.utils.PathUtils
import java.io.File
import java.io.FileInputStream

/**
 * Author:ruchao.jiang
 * Created: 2019/3/28 19:53
 * Email:ruchao.jiang@uama.com.cn
 */
class UamaWebSupportManager  {
    companion object {
        const val COMMON_RECODE = 2019
        fun getWebResourceResponse(url: String = ""): WebResourceResponse?{
            val webResourceResponse = WebResourceResponse()
            webResourceResponse.encoding = "gzip"
            webResourceResponse.mimeType = "image/png"
            val file = File(getUrlByHtmlPath(url))
            if(!file.exists())return null
            val fileStream = FileInputStream(file)
            webResourceResponse.data = fileStream
            return webResourceResponse
        }


        private var mFunction: CallBackFunction? = null
        private var choosePicFunc:CallBackFunction?=null

        fun initWebview(activity: Activity, webView: BridgeWebView) {
            val settings = webView.settings
            settings.allowFileAccess = true
            settings.domStorageEnabled = true//允许DCOM
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            val webViewClient = object : BridgeWebViewClient(activity, webView) {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    if (request.url?.scheme?.contains("lmimgs") == true) {
                        return getWebResourceResponse(request.url.path?:"")
                    }
                    return null
                }

                override fun shouldInterceptRequest(view: WebView, urlStr: String): WebResourceResponse? {
                    if (urlStr.contains(PrefixUrl)) {
                        return getWebResourceResponse(urlStr)
                    }
                    return null
                }
            }
            webView.webViewClient = webViewClient
            webView.webChromeClient = BridgeWebChromeClient(object : BridgeWebChromeClient.FileChooserCallback {
                override fun showFileChooserUris(valueCallback: ValueCallback<Array<Uri>>) {

                }

                override fun showFileChooserUri(valueCallback: ValueCallback<Uri>) {

                }
            })
            webView.webChromeClient = BridgeWebChromeClient(object : BridgeWebChromeClient.FileChooserCallback {
                override fun showFileChooserUris(valueCallback: ValueCallback<Array<Uri>>) {

                }

                override fun showFileChooserUri(valueCallback: ValueCallback<Uri>) {

                }
            })

            // 拨打电话
            webView.registerHandler("_app_tel") { data, _ ->
                AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage("确定拨打$data?")
                        .setPositiveButton("确定"){_,_->
                            PhoneUtils.dial(data)
                        }.setNegativeButton("取消"){_,_->
                        }.create()
                        .show()
            }

            // 扫一扫
            webView.registerHandler("_app_scan") { data, function ->
                mFunction = function
                if(!PermissionUtils.isGranted(Manifest.permission.CAMERA
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.READ_EXTERNAL_STORAGE)){
                    PermissionUtils.permission(PermissionConstants.CAMERA,PermissionConstants.STORAGE).callback(object: PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            val intent = Intent(activity, CaptureActivity::class.java)
                            activity.startActivityForResult(intent, COMMON_RECODE)
                        }

                        override fun onDenied() {

                        }
                    }).request()
                }else{
                    val intent = Intent(activity, CaptureActivity::class.java)
                    activity.startActivityForResult(intent, COMMON_RECODE)
                }
            }


            //发短信
            webView.registerHandler("_app_sendSMG", object : BridgeHandler {
                override fun handler(data: String?, call: CallBackFunction?) {
                    data?.let {
                        val bean: DialBean? = Gson().fromJson(it, DialBean::class.java)
                        PhoneUtils.sendSms(bean?.phone ?: "", bean?.text ?: "")
                    }
                    //
                }
            })


            //选择图片:此处存在只拍照，需要修改逻辑
            webView.registerHandler("chooseImage") { data, call ->
                data?.let {
                    val bean: PickBean? = Gson().fromJson(it, PickBean::class.java)
                    bean?.let {
                        choosePicFunc = call
                        pick(activity,bean.maxCount?:0,when(bean.type){
                            0->true
                            2->false
                            else->false
                        })
                    }
                }
            }


            //图片预览
            webView.registerHandler("_app_previewPic") { data, call ->
                data?.let {
                    val bean: PreViewBean = Gson().fromJson(it, PreViewBean::class.java)
                    val intent = Intent(activity, ImagePreViewActivity::class.java)
                    intent.putExtra("bean", bean)
                    activity.startActivity(intent)
                }
            }


            //webView.registerHandler("_app_getNetstatus",hand)


            // 网络状态
            webView.registerHandler("_app_getNetstatus", object : BridgeHandler {
                override fun handler(data: String?, call: CallBackFunction?) {
                    val dat = H5RouteUtils._app_getNetstatus()
                    val callBa = Gson().toJson(dat)
                    call?.onCallBack(callBa)
                }
            })

        }

        const val REQUEST_CODE_CHOOSE = 10800
        private fun pick(activity: Activity, maxNumber:Int = 9, enableCapture:Boolean = true) {
            if (!PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE
                            , Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PermissionUtils.permission(PermissionConstants.STORAGE).callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        realPickImage(activity, maxNumber,enableCapture)
                    }

                    override fun onDenied() {

                    }
                }).request()
            } else {
                realPickImage(activity, maxNumber,enableCapture)
            }
        }

        private fun realPickImage(activity: Activity,maxNumber:Int = 9,enableCapture:Boolean = true){
            Matisse
                    .from(activity)
                    .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.WEBP), false)
                    .countable(true)
                    .capture(enableCapture)
                    .captureStrategy(CaptureStrategy(true, BuildConfig.APPLICATION_ID,"release"))
                    .maxSelectable(maxNumber)
                    .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                    .gridExpectedSize(activity.resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    .thumbnailScale(0.85f)
                    .imageEngine(Glide4Engine())    // for glide-V4
                    .forResult(REQUEST_CODE_CHOOSE)
        }
        const val PrefixUrl = "lmimgs://"
        fun getHtmlPathByUrl(url:String)= PrefixUrl+url

        private fun getUrlByHtmlPath(path:String) = path.replace(PrefixUrl,"")

        fun destroyWebView(webView: BridgeWebView?){
            if (webView != null) {
                try {
                    webView.onPause()
                    webView.removeAllViews()
                    webView.destroy()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?,activity: Activity) {
            when (requestCode) {
                REQUEST_CODE_CHOOSE -> {
                    data?.let {
                        val selectList = Matisse.obtainResult(data)
                        val pathList = selectList.map {uri->
                            getHtmlPathByUrl(PathUtils.getPath(activity,uri))
                        }.toMutableList()
                        choosePicFunc?.onCallBack(UploadPicture(pathList).toJsonStringByGson())
                    }
                }
                COMMON_RECODE -> {
                    data?.let {
                        val bundle: Bundle? = it.extras
                        bundle?.let {
                            if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                                val result: String? = bundle.getString(CodeUtils.RESULT_STRING)
                                val scanBean = ScanBean(result)
                                mFunction?.onCallBack(scanBean.toJsonStringByGson())
                            } else {
                                Toast.makeText(activity, "解析二维码失败", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                }
            }
        }

    }

}




