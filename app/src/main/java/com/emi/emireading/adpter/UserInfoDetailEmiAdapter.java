package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.entities.DetailInfo;

import java.util.List;

/**
 * @author :zhoujian
 * @description : 用户详情适配器
 * @company :翼迈科技
 * @date 2018年03月09日下午 02:32
 * @Email: 971613168@qq.com
 */

public class UserInfoDetailEmiAdapter extends BaseEmiAdapter<DetailInfo, BaseViewHolder> {
    public UserInfoDetailEmiAdapter(@Nullable List<DetailInfo> data) {
        super(R.layout.item_layout_userinfo_detail, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DetailInfo detailInfo) {
        helper.setText(R.id.tvLabel, detailInfo.getLabel(),detailInfo.getLabelColor());
        helper.setText(R.id.tvInfo, detailInfo.getValue(), detailInfo.getValueColor());
    }


}
