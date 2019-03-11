package com.emi.emireading.ui.setting;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.PreferenceUtils;

import static com.emi.emireading.core.config.EmiConstants.CHANNEL_TYPE_KEY;
import static com.emi.emireading.core.config.EmiConstants.CHANNEL_TYPE_NORMAL;
import static com.emi.emireading.core.config.EmiConstants.CHANNEL_TYPE_SPECIAL;

/**
 * 通道板协议配置
 * @author :zhoujian
 * @company :翼迈科技
 * @date: 2017年08月09日上午 10:51
 * @Email: 971613168@qq.com
 */

public class ChannelProtocolSettingActivity extends BaseActivity implements View.OnClickListener{
    private RelativeLayout relay_7833;
    private RelativeLayout relay_1001;
    private ImageView iv7833;
    private ImageView iv1001;
    @Override
    protected int getContentLayout() {
        return R.layout.activity_select_protocal;
    }

    @Override
    protected void initIntent() {

    }

    @Override
    protected void initUI() {
        relay_7833 = (RelativeLayout) findViewById(R.id.relay_7833);
        relay_1001 = (RelativeLayout) findViewById(R.id.relay_1001);
        iv7833 = (ImageView) findViewById(R.id.iv7833);
        iv1001 = (ImageView) findViewById(R.id.iv1001);
        relay_7833.setOnClickListener(this);
        relay_1001.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        loadChannelType();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.relay_7833:
                iv7833.setVisibility(View.VISIBLE);
                iv1001.setVisibility(View.GONE);
                PreferenceUtils.putInt(CHANNEL_TYPE_KEY,CHANNEL_TYPE_NORMAL);
                finish();
                break;
            case R.id.relay_1001:
                iv7833.setVisibility(View.GONE);
                iv1001.setVisibility(View.VISIBLE);
                PreferenceUtils.putInt(CHANNEL_TYPE_KEY,CHANNEL_TYPE_SPECIAL);
                finish();
                break;
                default:
                    break;
        }
    }
    private void loadChannelType(){
        int type = PreferenceUtils.getInt(CHANNEL_TYPE_KEY,CHANNEL_TYPE_SPECIAL);
        if (type == CHANNEL_TYPE_SPECIAL ) {
            iv7833.setVisibility(View.GONE);
            iv1001.setVisibility(View.VISIBLE);
        }else if (type==CHANNEL_TYPE_NORMAL) {
            iv7833.setVisibility(View.VISIBLE);
            iv1001.setVisibility(View.GONE);
        }

    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }
}
