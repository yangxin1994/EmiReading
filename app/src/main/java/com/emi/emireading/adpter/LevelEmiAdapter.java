package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.entities.AreaInfo;

import java.util.List;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年01月30日下午 01:29
 * @Email: 971613168@qq.com
 */

public class LevelEmiAdapter extends BaseEmiAdapter<AreaInfo,BaseViewHolder> {

    public LevelEmiAdapter(@Nullable List<AreaInfo> data) {
         super(R.layout.item_level_layout, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, AreaInfo areaInfo) {
        helper.setText(R.id.tvLevelContent,areaInfo.getXlmc());
    }


}
