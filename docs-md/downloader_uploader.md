# Usage of Downloader and Uploader

Downloader and Uploader are the global download and upload managers in the Android SDK.

## Main functions

- All tasks are differentiated by tag
- Managing tasks globally
- With thread pool, support setting concurrency number (default download 3 concurrency, upload 1 concurrency)
- Using database to store task schedule
- Contains multiple task states (NONE、WAITING、LOADING、PAUSE、ERROR、FINISH)
- Support breakpoint download/upload

## Progress

[Progress](../qingstor-sdk-android/src/main/java/com/chengww/qingstor_sdk_android/db/Progress.java) is an object that records progress information held in Downloader and Uploader.
It will appear in the parameters of the callback method during the callback of progress.

The member variable **status** represents the current state of the task, which is always one of NONE, WAITING, LOADING, PAUSE, ERROR, FINISH. The rest of the internal fields refer to source code comments.

## How to use

### Global Configuration

The following configuration can be initialized at first use:

```Java
Downloader.getInstance() = downloader;

// Set up the global download directory, which will be used by default for each task if the directory is not set in the future. If not, the default is the download directory.
String path = Environment.getExternalStorageDirectory().getPath() + "/download/";
downloader.setFolder(path);

// Set the number of simultaneous download tasks (default 3). Effective only on the first call
downloader.getThreadPool().setCorePoolSize(3);

// Add all download task end monitoring
downloader.addOnAllTaskEndListener(listener);

```

### Global Task Operating Ability

The calling object of the following methods is Downloader.getInstance()

- startAll()：Start all tasks.
- pauseAll()：Pause all tasks.
- removeAll()/removeAll(false)：Remove all tasks without deleting downloaded files.
- removeAll(true)：Remove all tasks and delete downloaded files.
- removeTask()：Remove the task with the tag.
- getTaskMap()：Get the map of all current download tasks.
- getTask()：Get the task with the tag.
- hasTask()：Determines whether a task identified as tag exists.

### Single Task Operating Ability

The calling object of the following methods is DownloadTask

- start()：Start a task (start a new/paused/failed task).
- pause()：Pause a task.
- remove()/remove(false)：Remove a task without deleting the downloaded file.
- remove(true)：Remove a task and delete the downloaded file.
- restart()：Restart a task. The task and file before will be deleted, and then the file will be downloaded again.
- save()：Save the data of a task. For more information, see the following: ** Create a new task and start **

Some Single Task Configuration-related Approaches

- folder()：Individually specify the download folder for the current task. If not specified, the default download path is ` /storage/emulated/0/download `.
- fileName()：Manually specify the name of the downloaded file. It is recommended that you do not specify it yourself, and it will be automatically retrieved.
- extra()：An extension field equivalent to a database, providing three extension fields that allow users to save custom data.
- register()：The method of registering listeners. Multiple listeners can be registered and take effect simultaneously. When the status changes, each listener receives the notification.

#### Create a new task and start

```Java
DownloadTask task = Downloader.request(tag, bucket, objectKey);
```

Using this method, downloadTask objects can be created. Accept three parameters, the first parameter is tag, which represents the unique identity of the current task.
As mentioned in the introduction, all download tasks are differentiated by tag, and different tasks must use different tags.
The same download address, if using different tags, will also be considered as two download tasks, different download addresses, such as using the same tag, will also be considered as the same task (leading to breakpoints confusion).

The start() method cannot be invoked directly to start the task after the task is created using the above method.
All newly created tasks need to call save() method to save progress information before they can call the start method.
If the parameters of a task are modified, such as modifying extra data, downloading folders, etc., the save() method must also be called to save the data.

Here is a sample code to create a new task and start it:

```Java
Downloader.request(tag, bucket, objectKey).save().start();
```

### Task Listeners

Download Listener is used for download listening callbacks.
The downloadListener construction method needs to pass in a tag, which uniquely identifies the current listener.
The main purpose of the tag is to facilitate the cancellation of the listener and prevent data confusion.
The listener has five callback methods, all in the main thread callback.
The onProgress() method is called back not only when the progress changes, but also when the download status changes. Many times, this method is enough to monitor state changes.

Register a listener please use `task.register(listener);`
Cancel a listener please use `task.unregister(tag);`

Register a all task end listener please use `Downloader.getInstance().addOnAllTaskEndListener(allTaskEndListener);` 。
Cancel a all task end listener please use `Downloader.getInstance().removeOnAllTaskEndListener(allTaskEndListener);` 。

### DownloadTaskManager

Information about download tasks is stored in the database.
The Download Task Manager class is a management class that adds, deletes and modifies the database for download tasks.

If not necessary, please do not use Download Task Manager's API directly to modify the relevant saved data.
The downloader API should be used to operate, so as to ensure the integrity and accuracy of data.

Here are three commonly used methods:

```Java
List<Progress> all = DownloadTaskManager.getInstance().getAll();
List<Progress> finished = DownloadTaskManager.getInstance().getFinished();
List<Progress> downloading = DownloadTaskManager.getInstance().getDownloading();
```

The methods mentioned above represent obtaining all download records, completed download records and incomplete download records from the database, respectively.
Get a list of Progress objects, and generally need to be used in conjunction with the Downloader method.
That is to say, after obtaining the data, the collection data of the database is restored to the taskMap of Downloader:
`Downloader.restore(DownloadTaskManager.getInstance().getAll());`



The above recovery methods will be used automatically in the initialization method of QingStor SDK Android.

In this way, the whole task information is stored and restored.



The above instructions on downloader use are all over.
The use of Uploader is almost identical to that of Downloader.

For more sample information, see [demo](../demo/README.md).
