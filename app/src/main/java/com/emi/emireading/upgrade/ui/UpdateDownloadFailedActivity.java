package com.emi.emireading.upgrade.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.emi.emireading.R;
import com.emi.emireading.upgrade.core.VersionChecker;
import com.emi.emireading.upgrade.event.UpdateEventBusUtil;
import com.emi.emireading.upgrade.event.UpdateEventType;
import com.emi.emireading.upgrade.helper.DownloadHelper;


/**
 * @author :zhoujian
 * @description : 下载失败页面
 * @company :翼迈科技
 * @date 2018年08月20日上午 08:57
 * @Email: 971613168@qq.com
 */

public class UpdateDownloadFailedActivity extends BaseUpdateActivity implements DialogInterface.OnCancelListener {
    private Dialog mDownloadFailedDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDownloadFailedDialog();
    }

    @Override
    public void showDefaultDialog() {
        mDownloadFailedDialog = new AlertDialog.Builder(this).setMessage(getString(R.string.version_check_download_fail)).setPositiveButton(getString(R.string.version_check_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                retryDownload();
            }
        }).setNegativeButton(getString(R.string.version_check_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onCancel(mDownloadFailedDialog);
            }
        }).create();
        mDownloadFailedDialog.setCanceledOnTouchOutside(false);
        mDownloadFailedDialog.setCancelable(true);
        mDownloadFailedDialog.show();
    }

    @Override
    public void showCustomDialog() {
        mDownloadFailedDialog = new AlertDialog.Builder(this).setMessage(getString(R.string.version_check_download_fail_retry)).
                setPositiveButton(getString(R.string.version_check_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        retryDownload();
                    }
                }).setNegativeButton(getString(R.string.version_check_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onCancel(mDownloadFailedDialog);
            }
        }).create();
        mDownloadFailedDialog.setCanceledOnTouchOutside(false);
        mDownloadFailedDialog.setCancelable(true);
        mDownloadFailedDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDownloadFailedDialog != null && mDownloadFailedDialog.isShowing()) {
            mDownloadFailedDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDownloadFailedDialog != null && !mDownloadFailedDialog.isShowing()) {
            mDownloadFailedDialog.show();
        }
    }

    private void showDownloadFailedDialog() {
        UpdateEventBusUtil.sendEvent(UpdateEventType.EVENT_ACTION_CLOSE_DOWNLOADING_ACTIVITY);
        DownloadHelper downloadHelper = getDownloadConfig();
        if (downloadHelper != null && downloadHelper.getCustomDownloadFailedListener() != null) {
            showCustomDialog();
        } else {
            showDefaultDialog();
        }

    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        checkForceUpdate();
        cancelHandler();
        VersionChecker.getInstance().cancelAllTask(this);
        finish();
    }

    private void retryDownload() {
        UpdateEventBusUtil.sendEvent(UpdateEventType.EVENT_ACTION_START_DOWNLOAD_APK);
        finish();
    }


}
