package com.emi.emireading.upgrade.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.core.request.OkHttpUtils;
import com.emi.emireading.upgrade.event.CommonEvent;
import com.emi.emireading.upgrade.event.UpdateEventType;


/**
 * @author :zhoujian
 * @description : 正在下载对话框
 * @company :翼迈科技
 * @date :2018/8/19
 * @Email: 971613168@qq.com
 */
public class UpdateDownloadingActivity extends BaseUpdateActivity implements DialogInterface.OnCancelListener {
    private Dialog mDownLoadingDialog;
    private int mProgress;
    private boolean destroy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLoadingDialog();
    }

    @Override
    public void showDefaultDialog() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.downloading_layout, null);
        mDownLoadingDialog = new AlertDialog.Builder(this).setTitle("").setView(loadingView).create();
        if (getDownloadConfig().getForceUpdateListener() != null) {
            mDownLoadingDialog.setCancelable(false);
        } else {
            mDownLoadingDialog.setCancelable(true);
        }
        mDownLoadingDialog.setCanceledOnTouchOutside(false);
        ProgressBar pb = loadingView.findViewById(R.id.pb);
        TextView tvProgress = loadingView.findViewById(R.id.tv_progress);
        tvProgress.setText(String.format(getString(R.string.version_check_download_progress), mProgress));
        pb.setProgress(mProgress);
        mDownLoadingDialog.show();
    }

    @Override
    public void showCustomDialog() {
        mDownLoadingDialog = getDownloadConfig().getCustomDownloadingDialogListener().getCustomDownloadingDialog(this, mProgress, getDownloadConfig().getAppVersionInfo());
        View cancelView = mDownLoadingDialog.findViewById(R.id.version_check__loading_dialog_cancel);
        if (cancelView != null) {
            cancelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCancel(mDownLoadingDialog);
                }
            });
        }
        mDownLoadingDialog.show();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        OkHttpUtils.getInstance().getOkHttpClient().dispatcher().cancelAll();
        cancelHandler();
        checkForceUpdate();
        if (dialogInterface != null) {
            dialogInterface.dismiss();
        }
        finish();
    }


    private void destroy() {
        if (mDownLoadingDialog != null) {
            mDownLoadingDialog.dismiss();
        }
        finish();
    }


    private void destroyWithOutDismiss() {
        if (mDownLoadingDialog != null && mDownLoadingDialog.isShowing()) {
            mDownLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyWithOutDismiss();
        destroy = true;
    }

    private void showLoadingDialog() {
        if (!destroy) {
            if (getDownloadConfig().getCustomDownloadingDialogListener() != null) {
                showCustomDialog();
            } else {
                showDefaultDialog();
            }
            mDownLoadingDialog.setOnCancelListener(this);
        }
    }


    @Override
    public void onReceiveEvent(CommonEvent commonEvent) {
        switch (commonEvent.getEventType()) {
            case UpdateEventType.EVENT_ACTION_UPDATE_DOWNLOADING_PROGRESS:
                int progress = (int) commonEvent.getData();
                mProgress = progress;
                updateProgress(progress);
                break;
            case UpdateEventType.EVENT_ACTION_DOWNLOAD_COMPLETE:
            case UpdateEventType.EVENT_ACTION_CLOSE_DOWNLOADING_ACTIVITY:
                destroy();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        destroy = false;
        if (mDownLoadingDialog != null && !mDownLoadingDialog.isShowing()) {
            mDownLoadingDialog.show();
        }
    }

    private void updateProgress(int mProgress) {
        if (destroy || mDownLoadingDialog == null) {
            return;
        }
        if (getDownloadConfig().getCustomDownloadingDialogListener() != null) {
            getDownloadConfig().getCustomDownloadingDialogListener().updateProgress(mDownLoadingDialog, mProgress, getDownloadConfig().getAppVersionInfo());
        } else {
            ProgressBar pb = mDownLoadingDialog.findViewById(R.id.pb);
            pb.setProgress(mProgress);
            TextView tvProgress = mDownLoadingDialog.findViewById(R.id.tv_progress);
            tvProgress.setText(String.format(getString(R.string.version_check_download_progress), mProgress));
            if (!mDownLoadingDialog.isShowing()) {
                mDownLoadingDialog.show();
            }
        }
    }


}
