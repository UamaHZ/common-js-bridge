package com.uama.webview;

import android.support.v4.content.FileProvider;

/**
 * Created by ruchao.jiang on 2018/1/17.
 * Android 从 N 开始不允许以 file:// 的方式通过 Intent 在两个 App 之间分享文件，
 *取而代之的是通过 FileProvider 生成 content://Uri 。如果在 Android N 以上的版本继续使用
 *file:// 的方式分享文件，则系统会直接抛出异常，导致 App 出现 Crash
 */

public class MatisseCaptureProvider extends FileProvider {}
