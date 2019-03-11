package com.emi.emireading.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog.Style.SPIN_INDETERMINATE;

/**
 * @author :zhoujian
 * @description : 数据统计
 * @company :翼迈科技
 * @date 2018年03月14日下午 04:06
 * @Email: 971613168@qq.com
 */

public class ActivityCount extends BaseActivity {
    private Context context;
    private TextView tvAllMeter;
    private TextView tvNoRead;
    private TextView tvHasRead;
    private TextView tvAbnormal;
    private TextView tvFailed;
    private TextView tvSuccess;
    private TextView tvAllWaterUseAge;
    private EmiMultipleProgressDialog dialog;
    private MyOperator myOperator;
    private SQLiteHelper sqLiteHelper;
    private ArrayList<UserInfo> allDataList;
    private String fileName;
    private int noReadCount;
    private int hasReadCount;
    private int abnormalCount;
    private int allWaterUsage;
    private int failedCount;
    private int successCount;
    private static final int MSG_FINISH = 0;
    private static final int MSG_ERROR = -1;
    private ImageView ivBack;
    private MyHandler myHandler = new MyHandler(this);

    @Override
    protected int getContentLayout() {
        return R.layout.activity_count;
    }

    @Override
    protected void initIntent() {
        context = this;
        fileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        sqLiteHelper = new SQLiteHelper(EmiReadingApplication.getAppContext());
    }

    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ActivityCount activity = (ActivityCount) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_FINISH:
                        activity.closeDialog();
                        activity.loadCount();
                        activity.showCount();
                        break;
                    case MSG_ERROR:
                        ToastUtil.showShortToast("未获取到文件名");
                        activity.closeDialog();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void initUI() {
        initView();
    }

    private void initView() {
        tvAllMeter = findViewById(R.id.tvAllMeter);
        tvNoRead = findViewById(R.id.tvNoRead);
        tvHasRead = findViewById(R.id.tvHasRead);
        tvAbnormal = findViewById(R.id.tvAbnormal);
        tvSuccess = findViewById(R.id.tvSuccess);
        tvAllWaterUseAge = findViewById(R.id.tvAllWaterUseAge);
        tvFailed = findViewById(R.id.tvFailed);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        showDialog();
        ThreadPoolManager.EXECUTOR.execute(new GetDataRunnable());
    }

    private void showDialog() {
        dialog = EmiMultipleProgressDialog.create(context)
                .setStyle(SPIN_INDETERMINATE)
                .setLabel("正在查询...")
                .setCancellable(false)
                .show();
    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void getDataFromSq() {
        myOperator = new MyOperator(sqLiteHelper.getWritableDatabase());
        if (fileName != null) {
            allDataList = (ArrayList<UserInfo>) myOperator.find(fileName);
        } else {
            sendMsg(MSG_ERROR);
        }
    }

    private class GetDataRunnable implements Runnable {
        @Override
        public void run() {
            getDataFromSq();
            sendMsg(MSG_FINISH);
        }
    }

    private void loadCount() {
        for (UserInfo userInfo : allDataList) {
            allWaterUsage += userInfo.curyl;
            switch (userInfo.state) {
                case EmiConstants.STATE_NO_READ:
                    noReadCount++;
                    break;
                case EmiConstants.STATE_PEOPLE_RECORDING:
                case EmiConstants.STATE_SUCCESS:
                case EmiConstants.STATE_FAILED:
                case EmiConstants.STATE_WARNING:
                    hasReadCount++;
                    if (EmiConstants.STATE_FAILED == userInfo.state) {
                        failedCount++;
                    } else if (EmiConstants.STATE_WARNING == userInfo.state) {
                        abnormalCount++;
                    } else if (EmiConstants.STATE_SUCCESS == userInfo.state || EmiConstants.STATE_PEOPLE_RECORDING == userInfo.state) {
                        successCount++;
                    }
                    break;
                default:
                    break;
            }
        }

    }

    private void sendMsg(int what) {
        myHandler.sendEmptyMessage(what);
    }


    private void showCount() {
        tvAllMeter.setText(String.valueOf(allDataList.size()));
        tvAbnormal.setText(String.valueOf(abnormalCount));
        tvHasRead.setText(String.valueOf(hasReadCount));
        tvSuccess.setText(String.valueOf(successCount));
        tvNoRead.setText(String.valueOf(noReadCount));
        tvFailed.setText(String.valueOf(failedCount));
        String allUsage = String.valueOf(allWaterUsage) + "吨";
        tvAllWaterUseAge.setText(allUsage);
    }

}
