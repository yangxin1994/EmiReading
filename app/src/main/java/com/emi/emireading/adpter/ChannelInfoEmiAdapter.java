package com.emi.emireading.adpter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.entities.UserInfo;

import java.util.List;

/**
 * @author :zhoujian
 * @description : 通道号信息适配器
 * @company :翼迈科技
 * @date 2018年03月15日上午 10:26
 * @Email: 971613168@qq.com
 */

public class ChannelInfoEmiAdapter extends BaseEmiAdapter<UserInfo, BaseViewHolder> {

    public ChannelInfoEmiAdapter(@Nullable List<UserInfo> data) {
        super(R.layout.item_channel_list, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserInfo userInfo) {
        helper.setText(R.id.tvChannelName, "通道号：" + userInfo.getChannelNumber());
        if (TextUtils.isEmpty(userInfo.channelAddress)) {
            helper.setText(R.id.tvChannelAddress, "安装地址：" + userInfo.useraddr);
        } else {
            helper.setText(R.id.tvChannelAddress, "安装地址：" + userInfo.getChannelAddress());
        }
        LogUtil.d("当前的channel"+userInfo.channel);
        if (EmiStringUtil.isEmpty(userInfo.channel)) {
            helper.setImageResource(R.id.ivReadState, R.mipmap.image_label_no_read);
        } else {
            helper.setImageResource(R.id.ivReadState, R.mipmap.image_label_read);
        }
    }
}
