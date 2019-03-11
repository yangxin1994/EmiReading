package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.entities.UserInfo;

import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.EMPTY_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.ERROR_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;


/**
 * @author :zhoujian
 * @description : 用户列表适配器
 * @company :翼迈科技
 * @date: 2017年10月23日下午 02:26
 * @Email: 971613168@qq.com
 */

public class UserListEmiAdapter extends BaseEmiAdapter<UserInfo,BaseViewHolder> {
    public UserListEmiAdapter(@Nullable List<UserInfo> data) {
        super(R.layout.list_item_query, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserInfo item) {
        helper.setText(R.id.tvUserId,item.accountnum);
        helper.setText(R.id.tvAddress,item.useraddr);
        helper.setText(R.id.tvMeterAddress,item.meteraddr);
        switch (item.state) {
            case STATE_FAILED:
                helper.setText(R.id.tvCurrentUseAge,ERROR_METER_DATA);
                break;
            case STATE_NO_READ:
                helper.setText(R.id.tvCurrentUseAge,EMPTY_METER_DATA);
                break;
            default:
                helper.setText(R.id.tvCurrentUseAge,item.curyl+"");
                break;
        }

        switch (item.state){
            case STATE_NO_READ:
                //未抄
                helper.setImageResource(R.id.ivState,(R.mipmap.red));
                break;
            case STATE_FAILED:
                //失败
                helper.setImageResource(R.id.ivState,(R.mipmap.red));
                break;
            case STATE_SUCCESS:
                //正常
                helper.setImageResource(R.id.ivState,(R.mipmap.star));
                break;
            case STATE_WARNING:
                //用数量异常
                helper.setImageResource(R.id.ivState,(R.mipmap.know));
                break;
            case STATE_PEOPLE_RECORDING:
                //人工补录
                helper.setImageResource(R.id.ivState,(R.mipmap.star));
                break;
                default:
                    break;
            }
    }


}
