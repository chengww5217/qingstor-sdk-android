package com.chengww.demo.model;

import android.support.annotation.NonNull;

/**
 * Created by chengww on 2019/2/28.
 */
public class MessageEvent<T> {
    private int code;
    private T data;

    public MessageEvent(int code) {
        this.code = code;
    }

    public MessageEvent(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        return "EventMessage{" +
                "code=" + code +
                ", data=" + data +
                '}';
    }
}
