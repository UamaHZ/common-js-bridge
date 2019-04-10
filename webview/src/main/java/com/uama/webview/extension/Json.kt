package com.uama.webview.extension


import com.google.gson.Gson

/**
 * Created by liwei on 2018/7/5 9:46
 * Email: liwei@uama.com.cn
 * Description: json 相关的 extension function
 */
/**
 * 将任意对象通过 Gson 转换为 json 字符串
 */
inline fun <reified T : Any> T.toJsonStringByGson(): String = Gson().toJson(this)
