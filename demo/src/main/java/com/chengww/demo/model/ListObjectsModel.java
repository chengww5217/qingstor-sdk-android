package com.chengww.demo.model;

import com.qingstor.sdk.service.Types;

/**
 * Created by chengww on 2019/2/28.
 */
public class ListObjectsModel {
    private Types.KeyModel file;
    private String folder;

    public ListObjectsModel(Types.KeyModel file) {
        this.file = file;
    }

    public ListObjectsModel(String folder) {
        this.folder = folder;
    }

    public Types.KeyModel getFile() {
        return file;
    }

    public void setFile(Types.KeyModel file) {
        this.file = file;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
