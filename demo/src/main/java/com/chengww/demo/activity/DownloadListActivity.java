package com.chengww.demo.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.chengww.demo.R;
import com.chengww.demo.adapter.DownloadListAdapter;
import com.chengww.demo.utils.BindContentView;
import com.chengww.qingstor_sdk_android.db.Progress;
import com.chengww.qingstor_sdk_android.task.DownloadTask;
import com.chengww.qingstor_sdk_android.task.Downloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

@BindContentView(R.layout.activity_download_list)
public class DownloadListActivity extends BaseActivity {

    @BindView(R.id.rv_download_list)
    RecyclerView rvDownloadList;
    @BindView(R.id.tv_download_path)
    TextView tvDownloadPath;
    private DownloadListAdapter adapter;

    @Override
    protected void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.download_list);
        }
        tvDownloadPath.setText(getString(R.string.download_path) + Downloader.getInstance().getFolder());

        adapter = new DownloadListAdapter(null);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            DownloadTask task = (DownloadTask) adapter.getData().get(position);
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
                case Progress.FINISH:
                    break;
            }
        });

        notifyList();

        TextView empty = new TextView(this);
        empty.setText(R.string.no_download_task);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 40, 0, 40);
        adapter.setEmptyView(empty);

        rvDownloadList.setLayoutManager(new LinearLayoutManager(this));
        rvDownloadList.setAdapter(adapter);

    }

    private void notifyList() {
        Map<String, DownloadTask> taskMap = Downloader.getInstance().getTaskMap();
        Collection<DownloadTask> values = taskMap.values();
        List<DownloadTask> tasks;
        if (values instanceof List) {
            tasks = (List<DownloadTask>) values;
        } else {
            tasks = new ArrayList<>(values);
        }
        adapter.setNewData(tasks);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Map<String, DownloadTask> taskMap = Downloader.getInstance().getTaskMap();
        for (DownloadTask task : taskMap.values()) {
            task.unRegister(task.progress.tag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_download_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                Downloader.getInstance().removeAll(true);
                notifyList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
