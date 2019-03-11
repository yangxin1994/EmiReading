package com.emi.emireading.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectDeviceEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.CommonSelect;
import com.emi.emireading.entities.ExportStrategy;
import com.emi.emireading.ui.ActivityExportTypeSetting;
import com.emi.emireading.widget.view.MenuItemView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.TXT_EXPORT_FILE_NAME;

/**
 * @author :zhoujian
 * @description : 导出设置
 * @company :翼迈科技
 * @date 2018年03月20日上午 10:34
 * @Email: 971613168@qq.com
 */

public class ExportSettingActivity extends BaseActivity implements View.OnClickListener {
    private MenuItemView mivFilterWaterUsage;
    private MenuItemView mivKeepSame;
    private Context mContext;
    private MenuItemView mivExportType;
    private MenuItemView mivIsCreateDate;
    private int selectPosition = 0;
    private MenuItemView mivExportMode;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_export_setting;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        mivFilterWaterUsage = findViewById(R.id.mivFilterWaterUsage);
        mivExportType = findViewById(R.id.mivExportType);
        mivExportType.setOnClickListener(this);
        mivIsCreateDate = findViewById(R.id.mivIsCreateDate);
        mivExportMode = findViewById(R.id.mivExportMode);
        mivExportMode.setOnClickListener(this);
        mivIsCreateDate.setOnToggleChangedlistener(new MenuItemView.OnToggleChangedListener() {
            @Override
            public void onToggle(boolean on) {
                EmiUtils.setIsShowCreateDate(on);
                EmiConfig.isShowCreateDateDialog = on;
            }
        });
        initSetting();
    }

    @Override
    protected void initData() {
        mivFilterWaterUsage.setOnToggleChangedlistener(new MenuItemView.OnToggleChangedListener() {
            @Override
            public void onToggle(boolean on) {
                EmiUtils.setIsFilterWaterUsage(on);
                EmiConfig.isFilter = on;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mivFilterWaterUsage:
                break;
            case R.id.mivExportType:
                openActivity(mContext, ActivityExportTypeSetting.class);
                break;
            case R.id.mivExportMode:
                showExportTypeDialog();
                break;
            default:
                break;
        }
    }

    private void initSetting() {
        loadExportStrategy();
        EmiConfig.isFilter = EmiUtils.getIsFilter();
        if (EmiConfig.exportStrategy == null) {
            mivExportMode.setRightLabel("未知");
        } else {
            mivExportMode.setRightLabel(EmiConfig.exportStrategy.cityName);
        }
        EmiConfig.isShowCreateDateDialog = EmiUtils.isShowCreateDate();
        LogUtil.w(TAG, "是否过滤用水量:" + EmiConfig.isFilter);
        mivFilterWaterUsage.toggleByState(EmiConfig.isFilter);
        mivIsCreateDate.toggleByState(EmiConfig.isShowCreateDateDialog);
    }

    private void showConfig() {
        EmiConfig.EXPORT_TYPE = EmiUtils.getExportType();
        StringBuilder stringBuilder = new StringBuilder("导出类型(");
        switch (EmiConfig.EXPORT_TYPE) {
            case EmiConstants.STATE_ALL:
                stringBuilder.append("全部)");
                break;
            case EmiConstants.STATE_HAS_READ:
                stringBuilder.append("已抄)");
                break;
            case EmiConstants.STATE_NO_READ:
                stringBuilder.append("未抄)");
                break;
            case EmiConstants.STATE_FAILED:
                stringBuilder.append("失败)");
                break;
            case EmiConstants.STATE_SUCCESS:
                stringBuilder.append("正常)");
                break;
            case EmiConstants.STATE_NORMAL:
                stringBuilder.append("正常、异常、补录)");
                break;
            case EmiConstants.STATE_PEOPLE_RECORDING:
                stringBuilder.append("补录)");
                break;
            default:
                break;
        }
        mivExportType.setLabel(stringBuilder.toString());
    }


    @Override
    protected void onResume() {
        showConfig();
        super.onResume();
    }


    private void showExportTypeDialog() {
        LitePal.deleteAll(ExportStrategy.class);
        final List<ExportStrategy> exportStrategyList = LitePal.findAll(ExportStrategy.class);
        if (exportStrategyList.isEmpty()) {
         /*   String result = FileUtil.getAssetsJson(EmiReadingApplication.getAppContext(), JSON_EXPORT_FILE_NAME);
            List<ExportStrategy> list = JSON.parseArray(result, ExportStrategy.class);
            exportStrategyList.addAll(list);*/
            exportStrategyList.addAll(getExportStrategyFromAssets());
        }
        String exportConfigPath = EmiConfig.DOWN_LOAD_PATH + "/" + TXT_EXPORT_FILE_NAME;
        String exportConfigJson = FileUtil.getTxtFileContent(exportConfigPath);
        if (!TextUtils.isEmpty(exportConfigJson) && EmiStringUtil.isJSONValid(exportConfigJson)) {
            exportStrategyList.addAll(getExportStrategyFromConfigFile(exportConfigJson));
            LogUtil.i("符合条件：" + exportStrategyList.size());
        } else {
            LogUtil.e("不符合条件:" + exportConfigJson);
        }
        if (EmiConfig.exportStrategy == null) {
            EmiConfig.exportStrategy = exportStrategyList.get(0);
        }
        for (int i = 0; i < exportStrategyList.size(); i++) {
            LogUtil.i(TAG, "解析的内容：" + exportStrategyList.get(i).exportFormatJson);
        }

        final List<CommonSelect> commonSelectList = new ArrayList<>();
        CommonSelect commonSelect;
        for (ExportStrategy exportStrategy : exportStrategyList) {
            commonSelect = new CommonSelect(exportStrategy.cityName, false);
            commonSelectList.add(commonSelect);
        }
        final CommonSelectDeviceEmiAdapter commonSelectAdapter = new CommonSelectDeviceEmiAdapter(commonSelectList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("导出模式选择");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                EmiConfig.exportStrategy = exportStrategyList.get(selectPosition);
                mivExportMode.setRightLabel(EmiConfig.exportStrategy.cityName);
                EmiUtils.saveExportStrategy(EmiConfig.exportStrategy);
                ToastUtil.showShortToast("设置成功");
            }
        });
        final CommonSelectDialog commonSelectDialog = builder.create();
        commonSelectDialog.setCancelable(true);
        commonSelectDialog.setCanceledOnTouchOutside(true);
        commonSelectAdapter.bindToRecyclerView(builder.recyclerView);
        commonSelectAdapter.select(commonSelectList, getSelectPosition(exportStrategyList));
        commonSelectAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                commonSelectAdapter.select(commonSelectList, position);
                selectPosition = position;
            }
        });
        builder.setAdapter(commonSelectAdapter);
        commonSelectDialog.show();
    }


    private int getSelectPosition(List<ExportStrategy> strategyList) {
        for (int i = 0; i < strategyList.size(); i++) {
            if (strategyList.get(i).cityName.equals(EmiConfig.exportStrategy.cityName)) {
                return i;
            }
        }
        return 0;
    }
}
