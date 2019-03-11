package com.emi.emireading.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectDeviceEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.CommonSelect;
import com.emi.emireading.widget.view.MenuItemView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConfig.IS_SKIP_SELECT_FILE;
import static com.emi.emireading.core.config.EmiConstants.PREF_READING_DELAY;
import static com.emi.emireading.core.config.EmiConstants.READ_DELAY_DEFAULT;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;

/**
 * @author :zhoujian
 * @description : 抄表设置
 * @company :翼迈科技
 * @date 2018年06月08日上午 09:36
 * @Email: 971613168@qq.com
 */

public class ReadMeterSettingActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private final int METER_TYPE_SETTING_CODE = 101;
    private MenuItemView mivMeterTypeSetting;
    private MenuItemView mivAutoRepeatReadSetting;
    private MenuItemView mivAutoReadTypeSetting;
    private MenuItemView mivSkipFile;
    private MenuItemView mivReadingDelaySetting;
    public final static String PREF_IS_SKIP_SELECT_FILE = "PREF_IS_SKIP_SELECT_FILE";

    @Override
    protected int getContentLayout() {
        return R.layout.activity_read_meter_setting;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        mivAutoRepeatReadSetting = findViewById(R.id.mivAutoRepeatReadSetting);
        mivMeterTypeSetting = findViewById(R.id.mivMeterTypeSetting);
        mivSkipFile = findViewById(R.id.mivSkipFile);
        mivAutoReadTypeSetting = findViewById(R.id.mivAutoReadTypeSetting);
        mivReadingDelaySetting = findViewById(R.id.mivReadingDelaySetting);
        mivMeterTypeSetting.setOnClickListener(this);
        mivAutoRepeatReadSetting.setOnClickListener(this);
        mivReadingDelaySetting.setOnClickListener(this);
        mivAutoReadTypeSetting.setOnClickListener(this);
        mivSkipFile.toggleByState(IS_SKIP_SELECT_FILE);
    }

    @Override
    protected void initData() {
        EmiConfig.REPEAT_COUNT = EmiUtils.getAutoRepeatCount();
        EmiConfig.READING_DELAY = PreferenceUtils.getInt(PREF_READING_DELAY, READ_DELAY_DEFAULT);
        EmiConfig.AUTO_READ_TYPE = EmiUtils.getAutoReadType();
        showMeterType();
        showAutoReadTypeSetting();
        IS_SKIP_SELECT_FILE = PreferenceUtils.getBoolean(PREF_IS_SKIP_SELECT_FILE, false);
        mivSkipFile.setOnToggleChangedlistener(new MenuItemView.OnToggleChangedListener() {
            @Override
            public void onToggle(boolean on) {
                IS_SKIP_SELECT_FILE = on;
                if (on) {
                    new SweetAlertDialog(mContext, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                            .setTitleText("注意")
                            .setContentText("该功能开启后，会自动打开“读通道号”开关")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    EmiUtils.saveChannelSetting(true);
                                    PreferenceUtils.putBoolean(PREF_IS_SKIP_SELECT_FILE, IS_SKIP_SELECT_FILE);
                                }
                            })
                            .show();
                } else {
                    EmiUtils.saveChannelSetting(false);
                }
            }
        });
        showAutoRepeatSetting();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mivMeterTypeSetting:
                //水表类型设置
                startActivityForResult(new Intent(mContext, MeterTypeSettingActivity.class), METER_TYPE_SETTING_CODE);
                break;
            case R.id.mivReadingDelaySetting:
                //抄表延时设置
                openActivity(mContext, ReadingDelaySettingActivity.class);
                break;
            case R.id.mivAutoRepeatReadSetting:
                showAutoRepeatReadSettingDialog();
                break;
            case R.id.mivAutoReadTypeSetting:
                showAutoReadTypeSettingDialog();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case METER_TYPE_SETTING_CODE:
                if (resultCode == RESULT_OK) {
                    showMeterType();
                }
                break;
            default:
                break;
        }
    }

    private void showMeterType() {
        StringBuilder sb = new StringBuilder("");
        switch (EmiUtils.getStandardType()) {
            case EmiConstants.STANDARD_TYPE_SPECIAL:
                sb.append("特殊市场（");
                break;
            case EmiConstants.STANDARD_TYPE_NORMAL:
                sb.append("普通市场（");
                break;
            default:
                sb.append("普通市场（");
                break;
        }
        switch (EmiUtils.getMeterType()) {
            case EmiConstants.METER_TYPE_DUI:
                sb.append("对射式水表）");
                break;
            case EmiConstants.METER_TYPE_FAN:
                sb.append("反射式水表）");
                break;
            default:
                sb.append("对射式水表）");
                break;
        }
        mivMeterTypeSetting.setRightLabel(sb.toString());
    }

    /**
     * 自动补抄设置对话框
     */
    private void showAutoRepeatReadSettingDialog() {
        final List<CommonSelect> commonSelectList = new ArrayList<>();
        CommonSelect commonSelect = new CommonSelect("关闭", true);
        CommonSelect commonSelect1 = new CommonSelect("一次", false);
        CommonSelect commonSelect2 = new CommonSelect("三次", false);
        commonSelectList.add(commonSelect);
        commonSelectList.add(commonSelect1);
        commonSelectList.add(commonSelect2);
        final CommonSelectDeviceEmiAdapter commonSelectAdapter = new CommonSelectDeviceEmiAdapter(commonSelectList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("自动补抄设置");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                EmiUtils.setAutoRepeatCount(EmiConfig.REPEAT_COUNT);
                showAutoRepeatSetting();
                ToastUtil.showShortToast("设置成功");
            }
        });
        final CommonSelectDialog commonSelectDialog = builder.create();
        commonSelectDialog.setCancelable(true);
        commonSelectDialog.setCanceledOnTouchOutside(true);
        commonSelectAdapter.bindToRecyclerView(builder.recyclerView);
        commonSelectAdapter.select(commonSelectList, getRepeatSettingIndex());
        commonSelectAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                commonSelectAdapter.select(commonSelectList, position);
                switch (position) {
                    case 0:
                        EmiConfig.REPEAT_COUNT = 0;
                        break;
                    case 1:
                        EmiConfig.REPEAT_COUNT = 1;
                        break;
                    case 2:
                        EmiConfig.REPEAT_COUNT = 3;
                        break;
                    default:
                        EmiConfig.REPEAT_COUNT = 1;
                        break;
                }
            }
        });
        builder.setAdapter(commonSelectAdapter);
        commonSelectDialog.show();
    }

    private int getRepeatSettingIndex() {
        switch (EmiConfig.REPEAT_COUNT) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 3:
                return 2;
            default:
                return 1;
        }
    }

    private int getAutoReadSettingTypeIndex() {
        switch (EmiConfig.AUTO_READ_TYPE) {
            case 0:
                return 0;
            case EmiConstants.STATE_NO_READ:
                return 1;
            case EmiConstants.STATE_WARNING:
                return 2;
            case EmiConstants.STATE_SUCCESS:
                return 3;
            case EmiConstants.STATE_FAILED:
                return 4;
            default:
                return 0;
        }
    }

    private void showAutoRepeatSetting() {
        switch (EmiConfig.REPEAT_COUNT) {
            case 0:
                mivAutoRepeatReadSetting.setRightLabel("关闭");
                break;
            case 1:
                mivAutoRepeatReadSetting.setRightLabel("一次");
                break;
            case 3:
                mivAutoRepeatReadSetting.setRightLabel("三次");
                break;
            default:
                mivAutoRepeatReadSetting.setRightLabel("一次");
                break;
        }
    }


    private void showAutoReadTypeSetting() {
        switch (EmiConfig.AUTO_READ_TYPE) {
            case 0:
                mivAutoReadTypeSetting.setRightLabel("抄全部表");
                break;
            case STATE_NO_READ:
                mivAutoReadTypeSetting.setRightLabel("抄未抄表");
                break;
            case STATE_WARNING:
                mivAutoReadTypeSetting.setRightLabel("抄异常表");
                break;
            case STATE_FAILED:
                mivAutoReadTypeSetting.setRightLabel("抄失败表");
                break;
            default:
                mivAutoReadTypeSetting.setRightLabel("抄全部表");
                break;
        }
    }

    private void showDelaySetting() {
        mivReadingDelaySetting.setRightLabel((float) EmiConfig.READING_DELAY / 1000 + "秒");
    }

    @Override
    protected void onResume() {
        showDelaySetting();
        super.onResume();
    }


    /**
     * 自动抄表设置对话框
     */
    private void showAutoReadTypeSettingDialog() {
        final List<CommonSelect> commonSelectList = new ArrayList<>();
        CommonSelect commonSelect = new CommonSelect("抄全部表", true);
        CommonSelect commonSelect1 = new CommonSelect("抄未抄表", false);
        CommonSelect commonSelect2 = new CommonSelect("抄异常表", false);
        CommonSelect commonSelect3 = new CommonSelect("抄正常表", false);
        CommonSelect commonSelect4 = new CommonSelect("抄失败表", false);
        commonSelectList.add(commonSelect);
        commonSelectList.add(commonSelect1);
        commonSelectList.add(commonSelect2);
        commonSelectList.add(commonSelect3);
        commonSelectList.add(commonSelect4);
        final CommonSelectDeviceEmiAdapter commonSelectAdapter = new CommonSelectDeviceEmiAdapter(commonSelectList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("抄表类型设置");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                EmiUtils.setAutoReadType(EmiConfig.AUTO_READ_TYPE);
                showAutoReadTypeSetting();
                ToastUtil.showShortToast("设置成功");
            }
        });
        final CommonSelectDialog commonSelectDialog = builder.create();
        commonSelectDialog.setCancelable(true);
        commonSelectDialog.setCanceledOnTouchOutside(true);
        commonSelectAdapter.bindToRecyclerView(builder.recyclerView);
        commonSelectAdapter.select(commonSelectList, getAutoReadSettingTypeIndex());
        commonSelectAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                commonSelectAdapter.select(commonSelectList, position);
                switch (position) {
                    case 0:
                        EmiConfig.AUTO_READ_TYPE = STATE_ALL;
                        break;
                    case 1:
                        EmiConfig.AUTO_READ_TYPE = STATE_NO_READ;
                        break;
                    case 2:
                        EmiConfig.AUTO_READ_TYPE = STATE_WARNING;
                        break;
                    case 3:
                        EmiConfig.AUTO_READ_TYPE = STATE_SUCCESS;
                        break;
                    case 4:
                        EmiConfig.AUTO_READ_TYPE = STATE_FAILED;
                        break;
                    default:
                        EmiConfig.AUTO_READ_TYPE = STATE_ALL;
                        break;
                }
            }
        });
        builder.setAdapter(commonSelectAdapter);
        commonSelectDialog.show();
    }
}
