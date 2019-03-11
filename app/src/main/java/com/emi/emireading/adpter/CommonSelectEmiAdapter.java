package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;

import java.util.List;

/**
 * @author :zhoujian
 * @description : 选择加载文件适配器
 * @company :翼迈科技
 * @date: 2017年12月06日下午 12:36
 * @Email: 971613168@qq.com
 */

public class CommonSelectEmiAdapter extends BaseEmiAdapter<String,BaseViewHolder> {
    public CommonSelectEmiAdapter(@Nullable List<String> data) {
        super(R.layout.item_select_file, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String fileName) {
        helper.setText(R.id.tvSelectFileName,fileName);
    }

}
