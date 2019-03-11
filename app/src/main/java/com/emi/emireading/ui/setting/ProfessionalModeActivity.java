package com.emi.emireading.ui.setting;

import android.content.Context;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;

/**
 * @author :zhoujian
 * @description : 专业模式
 * @company :翼迈科技
 * @date 2018年06月08日上午 10:21
 * @Email: 971613168@qq.com
 */

public class ProfessionalModeActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mivDebug:
                openActivity(mContext, EmiDebugActivity.class);
                break;
            case R.id.mivReadMeterSetting:
                openActivity(mContext, ReadMeterSettingActivity.class);
                break;
            case R.id.mivExportSetting:
                openActivity(mContext, ExportSettingActivity.class);
                break;
            case R.id.mivOtherSetting:
                openActivity(mContext, OtherSettingActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_professional_mode;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        findViewById(R.id.mivDebug).setOnClickListener(this);
        findViewById(R.id.mivReadMeterSetting).setOnClickListener(this);
        findViewById(R.id.mivExportSetting).setOnClickListener(this);
        findViewById(R.id.mivOtherSetting).setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }
}
