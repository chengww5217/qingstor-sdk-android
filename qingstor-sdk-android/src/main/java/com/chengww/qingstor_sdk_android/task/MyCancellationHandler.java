package com.chengww.qingstor_sdk_android.task;

import com.qingstor.sdk.request.CancellationHandler;

/**
 * Created by chengww on 2019/3/5.
 */
public class MyCancellationHandler implements CancellationHandler {
    private boolean paused;

    @Override
    public boolean isCancelled() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
