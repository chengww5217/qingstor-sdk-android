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
package com.chengww.qingstor_sdk_android.utils;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.chengww.qingstor_sdk_android.QingstorHelper;

/**
 * Created by chengww on 2018/12/28.
 */
public class ResUtils {
    public static String string(Context context, String key) {
        if (TextUtils.isEmpty(key)) return key;
        int stringID = context.getResources().getIdentifier(key, "string", context.getPackageName());
        if (stringID == 0) return key;
        return context.getString(stringID);
    }

    public static String string(String key) {
        String result;
        Application context = QingstorHelper.getInstance().getContext();
        if (context != null) result = string(context, key);
        else result = key;
        return result;
    }


    public static String string(int id) {
        String result;
        Application context = QingstorHelper.getInstance().getContext();
        if (context != null) result = context.getString(id);
        else result = String.valueOf(id);
        return result;
    }
}
