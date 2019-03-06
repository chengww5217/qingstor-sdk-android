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
package com.chengww.qingstor_sdk_android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.SystemClock;

import com.chengww.qingstor_sdk_android.QingstorHelper;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.chengww.qingstor_sdk_android.utils.IOUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Progress implements Serializable {

    public static final int NONE = 0;         // None status
    public static final int WAITING = 1;
    public static final int LOADING = 2;
    public static final int PAUSE = 3;
    public static final int ERROR = 4;
    public static final int FINISH = 5;

    public static final String TAG = "tag";
    public static final String BUCKET = "bucket";
    public static final String OBJECT_KEY = "objectKey";
    public static final String FOLDER = "folder";
    public static final String FILE_PATH = "filePath";
    public static final String FILE_NAME = "fileName";
    public static final String FRACTION = "fraction";
    public static final String TOTAL_SIZE = "totalSize";
    public static final String CURRENT_SIZE = "currentSize";
    public static final String STATUS = "status";
    public static final String DATE = "date";
    public static final String EXTRA1 = "extra1";
    public static final String EXTRA2 = "extra2";
    public static final String EXTRA3 = "extra3";
    public static final String RECORDER_BEAN = "recorder_bean"; // Recorder's bean of UploadManager

    public String tag;
    public MyBucket myBucket;
    public String objectKey;
    public String folder;                           // Kept folder
    public String filePath;                         // File kept path
    public String fileName;                         // File kept name
    public float fraction;                          // Fraction, 0 - 1, float
    public long totalSize;                          // Total bytes size, byte
    public long currentSize;                        // Current bytes size, byte
    public transient long speed;                    // Speed，bytes/s
    public int status;
    public long date;                               // Created date
    public Serializable extra1;                     // Extra data
    public Serializable extra2;
    public Serializable extra3;
    public TaskException exception;
    public byte[] recorderBean;

    private transient long tempSize;
    private transient long lastRefreshTime;
    private transient List<Long> speedBuffer;

    public Progress() {
        lastRefreshTime = SystemClock.elapsedRealtime();
        totalSize = -1;
        date = System.currentTimeMillis();
        speedBuffer = new ArrayList<>();
    }

    public static Progress changeProgress(Progress progress, long writeSize, Action action) {
        return changeProgress(progress, writeSize, progress.totalSize, action);
    }

    public static Progress changeProgress(final Progress progress, long writeSize, long totalSize, final Action action) {
        progress.totalSize = totalSize;
        progress.currentSize += writeSize;
        progress.tempSize += writeSize;

        long currentTime = SystemClock.elapsedRealtime();
        boolean isNotify = (currentTime - progress.lastRefreshTime) >= QingstorHelper.REFRESH_TIME;
        if (isNotify || progress.currentSize == totalSize) {
            long diffTime = currentTime - progress.lastRefreshTime;
            if (diffTime == 0) diffTime = 1;
            progress.fraction = progress.currentSize * 1.0f / totalSize;
            progress.speed = progress.bufferSpeed(progress.tempSize * 1000 / diffTime);
            progress.lastRefreshTime = currentTime;
            progress.tempSize = 0;
            if (action != null) {
                action.call(progress);
            }
        }
        return progress;
    }

    private long bufferSpeed(long speed) {
        speedBuffer.add(speed);
        if (speedBuffer.size() > 10) {
            speedBuffer.remove(0);
        }
        long sum = 0;
        for (float speedTemp : speedBuffer) {
            sum += speedTemp;
        }
        return sum / speedBuffer.size();
    }

    public void from(Progress progress) {
        totalSize = progress.totalSize;
        currentSize = progress.currentSize;
        fraction = progress.fraction;
        speed = progress.speed;
        lastRefreshTime = progress.lastRefreshTime;
        tempSize = progress.tempSize;
    }

    public interface Action {
        void call(Progress progress);
    }

    public static ContentValues buildContentValues(Progress progress) {
        ContentValues values = new ContentValues();
        values.put(TAG, progress.tag);
        values.put(BUCKET, IOUtils.toByteArray(progress.myBucket));
        values.put(OBJECT_KEY, progress.objectKey);
        values.put(FOLDER, progress.folder);
        values.put(FILE_PATH, progress.filePath);
        values.put(FILE_NAME, progress.fileName);
        values.put(FRACTION, progress.fraction);
        values.put(TOTAL_SIZE, progress.totalSize);
        values.put(CURRENT_SIZE, progress.currentSize);
        values.put(STATUS, progress.status);
        values.put(DATE, progress.date);
        values.put(RECORDER_BEAN, progress.recorderBean);
        values.put(EXTRA1, IOUtils.toByteArray(progress.extra1));
        values.put(EXTRA2, IOUtils.toByteArray(progress.extra2));
        values.put(EXTRA3, IOUtils.toByteArray(progress.extra3));
        return values;
    }

    public static ContentValues buildUpdateContentValues(Progress progress) {
        ContentValues values = new ContentValues();
        values.put(FRACTION, progress.fraction);
        values.put(TOTAL_SIZE, progress.totalSize);
        values.put(CURRENT_SIZE, progress.currentSize);
        values.put(STATUS, progress.status);
        values.put(DATE, progress.date);
        values.put(RECORDER_BEAN, progress.recorderBean);
        return values;
    }

    public static Progress parseCursorToBean(Cursor cursor) {
        Progress progress = new Progress();
        progress.tag = cursor.getString(cursor.getColumnIndex(Progress.TAG));
        progress.myBucket = (MyBucket) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.BUCKET)));
        progress.objectKey = cursor.getString(cursor.getColumnIndex(Progress.OBJECT_KEY));
        progress.folder = cursor.getString(cursor.getColumnIndex(Progress.FOLDER));
        progress.filePath = cursor.getString(cursor.getColumnIndex(Progress.FILE_PATH));
        progress.fileName = cursor.getString(cursor.getColumnIndex(Progress.FILE_NAME));
        progress.fraction = cursor.getFloat(cursor.getColumnIndex(Progress.FRACTION));
        progress.totalSize = cursor.getLong(cursor.getColumnIndex(Progress.TOTAL_SIZE));
        progress.currentSize = cursor.getLong(cursor.getColumnIndex(Progress.CURRENT_SIZE));
        progress.status = cursor.getInt(cursor.getColumnIndex(Progress.STATUS));
        progress.date = cursor.getLong(cursor.getColumnIndex(Progress.DATE));
        progress.extra1 = (Serializable) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.EXTRA1)));
        progress.extra2 = (Serializable) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.EXTRA2)));
        progress.extra3 = (Serializable) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.EXTRA3)));
        progress.recorderBean = cursor.getBlob(cursor.getColumnIndex(Progress.RECORDER_BEAN));
        return progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Progress progress = (Progress) o;
        return tag != null ? tag.equals(progress.tag) : progress.tag == null;

    }

    @Override
    public int hashCode() {
        return tag != null ? tag.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Progress{" +
                "tag='" + tag + '\'' +
                ", bucket=" + myBucket +
                ", objectKey='" + objectKey + '\'' +
                ", folder='" + folder + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fraction=" + fraction +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", speed=" + speed +
                ", status=" + status +
                ", date=" + date +
                ", extra1=" + extra1 +
                ", extra2=" + extra2 +
                ", extra3=" + extra3 +
                ", recorderBean=" + Arrays.toString(recorderBean) +
                '}';
    }
}
