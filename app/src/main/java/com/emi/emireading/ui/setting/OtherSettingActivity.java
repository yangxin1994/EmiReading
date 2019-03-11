package com.emi.emireading.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.widget.view.MenuItemView;
import com.emi.emireading.widget.view.dialog.InputDialog;

import static com.emi.emireading.common.EmiUtils.saveIsEnableModifyFile;
import static com.emi.emireading.core.config.EmiConstants.PREF_PHONE_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.PREF_REMARK;

/**
 * @author :zhoujian
 * @description : 其他设置
 * @company :翼迈科技
 * @date 2018年06月08日上午 10:28
 * @Email: 971613168@qq.com
 */

public class OtherSettingActivity extends BaseActivity implements View.OnClickListener {
    private MenuItemView mivShowPeopleRecord;
    private MenuItemView mivFileEdit;
    private Context mContext;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mivSetContact:
                showInputDialog();
                break;
            case R.id.mivRemark:
                showInputRemarkDialog();
                break;
            default:
                break;
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_other_setting;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        mivFileEdit = findViewById(R.id.mivFileEdit);
        mivShowPeopleRecord = findViewById(R.id.mivShowPeopleRecord);
        findViewById(R.id.mivRemark).setOnClickListener(this);
        mivFileEdit.setOnClickListener(this);
        findViewById(R.id.mivSetContact).setOnClickListener(this);
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


    private void showInputRemarkDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("请输入您要备注的信息");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String inputMsg = builder.editText.getText().toString();
                if (!TextUtils.isEmpty(inputMsg)) {
                    EmiConfig.EMI_REMARK = inputMsg;
                    PreferenceUtils.putString(PREF_REMARK, inputMsg);
                    ToastUtil.showShortToast("备注已保存");
                    dialog.dismiss();
                } else {
                    ToastUtil.showShortToast("您未输入任何信息");
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog dialog = builder.create();
        builder.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setInputMaxLength(40);
        builder.editText.setText(EmiConfig.EMI_REMARK);
        builder.editText.setSelection(EmiConfig.EMI_REMARK.length());
        dialog.show();
    }

}
