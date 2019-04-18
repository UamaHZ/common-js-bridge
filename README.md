# common-js-bridge

[![](https://jitpack.io/v/UamaHZ/common-js-bridge.svg)](https://jitpack.io/#UamaHZ/common-js-bridge)

封装一些原生能力供`js`调用：

- 图片选择及预览
- 拨打电话
- 扫一扫
- 发送短信
- 获取网络状态

如何使用？

我们只要使用`UamaWebSupportManager`这个类提供的相关静态方法即可（代码由kotlin书写）:

**注意，由于我们的webview使用的远程x5版本，暂时不支持64位处理器，所以我们需要对App限制降级到32位运行；处理方案如下（[x5说明](https://x5.tencent.com/tbs/technical.html#/detail/sdk/1/34cf1488-7dc2-41ca-a77f-0014112bcab7)）：**

1. 打开对应module中的build.gradle文件,在文件的android{}中的defaultConfig{}里(如果没有defaultConfig{}则手动添加)添加如下配置: ndk{abiFilters "armeabi"}，如果配置后编译报错，那么需要在gradle.properties文件中加上Android.useDeprecatedNdk=true；
2. 找出build.gradle中配置的so加载目录:jniLibs.srcDir:customerDir,如果没有该项配置则so加载目录默认为：src/main/jniLibs，需要将.so文件都放置在so加载目录的armeabi文件夹下(没有该目录则新建一个，AP中没有使用到.so文件则需要拷贝任意一个32位的so文件到该目录下，如果没有合适的so可以到官网http://x5.tencent.com/tbs/sdk.html下载官网“SDK接入示例“,拷贝对应目录下的liblbs.so文件)，so加载目录下不要有其他以”armeabi“开头的文件夹。如果仍未能解决您的问题，请直接在论坛回复并描述您的问题 

针对`java`集成：

1. 初始化桥相关内容：`UamaWebSupportManager.Companion.initWebview(this,bridgeWebView);`

2. ```java
    @Override
       protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
           UamaWebSupportManager.Companion.onActivityResult(requestCode,resultCode,data,this);
       }
       
       @Override
       protected void onDestroy() {
           super.onDestroy();
           UamaWebSupportManager.Companion.destroyWebView(bridgeWebView);
       }
    ```
