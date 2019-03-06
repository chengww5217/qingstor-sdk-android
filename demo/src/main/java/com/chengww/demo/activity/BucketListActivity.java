package com.chengww.demo.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chengww.demo.R;
import com.chengww.demo.adapter.BucketListAdapter;
import com.chengww.demo.constants.Constants;
import com.chengww.demo.model.ObjectListEventModel;
import com.chengww.demo.model.MessageEvent;
import com.chengww.demo.utils.BindContentView;
import com.chengww.demo.utils.BindEventBus;
import com.chengww.demo.utils.SharedPreferencesUtils;
import com.chengww.demo.utils.ToastUtils;
import com.chengww.qingstor_sdk_android.QingstorHelper;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.qingstor.sdk.config.EnvContext;
import com.qingstor.sdk.request.ResponseCallBack;
import com.qingstor.sdk.service.Bucket;
import com.qingstor.sdk.service.QingStor;
import com.qingstor.sdk.service.Types;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

@BindEventBus
@BindContentView(R.layout.activity_bucket_list)
public class BucketListActivity extends BaseActivity {

    List<Types.BucketModel> buckets;
    @BindView(R.id.rv_bucket_list)
    RecyclerView rvBucketList;
    private BucketListAdapter adapter;
    private AlertDialog createBucketDialog;

    @Override
    protected void init() {
        if (buckets == null) {
            ToastUtils.show(this, R.string.parameter_error);
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.bucket_list);
        }

        adapter = new BucketListAdapter(R.layout.item_bucket_list, buckets);
        TextView emptyView = new TextView(this);
        emptyView.setText(R.string.no_bucket_exist);
        emptyView.setGravity(Gravity.CENTER);
        adapter.setEmptyView(emptyView);
        rvBucketList.setLayoutManager(new LinearLayoutManager(this));
        rvBucketList.setAdapter(adapter);

        adapter.setOnItemClickListener((adapter1, view, position) -> {
            Types.BucketModel bucketModel = (Types.BucketModel) adapter1.getData().get(position);
            // Create a bucket
            QingStor qingStor = getQingStor();
            Bucket bucket = qingStor.getBucket(bucketModel.getName(), bucketModel.getLocation());

            ObjectListEventModel model = new ObjectListEventModel(bucket);
            EventBus.getDefault().postSticky(new MessageEvent<>(Constants.LIST_OBJECTS, model));
            startActivity(new Intent(BucketListActivity.this, ObjectListActivity.class));
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveBuckets(MessageEvent<List<Types.BucketModel>> messageEvent) {
        if (buckets == null && messageEvent != null && messageEvent.getCode() == Constants.LIST_BUCKETS) {
            buckets = messageEvent.getData();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bucket_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showCreateBucketDialog();
                return true;
            case R.id.action_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showCreateBucketDialog() {
        View view = View
                .inflate(this, R.layout.dialog_create_bucket, null);
        EditText etZone = view.findViewById(R.id.et_zone);
        EditText etBucketName = view.findViewById(R.id.et_bucket_name);

        if (createBucketDialog == null) {
            createBucketDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        boolean inputChecked = true;
                        String zone = etZone.getText().toString();
                        String bucketName = etBucketName.getText().toString();

                        if (TextUtils.isEmpty(zone)) {
                            ToastUtils.show(BucketListActivity.this, R.string.zone_not_empty);
                            inputChecked = false;
                        } else if (TextUtils.isEmpty(bucketName)) {
                            ToastUtils.show(BucketListActivity.this, R.string.bucket_name_not_empty);
                            inputChecked = false;
                        }

                        if (inputChecked) {
                            createBucket(zone, bucketName);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                    .create();
        }

        createBucketDialog.show();


    }

    private void createBucket(String zone, String bucketName) {
        showLoading(R.string.brvah_loading);
        QingStor qingStor = getQingStor();
        Bucket bucket = qingStor.getBucket(bucketName, zone);
        bucket.putAsync(new ResponseCallBack<Bucket.PutBucketOutput>() {
            @Override
            public void onAPIResponse(Bucket.PutBucketOutput output) {
                dismissLoading();
                try {
                    QingstorHelper.getInstance().handleResponse(output);
                    refresh();
                } catch (TaskException e) {
                    ToastUtils.show(BucketListActivity.this, e.getI18nHint());
                }
            }
        });
    }

    private void refresh() {
        showLoading(R.string.brvah_loading);
        QingStor qingStor = getQingStor();
        qingStor.listBucketsAsync(null, new ResponseCallBack<QingStor.ListBucketsOutput>() {
            @Override
            public void onAPIResponse(QingStor.ListBucketsOutput output) {
                dismissLoading();
                try {
                    QingstorHelper.getInstance().handleResponse(output);
                    // Success
                    List<Types.BucketModel> buckets = output.getBuckets();
                    adapter.setNewData(buckets);
                    BucketListActivity.this.buckets = buckets;
                } catch (TaskException exception) {
                    ToastUtils.show(BucketListActivity.this, exception.getI18nHint());
                }
            }
        });
    }

    private QingStor qingStor;
    private QingStor getQingStor() {
        if (qingStor == null) {
            String accessKey = (String) SharedPreferencesUtils.get(
                    BucketListActivity.this, Constants.ACCESS_KEY, "");
            String accessSecret = (String) SharedPreferencesUtils.get(
                    BucketListActivity.this, Constants.ACCESS_SECRET, "");
            EnvContext context = new EnvContext(accessKey, accessSecret);
            qingStor = new QingStor(context);
        }

        return qingStor;
    }

}
