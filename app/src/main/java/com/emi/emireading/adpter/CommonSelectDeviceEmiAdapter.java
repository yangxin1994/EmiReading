package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.entities.CommonSelect;

import java.util.List;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年01月03日下午 02:48
 * @Email: 971613168@qq.com
 */

public class CommonSelectDeviceEmiAdapter extends BaseEmiAdapter<CommonSelect, BaseViewHolder> {
    private boolean checkBoxVisibility = true;

    public CommonSelectDeviceEmiAdapter(@Nullable List<CommonSelect> data) {
        super(R.layout.item_select_common, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, CommonSelect commonSelect) {
        helper.setText(R.id.tvContent, commonSelect.getContent());
        helper.setChecked(R.id.rb_select, commonSelect.isSelect());
        helper.setVisible(R.id.rb_select, checkBoxVisibility);
    }


    public void select(List<CommonSelect> data, int position) {
        for (int i = 0; i < data.size(); i++) {
            if (position == i) {
                data.get(i).setSelect(true);
            } else {
                data.get(i).setSelect(false);
            }
        }
        notifyDataSetChanged();
    }

    public void setCheckBoxVisibility(boolean visibility) {
        checkBoxVisibility = visibility;
    }
}
