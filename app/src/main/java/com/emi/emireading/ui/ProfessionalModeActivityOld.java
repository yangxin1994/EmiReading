package com.emi.emireading.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.ui.debug.ChannelDebugActivityNew;
import com.emi.emireading.ui.debug.SingleMeterDebugActivityTest;
import com.emi.emireading.ui.setting.ExportSettingActivity;
import com.emi.emireading.ui.setting.ReadingDelaySettingActivity;
import com.emi.emireading.widget.view.MenuItemView;
import com.emi.emireading.widget.view.dialog.InputDialog;

import static com.emi.emireading.common.EmiUtils.saveIsEnableModifyFile;
import static com.emi.emireading.core.config.EmiConstants.PREF_PHONE_NUMBER;

/**
 * @author :zhoujian
 * @description : 专业模式
 * @company :翼迈科技
 * @date 2018年05月07日上午 11:34
 * @Email: 971613168@qq.com
 */

public class ProfessionalModeActivityOld extends BaseActivity implements View.OnClickListener {
    private MenuItemView mivShowPeopleRecord;
    private MenuItemView mivFileEdit;
    private Context mContext;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_professional_setting_old;
    }

    @Override
    protected void initIntent() {
        mContext = ProfessionalModeActivityOld.this;
    }

    @Override
    protected void initUI() {
        mivShowPeopleRecord = findViewById(R.id.mivShowPeopleRecord);
        findViewById(R.id.mivReadingDelay).setOnClickListener(this);
        findViewById(R.id.mivSingleDebug).setOnClickListener(this);
        findViewById(R.id.mivExportSetting).setOnClickListener(this);
        findViewById(R.id.mivChannelDebug).setOnClickListener(this);
        findViewById(R.id.mivSetContact).setOnClickListener(this);
        mivFileEdit = findViewById(R.id.mivFileEdit);
        mivShowPeopleRecord.setOnToggleChangedlistener(new MenuItemView.OnToggleChangedListener() {
            @Override
            public void onToggle(boolean on) {
                EmiUtils.savePeopleRecordSetting(on);
            }
        });
        mivFileEdit.setOnToggleChangedlistener(new MenuItemView.OnToggleChangedListener() {
            @Override
            public void onToggle(boolean on) {
                saveIsEnableModifyFile(on);
            }
        });
    }

    @Override
    protected void initData() {
        mivShowPeopleRecord.toggleByState(EmiUtils.isShowPeopleRecord());
        mivFileEdit.toggleByState(EmiUtils.isEnableModifyFile());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mivSingleDebug:
                openActivity(mContext, SingleMeterDebugActivityTest.class);
                break;
            case R.id.mivExportSetting:
                openActivity(mContext, ExportSettingActivity.class);
                break;
            case R.id.mivShowPeopleRecord:
                break;
            case R.id.mivChannelDebug:
                openActivity(mContext, ChannelDebugActivityNew.class);
                break;
            case R.id.mivReadingDelay:
                openActivity(mContext, ReadingDelaySettingActivity.class);
                break;
            case R.id.mivSetContact:
                showInputDialog();
                break;
            default:
                break;
        }
    }


    private void showInputDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("请输入联系方式");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String inputMsg = builder.editText.getText().toString();
                if (EmiUtils.isPhoneNumber(inputMsg)) {
                    EmiConfig.EMI_PHONE = inputMsg;
                    PreferenceUtils.putString(PREF_PHONE_NUMBER, inputMsg);
                    ToastUtil.showShortToast("号码已保存");
                    dialog.dismiss();
                } else {
                    ToastUtil.showShortToast("请输入正确手机号");
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


}



