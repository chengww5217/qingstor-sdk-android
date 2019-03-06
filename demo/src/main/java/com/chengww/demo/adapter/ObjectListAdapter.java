package com.chengww.demo.adapter;

import android.text.format.Formatter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chengww.demo.R;
import com.chengww.demo.model.ListObjectsModel;
import com.chengww.demo.utils.MyDateUtils;
import com.chengww.demo.utils.StringUtils;
import com.qingstor.sdk.service.Types;

import java.util.List;

/**
 * Created by chengww on 2019/3/4.
 */
public class ObjectListAdapter extends BaseQuickAdapter<ListObjectsModel, BaseViewHolder> {
    public ObjectListAdapter(int layoutResId, List<ListObjectsModel> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ListObjectsModel item) {
        Types.KeyModel file = item.getFile();
        String folder = item.getFolder();
        if (file == null) {
            helper.setGone(R.id.tv_content, false)
                    .setText(R.id.tv_title, StringUtils.getFileName(folder))
                    .setImageResource(R.id.iv_profile, R.drawable.ic_folder_40dp)
                    .setGone(R.id.iv_delete, false);
        } else {
            String modified = MyDateUtils.formateUTC(file.getCreated());
            String fileSize = Formatter.formatFileSize(mContext, file.getSize());
            String mimeType = file.getMimeType();

            helper.setGone(R.id.tv_content, true)
                    .setText(R.id.tv_title, StringUtils.getFileName(file.getKey()))
                    .setImageResource(R.id.iv_profile, R.drawable.ic_file_40dp)
                    .setText(R.id.tv_content, modified + " " + fileSize + " " + mimeType)
                    .setGone(R.id.iv_delete, true)
                    .addOnClickListener(R.id.iv_delete);

        }
    }
}

