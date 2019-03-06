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
package com.chengww.qingstor_sdk_android.recorder;

import com.chengww.qingstor_sdk_android.db.Progress;
import com.qingstor.sdk.upload.Recorder;

/**
 * Created by chengww on 2018/12/28.
 */
public class DBBeanRecorder implements Recorder {
    private Progress progress;

    public DBBeanRecorder(Progress progress) {
        this.progress = progress;
    }

    @Override
    public void set(String key, byte[] data) {
        progress.recorderBean = data;
    }

    @Override
    public byte[] get(String key) {
        return progress.recorderBean;
    }

    @Override
    public void del(String key) {
        progress.recorderBean = null;
    }
}
