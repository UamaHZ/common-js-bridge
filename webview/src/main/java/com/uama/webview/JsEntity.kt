package com.uama.webview

import java.io.Serializable
import java.lang.StringBuilder

data class NetStatus(var netType:Int,var downLoadSpeed:Long =0L ,var uploadSpeed:Long = 0L)

data class PhoneBook(var name:String?,var phoneList:MutableList<String>?)

fun MutableList<String>?.getPhoneNumber():String{
    val sb = StringBuilder()
    if(!this.isNullOrEmpty()){
        forEach {
            sb.append(it )
        }
    }
    return sb.toString()
}

data class DialBean(var phone:String?,var text:String?)

data class ScanBean(var content:String?)

data class PickBean(var type:Int?,var maxCount:Int?,var cbName:String?)

data class UploadPicture(var tempFilePaths:MutableList<String>)

data class PreViewBean(var currentIndex:Int?,var imageUrls:MutableList<String>?): Serializable