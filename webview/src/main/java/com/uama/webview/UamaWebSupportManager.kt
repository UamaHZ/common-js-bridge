package com.uama.webview

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import cn.com.uama.imageuploader.LMImageUploader
import cn.com.uama.imageuploader.UploadListener
import cn.com.uama.imageuploader.UploadType
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.PhoneUtils
import com.google.gson.Gson
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.export.external.interfaces.WebResourceResponse
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.uama.webview.extension.toJsonStringByGson
import com.uama.webview.matisse.ImagePreViewActivity
import com.uama.weight.uama_webview.BridgeHandler
import com.uama.weight.uama_webview.BridgeWebView
import com.uama.weight.uama_webview.BridgeWebViewClient
import com.uama.weight.uama_webview.CallBackFunction
import com.uama.zxing.matisse.GifSizeFilter
import uama.hangzhou.image.album.Matisse
import uama.hangzhou.image.album.MimeType
import uama.hangzhou.image.album.engine.impl.GlideEngine
import uama.hangzhou.image.album.filter.Filter
import uama.hangzhou.image.album.internal.entity.CaptureStrategy
import uama.share.ShareDialog
import uama.share.bean.UamaH5ShareEntity
import uama.share.callback.UamaShareCallback
import java.io.File
import java.io.FileInputStream

/**
 * Author:ruchao.jiang
 * Created: 2019/3/28 19:53
 * Email:ruchao.jiang@uama.com.cn
 */
data class H5ShareEntity(var webpageUrl:String): UamaH5ShareEntity()
class UamaWebSupportManager {
    companion object {
        const val COMMON_RECODE = 2019
        fun getWebResourceResponse(url: String = ""): WebResourceResponse? {
            val webResourceResponse = WebResourceResponse()
            webResourceResponse.encoding = "gzip"
            webResourceResponse.mimeType = "image/png"
            val file = File(getUrlByHtmlPath(url))
            if (!file.exists()) return null
            val fileStream = FileInputStream(file)
            webResourceResponse.data = fileStream
            return webResourceResponse
        }


        private var mFunction: CallBackFunction? = null
        private var choosePicFunc: CallBackFunction? = null

        fun initWebview(activity: Activity, webView: BridgeWebView) {
            val settings = webView.settings
            settings.allowFileAccess = true
            settings.domStorageEnabled = true//允许DCOM
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            val userAgent = settings.userAgentString + "@uama"
            settings.userAgentString = userAgent
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            val webViewClient = object : BridgeWebViewClient(activity, webView) {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    if (request.url?.scheme?.contains("lmimgs") == true) {
                        return getWebResourceResponse(request.url.path ?: "")
                    }
                    return null
                }

                override fun shouldInterceptRequest(view: WebView, urlStr: String): WebResourceResponse? {
                    if (urlStr.contains(PrefixUrl)) {
                        return getWebResourceResponse(urlStr)
                    }
                    return null
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    // 处理拨打电话的 url，以覆盖 BridgeWebViewClient 中的行为
                    // BridgeWebViewClient 中的拨打电话代码需要申请 CALL_PHONE 权限
                    // 之所以没有使用桥方法来拨打电话，是因为在做项目缴费的时候 h5 里面没有使用桥方法
                    // 临近封板才发现拨打电话有问题，如果要改为使用桥方法，前端、Android、iOS 三端都要改代码
                    // 为了最小化影响，暂时这样处理
                    if (url?.contains("tel:") == true) {
                        AlertDialog.Builder(activity)
                                .setMessage("确认拨打此电话?")
                                .setPositiveButton("确定") { _, _ ->
                                    try {
                                        activity.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .setNegativeButton("取消", null)
                                .show()
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }
            webView.webViewClient = webViewClient
            // 拨打电话
            webView.registerHandler("makePhoneCall") { data, _ ->
                val bean : PhoneBean? = Gson().fromJson(data, PhoneBean::class.java)
                AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage("确定拨打${bean?.phoneNumber}?")
                        .setPositiveButton("确定") { _, _ ->
                            PhoneUtils.dial(bean?.phoneNumber)
                        }.setNegativeButton("取消") { _, _ ->
                        }.create()
                        .show()
            }

            // 扫一扫
            webView.registerHandler("scanCode") { data, function ->
                mFunction = function
                if (!PermissionUtils.isGranted(Manifest.permission.CAMERA
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.STORAGE).callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            val intent = Intent(activity, ScanActivity::class.java)
                            activity.startActivityForResult(intent, COMMON_RECODE)
                        }

                        override fun onDenied() {

                        }
                    }).request()
                } else {
                    val intent = Intent(activity, ScanActivity::class.java)
                    activity.startActivityForResult(intent, COMMON_RECODE)
                }
            }


            //发短信
            webView.registerHandler("sendSMG", object : BridgeHandler {
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
                        pick(activity, bean.maxCount ?: 0, when (bean.type) {
                            0 -> true
                            2 -> false
                            else -> false
                        })
                    }
                }
            }


            //图片预览
            webView.registerHandler("previewImage") { data, call ->
                data?.let {
                    val bean: PreViewBean = Gson().fromJson(it, PreViewBean::class.java)
                    val intent = Intent(activity, ImagePreViewActivity::class.java)
                    val list= bean.imageUrls?.map {path->
                        getUrlByHtmlPath(path)
                    }?.toMutableList()
                    val realPreviewBean = PreViewBean(bean.currentIndex,list)
                    intent.putExtra("bean", realPreviewBean)
                    activity.startActivity(intent)
                }
            }

            //分享
            webView.registerHandler("share"){
                data, call ->
                    val  entity:JsShare = Gson().fromJson(data, JsShare::class.java)
                    val type = entity.types
                    if(type?.isEmpty() == true)return@registerHandler
                    val shareDialog = ShareDialog(activity)
                            .setThumbImageUrl(entity.imageUrl)
                            .setDescription(entity.content)
                            .setSmsText(entity.smsMessage)
                            .setTitle(entity.title)
                            .setWebpageUrl(entity.webpageUrl)
                            .setCallback(object:UamaShareCallback{
                                override fun onShareSuccess(shareType: Int) {
                                    call.onCallBack("0")
                                }

                                override fun onShareCanceled(shareType: Int) {
                                    call.onCallBack("2")
                                }

                                override fun onShareError(shareType: Int, errorCode: Int) {
                                    call.onCallBack("1")
                                }
                            })

                    if(type?.contains(1) == true){
                        shareDialog.showQQ()
                    }
                    if(type?.contains(2) == true){
                        shareDialog.showWechat()
                    }
                    if(type?.contains(3) == true){
                        shareDialog.showWechatCircle()
                    }
                    if(type?.contains(4) == true){
                        shareDialog.showSms(true)
                    }
                    shareDialog.show()
            }

            // 网络状态
            webView.registerHandler("_app_getNetstatus") { data, call ->
                val dat = H5RouteUtils._app_getNetstatus()
                val callBa = Gson().toJson(dat)
                call?.onCallBack(callBa)
            }

            class MineUploadListener(val call:CallBackFunction):UploadListener{
                override fun onSuccess(str: String) {
                    call.onCallBack(str)
                }

                override fun onError(p0: String, p1: String) {
                    call.onCallBack("")
                }
            }

            webView.registerHandler("uploadImage"){data,call->
                data?.let {
                    val jsBean = Gson().fromJson(data,JsImageBean::class.java)
                    Log.i("jsBean",jsBean?.uploadUrl)
                    if(jsBean?.imageFilePaths?.isNotEmpty() == true){
                        val imgPaths = jsBean.imageFilePaths.map {path->
                            getUrlByHtmlPath(path)
                        }
                        val type:String = when(jsBean.type?.isNotEmpty()==true) {
                            true-> jsBean.type!!
                            false-> UploadType.USER
                        }
                        if(jsBean.uploadUrl?.isNotEmpty() == true){
                            LMImageUploader.compressAndUpload(jsBean.uploadUrl,activity, imgPaths, type,MineUploadListener(call))
                        }else{
                            LMImageUploader.compressAndUpload(activity, imgPaths,  type, MineUploadListener(call))
                        }
                    }
                }
            }
        }

        const val REQUEST_CODE_CHOOSE = 10800
        private fun pick(activity: Activity, maxNumber: Int = 9, enableCapture: Boolean = true) {
            if (!PermissionUtils.isGranted(PermissionConstants.STORAGE,PermissionConstants.CAMERA)) {
                PermissionUtils.permission(PermissionConstants.STORAGE,PermissionConstants.CAMERA).callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        realPickImage(activity, maxNumber, enableCapture)
                    }

                    override fun onDenied() {

                    }
                }).request()
            } else {
                realPickImage(activity, maxNumber, enableCapture)
            }
        }

        private fun realPickImage(activity: Activity, maxNumber: Int = 9, enableCapture: Boolean = true) {
            Matisse
                    .from(activity)
                    .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.WEBP), false)
                    .countable(true)
                    .capture(enableCapture)
                    .captureStrategy(CaptureStrategy(true, activity.application.packageName + ".provider"))
                    .maxSelectable(maxNumber)
                    .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                    .gridExpectedSize(activity.resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    .thumbnailScale(0.85f)
                    .imageEngine(GlideEngine())    // for glide-V4
                    .forResult(REQUEST_CODE_CHOOSE)
        }

        const val PrefixUrl = "lmimgs://"
        fun getHtmlPathByUrl(url: String) = PrefixUrl + url

        private fun getUrlByHtmlPath(path: String) = path.replace(PrefixUrl, "")

        fun destroyWebView(webView: BridgeWebView?) {
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

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity) {
            when (requestCode) {
                REQUEST_CODE_CHOOSE -> {
                    data?.let {
                        val pathList = Matisse.obtainPathResult(data)?.map { path ->
                            getHtmlPathByUrl(path)
                        }?.toMutableList()
                        if(pathList!=null){
                            choosePicFunc?.onCallBack(UploadPicture(pathList).toJsonStringByGson())
                        }
                    }
                }
                COMMON_RECODE -> {
                    if (resultCode != RESULT_OK) return
                    data?.let {
                        val bundle: Bundle? = it.extras
                        bundle?.let {
                            val result: String? = bundle.getString("result")
                            val scanBean = ScanBean(result)
                            mFunction?.onCallBack(scanBean.toJsonStringByGson())
                        }

                    }
                }
            }
        }

    }

}




