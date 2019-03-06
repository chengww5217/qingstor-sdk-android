package com.chengww.demo.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chengww.demo.R;
import com.qingstor.sdk.service.Types;

import java.util.List;

/**
 * Created by chengww on 2019/3/4.
 */
public class BucketListAdapter extends BaseQuickAdapter<Types.BucketModel, BaseViewHolder> {
    public BucketListAdapter(int layoutResId, List<Types.BucketModel> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Types.BucketModel item) {
        helper.setText(R.id.tv_title, item.getName())
                .setText(R.id.tv_content, item.getURL());
    }
}

