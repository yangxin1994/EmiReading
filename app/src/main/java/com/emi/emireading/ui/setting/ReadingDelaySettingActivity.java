package com.emi.emireading.ui.setting;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.widget.view.TitleView;

import static com.emi.emireading.core.config.EmiConstants.PREF_READING_DELAY;

/**
 * @author :zhoujian
 * @description : 抄表延时
 * @company :翼迈科技
 * @date 2018年05月30日上午 11:27
 * @Email: 971613168@qq.com
 */

public class ReadingDelaySettingActivity extends BaseActivity {
    private TitleView titleView;
    private RadioGroup radioGroup;
    private RadioButton rb500;
    private RadioButton rb1000;
    private RadioButton rb2000;

    @Override
    protected int getContentLayout() {
        return R.layout.emi_activity_reading_delay_setting;
    }

    @Override
    protected void initIntent() {

    }

    @Override
    protected void initUI() {
        titleView = findViewById(R.id.titleView);
        radioGroup = findViewById(R.id.radioGroup);
        rb500 = findViewById(R.id.rb500);
        rb1000 = findViewById(R.id.rb1000);
        rb2000 = findViewById(R.id.rb2000);
        initCheck(EmiConfig.READING_DELAY);
    }

    @Override
    protected void initData() {
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "延时时间：" + EmiConfig.READING_DELAY);
                PreferenceUtils.putInt(PREF_READING_DELAY, EmiConfig.READING_DELAY);
                ToastUtil.showShortToast("设置已保存");
                finish();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (rb500.getId() == checkedId) {
                    EmiConfig.READING_DELAY = 800;
                }
                if (rb1000.getId() == checkedId) {
                    EmiConfig.READING_DELAY = 1000;
                }
                if (rb2000.getId() == checkedId) {
                    EmiConfig.READING_DELAY = 2000;
                }
            }
        });
    }


    private void initCheck(int delay) {
        switch (delay) {
            case 800:
                rb500.setChecked(true);
                break;
            case 1000:
                rb1000.setChecked(true);
                break;
            case 2000:
                rb2000.setChecked(true);
                break;
            default:
                rb500.setChecked(true);
                break;
        }
    }

}
