package com.emi.emireading;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.entities.LoadStrategy;
import com.emi.emireading.ui.AutoReadMeterActivityNew;
import com.emi.emireading.ui.FileListActivity;
import com.emi.emireading.ui.MeterQueryActivityNew;
import com.emi.emireading.ui.TaskQueryActivity;
import com.emi.emireading.ui.load.LoadTaskActivityNew;
import com.emi.emireading.ui.setting.SettingActivity;
import com.emi.emireading.widget.view.emimenu.EmiMenu;
import com.emi.emireading.widget.view.emimenu.EmiMenuItem;
import com.emi.emireading.widget.view.emimenu.MenuAnimation;
import com.emi.emireading.widget.view.emimenu.OnDismissedListener;
import com.emi.emireading.widget.view.emimenu.OnMenuItemClickListener;

import org.apache.commons.lang.StringUtils;
import org.litepal.LitePal;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConfig.IS_SKIP_SELECT_FILE;
import static com.emi.emireading.core.config.EmiConstants.DEFAULT_CITY;
import static com.emi.emireading.core.config.EmiConstants.JSON_LOAD_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.PREF_CURRENT_CITY;
import static com.emi.emireading.core.config.EmiConstants.PREF_PHONE_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.PREF_REMARK;
import static com.emi.emireading.core.config.EmiConstants.SPLIT_CHAR_DEFAULT;
import static com.emi.emireading.core.config.EmiConstants.TXT_LOAD_FILE_NAME;
import static com.emi.emireading.ui.setting.ReadMeterSettingActivity.PREF_IS_SKIP_SELECT_FILE;

/**
 * @author :zhoujian
 * @description : 主页面
 * @company :翼迈科技
 * @date: 2017年12月25日上午 11:53
 * @Email: 971613168@qq.com
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private List<LoadStrategy> strategyList = new ArrayList<>();
    private TextView spinner;
    private TextView titleView;
    private int selectIndex;
    private Context context;
    private EmiMenu mEmiMenu;
    /**
     * 存储权限
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private TextView tvMobilePhone;
    private TextView tvRemark;

    @Override
    protected int getContentLayout() {
        return R.layout.emi_activity_main;
    }

    @Override
    protected void initIntent() {
        context = this;
    }

    @Override
    protected void initUI() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE
            );
        }
        LitePal.deleteAll(LoadStrategy.class);
        String localConfig = FileUtil.getAssetsJson(this, JSON_LOAD_FILE_NAME);
        List<LoadStrategy> listLoadStrategy = JSON.parseArray(localConfig, LoadStrategy.class);
        LitePal.saveAll(listLoadStrategy);
        strategyList.clear();
        strategyList.addAll(LitePal.findAll(LoadStrategy.class));
        strategyList.addAll(getLoadStrategyConfig());
        selectIndex = findStrategyByCity(getCurrentCity());
        LogUtil.w("获取的索引:" + selectIndex);
        init(strategyList);
    }

    @Override
    protected void initData() {

    }


    private void init(List<LoadStrategy> loadStrategyList) {
        EmiConfig.EMI_PHONE = PreferenceUtils.getString(PREF_PHONE_NUMBER, "");
        EmiConfig.EMI_REMARK = PreferenceUtils.getString(PREF_REMARK, "");
        IS_SKIP_SELECT_FILE = PreferenceUtils.getBoolean(PREF_IS_SKIP_SELECT_FILE, false);
        tvMobilePhone = findViewById(R.id.tvMobilePhone);
        tvRemark = findViewById(R.id.tvRemark);
        titleView = findViewById(R.id.tvTitle);
        spinner = findViewById(R.id.spinner);
        findViewById(R.id.linLoadTask).setOnClickListener(this);
        findViewById(R.id.linTaskQuery).setOnClickListener(this);
        findViewById(R.id.linAutoReading).setOnClickListener(this);
        findViewById(R.id.linDataQuery).setOnClickListener(this);
        findViewById(R.id.ivMore).setOnClickListener(this);
        if (strategyList != null) {
            EmiConfig.loadStrategy = loadStrategyList.get(selectIndex);
            String titleName = EmiConfig.loadStrategy.getCity();
            titleName += "抄表";
            titleView.setText(titleName);
            spinner.setText(strategyList.get(selectIndex).getCity());
        }
        spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadStrategyListPop(strategyList);
            }
        });

    }

    private void loadConfig(LoadStrategy currentStrategy) {
        EmiConfig.CURRENT_CITY = currentStrategy.getCity();
        EmiConfig.USER_ID_INDEX = currentStrategy.getUserIdIndex();
        EmiConfig.USER_NAME_INDEX = currentStrategy.getUserNameIndex();
        EmiConfig.CHANNEL_INDEX = currentStrategy.getChannelIndex();
        EmiConfig.USER_ADDRESS_INDEX = currentStrategy.getUserAddressIndex();
        EmiConfig.LAST_READING_INDEX = currentStrategy.getLastReadingIndex();
        EmiConfig.FIRM_CODE_INDEX = currentStrategy.getFirmCodeIndex();
        EmiConfig.LAST_USAGE_INDEX = currentStrategy.getLastUsageIndex();
        EmiConfig.METER_ID_INDEX = currentStrategy.getMeterIdIndex();
        EmiConfig.IS_MERGE = currentStrategy.isMerge();
        EmiConfig.FIELD_COUNT = currentStrategy.getFieldCount();
        EmiConfig.MERGE_INFO_INDEX = currentStrategy.getMergeInfoIndex();
        EmiConfig.FILE_TYPE = currentStrategy.getFileType();
        EmiConfig.FILE_SUFFIX = currentStrategy.getFileType();
        EmiConfig.CURRENT_SUFFIX = currentStrategy.getFileType();
        EmiConfig.PEOPLE_RECORDING_INDEX = currentStrategy.getPeopleRecordingIndex();
        EmiConfig.IS_SUPPORT_UPLOAD = currentStrategy.supportUpload;
        LogUtil.d(TAG, "PEOPLE_RECORDING_INDEX=" + currentStrategy.getPeopleRecordingIndex());
        EmiConfig.IS_GBK = EmiUtils.isGBK();
        if (EmiStringUtil.isEmpty(currentStrategy.getSplitChar())) {
            EmiConfig.SPLIT_CHAR = SPLIT_CHAR_DEFAULT;
        } else {
            EmiConfig.SPLIT_CHAR = currentStrategy.getSplitChar();
        }
        EmiConfig.EXPORT_TYPE = EmiUtils.getExportType();
        EmiConfig.isFilter = EmiUtils.getIsFilter();
    }

    private void saveConfig(LoadStrategy currentStrategy) {
        PreferenceUtils.putString(PREF_CURRENT_CITY, currentStrategy.getCity());
    }


    private int findStrategyByCity(String cityName) {
        List<LoadStrategy> strategies = LitePal.findAll(LoadStrategy.class);
        strategies.addAll(getLoadStrategyConfig());
        if (cityName != null) {
            for (int i = 0; i < strategies.size(); i++) {
                if (cityName.equals(strategies.get(i).getCity())) {
                    loadConfig(strategies.get(i));
                    return i;
                }
            }
        }
        return 0;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.linLoadTask:
                openActivity(this, LoadTaskActivityNew.class);
                break;
            case R.id.linDataQuery:
                openActivity(this, MeterQueryActivityNew.class);
                break;
            case R.id.linAutoReading:
                if (IS_SKIP_SELECT_FILE) {
                    openActivity(this, AutoReadMeterActivityNew.class);
                } else {
                    openActivity(this, FileListActivity.class);
                }
                break;
            case R.id.ivMore:
                openActivity(this, SettingActivity.class);
//                openActivity(this, LoginActivity.class);
                break;
            case R.id.linTaskQuery:
                openActivity(this, TaskQueryActivity.class);
                break;
            default:
                break;
        }
    }

    private String getCurrentCity() {
        return PreferenceUtils.getString(PREF_CURRENT_CITY, DEFAULT_CITY);
    }


    private void executeStrategy(LoadStrategy selectStrategy) {
        String titleName = selectStrategy.getCity();
        titleName += "抄表";
        titleView.setText(titleName);
        loadConfig(selectStrategy);
        saveConfig(selectStrategy);
    }

    @Override
    protected void onDestroy() {
        LogUtil.w(TAG, "程序已退出---------->onDestroy()");
        disConnected();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (EmiUtils.isDebugMode()) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.INVISIBLE);
        }
        super.onResume();
        showMobilePhone();
        showRemark();
    }

    private MyHandler mHandler = new MyHandler(MainActivity.this);

    private static class MyHandler extends Handler {
        private WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }
    }


    private void disConnected() {
        if (null != EmiConstants.bluetoothSocket && EmiConstants.bluetoothSocket.isConnected()) {
            try {
                EmiConstants.bluetoothSocket.close();
                LogUtil.d(TAG, "蓝牙已关闭");
                EmiConfig.bluetoothConnectStatus = false;
                EmiConstants.bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.w(TAG, "EmiConstants.bluetoothSocket为null");
        }
    }

    private void showMobilePhone() {
        if (StringUtils.isNotEmpty(EmiConfig.EMI_PHONE)) {
            StringBuilder sb = new StringBuilder("电话：");
            sb.append(EmiConfig.EMI_PHONE);
            tvMobilePhone.setText(sb.toString());
        }

    }

    private void showRemark() {
        tvRemark.setText(EmiStringUtil.formatNull(EmiConfig.EMI_REMARK));
    }


    private void showLoadStrategyListPop(final List<LoadStrategy> strategies) {
        mEmiMenu = new EmiMenu.Builder(context)
                .setLifecycleOwner(this)
                .setAnimation(MenuAnimation.SHOW_UP_TOP_LEFT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setTextColor(ContextCompat.getColor(context, R.color.white))
                .setSelectedTextColor(ContextCompat.getColor(context, R.color.black))
                .setMenuColor(ContextCompat.getColor(context, R.color.blue))
                .setSelected(selectIndex)
                .setSelectedMenuColor(ContextCompat.getColor(context, R.color.white_small))
                .setOnMenuItemClickListener(new OnMenuItemClickListener<EmiMenuItem>() {
                    @Override
                    public void onItemClick(int position, EmiMenuItem item) {
                        mEmiMenu.dismiss();
                        String titleName = strategies.get(position).getCity();
                        EmiConfig.loadStrategy = strategies.get(position);
                        ToastUtils.showToastNormal("当前已选择" + titleName + "模式");
                        spinner.setText(titleName);
                        selectIndex = position;
                        titleName += "抄表";
                        titleView.setText(titleName);
                        LogUtil.w(TAG, "点击的位置索引：" + position);
                        executeStrategy(strategies.get(position));
                    }
                })
                .setOnDismissListener(new OnDismissedListener() {
                    @Override
                    public void onDismissed() {

                    }
                }).build();
        EmiMenuItem emiMenuItem;
        for (LoadStrategy strategy : strategies) {
            if (EmiConfig.loadStrategy.getCity().equals(strategy.getCity())) {
                emiMenuItem = new EmiMenuItem(strategy.getCity(), true);
            } else {
                emiMenuItem = new EmiMenuItem(strategy.getCity(), false);
            }
            mEmiMenu.addItem(emiMenuItem);
        }
        mEmiMenu.showAsDropDown(spinner);
    }


    private List<LoadStrategy> getLoadStrategyConfig() {
        List<LoadStrategy> loadStrategyList = new ArrayList<>();
        String txtPath = EmiConfig.DOWN_LOAD_PATH + "/" + TXT_LOAD_FILE_NAME;
        String userConfig = FileUtil.getTxtFileContent(txtPath);
        if (EmiStringUtil.isJSONValid(userConfig)) {
            try {
                List<LoadStrategy> useConfigList = JSON.parseArray(userConfig, LoadStrategy.class);
                if (useConfigList == null || useConfigList.isEmpty()) {
                    return loadStrategyList;
                }
                return useConfigList;
            } catch (JSONException e) {
                LogUtil.e("解析异常" + e.toString());
            }
        }
        return loadStrategyList;
    }

}
