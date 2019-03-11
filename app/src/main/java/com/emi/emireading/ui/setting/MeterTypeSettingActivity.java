package com.emi.emireading.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectDeviceEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.CommonSelect;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;

import java.util.ArrayList;
import java.util.List;


/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date: 2017年08月18日下午 02:39
 * @Email: 971613168@qq.com
 */

public class MeterTypeSettingActivity extends BaseActivity implements View.OnClickListener {
    private RelativeLayout relay_dui;
    private RelativeLayout relay_fan;
    private ImageView ivSpecial;
    private ImageView ivNormal;
    private Context mContext;
    /**
     * 水表类型
     */
    private int meterType = -1;
    /**
     * 市场类型
     */
    private int standardType = -1;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_meter_setting;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        relay_dui = (RelativeLayout) findViewById(R.id.relay_special);
        relay_fan = (RelativeLayout) findViewById(R.id.relay_normal);
        ivSpecial = (ImageView) findViewById(R.id.ivSpecial);
        ivNormal = (ImageView) findViewById(R.id.ivNormal);
        relay_dui.setOnClickListener(this);
        relay_fan.setOnClickListener(this);
        showUserConfig();
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relay_special:
                standardType = EmiConstants.STANDARD_TYPE_SPECIAL;
                showMeterTypeDialog();
             /*   PreferenceUtils.putString(Constants.METER_TYPE_KEY,METER_TYPE_DUI);
                EMIConfig.MeterType = MeterType.MeterTYPE_Dui;*/
                //                finish();
                break;
            case R.id.relay_normal:
                standardType = EmiConstants.STANDARD_TYPE_NORMAL;
          /*      iv7833.setVisibility(View.GONE);
                iv1001.setVisibility(View.VISIBLE);
                finish();*/
                showMeterTypeDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }

    private void showMeterTypeDialog() {
        final List<CommonSelect> commonSelectList = new ArrayList<>();
        CommonSelect commonSelect = new CommonSelect(getResources().getString(R.string.meter_type_dui), true);
        CommonSelect commonSelect1 = new CommonSelect(getResources().getString(R.string.meter_type_fan), false);
        commonSelectList.add(commonSelect);
        commonSelectList.add(commonSelect1);
        final CommonSelectDeviceEmiAdapter commonSelectAdapter = new CommonSelectDeviceEmiAdapter(commonSelectList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("请选择水表类型");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveConfig(standardType, meterType);
                showUserConfig();
                dialog.dismiss();
                ToastUtil.showShortToast("设置成功");
                finish();
            }
        });
        final CommonSelectDialog commonSelectDialog = builder.create();
        commonSelectDialog.setCancelable(true);
        commonSelectDialog.setCanceledOnTouchOutside(true);
        commonSelectAdapter.bindToRecyclerView(builder.recyclerView);
        LogUtil.i(TAG, "水表类型：" + EmiUtils.getMeterType());
        commonSelectAdapter.select(commonSelectList, getIndex());
        commonSelectAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                commonSelectAdapter.select(commonSelectList, position);
                switch (position) {
                    case 0:
                        meterType = EmiConstants.METER_TYPE_DUI;
                        break;
                    case 1:
                        meterType = EmiConstants.METER_TYPE_FAN;
                        break;
                    default:
                        meterType = EmiConstants.METER_TYPE_DUI;
                        break;
                }
            }
        });
        builder.setAdapter(commonSelectAdapter);
        commonSelectDialog.show();
    }

    private void showSpecial() {
        ivNormal.setVisibility(View.INVISIBLE);
        ivSpecial.setVisibility(View.VISIBLE);
    }

    private void showNormal() {
        ivNormal.setVisibility(View.VISIBLE);
        ivSpecial.setVisibility(View.INVISIBLE);
    }

    private void saveConfig(int standardType, int meterType) {
        EmiUtils.saveStandardType(standardType);
        EmiUtils.saveMeterType(meterType);
    }


    private void showUserConfig() {
        LogUtil.w(TAG, "获取到的：" + EmiUtils.getStandardType());
        switch (EmiUtils.getStandardType()) {
            case EmiConstants.STANDARD_TYPE_SPECIAL:
                showSpecial();
                break;
            case EmiConstants.STANDARD_TYPE_NORMAL:
                showNormal();
                break;
            default:
                break;
        }
    }


    private int getIndex() {
        switch (EmiUtils.getMeterType()) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 0;
        }
    }
}
