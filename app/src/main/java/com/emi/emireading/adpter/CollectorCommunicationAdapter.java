package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.entities.UserInfo;

import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.EMPTY_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.ERROR_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_READ_ED;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_WRITE;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_NO_WRITE;

/**
 * @author :zhoujian
 * @description : 采集器通讯列表适配器
 * @company :翼迈科技
 * @date 2018年06月21日上午 11:36
 * @Email: 971613168@qq.com
 */

public class CollectorCommunicationAdapter extends BaseEmiAdapter<UserInfo, BaseViewHolder> {
    public static final int FAILED_DATA = -1;
    public static final int EMPTY_DATA = -2;

    public CollectorCommunicationAdapter(@Nullable List<UserInfo> data) {
        super(R.layout.item_layout_meter_info, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserInfo item) {
        helper.setText(R.id.tvMeterId, item.meteraddr);
        switch (item.curdata) {
            case EMPTY_DATA:
                helper.setText(R.id.tvMeterData, EMPTY_METER_DATA);
                break;
            case FAILED_DATA:
                helper.setText(R.id.tvMeterData, ERROR_METER_DATA);
                break;
            default:
                if (item.curdata >= 0) {
                    helper.setText(R.id.tvMeterData, String.valueOf(item.curdata));
                } else {
                    if (item.state == STATE_FAILED || item.state == STATE_NO_READ || item.state == STATE_ALL) {
                        helper.setText(R.id.tvMeterData, ERROR_METER_DATA);
                    } else {
                        LogUtil.d(TAG, "状态：" + item.state);
                        helper.setText(R.id.tvMeterData, String.valueOf(item.curdata));
                    }
                }
                break;
        }

        helper.setText(R.id.tvMeterLocation, EmiStringUtil.formatNull(item.useraddr));
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
