package com.chengww.demo.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by chengww on 2019/2/28.<br/>
 * Used for show a toast.
 */
public class ToastUtils {
    private static Toast toast = null;

    public static void show(Context context, int strRes) {
        show(context, context.getString(strRes));
    }

    public static void show(Context context, String msg) {
        if (null == context || TextUtils.isEmpty(msg))  return;
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        toast.setText(msg);
        toast.show();
    }

}
