package com.chengww.demo.model;

import com.qingstor.sdk.service.Bucket;

import java.io.File;

/**
 * Created by chengww on 2019/3/5.
 */
public class UploadListModel {
    private Bucket bucket;
    private String prefix;
    private File file;

    public UploadListModel(Bucket bucket) {
        this.bucket = bucket;
    }

    public UploadListModel(Bucket bucket, String prefix) {
        this.bucket = bucket;
        this.prefix = prefix;
    }

    public UploadListModel(Bucket bucket, String prefix, File file) {
        this.bucket = bucket;
        this.prefix = prefix;
        this.file = file;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
