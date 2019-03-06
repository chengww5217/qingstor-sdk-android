# QingStor SDK for Android

[![下载](https://api.bintray.com/packages/chengww5217/chengww/qingstor-sdk-android/images/download.svg)](https://bintray.com/chengww5217/chengww/qingstor-sdk-android)
[![API 参考](http://img.shields.io/badge/api-reference-green.svg)](http://docs.qingcloud.com)
[![许可](http://img.shields.io/badge/license-apache%20v2-blue.svg)](https://github.com/chengww5217/qingstor-sdk-android/blob/master/LICENSE)

[English](./README.md) | 简体中文

青云对象存储 Android 平台 SDK 非官方版本。

本项目基于 [qingstor-sdk-java](https://github.com/yunify/qingstor-sdk-java)。

以下是 qingstor-sdk-android 和 qingstor-sdk-java 之间的不同点:

- 那些应该回调在主线程的方法(onProgress() 和 onAPIResponse() 等), 将会正确回调在**安卓主线程**中。
- 使用安卓自带的 JSON 包替换 `org.json:json:20140107`。
- 移除 `org.yaml:snakeyaml:1.17` 包以及其相关的代码。
- 新增专为安卓设计的 Downloader 和 Uploader 管理器。
- 新增方法 QingstorHelper#handleResponse() 来处理 callback 中的回应。

您可以在这里看到包含完整包结构的 SDK 文档: [github pages](https://chengww5217.github.io/qingstor-sdk-android/).

## 开始使用

### 安装

配置此依赖项:

```
implementation 'com.chengww:qingstor-sdk-android:0.1.0'
```

### 使用

#### 准备工作

在你的 Application 中初始化 QingstorHelper:

```Java
QingstorHelper.getInstance().init(this);
```

**PS: 别忘了在清单文件中注册你的 Application。**

#### 独立的 API 使用

每一个独立的 API 都和 [qingstor-sdk-java](https://github.com/yunify/qingstor-sdk-java/blob/master/README.md) 中的方法一一对应。

你可以直接使用 `XXXAsync()/sendAsync()` 方法, 然后会在**安卓主线程**中回调。

例如，下面是列取 Bucket(List Buckets) 的代码.

```Java
EnvContext context = new EnvContext(accessKey, accessSecret);
QingStor qingStor = new QingStor(context);
qingStor.listBucketsAsync(null, new ResponseCallBack<QingStor.ListBucketsOutput>() {
    @Override
    public void onAPIResponse(QingStor.ListBucketsOutput output) {
        try {
            QingstorHelper.getInstance().handleResponse(output);
            // Success
            List<Types.BucketModel> buckets = output.getBuckets();
            // TODO: Do something here
        } catch (TaskException exception) {
            // Error
            ToastUtils.show(MainActivity.this, exception.getI18nHint());
        }
    }
});

```

List Objects:

```Java
EnvContext context = new EnvContext("accessKey","accessSecret");
QingStor stor = new QingStor(context);
Bucket bucket = stor.getBucket("bucketName", "pek3b");
Bucket.ListObjectsInput listObjectsInput = new Bucket.ListObjectsInput();
listObjectsInput.setLimit(100);
listObjectsInput.setDelimiter("/");
listObjectsInput.setPrefix(prefix);
bucket.listObjectsAsync(listObjectsInput, new ResponseCallBack<Bucket.ListObjectsOutput>() {
    @Override
    public void onAPIResponse(Bucket.ListObjectsOutput output) {
        try {
            QingstorHelper.getInstance().handleResponse(output);
            // TODO: Do something here
        } catch (TaskException e) {
            ToastUtils.show(ObjectListActivity.this, e.getI18nHint());
        }
    }
});
```

PS: 不要像下面代码一样直接上传/下载文件。因为 XXXAsync() 方法会在主线程中回调，这里不能写文件。
```Java
// 错误示范，不要这样尝试。
bucket.getObjectAsync("objectName", null, new ResponseCallBack<Bucket.GetObjectOutput>() {
    @Override
    public void onAPIResponse(Bucket.GetObjectOutput output) {
        // 在此写文件...
    }
});
```
你应该新建一个线程，使用同步的方法来获取。或者[使用安卓 SDK 中的 Downloader/Uploader](./docs-md/downloader_uploader-zh_CN.md)。

查看更多示例信息，请参考 [demo](./demo/README-zh_CN.md)。

查看我们的 [releases](https://github.com/chengww5217/qingstor-sdk-android/releases) 和 [change logs](./CHANGELOG.md) 来获取关于新特性、bug 修复和新功能的信息。

## 对应文档

- [QingCloud Documentation Overview](https://docs.qingcloud.com)
- [QingStor APIs](https://docs.qingcloud.com/qingstor/api/index.html)
- [Guide of qingstor-sdk-java](https://github.com/yunify/qingstor-sdk-java/blob/master/docs/guide_zh.md)

## 混淆

如果你使用混淆，请将 [proguard-rules.pro](./qingstor-sdk-android/proguard-rules.pro) 内的规则加入你的混淆文件。

## LICENSE

The Apache License (Version 2.0, January 2004).