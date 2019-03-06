# QingStor SDK for Android Demo

[English](./README.md) | 简体中文

该项目仅为简单体验，并没有覆盖所有 API。点击下载 APK 体验：

- [Github 分流](./release/demo-release.apk?raw=true)
- [中国下载地址](https://js-cdn.pek3b.qingstor.com/qingstor-sdk-android/demo-release.apk)

主要使用了以下 API：

- ListBuckets: `MainActivity#listBuckets()`、`BucketListActivity#refresh()`
- PutBucket: `BucketListActivity#createBucket(zone, bucketName)`
- ListObjects: `ObjectListActivity#listObject(isLoadMore)`
- DeleteObjects: `ObjectListActivity#deleteObject()`
- Downloader 相关 API：`ObjectListActivity`、`DownloadListActivity`、`DownloadListAdapter`
- Uploader 相关 API：`UploadListActivity`、`UploadDetailActivity`、`UploadListAdapter`

项目中使用 JDK 1.8 进行编译，其内部的某些匿名内部类已转换为 lambda 表达式（因 QingStor SDK Java 的问题，Java sdk 内的相关匿名内部类不能转换成 lambda）。

