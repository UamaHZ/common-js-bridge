package com.uama.webview

import android.app.Activity
import android.content.Intent
import com.uama.zxing.CaptureBaseActivity
import com.uama.zxing.OnScanResult

class ScanActivity: CaptureBaseActivity(){
    override fun getOnScanResult() = OnScanResult { _, code ->
        val bundle = Intent()
        bundle.putExtra("result",code)
        setResult(Activity.RESULT_OK,bundle)
        finish()
    }
}