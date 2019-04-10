# common-js-bridge

[![img](https://jitpack.io/v/UamaHZ/common-js-bridge.svg)](https://jitpack.io/#)(https://jitpack.io/#UamaHZ/common-js-bridge/0.1-alpha)

封装一些原生能力供`js`调用：

- 图片选择及预览
- 拨打电话
- 扫一扫
- 发送短信
- 获取网络状态

如何使用？

我们只要使用`UamaWebSupportManager`这个类提供的相关静态方法即可（代码由kotlin书写）:

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

