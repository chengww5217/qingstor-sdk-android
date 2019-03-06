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

import com.chengww.qingstor_sdk_android.QingstorHelper;
import com.chengww.qingstor_sdk_android.db.Progress;
import com.chengww.qingstor_sdk_android.db.UploadTaskManager;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.chengww.qingstor_sdk_android.listener.UploadListener;
import com.chengww.qingstor_sdk_android.recorder.DBBeanRecorder;
import com.chengww.qingstor_sdk_android.utils.IOUtils;
import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.model.OutputModel;
import com.qingstor.sdk.request.CancellationHandler;
import com.qingstor.sdk.service.Bucket;
import com.qingstor.sdk.upload.Recorder;
import com.qingstor.sdk.upload.UploadManager;
import com.qingstor.sdk.upload.UploadManagerCallback;
import com.qingstor.sdk.upload.UploadProgressListener;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chengww on 2018/12/28.
 */
public class UploadTask<String> extends UploadManagerCallback implements Runnable, Comparable, UploadProgressListener {

    public Progress progress;
    public Map<Object, UploadListener<String>> listeners;
    private ThreadPoolExecutor executor;
    private Recorder recorder;
    private UploadManager manager;
    private PriorityRunnable priorityRunnable;
    private MyCancellationHandler cancellationHandler;

    public UploadTask(java.lang.String tag, Bucket bucket, java.lang.String objectKey, java.lang.String filePath) {
        IOUtils.checkNotNull(tag, "tag == null");
        IOUtils.checkNotNull(bucket, "bucket == null");
        IOUtils.checkNotNull(objectKey, "objectKey == null");
        progress = new Progress();
        progress.tag = tag;

        progress.myBucket = IOUtils.getMyBucket(bucket);
        progress.objectKey = objectKey;
        progress.status = Progress.NONE;
        progress.totalSize = -1;
        progress.filePath = filePath;

        init(progress);
    }


    public UploadTask(Progress progress) {
        IOUtils.checkNotNull(progress, "progress == null");
        this.progress = progress;
        init(progress);
    }

    private void init(Progress progress) {
        executor = Uploader.getInstance().getThreadPool().getExecutor();
        listeners = new HashMap<>();
        recorder = new DBBeanRecorder(progress);
        manager = new UploadManager(IOUtils.getBucket(progress.myBucket), recorder,
                this, null, this);
        File file = new File(progress.filePath);
        if (file.exists())
            progress.fileName = file.getName();
    }

    public UploadTask<String> extra1(Serializable extra1) {
        progress.extra1 = extra1;
        return this;
    }

    public UploadTask<String> extra2(Serializable extra2) {
        progress.extra2 = extra2;
        return this;
    }

    public UploadTask<String> extra3(Serializable extra3) {
        progress.extra3 = extra3;
        return this;
    }

    public UploadTask<String> save() {
        UploadTaskManager.getInstance().replace(progress);
        return this;
    }

    public UploadTask<String> register(UploadListener<String> listener) {
        if (listener != null) {
            listeners.put(listener.tag, listener);
        }
        return this;
    }

    public void unRegister(UploadListener<String> listener) {
        IOUtils.checkNotNull(listener, "listener == null");
        listeners.remove(listener.tag);
    }

    public void unRegister(java.lang.String tag) {
        IOUtils.checkNotNull(tag, "tag == null");
        listeners.remove(tag);
    }

    public UploadTask<String> start() {
        if (Uploader.getInstance().getTask(progress.tag) == null || UploadTaskManager.getInstance().get(progress.tag) == null) {
            throw new IllegalStateException("you must call UploadTask#save() before UploadTask#start()！");
        }
        if (progress.status != Progress.WAITING && progress.status != Progress.LOADING) {
            postOnStart(progress);
            postWaiting(progress);
            priorityRunnable = new PriorityRunnable(this);
            executor.execute(priorityRunnable);
        }
        return this;
    }

    public void restart() {
        pause();
        progress.status = Progress.NONE;
        progress.currentSize = 0;
        progress.fraction = 0;
        progress.speed = 0;
        UploadTaskManager.getInstance().replace(progress);
        start();
    }

    public void pause() {
        if (progress.status == Progress.WAITING || progress.status == Progress.NONE) {
            postPause(progress);
        } else if (progress.status == Progress.LOADING) {
            progress.speed = 0;
            progress.status = Progress.PAUSE;
            cancellationHandler.setPaused(true);
            postPause(progress);
        }
        executor.remove(priorityRunnable);

    }

    public UploadTask<String> remove() {
        pause();
        UploadTaskManager.getInstance().delete(progress.tag);
        //noinspection unchecked
        UploadTask<String> task = (UploadTask<String>) Uploader.getInstance().removeTask(progress.tag);
        postOnRemove(progress);
        return task;
    }

    @Override
    public void run() {
        progress.status = Progress.LOADING;
        postLoading(progress);

        try {
            cancellationHandler = new MyCancellationHandler();
            manager.setCancellationHandler(cancellationHandler);
            manager.put(new File(progress.filePath), progress.objectKey, null, null);
        } catch (QSException e) {
            postOnError(progress, new TaskException(e));
        }
    }

    private void postOnStart(final Progress progress) {
        progress.speed = 0;
        progress.status = Progress.NONE;
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (UploadListener<String> listener : listeners.values()) {
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
                for (UploadListener<String> listener : listeners.values()) {
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
                for (UploadListener<String> listener : listeners.values()) {
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
                for (UploadListener<String> listener : listeners.values()) {
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
                for (UploadListener<String> listener : listeners.values()) {
                    listener.onProgress(progress);
                    listener.onError(progress);
                }
            }
        });
    }

    private void postOnFinish(final Progress progress, final String t) {
        progress.speed = 0;
        progress.fraction = 1.0f;
        progress.status = Progress.FINISH;
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (UploadListener<String> listener : listeners.values()) {
                    listener.onProgress(progress);
                    listener.onFinish(t, progress);
                }
            }
        });
    }

    private void postOnRemove(final Progress progress) {
        updateDatabase(progress);
        QingstorHelper.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (UploadListener<String> listener : listeners.values()) {
                    listener.onRemove(progress);
                }
                listeners.clear();
            }
        });
    }

    private void updateDatabase(Progress progress) {
        ContentValues contentValues = Progress.buildUpdateContentValues(progress);
        UploadTaskManager.getInstance().update(contentValues, progress.tag);
    }

    @Override
    public void onProgress(java.lang.String objectKey, long currentSize, long totalSize) {
        if (cancellationHandler.isCancelled()) return;
        progress.status = Progress.LOADING;
        Progress.changeProgress(progress, currentSize - progress.currentSize, totalSize, new Progress.Action() {
            @Override
            public void call(Progress progress) {
                postLoading(progress);
            }
        });
    }


    @Override
    public void onAPIResponse(java.lang.String objectKey, OutputModel outputModel) {
        Integer statusCode = outputModel.getStatueCode();
        if (statusCode >= 200 && statusCode < 400) {
            postOnFinish(progress, (String) objectKey);
        } else if (statusCode != QSConstant.REQUEST_ERROR_CANCELLED) {
            postOnError(progress, new TaskException(outputModel));
        }
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}

