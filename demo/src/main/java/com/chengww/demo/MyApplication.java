package com.chengww.demo;

import android.app.Application;

import com.chengww.qingstor_sdk_android.QingstorHelper;
import com.chengww.qingstor_sdk_android.task.Downloader;

import java.io.File;

/**
 * Created by chengww on 2019/2/27.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        QingstorHelper.getInstance().init(this);
        String folder;
        File download = getExternalFilesDir("download");
        if (download != null && download.exists()) {
            folder = download.getAbsolutePath();
        } else {
            download = getFilesDir();
            folder = download.getAbsolutePath();
        }
        Downloader.getInstance().setFolder(folder);
    }
}
