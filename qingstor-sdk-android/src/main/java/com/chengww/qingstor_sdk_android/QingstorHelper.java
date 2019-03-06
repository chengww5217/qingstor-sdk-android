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
package com.chengww.qingstor_sdk_android;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.chengww.qingstor_sdk_android.db.DownloadTaskManager;
import com.chengww.qingstor_sdk_android.db.UploadTaskManager;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.chengww.qingstor_sdk_android.task.Downloader;
import com.chengww.qingstor_sdk_android.task.Uploader;
import com.chengww.qingstor_sdk_android.utils.IOUtils;
import com.qingstor.sdk.model.OutputModel;

/**
 * Created by chengww on 2018/12/25.
 */
public class QingstorHelper {
    public static long REFRESH_TIME = 300;         // Callback refresh time(ms)
    private Application context;                   // Application context
    private Handler mDelivery;

    private QingstorHelper() {
        mDelivery = new Handler(Looper.getMainLooper());
    }

    private static class StoreSDKHolder {
        private static QingstorHelper holder = new QingstorHelper();
    }

    public static QingstorHelper getInstance() {
        return StoreSDKHolder.holder;
    }

    public void runOnUiThread(Runnable run) {
        getDelivery().post(run);
    }

    /**
     * Must call in the Application. If not, download/upload tasks info cannot recorded.
     *
     * @param context Application context
     * @return this
     */
    public QingstorHelper init(Application context) {
        this.context = context;
        // Restore tasks from the database when the application is starting
        Downloader.getInstance();
        Downloader.restore(DownloadTaskManager.getInstance().getAll());

        Uploader.getInstance();
        Uploader.restore(UploadTaskManager.getInstance().getAll());
        return this;
    }

    public Application getContext() {
        IOUtils.checkNotNull(context, "Please call QingstorHelper.getInstance().init() first in application!");
        return context;
    }


    public Handler getDelivery() {
        return mDelivery;
    }

    /**
     * Handle the response of the sdk.
     * If status code is error, a {@link TaskException} will be thrown.
     * Else, your request is successful.
     * @param out response
     * @throws TaskException error exception
     */
    public void handleResponse(OutputModel out) throws TaskException {
        if (out == null) return;
        if (out.getStatueCode() >= 400) {
            throw new TaskException(out);
        }
    }


}
