package com.chengww.demo.activity;

import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chengww.demo.R;
import com.chengww.demo.constants.Constants;
import com.chengww.demo.model.MessageEvent;
import com.chengww.demo.model.ObjectListEventModel;
import com.chengww.demo.utils.BindContentView;
import com.chengww.demo.utils.BindEventBus;
import com.chengww.demo.utils.ToastUtils;
import com.chengww.qingstor_sdk_android.task.Uploader;
import com.qingstor.sdk.service.Bucket;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;

@BindEventBus
@BindContentView(R.layout.activity_upload_detail)
public class UploadDetailActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.et_object_key)
    EditText etObjectKey;
    @BindView(R.id.tv_bucket_info)
    TextView tvBucketInfo;
    @BindView(R.id.btn_ok)
    Button btnOk;

    private ObjectListEventModel model;
    private File file;
    private Bucket bucket;

    @Override
    protected void init() {
        if (model == null) {
            ToastUtils.show(this, R.string.parameter_error);
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.upload_to);
        }

        bucket = model.getBucket();
        String bucketInfo = "Zone: " + bucket.getZone() + "\nBucketName: " + bucket.getBucketName();
        tvBucketInfo.setText(bucketInfo);

        file = model.getFile();
        String prefix = model.getPrefix();
        if (prefix == null) prefix = "";

        etObjectKey.setText(prefix + file.getName());

        tvTitle.setText(file.getName());
        tvContent.setText(file.getAbsolutePath());

        btnOk.setOnClickListener(v -> {
            boolean isPassed = true;
            String objectKey = etObjectKey.getText().toString();
            if (TextUtils.isEmpty(objectKey) || objectKey.endsWith("/")) {
                ToastUtils.show(v.getContext(), R.string.object_key_not_empty);
                isPassed = false;
            }

            if (isPassed) {
                // Upload tag named with time millis, can upload a same file multiple times
                Uploader.request(file.getAbsolutePath() + "_" + System.currentTimeMillis(),
                        bucket, objectKey, file.getAbsolutePath())
                        .save().start();
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveData(MessageEvent<ObjectListEventModel> messageEvent) {
        if (model ==  null&& messageEvent != null && messageEvent.getCode() == Constants.UPLOAD_DETAIL) {
            model = messageEvent.getData();
        }
    }
}
