package com.emi.emireading.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.ui.AboutActivity;
import com.emi.emireading.ui.debug.ChannelDebugActivity;
import com.emi.emireading.update.DownloadApk;
import com.emi.emireading.widget.view.MenuItemView;
import com.emi.emireading.widget.view.dialog.CustomDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;
import com.emi.emireading.widget.view.wheelview.OnItemSelectedListener;
import com.emi.emireading.widget.view.wheelview.WheelView;

import java.util.ArrayList;

import static com.emi.emireading.common.EmiUtils.isNormalChannel;
import static com.emi.emireading.core.config.EmiConfig.APK_PATH_DOWNLOAD;
import static com.emi.emireading.core.config.EmiConfig.IS_SKIP_SELECT_FILE;
import static com.emi.emireading.ui.setting.ReadMeterSettingActivity.PREF_IS_SKIP_SELECT_FILE;


/**
 * 设置页面
 *
 * @author :zhoujian
 * @company :翼迈科技
 * @date: 2017年08月09日上午 11:14
 * @Email: 971613168@qq.com
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private WheelView wheelView;
    private ArrayList<String> cityList = new ArrayList<>();
    private MenuItemView miv_bluetoothSetting;
    private MenuItemView miv_channelProtocol;
    private MenuItemView miv_meterType;
    private MenuItemView miv_currentCity;
    private MenuItemView miv_clearData;
    private MenuItemView miv_debugMode;
    private MenuItemView miv_Help;
    private MenuItemView miv_About;
    private MenuItemView miv_clearCreateFile;
    private MenuItemView miv_channl_debug;
    private MenuItemView miv_isNeedChannel;
    private MenuItemView mivEmiDebugging;
    private MenuItemView mivSkipFile;
    private final int CHANNEL_SETTING_CODE = 100;
    private final int METER_TYPE_SETTING_CODE = 101;
    private Context mContext;
    private MenuItemView miv_exportSetting;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        miv_bluetoothSetting = (MenuItemView) findViewById(R.id.miv_bluetoothSetting);
        miv_channelProtocol = (MenuItemView) findViewById(R.id.miv_channelProtocol);
        miv_currentCity = (MenuItemView) findViewById(R.id.miv_currentCity);
        miv_clearData = (MenuItemView) findViewById(R.id.miv_clearData);
        miv_meterType = (MenuItemView) findViewById(R.id.miv_meterType);
        miv_debugMode = (MenuItemView) findViewById(R.id.miv_debugMode);
        miv_clearCreateFile = (MenuItemView) findViewById(R.id.miv_clearCreateFile);
        miv_Help = (MenuItemView) findViewById(R.id.miv_Help);
        miv_About = (MenuItemView) findViewById(R.id.miv_About);
        miv_channl_debug = (MenuItemView) findViewById(R.id.channel_board_debugging);
        miv_isNeedChannel = (MenuItemView) findViewById(R.id.miv_isNeedChannel);
        mivSkipFile = findViewById(R.id.mivSkipFile);
        miv_exportSetting = findViewById(R.id.miv_exportSetting);
        mivEmiDebugging = findViewById(R.id.mivEmiDebugging);
        miv_bluetoothSetting.setOnClickListener(this);
        miv_currentCity.setOnClickListener(this);
        miv_clearData.setOnClickListener(this);
        miv_clearCreateFile.setOnClickListener(this);
        miv_channl_debug.setOnClickListener(this);
        miv_exportSetting.setOnClickListener(this);
        mivEmiDebugging.setOnClickListener(this);
        miv_debugMode.setOnClickListener(this);
        miv_debugMode.setOnToggleChangedlistener(new MenuItemView.OnToggleChangedListener() {
            @Override
            public void onToggle(boolean on) {
                EmiUtils.saveDebugMode(on);
                loadSettingState();
                if (!on) {
                    //关闭专业模式
                    EmiUtils.saveProfessionalMode(false);
                }
            }
        });

        miv_isNeedChannel.setOnToggleChangedlistener(new MenuItemView.OnToggleChangedListener() {
            @Override
            public void onToggle(boolean on) {
                EmiUtils.saveChannelSetting(on);
                if (on) {
                    miv_channelProtocol.setVisibility(View.VISIBLE);
                } else {
                    EmiConfig.IS_SKIP_SELECT_FILE = false;
                    PreferenceUtils.putBoolean(PREF_IS_SKIP_SELECT_FILE, false);
                    miv_channelProtocol.setVisibility(View.GONE);
                    IS_SKIP_SELECT_FILE = false;
                    mivSkipFile.toggleOff();
                    PreferenceUtils.putBoolean(PREF_IS_SKIP_SELECT_FILE, IS_SKIP_SELECT_FILE);
                }
            }
        });
        miv_Help.setOnClickListener(this);
        miv_meterType.setOnClickListener(this);
        miv_channelProtocol.setOnClickListener(this);
        miv_About.setOnClickListener(this);
        mivSkipFile.toggleByState(IS_SKIP_SELECT_FILE);
    }

    @Override
    protected void initData() {
        init();
        //1.注册下载广播接收器
        //2.删除已存在的Apk
        DownloadApk.registerBroadcast(this);
        DownloadApk.removeFile(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //蓝牙设置
            case R.id.miv_bluetoothSetting:
                //                startActivity(new Intent(mContext, FindAndConnetBlueActivity.class));
                //                openActivity(mContext, DownloadExcelActivity.class);
                break;
            //当前城市
            case R.id.miv_currentCity:
                showPopWindow();
                break;
            case R.id.miv_channelProtocol:
                startActivityForResult(new Intent(mContext, ChannelProtocolSettingActivity.class), CHANNEL_SETTING_CODE);
                break;
            //清除数据
            case R.id.miv_clearData:
                //                deleteFile();
                startActivity(new Intent(mContext, ClearFileActivity.class));
                break;
            case R.id.miv_debugMode:
                break;
            case R.id.miv_Help:
                startActivity(new Intent(mContext, HelpActivity.class));
                break;
            case R.id.miv_About:
                startActivity(new Intent(mContext, AboutActivity.class));
                break;
            case R.id.miv_meterType:
                startActivityForResult(new Intent(mContext, MeterTypeSettingActivity.class), METER_TYPE_SETTING_CODE);
                break;
            case R.id.miv_clearNeedFile:
                FileUtil.deleteDirectory(APK_PATH_DOWNLOAD);
                notifyFileExplore(APK_PATH_DOWNLOAD);
                deleteNeedDirFile();
                break;
            case R.id.miv_clearCreateFile:
                FileUtil.deleteDirectory(APK_PATH_DOWNLOAD);
                notifyFileExplore(APK_PATH_DOWNLOAD);
                deleteCreateDirFile();
                break;
            case R.id.channel_board_debugging:
                startActivity(new Intent(mContext, ChannelDebugActivity.class));
                break;
            case R.id.miv_exportSetting:
                openActivity(mContext, ExportSettingActivity.class);
                break;
            case R.id.mivEmiDebugging:
                doProfessionalMode();
                break;

            default:
                break;
        }
    }


    private void showPopWindow() {
        getWindowManager();
        PopupWindow popupWindow;
        View popupWindowView;
        popupWindowView = getLayoutInflater().inflate(R.layout.pop_select_city, null);
        wheelView = (WheelView) popupWindowView.findViewById(R.id.wvCity);
        wheelView.setItems(cityList);
        wheelView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
            }
        });
        popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.AnimationBottomFade);
        //菜单背景色
        ColorDrawable dw = new ColorDrawable(0xffffffff);
        popupWindow.setBackgroundDrawable(dw);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
        popupWindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_setting, null), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        backgroundAlpha(0.5f);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //0.0-1.0
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    private void init() {
        //        loadCityType();
        //数据库操作辅助类
        showMeterType();
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
                                    miv_isNeedChannel.toggleOn();
                                    EmiUtils.saveChannelSetting(true);
                                }
                            })
                            .show();
                } else {
                    EmiUtils.saveChannelSetting(false);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHANNEL_SETTING_CODE:
                if (resultCode == RESULT_OK) {
                    if (isNormalChannel()) {
                        miv_channelProtocol.setRightLabel(getString(R.string.channel_normal));
                    } else {
                        miv_channelProtocol.setRightLabel(getString(R.string.channel_special));
                    }
                }
                break;
            case METER_TYPE_SETTING_CODE:
                if (resultCode == RESULT_OK) {
                    showMeterType();
                }
                break;
            default:
                break;
        }
    }


    public void deleteFile() {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("确保抄表数据已下载导出且已过期,清空数据库将不可恢复!确定清空吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (FileUtil.deleteDirectory(EmiConfig.TempPath)) {
                    Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                    notifyFileExplore(EmiConfig.TempPath);
                } else {
                    Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
                }
                //清除数据库
                getSqOperator().deleteAll();
                //                Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                //清除SharedPreferences
                dialog.dismiss();
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
        miv_meterType.setRightLabel(sb.toString());
    }

    private void loadSettingState() {
        if (EmiUtils.isDebugMode()) {
            miv_meterType.setVisibility(View.GONE);
            miv_channl_debug.setVisibility(View.GONE);
            miv_isNeedChannel.setVisibility(View.VISIBLE);
            mivSkipFile.setVisibility(View.VISIBLE);
        } else {
            miv_isNeedChannel.setVisibility(View.GONE);
            miv_meterType.setVisibility(View.GONE);
            miv_currentCity.setVisibility(View.GONE);
            miv_channl_debug.setVisibility(View.GONE);
            miv_isNeedChannel.setVisibility(View.GONE);
            mivSkipFile.setVisibility(View.GONE);
        }
    }

    /**
     * 删除翼迈所需文件文件夹
     */
    public void deleteNeedDirFile() {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("将要删除EMI抄表“翼迈所需文件”目录下的所有文件,确定删除吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FileUtil.deleteDirectory(APK_PATH_DOWNLOAD);
                notifyFileExplore(APK_PATH_DOWNLOAD);
                if (FileUtil.deleteDirectory(EmiConfig.NeedFilePath)) {
                    Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                    notifyFileExplore(EmiConfig.NeedFilePath);
                } else {
                    Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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


    /**
     * 删除翼迈所需文件文件夹
     */
    public void deleteCreateDirFile() {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("将要删除EMI抄表“翼迈生成文件”目录下的所有文件,确定删除吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (FileUtil.deleteDirectory(EmiConfig.GeneratePath)) {
                    Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                    notifyFileExplore(EmiConfig.GeneratePath);
                } else {
                    Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadApk.unregisterBroadcast(this);
    }


    private void loadUserSetting() {
        if (EmiUtils.isNeedChannel()) {
            miv_channelProtocol.setVisibility(View.VISIBLE);
            miv_isNeedChannel.toggleOn(false);
            showChannelType();
        } else {
            showChannelType();
            miv_channelProtocol.setVisibility(View.GONE);
            miv_isNeedChannel.toggleOff(false);
        }

        if (EmiUtils.isDebugMode()) {
            //调试关闭
            miv_debugMode.toggleOn(false);
        } else {
            miv_debugMode.toggleOff(false);
        }
        loadSettingState();
    }


    private void showChannelType() {
        if (EmiUtils.isNormalChannel()) {
            miv_channelProtocol.setRightLabel("普通通道板");
        } else {
            miv_channelProtocol.setRightLabel("特殊通道板");
        }
    }


    private void doProfessionalMode() {
        if (EmiUtils.isProfessionalMode()) {
            //如果是“专业模式”,直接跳转
            doSkipProfessionSetting();
        } else {
            final InputDialog.Builder builder = new InputDialog.Builder(mContext);
            builder.setTitle("请输入售后编码");
            builder.setMessage("");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (builder.editText.getText().toString().equals(getResources().getString(R.string.debug_password))) {
                        EmiUtils.saveProfessionalMode(true);
                        doSkipProfessionSetting();
                    } else {
                        ToastUtil.showShortToast(getResources().getString(R.string.input_error));
                    }
                    dialog.dismiss();
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

    /**
     * 跳转到专业模式
     */
    private void doSkipProfessionSetting() {
        openActivity(mContext, ProfessionalModeActivity.class);
    }

    @Override
    protected void onResume() {
        loadUserSetting();
        super.onResume();
    }


}
