# QingStor SDK for Android Demo

English | [简体中文](./README-zh_CN.md)

The project is a simple experience and does not cover all APIs.

Click to download the APK file:

- [Github download address](./release/demo-release.apk)
- [Download address for China](https://js-cdn.pek3b.qingstor.com/qingstor-sdk-android/demo-release.apk)

The following APIs are mainly used:

- ListBuckets: `MainActivity#listBuckets()`、`BucketListActivity#refresh()`
- PutBucket: `BucketListActivity#createBucket(zone, bucketName)`
- ListObjects: `ObjectListActivity#listObject(isLoadMore)`
- DeleteObjects: `ObjectListActivity#deleteObject()`
- Downloader related APIs：`ObjectListActivity`、`DownloadListActivity`、`DownloadListAdapter`
- Uploader related APIs：`UploadListActivity`、`UploadDetailActivity`、`UploadListAdapter`

JDK 1.8 is used for compilation in the project, and some anonymous inner classes in the project have been converted to lambda expressions (due to QingStor SDK Java's problem, the related anonymous inner classes in Java SDK can not be converted to the lambda).