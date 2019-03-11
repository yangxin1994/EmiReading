package com.emi.emireading.ui;

import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.entities.UserInfo;

import java.util.ArrayList;

import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;


/**
 * @author :zhoujian
 * @description : 数据统计
 * @company :翼迈科技
 * @date: 2017年10月24日下午 02:39
 * @Email: 971613168@qq.com
 */

public class ChannelCountActivity extends BaseActivity {
    private TextView tvTotalCount;
    private TextView allreads;
    private TextView notread;
    private TextView reads;
    private TextView unnormalreads;
    private TextView tvFailed;
    private TextView allyl;
    private ArrayList<UserInfo> mDataList = new ArrayList<>();
    private int totalCount;
    private int failCount;
    private int readsCount;
    private int warningCount;
    private int noReadCount;
    private int successCount;
    private int totalWaterUsage;
    private TextView tvSuccess;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_channel_count;
    }

    @Override
    protected void initIntent() {

    }

    @Override
    protected void initUI() {
        initView();
    }

    @Override
    protected void initData() {
        mDataList.addAll(EmiConfig.userInfoArrayList);
        LogUtil.d(TAG, "长度：" + mDataList.size());
        showCount();
    }

    private void initView() {
        allreads = (TextView) findViewById(R.id.allreads);
        notread = (TextView) findViewById(R.id.notread);
        reads = (TextView) findViewById(R.id.reads);
        tvFailed = (TextView) findViewById(R.id.tvFailed);
        unnormalreads = (TextView) findViewById(R.id.unnormalreads);
        allyl = (TextView) findViewById(R.id.allyl);
        tvSuccess = findViewById(R.id.tvSuccess);
    }


    private void showCount() {
        totalCount = mDataList.size();
        for (UserInfo userInfo : mDataList) {
            totalWaterUsage += userInfo.curyl;
            switch (userInfo.state) {
                case STATE_SUCCESS:
                    readsCount++;
                    successCount++;
                    break;
                case STATE_PEOPLE_RECORDING:
                    readsCount++;
                    successCount++;
                    break;
                case STATE_NO_READ:
                    noReadCount++;
                    break;
                case STATE_WARNING:
                    warningCount++;
                    readsCount++;
                    break;
                case STATE_FAILED:
                    failCount++;
                    readsCount++;
                    break;
                default:
                    break;
            }
        }
        allreads.setText(totalCount + "");
        notread.setText(noReadCount + "");
        reads.setText(readsCount + "");
        unnormalreads.setText(warningCount + "");
        tvFailed.setText(failCount + "");
        tvSuccess.setText(successCount + "");
        allyl.setText(totalWaterUsage + "（吨）");
    }
}
