package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.entities.UserInfo;

import java.util.List;

import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_READ_ED;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_WRITE;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_NO_WRITE;

/**
 * @author :zhoujian
 * @description : 水表信息适配器
 * @company :翼迈科技
 * @date 2018年05月23日下午 12:58
 * @Email: 971613168@qq.com
 */

public class MeterInfoEmiAdapter extends BaseEmiAdapter<UserInfo, BaseViewHolder> {
    public MeterInfoEmiAdapter(@Nullable List<UserInfo> data) {
        super(R.layout.item_layout_meter_info, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserInfo item) {
        helper.setText(R.id.tvMeterId, item.meteraddr);
        switch (item.state) {
            case EmiConstants.STATE_FAILED:
                helper.setText(R.id.tvMeterData, EmiConstants.ERROR_METER_DATA);
                break;
            case EmiConstants.STATE_NO_READ:
                helper.setText(R.id.tvMeterData, EmiConstants.ERROR_METER_DATA);
                break;
            default:
                helper.setText(R.id.tvMeterData, String.valueOf(item.curdata));
                break;
        }

        helper.setText(R.id.tvMeterLocation, item.useraddr);
        switch (item.uploadState) {
            case STATE_NO_WRITE:
                //未写入
                helper.setImageResource(R.id.ivState, R.mipmap.image_no_write);
                break;
            case STATE_HAS_READ_ED:
                //已读取
                helper.setImageResource(R.id.ivState, R.mipmap.image_has_read);
                break;
            case STATE_HAS_WRITE:
                //待读取
                helper.setImageResource(R.id.ivState, R.mipmap.image_wait_read);
                break;
            default:
                helper.setImageResource(R.id.ivState, R.mipmap.image_abnormal);
                break;
        }
    }
}
