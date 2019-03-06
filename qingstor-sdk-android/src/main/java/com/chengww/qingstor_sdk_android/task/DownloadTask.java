/*
 * Copyright 2018 chengww
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chengww.qingstor_sdk_android.task;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.chengww.qingstor_sdk_android.QingstorHelper;
import com.chengww.qingstor_sdk_android.db.DownloadTaskManager;
import com.chengww.qingstor_sdk_android.db.Progress;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.chengww.qingstor_sdk_android.listener.DownloadListener;
import com.chengww.qingstor_sdk_android.utils.IOUtils;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.service.Bucket;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chengww on 2018/12/28.
 */
public class DownloadTask implements Comparable, Runnable {

    private static final int BUFFER_SIZE = 1024 * 8;

    public Progress progress;
    public Map<Object, DownloadListener> listeners;
    private ThreadPoolExecutor executor;
    private PriorityRunnable priorityRunnable;

    public DownloadTask(String tag, Bucket bucket, String objectKey) {
        IOUtils.checkNotNull(tag, "tag == null");
        IOUtils.checkNotNull(bucket, "bucket == null");
        IOUtils.checkNotNull(objectKey, "objectKey == null");
        progress = new Progress();
        progress.tag = tag;
        progress.folder = Downloader.getInstance().getFolder();
        progress.myBucket = IOUtils.getMyBucket(bucket);
        progress.status = Progress.NONE;
        progress.totalSize = -1;
        progress.objectKey = objectKey;

        init();
    }

    public DownloadTask(Progress progress) {
        IOUtils.checkNotNull(progress, "progress == null");
        this.progress = progress;
        init();
    }

    private void init() {
        executor = Downloader.getInstance().getThreadPool().getExecutor();
        listeners = new HashMap<>();
    }

    public DownloadTask folder(String folder) {
        if (folder != null && !TextUtils.isEmpty(folder.trim())) {
            progress.folder = folder;
        } else {
            Log.w(Downloader.TAG, "folder is null, ignored!");
        }
        return this;
    }

    public DownloadTask fileName(String fileName) {
        if (fileName != null && !TextUtils.isEmpty(fileName.trim())) {
            progress.fileName = fileName;
        } else {
            Log.w(Downloader.TAG, "fileName is null, ignored!");
        }
        return this;
    }

    public DownloadTask extra1(Serializable extra1) {
        progress.extra1 = extra1;
        return this;
    }

    public DownloadTask extra2(Serializable extra2) {
        progress.extra2 = extra2;
        return this;
    }

    public DownloadTask extra3(Serializable extra3) {
        progress.extra3 = extra3;
        return this;
    }

    public DownloadTask save() {
        if (TextUtils.isEmpty(progress.fileName)) {
            progress.fileName = IOUtils.getFileName(progress.objectKey);
        }

        if (!TextUtils.isEmpty(progress.folder) && !TextUtils.isEmpty(progress.fileName)) {
            progress.filePath = new File(progress.folder, progress.fileName).getAbsolutePath();
        }
        DownloadTaskManager.getInstance().replace(progress);
        return this;
    }

    public DownloadTask register(DownloadListener listener) {
        if (listener != null) {
            listeners.put(listener.tag, listener);
        }
        return this;
    }

    public void unRegister(DownloadListener listener) {
        IOUtils.checkNotNull(listener, "listener == null");
        listeners.remove(listener.tag);
    }

    public void unRegister(String tag) {
        IOUtils.checkNotNull(tag, "tag == null");
        listeners.remove(tag);
    }

    public void start() {
        if (Downloader.getInstance().getTask(progress.tag) == null || DownloadTaskManager.getInstance().get(progress.tag) == null) {
            throw new IllegalStateException("you must call DownloadTask#save() before DownloadTask#start()！");
        }
        if (progress.status == Progress.NONE || progress.status == Progress.PAUSE || progress.status == Progress.ERROR) {
            postOnStart(progress);
            postWaiting(progress);
            priorityRunnable = new PriorityRunnable(this);
            executor.execute(priorityRunnable);
        } else if (progress.status == Progress.FINISH) {
            if (progress.filePath == null) {
                postOnError(progress, TaskException.FILE_INVALID("The file of the task with tag:" + progress.tag + " may be invalid or damaged, please call the method restart() to download again！"));
            } else {
                File file = new File(progress.filePath);
                if (file.exists() && file.length() == progress.totalSize) {
                    postOnFinish(progress, new File(progress.filePath));
                } else {
                    postOnError(progress, TaskException.FILE_INVALID("the file " + progress.filePath + " may be invalid or damaged, please call the method restart() to download again！"));
                }
            }
        } else {
            Log.w(Downloader.TAG, "the task with tag " + progress.tag + " is already in the download queue, current task status is " + progress.status);
        }
    }

    public void restart() {
        pause();
        IOUtils.delFileOrFolder(progress.filePath);
        progress.status = Progress.NONE;
        progress.currentSize = 0;
        progress.fraction = 0;
        progress.speed = 0;
        DownloadTaskManager.getInstance().replace(progress);
        start();
    }

    /**
     * 暂停的方法
     */
    public void pause() {
        executor.remove(priorityRunnable);
        if (progress.status == Progress.WAITING) {
            postPause(progress);
        } else if (progress.status == Progress.LOADING) {
            progress.speed = 0;
            progress.status = Progress.PAUSE;
        } else {
            Log.w(Downloader.TAG, "only the task with status WAITING(1) or LOADING(2) can pause, current status is " + progress.status);
        }
    }

    /**
     * 删除一个任务,会删除下载文件
     */
    public void remove() {
        remove(false);
    }

    /**
     * 删除一个任务,会删除下载文件
     */
    public DownloadTask remove(boolean isDeleteFile) {
        pause();
        if (isDeleteFile) IOUtils.delFileOrFolder(progress.filePath);
        DownloadTaskManager.getInstance().delete(progress.tag);
        DownloadTask task = Downloader.getInstance().removeTask(progress.tag);
        postOnRemove(progress);
        return task;
    }

    @Override
    public void run() {
        // Check breakpoint
        long startPosition = progress.currentSize;
        if (startPosition < 0) {
            postOnError(progress, TaskException.BREAKPOINT_EXPIRED());
            return;
        }
        if (startPosition > 0) {
            if (!TextUtils.isEmpty(progress.filePath)) {
                File file = new File(progress.filePath);
                if (!file.exists()) {
                    postOnError(progress, TaskException.BREAKPOINT_NOT_EXIST());
                    return;
                }
            }
        }

        //request network from startPosition
        Bucket.GetObjectInput input = new Bucket.GetObjectInput();
        input.setRange("bytes=" + startPosition + "-");
        InputStream byteStream;
        try {
            Bucket.GetObjectOutput out = IOUtils.getBucket(progress.myBucket).getObject(progress.objectKey, input);
            // Check network data
            byteStream = out.getBodyInputStream();
            if (out.getStatueCode() >= 200 && out.getStatueCode() < 400) {
                if (byteStream != null) {
                    if (progress.totalSize == -1 && out.getContentLength() != null) {
                        progress.totalSize = out.getContentLength();
                    }

                    // Create filename
                    String fileName = progress.fileName;
                    if (TextUtils.isEmpty(fileName)) {
                        fileName = IOUtils.getFileName(progress.objectKey);
                        progress.fileName = fileName;
                    }
                    if (!IOUtils.createFolder(progress.folder)) {
                        postOnError(progress, TaskException.NOT_AVAILABLE());
                        return;
                    }

                    // Create and check file
                    File file;
                    if (TextUtils.isEmpty(progress.filePath)) {
                        file = new File(progress.folder, fileName);
                        progress.filePath = file.getAbsolutePath();
                    } else {
                        file = new File(progress.filePath);
                    }
                    if (startPosition > 0 && !file.exists()) {
                        postOnError(progress, TaskException.BREAKPOINT_EXPIRED());
                        return;
                    }
                    if (startPosition > progress.totalSize) {
                        postOnError(progress, TaskException.BREAKPOINT_EXPIRED());
                        return;
                    }
                    if (startPosition == 0 && file.exists()) {
                        IOUtils.delFileOrFolder(file);
                    }
                    if (startPosition == progress.totalSize && startPosition > 0) {
                        if (file.exists() && startPosition == file.length()) {
                            postOnFinish(progress, file);
                            return;
                        } else {
                            postOnError(progress, TaskException.BREAKPOINT_EXPIRED());
                            return;
                        }
                    }

                    // Start downloading
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                    randomAccessFile.seek(startPosition);
                    progress.currentSize = startPosition;

                    DownloadTaskManager.getInstance().replace(progress);
                    download(byteStream, randomAccessFile, progress);

                    // Check finish status
                    if (progress.status == Progress.PAUSE) {
                        postPause(progress);
                    } else if (progress.status == Progress.LOADING) {
                        if (file.length() == progress.totalSize) {
                            postOnFinish(progress, file);
                        } else {
                            postOnError(progress, TaskException.BREAKPOINT_EXPIRED());
                        }
                    } else {
                        postOnError(progress, TaskException.UNKNOWN());
                    }
                } else {
                    postOnError(progress, new TaskException("BodyInputStream is null, skipped."));
                }
            } else {
                postOnError(progress, new TaskException(out));
            }

        } catch (QSException e) {
            postOnError(progress, new TaskException(e));
        } catch (IOException e) {
            postOnError(progress, TaskException.IO_EXCEPTION(e));
        }

    }

    /**
     * 执行文件下载
     */
    private void download(InputStream input, RandomAccessFile out, Progress progress) throws IOException {
        if (input == null || out == null) return;

        progress.status = Progress.LOADING;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        int len;
        try {
            while ((len = in.read(buffer, 0, BUFFER_SIZE)) != -1 && progress.status == Progress.LOADING) {
                out.write(buffer, 0, len);

                Progress.changeProgress(progress, len, progress.totalSize, new Progress.Action() {
                    @Override
                    public void call(Progress progress) {
                        postLoading(progress);
                    }
                });
            }
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(input);
        }
    }

    private void postOnStart(final Progress progress) {
        progress.speed = 0;
        progress.status = Progress.NONE;
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onStart(progress);
                }
            }
        });
    }

    private void postWaiting(final Progress progress) {
        progress.speed = 0;
        progress.status = Progress.WAITING;
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                }
            }
        });
    }

    private void postPause(final Progress progress) {
        progress.speed = 0;
        progress.status = Progress.PAUSE;
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                }
            }
        });
    }

    private void postLoading(final Progress progress) {
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                }
            }
        });
    }

    private void postOnError(final Progress progress, final TaskException throwable) {
        progress.speed = 0;
        progress.status = Progress.ERROR;
        progress.exception = throwable;
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                    listener.onError(progress);
                }
            }
        });
    }

    private void postOnFinish(final Progress progress, final File file) {
        progress.speed = 0;
        progress.fraction = 1.0f;
        progress.status = Progress.FINISH;
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onProgress(progress);
                    listener.onFinish(file, progress);
                }
            }
        });
    }

    private void postOnRemove(final Progress progress) {
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DownloadListener listener : listeners.values()) {
                    listener.onRemove(progress);
                }
                listeners.clear();
            }
        });
    }

    private void updateDatabase(Progress progress) {
        ContentValues contentValues = Progress.buildUpdateContentValues(progress);
        DownloadTaskManager.getInstance().update(contentValues, progress.tag);
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
