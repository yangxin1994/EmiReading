package com.emi.emireading.ui.setting;

import android.content.Context;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.ui.debug.ChannelDebugActivityNew;
import com.emi.emireading.ui.debug.SingleMeterDebugActivity;

/**
 * @author :zhoujian
 * @description : 调试页面
 * @company :翼迈科技
 * @date 2018年06月08日上午 10:09
 * @Email: 971613168@qq.com
 */

public class EmiDebugActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_debug_layout;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        findViewById(R.id.mivMeterDebug).setOnClickListener(this);
        findViewById(R.id.mivChannelDebug).setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mivMeterDebug:
//                openActivity(mContext, SingleMeterDebugActivityTest.class);
                openActivity(mContext, SingleMeterDebugActivity.class);
                break;
            case R.id.mivChannelDebug:
                openActivity(mContext, ChannelDebugActivityNew.class);
                break;
            default:
                break;
        }
    }
}
