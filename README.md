# QingStor SDK for Android

[![Download](https://api.bintray.com/packages/chengww5217/chengww/qingstor-sdk-android/images/download.svg)](https://bintray.com/chengww5217/chengww/qingstor-sdk-android)
[![API Reference](http://img.shields.io/badge/api-reference-green.svg)](http://docs.qingcloud.com)
[![License](http://img.shields.io/badge/license-apache%20v2-blue.svg)](https://github.com/chengww5217/qingstor-sdk-android/blob/master/LICENSE)

English | [简体中文](./README-zh_CN.md)

The unofficial QingStor SDK for the platform Android.

This project is based on [qingstor-sdk-java](https://github.com/yunify/qingstor-sdk-java).

There are some differences between qingstor-sdk-android and qingstor-sdk-java:

- All of the methods which should have been called(onProgress() and onAPIResponse() etc.), will be called in the **Android Main Thread**.
- Replace `org.json:json:20140107` with Android's own.
- Remove `org.yaml:snakeyaml:1.17` and related codes.
- Add Downloader and Uploader management for Android.
- Add the method QingstorHelper#handleResponse() to handle the response of the callback.

You can see documents containing the complete package structure here: [github pages](https://chengww5217.github.io/qingstor-sdk-android/).


## Getting Started

### Installation

Configure this dependency:

```
implementation 'com.chengww:qingstor-sdk-android:0.1.0'
```

### Usage

#### Preparation

Init QingstorHelper in your Application:

```Java
QingstorHelper.getInstance().init(this);
```

**PS: Don't forget to register your Application in the manifest file.**

#### Independent API Usage

Each independent API refers to [qingstor-sdk-java](https://github.com/yunify/qingstor-sdk-java/blob/master/README.md).

You can use `XXXAsync()/sendAsync()`, and the callbacks will be called in the **Android Main Thread**.

For example, there are some codes to list buckets.

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

PS: Do not download/upload objects as below codes shown, cause XXXAsync() calls in the main thread where cannot write objects.
```Java
// A wrong demonstration. Do not try codes below.
bucket.getObjectAsync("objectName", null, new ResponseCallBack<Bucket.GetObjectOutput>() {
    @Override
    public void onAPIResponse(Bucket.GetObjectOutput output) {
        // Write the object into the storage here...
    }
});
```
You should new a thread to get the object use a synchronous method or [use the Downloader/Uploader in the android SDK](./docs-md/downloader_uploader.md).

See the [demo](./demo/README.md) for more sample information.

Checkout our [releases](https://github.com/chengww5217/qingstor-sdk-android/releases) and [change logs](./CHANGELOG.md) for information about the latest features, bug fixes and new ideas.

## Reference Documentations

- [QingCloud Documentation Overview](https://docs.qingcloud.com)
- [QingStor APIs](https://docs.qingcloud.com/qingstor/api/index.html)
- [Guide of qingstor-sdk-java](https://github.com/yunify/qingstor-sdk-java/blob/master/README.md)

## R8 / ProGuard

If you are using R8 or ProGuard add the options from [proguard-rules.pro](./qingstor-sdk-android/proguard-rules.pro).

## LICENSE

The Apache License (Version 2.0, January 2004).