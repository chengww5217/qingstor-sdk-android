package com.chengww.demo.activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chengww.demo.R;
import com.chengww.demo.adapter.ObjectListAdapter;
import com.chengww.demo.constants.Constants;
import com.chengww.demo.model.ListObjectsModel;
import com.chengww.demo.model.MessageEvent;
import com.chengww.demo.model.ObjectListEventModel;
import com.chengww.demo.utils.BindContentView;
import com.chengww.demo.utils.BindEventBus;
import com.chengww.demo.utils.ToastUtils;
import com.chengww.qingstor_sdk_android.QingstorHelper;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.chengww.qingstor_sdk_android.task.DownloadTask;
import com.chengww.qingstor_sdk_android.task.Downloader;
import com.qingstor.sdk.request.ResponseCallBack;
import com.qingstor.sdk.service.Bucket;
import com.qingstor.sdk.service.Types;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

@BindEventBus
@BindContentView(R.layout.activity_object_list)
public class ObjectListActivity extends BaseActivity {

    @BindView(R.id.rv_object_list)
    RecyclerView rvObjectList;
    @BindView(R.id.sr_object)
    SwipeRefreshLayout srObject;
    private Bucket bucket;
    private String prefix, marker;
    private ObjectListAdapter adapter;
    private ObjectListEventModel model;

    private void listObject(boolean isLoadMore) {
        Bucket.ListObjectsInput listObjectsInput = new Bucket.ListObjectsInput();
        listObjectsInput.setLimit(100);
        listObjectsInput.setDelimiter("/");
        listObjectsInput.setPrefix(prefix);
        if (isLoadMore) {
            listObjectsInput.setMarker(marker);
        }
        bucket.listObjectsAsync(listObjectsInput, new ResponseCallBack<Bucket.ListObjectsOutput>() {
            @Override
            public void onAPIResponse(Bucket.ListObjectsOutput output) {
                srObject.setRefreshing(false);
                try {
                    QingstorHelper.getInstance().handleResponse(output);
                    marker = output.getNextMarker();
                    prefix = output.getPrefix();
                    Boolean hasMoreBoolean = output.getHasMore();
                    boolean hasMore = hasMoreBoolean != null && hasMoreBoolean;
                    List<ListObjectsModel> objectModels = new ArrayList<>();

                    // Folders
                    List<String> commonPrefixes = output.getCommonPrefixes();
                    for (String folder : commonPrefixes) {
                        objectModels.add(new ListObjectsModel(folder));
                    }

                    // Files
                    List<Types.KeyModel> keys = output.getKeys();
                    for (Types.KeyModel keyModel : keys) {
                        objectModels.add(new ListObjectsModel(keyModel));
                    }

                    if (isLoadMore)
                        adapter.addData(objectModels);
                    else
                        adapter.setNewData(objectModels);

                    if (!hasMore) {
                        adapter.loadMoreEnd();
                    } else if (isLoadMore) {
                        adapter.loadMoreComplete();
                    }

                } catch (TaskException e) {
                    ToastUtils.show(ObjectListActivity.this, e.getI18nHint());
                    if (isLoadMore) adapter.loadMoreFail();
                }

            }
        });
    }

    @Override
    protected void init() {
        if (model == null) {
            ToastUtils.show(this, R.string.parameter_error);
            finish();
            return;
        }

        bucket = model.getBucket();
        prefix = model.getPrefix();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Title name
            if (TextUtils.isEmpty(prefix) || "/".equals(prefix)) {
                // Bucket Root
                actionBar.setTitle(bucket.getBucketName());
            } else {
                actionBar.setTitle(prefix);
            }
        }

        srObject.setOnRefreshListener(() -> listObject(false));

        adapter = new ObjectListAdapter(R.layout.item_object_list, null);
        TextView emptyView = new TextView(this);
        emptyView.setText(R.string.no_file_exits);
        emptyView.setGravity(Gravity.CENTER);
        adapter.setEmptyView(emptyView);
        rvObjectList.setLayoutManager(new LinearLayoutManager(this));
        rvObjectList.setAdapter(adapter);

        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(() -> listObject(true), rvObjectList);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            ListObjectsModel objectModel = (ListObjectsModel) adapter.getData().get(position);
            Types.KeyModel file = objectModel.getFile();
            if (file == null) {
                // Click The Folder
                ObjectListEventModel model = new ObjectListEventModel(bucket);
                model.setPrefix(objectModel.getFolder());
                EventBus.getDefault().postSticky(new MessageEvent<>(Constants.LIST_OBJECTS, model));
                startActivity(new Intent(this, ObjectListActivity.class));
            } else {
                // Click The File
                if (Downloader.getInstance().getTaskMap().containsKey(file.getKey() + file.getEtag())) {
                    ToastUtils.show(this, R.string.download_task_exist);
                } else {
                    Downloader.request(file.getKey() + file.getEtag(), bucket, file.getKey())
                            .save().start();
                    ToastUtils.show(this, R.string.download_task_added);
                }

            }

        });

        adapter.setOnItemChildClickListener(this::showDeleteConfirmDialog);

        srObject.setRefreshing(true);
        listObject(false);
    }

    private void showDeleteConfirmDialog(BaseQuickAdapter adapter, View view, int position) {
        if (view.getId() != R.id.iv_delete) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.sure_to_delete)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteObject(adapter, position))
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    private void deleteObject(BaseQuickAdapter adapter, int position) {
        showLoading(R.string.brvah_loading);
        ListObjectsModel objectModel = (ListObjectsModel) adapter.getData().get(position);
        Types.KeyModel file = objectModel.getFile();
        DownloadTask downloadTask = Downloader.getInstance().getTaskMap().get(file.getEtag());
        if (downloadTask != null) {
            downloadTask.remove(true);
        }
        bucket.deleteObjectAsync(file.getKey(), new ResponseCallBack<Bucket.DeleteObjectOutput>() {
            @Override
            public void onAPIResponse(Bucket.DeleteObjectOutput output) {
                dismissLoading();
                try {
                    QingstorHelper.getInstance().handleResponse(output);
                    ToastUtils.show(ObjectListActivity.this, R.string.delete_successful);
                    adapter.remove(position);
                } catch (TaskException e) {
                    ToastUtils.show(ObjectListActivity.this, e.getI18nHint());
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveData(MessageEvent<ObjectListEventModel> messageEvent) {
        if (model == null && messageEvent != null && messageEvent.getCode() == Constants.LIST_OBJECTS) {
            model = messageEvent.getData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_object_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                EventBus.getDefault().postSticky(new MessageEvent<>(Constants.UPLOAD_LIST, model));
                startActivity(new Intent(this, UploadListActivity.class));
                return true;
            case R.id.action_download:
                startActivity(new Intent(this, DownloadListActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
