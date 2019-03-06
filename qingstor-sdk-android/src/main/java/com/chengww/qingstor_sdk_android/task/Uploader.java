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

import android.util.Log;

import com.chengww.qingstor_sdk_android.db.Progress;
import com.chengww.qingstor_sdk_android.db.UploadTaskManager;
import com.qingstor.sdk.service.Bucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chengww on 2018/12/28.
 */
public class Uploader {

    public static final String TAG = "Qingstor_Uploader";
    private Map<String, UploadTask<?>> taskMap;         // All of the tasks 所有任务
    private UploadThreadPool threadPool;

    public static Uploader getInstance() {
        return UploaderHolder.instance;
    }

    private static class UploaderHolder {
        private static final Uploader instance = new Uploader();
    }

    private Uploader() {
        threadPool = new UploadThreadPool();
        taskMap = new LinkedHashMap<>();

        /*
          Check the validity of the data.
          Prevent exit during upload process,
          and state errors caused by no update of state during the second entry.
         */
        List<Progress> taskList = UploadTaskManager.getInstance().getUploading();
        for (Progress info : taskList) {
            if (info.status == Progress.WAITING || info.status == Progress.LOADING || info.status == Progress.PAUSE) {
                info.status = Progress.NONE;
            }
        }
        UploadTaskManager.getInstance().replace(taskList);
    }

    public static <T> UploadTask<T> request(String tag, Bucket bucket, String objectKey, String filePath) {
        Map<String, UploadTask<?>> taskMap = Uploader.getInstance().getTaskMap();
        //noinspection unchecked
        UploadTask<T> task = (UploadTask<T>) taskMap.get(tag);
        if (task == null) {
            task = new UploadTask<>(tag, bucket, objectKey, filePath);
            taskMap.put(tag, task);
        }
        return task;
    }

    /**
     * Restore tasks form database 从数据库中恢复任务
     */
    public static <T> UploadTask<T> restore(Progress progress) {
        Map<String, UploadTask<?>> taskMap = Uploader.getInstance().getTaskMap();
        //noinspection unchecked
        UploadTask<T> task = (UploadTask<T>) taskMap.get(progress.tag);
        if (task == null) {
            task = new UploadTask<>(progress);
            taskMap.put(progress.tag, task);
        }
        return task;
    }

    /**
     * Restore tasks form database 从数据库中恢复任务
     */
    public static List<UploadTask<?>> restore(List<Progress> progressList) {
        Map<String, UploadTask<?>> taskMap = Uploader.getInstance().getTaskMap();
        List<UploadTask<?>> tasks = new ArrayList<>();
        for (Progress progress : progressList) {
            UploadTask<?> task = taskMap.get(progress.tag);
            if (task == null) {
                task = new UploadTask<>(progress);
                taskMap.put(progress.tag, task);
            }
            tasks.add(task);
        }
        return tasks;
    }

    public void startAll() {
        for (Map.Entry<String, UploadTask<?>> entry : taskMap.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                Log.w(TAG, "Can't find task with tag = " + entry.getKey());
                continue;
            }
            task.start();
        }
    }

    public void pauseAll() {
        // Stop unstarted tasks first
        for (Map.Entry<String, UploadTask<?>> entry : taskMap.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                Log.w(TAG, "Can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status != Progress.LOADING) {
                task.pause();
            }
        }
        // Then stop ongoing tasks
        for (Map.Entry<String, UploadTask<?>> entry : taskMap.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                Log.w(TAG, "Can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status == Progress.LOADING) {
                task.pause();
            }
        }
    }

    public void removeAll() {
        Map<String, UploadTask<?>> map = new HashMap<>(taskMap);
        // Delete unstarted tasks first
        for (Map.Entry<String, UploadTask<?>> entry : map.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                Log.w(TAG, "Can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status != Progress.LOADING) {
                task.remove();
            }
        }
        // Then delete ongoing tasks
        for (Map.Entry<String, UploadTask<?>> entry : map.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                Log.w(TAG, "Can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status == Progress.LOADING) {
                task.remove();
            }
        }
    }

    public UploadThreadPool getThreadPool() {
        return threadPool;
    }

    public Map<String, UploadTask<?>> getTaskMap() {
        return taskMap;
    }

    public UploadTask<?> getTask(String tag) {
        return taskMap.get(tag);
    }

    public boolean hasTask(String tag) {
        return taskMap.containsKey(tag);
    }

    public UploadTask<?> removeTask(String tag) {
        return taskMap.remove(tag);
    }

    public void addOnAllTaskEndListener(XExecutor.OnAllTaskEndListener listener) {
        threadPool.getExecutor().addOnAllTaskEndListener(listener);
    }

    public void removeOnAllTaskEndListener(XExecutor.OnAllTaskEndListener listener) {
        threadPool.getExecutor().removeOnAllTaskEndListener(listener);
    }
}
