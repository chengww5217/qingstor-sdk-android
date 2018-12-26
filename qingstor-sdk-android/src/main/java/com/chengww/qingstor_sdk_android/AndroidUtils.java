package com.chengww.qingstor_sdk_android;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by chengww on 2018/12/25.
 */
public class AndroidUtils {
    private Handler mDelivery;

    private AndroidUtils() {
        mDelivery = new Handler(Looper.getMainLooper());
    }

    private static class StoreSDKHolder {
        private static AndroidUtils holder = new AndroidUtils();
    }

    public static AndroidUtils getInstance() {
        return StoreSDKHolder.holder;
    }

    public Handler getDelivery() {
        return mDelivery;
    }

    public void runOnUiThread(Runnable run) {
        getDelivery().post(run);
    }

}
