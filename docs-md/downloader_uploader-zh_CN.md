# Downloader 和 Uploader 的使用

Downloader 和 Uploader 是安卓 SDK 中的全局下载与上传管理器。

## 主要功能

- 所有任务按照 tag 进行区分
- 对任务进行全局管理
- 自带线程池，支持设置并发数(默认下载 3 并发，上传 1 并发)
- 采用数据库存储任务进度，便于管理
- 包含多个任务状态(无状态、下载、暂停、等待、出错、完成)
- 支持断点下载/上传

## Progress

[Progress](../qingstor-sdk-android/src/main/java/com/chengww/qingstor_sdk_android/db/Progress.java) 对象是 Downloader 和 Uploader 中持有的一个记录进度信息的对象。
它会在进度回调时，在回调方法的参数中出现。

成员变量 **status** 代表任务当前的状态，该状态永远是 NONE、WAITING、LOADING、PAUSE、ERROR、FINISH(无状态、下载、暂停、等待、出错、完成) 中的一种。其余内部各字段含义参考源代码注释。

## 使用

### 全局基本配置

以下配置可以在第一次使用时进行初始化：

```Java
Downloader.getInstance() = downloader;

// 设置全局下载目录，以后每个任务如果不设置目录，将默认用此目录。如果不设置，默认为 download 目录
String path = Environment.getExternalStorageDirectory().getPath() + "/download/";
downloader.setFolder(path);

// 设置同时下载任务数(默认 3)。仅第一次调用时生效
downloader.getThreadPool().setCorePoolSize(3);

// 添加所有下载任务结束的监听
downloader.addOnAllTaskEndListener(listener);

```

### 全局任务操作能力

以下方法的调用对象是 Downloader.getInstance()

- startAll()：开始所有任务。
- pauseAll()：暂停所有任务。
- removeAll()/removeAll(false)：移除所有任务，不删除下载的文件。
- removeAll(true)：移除所有任务，删除下载的文件。
- removeTask()：根据 tag 移除任务。
- getTaskMap()：获取当前所有下载任务的 map。
- getTask()：根据 tag 获取任务。
- hasTask()：判断标识为 tag 的任务是否存在。

### 单任务操作能力

以下方法的调用对象是 DownloadTask

- start()：开始任务（开始新任务、暂停的任务、失败的任务）。
- pause()：暂停任务。
- remove()/remove(false)：移除任务，不删除下载的文件。
- remove(true)：移除任务，删除下载的文件。
- restart()：重新开始任务。将会删除之前的任务和文件，然后从头开始重新下载该文件。
- save()：任务数据保存。详细信息见下方 **添加新任务并开始**

一些单任务配置相关的方法

- folder()：单独指定当前任务的下载文件夹。不指定，默认下载路径为 ` /storage/emulated/0/download ` 。
- fileName()：手动指定下载的文件名，建议不要自己指定，会自动获取。
- extra()：相当于数据库的扩展字段，提供三个扩展字段，允许用户保存自定义数据。
- register()：注册监听的方法。监听可以注册多个，同时生效，当状态发生改变的时候，每个监听都会收到通知。

#### 添加新任务并开始

```Java
DownloadTask task = Downloader.request(tag, bucket, objectKey);
```

使用该方法可以创建 DownloadTask 对象。接受三个参数，第一个参数是 tag，表示当前任务的唯一标识。
就像介绍中说的，所有下载任务按照 tag 区分，不同的任务必须使用不一样的tag。
相同的下载地址，如果使用不一样的 tag，也会认为是两个下载任务，不同的下载地址，如使用相同的 tag，也会认为是同一个任务（会导致断点错乱）。

使用上述方法创建 task 后并不能直接调用 start() 方法开始任务。所有新创建的任务都要先调用 save() 方法保存进度信息后才可调用开始方法。
如果对某个任务进行了参数的修改，比如修改了 extra 数据、下载文件夹等，也必须调用 save() 方法，以保存数据。

下面是创建任务并开始的一段示例代码：

```Java
Downloader.request(tag, bucket, objectKey).save().start();
```

## 任务监听

下载监听回调使用的是 DownloadListener 。
DownloadListener 的构造方法需要传入一个 tag，这个 tag 唯一标识当前 listener，主要目的是方便取消监听，同时可以防止数据错乱。
该 listener 有五个回调方法，全部在主线程回调。
onProgress() 方法不仅在进度变化的时候会被回调，下载状态变化的时候也会回调。很多时候，想监听状态变化，在这个方法中就足够了。

注册监听请使用 `task.register(listener);` 。
取消监听请使用 `task.unregister(tag);` 。

注册所有任务结束的监听请使用 `Downloader.getInstance().addOnAllTaskEndListener(allTaskEndListener);` 。
取消所有任务结束的监听请使用 `Downloader.getInstance().removeOnAllTaskEndListener(allTaskEndListener);` 。

### DownloadTaskManager

下载任务的相关信息是保存在数据库中的。DownloadTaskManager 类是对下载任务的数据库进行增删改查的管理类。
如非必要，请不要直接使用 DownloadTaskManager 的 api 修改相关保存的数据。而应该使用 Downloader 的 api 进行操作，这样才能确保数据的完整性和准确性。

下面介绍三个常用的方法：

```Java
List<Progress> all = DownloadTaskManager.getInstance().getAll();
List<Progress> finished = DownloadTaskManager.getInstance().getFinished();
List<Progress> downloading = DownloadTaskManager.getInstance().getDownloading();
```

上述方法分别表示从数据库中获取所有的下载记录，已完成的下载记录和未完成的下载记录。
获取的是 Progress 对象的列表，一般还需要和 Downloader 的方法配合使用。即获取数据后，将数据库的集合数据恢复到 Downloader 的 taskMap 中：
` Downloader.restore(DownloadTaskManager.getInstance().getAll()); `

以上恢复方法会在 QingStor SDK Android 的初始化方法中自动使用。
这样就实现了整个任务信息的存储和恢复。

以上关于 Downloader 的使用讲解已全部结束。Uploader 的使用几乎和 Downloader 完全一样。
如需更多示例信息，请参考相关 [demo](../demo/README-zh_CN.md)。
