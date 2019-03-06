package com.chengww.demo.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chengww.demo.R;
import com.chengww.demo.view.CircleProgressBar;
import com.chengww.qingstor_sdk_android.db.Progress;
import com.chengww.qingstor_sdk_android.exception.TaskException;
import com.chengww.qingstor_sdk_android.listener.UploadListener;
import com.chengww.qingstor_sdk_android.task.UploadTask;
import com.chengww.qingstor_sdk_android.task.Uploader;

import java.util.List;

/**
 * Created by chengww on 2019/3/4.
 */
public class UploadListAdapter extends BaseQuickAdapter<UploadTask<?>, BaseViewHolder> {

    public UploadListAdapter(@Nullable List<UploadTask<?>> data) {
        super(R.layout.item_download_list, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UploadTask<?> item) {
        Progress progress = item.progress;
        helper.setText(R.id.tv_title, progress.objectKey)
                .setTag(R.id.tv_title, progress.tag)
                .addOnClickListener(R.id.progress);
        UploadTask<String> task = (UploadTask<String>) Uploader.getInstance().getTask(progress.tag);
        if (task == null) return;
        task.register(new ListUploadListener(progress.tag, helper));
        refresh(helper, progress);
    }

    public void refresh(BaseViewHolder helper, Progress item) {
        CircleProgressBar progressBar = helper.getView(R.id.progress);
        switch (item.status) {
            default:
            case Progress.WAITING:
                progressBar.setStatus(CircleProgressBar.Status.Waiting);
                helper.setText(R.id.tv_content, R.string.waiting);
                break;
            case Progress.NONE:
            case Progress.PAUSE:
                progressBar.setStatus(CircleProgressBar.Status.Pause);
                helper.setText(R.id.tv_content, R.string.paused);
                break;
            case Progress.LOADING:
                progressBar.setStatus(CircleProgressBar.Status.Loading);

                String currentSize = Formatter.formatFileSize(mContext, item.currentSize);
                String totalSize = Formatter.formatFileSize(mContext, item.totalSize);
                String speed = Formatter.formatFileSize(mContext, item.speed);
                int progress = (int) (item.currentSize * 100 / item.totalSize);
                if (progress > 100) progress = 100;
                if (progress < 0) progress = 0;
                helper.setText(R.id.tv_content, currentSize + "/" + totalSize + " · " + speed + "/s");
                progressBar.setProgress(progress);
                break;
            case Progress.ERROR:
                progressBar.setStatus(CircleProgressBar.Status.Error);
                String errorMessage = mContext.getString(R.string.unknown_error);
                TaskException exception = item.exception;
                if (exception != null && !TextUtils.isEmpty(exception.getI18nHint())) {
                    errorMessage = exception.getI18nHint();
                }
                helper.setText(R.id.tv_content, errorMessage);
                break;
            case Progress.FINISH:
                progressBar.setStatus(CircleProgressBar.Status.Finish);
                helper.setText(R.id.tv_content, R.string.finished);
                break;
        }
    }

    private class ListUploadListener extends UploadListener<String> {

        private BaseViewHolder helper;

        ListUploadListener(String tag, BaseViewHolder helper) {
            super(tag);
            this.helper = helper;
        }

        @Override
        public void onStart(Progress progress) {
            if (progress.tag.equals(helper.getView(R.id.tv_title).getTag())) {
                refresh(helper, progress);
            }
        }

        @Override
        public void onProgress(Progress progress) {
            if (progress.tag.equals(helper.getView(R.id.tv_title).getTag())) {
                refresh(helper, progress);
            }
        }

        @Override
        public void onError(Progress progress) {
            if (progress.tag.equals(helper.getView(R.id.tv_title).getTag())) {
                refresh(helper, progress);
            }
        }

        @Override
        public void onFinish(String s, Progress progress) {
            if (progress.tag.equals(helper.getView(R.id.tv_title).getTag())) {
                refresh(helper, progress);
            }
        }

        @Override
        public void onRemove(Progress progress) {
            if (progress.tag.equals(helper.getView(R.id.tv_title).getTag())) {
                refresh(helper, progress);
            }
        }
    }


}