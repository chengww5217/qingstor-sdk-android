package com.chengww.qingstor_sdk_android.db;

import com.qingstor.sdk.config.EnvContext;

import java.io.Serializable;

/**
 * Created by chengww on 2019/3/4.
 */
public class MyBucket implements Serializable {
    private String zone;
    private String bucketName;
    private EnvContext envContext;

    public MyBucket(EnvContext envContext, String zone, String bucketName) {
        this.zone = zone;
        this.bucketName = bucketName;
        this.envContext = envContext;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public EnvContext getEnvContext() {
        return envContext;
    }

    public void setEnvContext(EnvContext envContext) {
        this.envContext = envContext;
    }
}
