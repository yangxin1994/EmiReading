package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.entities.UserInfo;

import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;

/**
 * @author :zhoujian
 * @description : 自动抄表
 * @company :翼迈科技
 * @date 2018年06月04日上午 08:41
 * @Email: 971613168@qq.com
 */

public class AutoReadEmiAdapter extends BaseEmiAdapter<UserInfo, BaseViewHolder> {


    public AutoReadEmiAdapter(@Nullable List<UserInfo> data) {
        super(R.layout.vlistview, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserInfo userInfo) {
        helper.setText(R.id.title, EmiStringUtil.formatNull(userInfo.accountnum));
        helper.setText(R.id.info, EmiStringUtil.formatNull(userInfo.username));
        helper.setText(R.id.yl, userInfo.curyl+"");
        switch (userInfo.state) {
            case STATE_FAILED:
                helper.setImageResource(R.id.imageView2, R.mipmap.red);
                break;
            case STATE_SUCCESS:
                helper.setImageResource(R.id.imageView2, R.mipmap.star);
                break;
            case STATE_WARNING:
                helper.setImageResource(R.id.imageView2, R.mipmap.know);
                break;
            case STATE_PEOPLE_RECORDING:
                helper.setImageResource(R.id.imageView2, R.mipmap.star);
                break;
            default:
                helper.setImageResource(R.id.imageView2, R.mipmap.red);
                break;
        }
    }
}
