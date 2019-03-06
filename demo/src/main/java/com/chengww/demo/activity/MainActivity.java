package com.chengww.demo.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.widget.EditText;

import com.chengww.demo.R;
import com.chengww.demo.constants.Constants;
import com.chengww.demo.model.MessageEvent;
import com.chengww.demo.utils.BindContentView;
import com.chengww.demo.utils.SharedPreferencesUtils;
import com.chengww.demo.utils.ToastUtils;
import com.chengww.qingstor_sdk_android.QingstorHelper;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.qingstor.sdk.config.EnvContext;
import com.qingstor.sdk.request.ResponseCallBack;
import com.qingstor.sdk.service.QingStor;
import com.qingstor.sdk.service.Types;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

@BindContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements ResponseCallBack<QingStor.ListBucketsOutput> {

    @BindView(R.id.et_access_key)
    EditText etAccessKey;
    @BindView(R.id.et_access_secret)
    EditText etAccessSecret;

    @Override
    protected void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.login);
        }

        String accessKey = (String)
                SharedPreferencesUtils.get(this, Constants.ACCESS_KEY, "");
        String accessSecret = (String)
                SharedPreferencesUtils.get(this, Constants.ACCESS_SECRET, "");

        etAccessKey.setText(accessKey);
        etAccessSecret.setText(accessSecret);
    }


    @OnClick(R.id.btn_list_buckets)
    void listBuckets() {
        String accessKey = etAccessKey.getText().toString();
        String accessSecret = etAccessSecret.getText().toString();
        boolean accessChecked = true;
        if (TextUtils.isEmpty(accessKey)) {
            ToastUtils.show(this, R.string.access_key_not_empty);
            accessChecked = false;
        } else if (TextUtils.isEmpty(accessSecret)) {
            ToastUtils.show(this, R.string.access_secret_not_empty);
            accessChecked = false;
        }

        if (accessChecked) {
            showLoading(R.string.brvah_loading);
            EnvContext context = new EnvContext(accessKey, accessSecret);
            QingStor qingStor = new QingStor(context);
            qingStor.listBucketsAsync(null, this);
        }
    }

    @Override
    public void onAPIResponse(QingStor.ListBucketsOutput output) {
        dismissLoading();
        try {
            QingstorHelper.getInstance().handleResponse(output);
            // Success
            List<Types.BucketModel> buckets = output.getBuckets();

            // Keep access in the storage
            SharedPreferencesUtils.put(this, Constants.ACCESS_KEY, etAccessKey.getText().toString());
            SharedPreferencesUtils.put(this, Constants.ACCESS_SECRET, etAccessSecret.getText().toString());
            // Show buckets in bucket list activity
            EventBus.getDefault().postSticky(new MessageEvent<>(Constants.LIST_BUCKETS, buckets));
            Intent intent = new Intent(this, BucketListActivity.class);
            startActivity(intent);
        } catch (TaskException exception) {
            ToastUtils.show(this, exception.getI18nHint());
        }
    }


}
