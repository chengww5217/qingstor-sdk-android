package com.chengww.demo.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.chengww.demo.R;
import com.chengww.demo.adapter.UploadListAdapter;
import com.chengww.demo.constants.Constants;
import com.chengww.demo.model.MessageEvent;
import com.chengww.demo.model.ObjectListEventModel;
import com.chengww.demo.utils.BindContentView;
import com.chengww.demo.utils.BindEventBus;
import com.chengww.demo.utils.FileUtils;
import com.chengww.demo.utils.ToastUtils;
import com.chengww.qingstor_sdk_android.db.Progress;
import com.chengww.qingstor_sdk_android.task.UploadTask;
import com.chengww.qingstor_sdk_android.task.Uploader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

@BindEventBus
@BindContentView(R.layout.activity_upload_list)
public class UploadListActivity extends BaseActivity {

    @BindView(R.id.rv_upload_list)
    RecyclerView rvUploadList;
    private UploadListAdapter adapter;
    private ObjectListEventModel model;

    @Override
    protected void init() {
        if (model == null) {
            ToastUtils.show(this, R.string.parameter_error);
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.upload_list);
        }

        adapter = new UploadListAdapter(null);
        notifyList();

        TextView empty = new TextView(this);
        empty.setText(R.string.no_upload_task);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 40, 0, 40);
        adapter.setEmptyView(empty);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            UploadTask<?> task = (UploadTask<?>) adapter.getData().get(position);
            if (view.getId() != R.id.progress || task == null) return;
            switch (task.progress.status) {
                case Progress.NONE:
                case Progress.PAUSE:
                case Progress.ERROR:
                    task.start();
                    break;
                case Progress.LOADING:
                case Progress.WAITING:
                    task.pause();
                    break;
            }
        });

        rvUploadList.setLayoutManager(new LinearLayoutManager(this));
        rvUploadList.setAdapter(adapter);
    }

    private void notifyList() {
        Map<String, UploadTask<?>> taskMap = Uploader.getInstance().getTaskMap();
        Collection<UploadTask<?>> values = taskMap.values();
        List<UploadTask<?>> tasks;
        if (values instanceof List) {
            tasks = (List<UploadTask<?>>) values;
        } else {
            tasks = new ArrayList<>(values);
        }
        adapter.setNewData(tasks);
    }

    private void selectFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_files)), Constants.SELECT_FILES);
        } catch (ActivityNotFoundException e) {
            ToastUtils.show(this, R.string.no_file_select_app);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.SELECT_FILES:
                    Uri uri = data.getData();
                    if (uri != null) {

                        // Copy the file to cache
                        showLoading(R.string.brvah_loading);
                        new Thread(() -> {
                            String path = null;
                            try {
                                path = FileUtils.getPathByUri(UploadListActivity.this, uri);
                            } catch (IllegalArgumentException exception) {

                                try {
                                    File cacheDir = getExternalCacheDir();
                                    if (cacheDir == null || !cacheDir.exists()) {
                                        cacheDir = getCacheDir();
                                    }
                                    path = cacheDir.getAbsolutePath() + File.separator + FileUtils.getUriFileName(uri);

                                    File file = new File(path);
                                    InputStream inputStream = UploadListActivity.this.getContentResolver().openInputStream(uri);
                                    if (inputStream == null) return;
                                    OutputStream outputStream = new FileOutputStream(file);
                                    int len;
                                    byte[] buffer = new byte[40960];
                                    while ((len = inputStream.read(buffer)) != -1)
                                        outputStream.write(buffer, 0, len);
                                    inputStream.close();
                                    outputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                            File copiedFile = new File(path);
                            if (copiedFile.exists()) {
                                model.setFile(copiedFile);
                                EventBus.getDefault().postSticky(new MessageEvent<>(Constants.UPLOAD_LIST_FILE_COPIED, model));
                            } else {
                                EventBus.getDefault().postSticky(new MessageEvent<>(Constants.UPLOAD_LIST_FILE_UNCOPIED, model));
                            }
                        }).start();

                    }
                    break;
                case Constants.UPLOAD_DETAIL:
                    notifyList();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Map<String, UploadTask<?>> taskMap = Uploader.getInstance().getTaskMap();
        for (UploadTask<?> task : taskMap.values()) {
            task.unRegister(task.progress.tag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_upload_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                checkPermissions();
                return true;
            case R.id.action_clear:
                Uploader.getInstance().removeAll();
                notifyList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveData(MessageEvent<ObjectListEventModel> messageEvent) {
        switch (messageEvent.getCode()) {
            case Constants.UPLOAD_LIST:
                if (model == null) {
                    model = messageEvent.getData();
                }
                break;
            case Constants.UPLOAD_LIST_FILE_COPIED:
                EventBus.getDefault().removeStickyEvent(messageEvent);
                dismissLoading();
                EventBus.getDefault().postSticky(new MessageEvent<>(Constants.UPLOAD_DETAIL, model));
                startActivityForResult(new Intent(this, UploadDetailActivity.class),
                        Constants.UPLOAD_DETAIL);
                break;
            case Constants.UPLOAD_LIST_FILE_UNCOPIED:
                EventBus.getDefault().removeStickyEvent(messageEvent);
                dismissLoading();
                ToastUtils.show(this, R.string.file_uncopied);
                break;
        }

    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static int REQUEST_PERMISSION_CODE = 1;

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            } else {
                selectFiles();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean hasPermission = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    hasPermission = false;
                    break;
                }
            }

            if (hasPermission) selectFiles();
            else ToastUtils.show(this, R.string.permission_defined);
        }
    }
}
