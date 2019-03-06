package com.chengww.demo.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.chengww.demo.utils.BindContentView;
import com.chengww.demo.utils.BindEventBus;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {

    private Unbinder bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<? extends BaseActivity> clazz = getClass();
        BindContentView bindContentView = clazz.getAnnotation(BindContentView.class);
        if (bindContentView != null && bindContentView.value() > 0) {
            setContentView(bindContentView.value());
            bind = ButterKnife.bind(this);
        }

        if (clazz.isAnnotationPresent(BindEventBus.class)) {
            EventBus.getDefault().register(this);
        }

        init();
    }

    protected abstract void init();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getClass().isAnnotationPresent(BindEventBus.class)) {
            EventBus.getDefault().removeAllStickyEvents();
            EventBus.getDefault().unregister(this);
        }
        bind.unbind();
    }

    private ProgressDialog dialog;

    public void showLoading(int strRes) {
        showLoading(getString(strRes));
    }

    public void showLoading(String hint) {
        if (null == dialog) {
            dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        if (!TextUtils.isEmpty(hint))
            dialog.setMessage(hint);
        dialog.setCancelable(true);
        dialog.show();
    }

    public void dismissLoading() {
        if (null != dialog && dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissLoading();
    }



}
