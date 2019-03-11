package com.emi.emireading.upgrade.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.core.utils.AppUtils;
import com.emi.emireading.upgrade.callback.CustomVersionDialogListener;
import com.emi.emireading.upgrade.core.CheckVersionService;
import com.emi.emireading.upgrade.core.VersionChecker;
import com.emi.emireading.upgrade.entity.AppVersionInfo;
import com.emi.emireading.upgrade.event.CommonEvent;
import com.emi.emireading.upgrade.event.UpdateEventBusUtil;
import com.emi.emireading.upgrade.event.UpdateEventType;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 升级对话框
 * @company :翼迈科技
 * @date :2018/8/19
 * @Email: 971613168@qq.com
 */
public class UpdateDialogActivity extends BaseUpdateActivity implements Dialog.OnCancelListener {
    private Dialog mVersionDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showVersionDialog();
    }

    @Override
    public void showDefaultDialog() {
        AppVersionInfo appVersionInfo = CheckVersionService.mDownloadHelper.getAppVersionInfo();
        String title = "", content = "";
        if (appVersionInfo != null) {
            title = appVersionInfo.getTitle();
            content = appVersionInfo.getContent();
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle(title).setMessage(content).setPositiveButton(getString(R.string.version_check_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dealVersionDialogCommit();
            }
        });
        if (getDownloadConfig().getForceUpdateListener() == null) {
            alertBuilder.setNegativeButton(getString(R.string.version_check_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onCancel(mVersionDialog);
                }
            });
            alertBuilder.setCancelable(false);
        } else {
            alertBuilder.setCancelable(false);
        }
        mVersionDialog = alertBuilder.create();
        mVersionDialog.setCanceledOnTouchOutside(false);
        mVersionDialog.show();
    }

    @Override
    public void showCustomDialog() {
        CustomVersionDialogListener dialogListener = getDownloadConfig().getCustomVersionDialogListener();
        if (dialogListener == null) {
            Toast.makeText(this, "dialogListener为null！", Toast.LENGTH_SHORT).show();
            return;
        }
        mVersionDialog = dialogListener.getCustomVersionDialog(this, getDownloadConfig().getAppVersionInfo());
        try {
            //自定义dialog，commit button 必须存在
            final View view = mVersionDialog.findViewById(R.id.version_check_version_dialog_commit);
            if (view == null) {
                throwWrongIdsException();
                return;
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dealVersionDialogCommit();
                }
            });
            //如果有取消按钮，id也必须对应
            View cancelView = mVersionDialog.findViewById(R.id.version_check__failed_dialog_cancel);
            if (cancelView != null) {
                cancelView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onCancel(mVersionDialog);
                    }
                });
            }
            mVersionDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            throwWrongIdsException();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        cancelHandler();
        checkForceUpdate();
        if (dialogInterface != null) {
            dialogInterface.dismiss();
        }
        VersionChecker.getInstance().cancelAllTask(this);
        finish();
    }

    private void dealVersionDialogCommit() {
        //如果是静默下载直接安装
        if (getDownloadConfig().isSilentDownload()) {
            String downloadPath = getDownloadConfig().getDownloadApkPath() + getString(R.string.version_check_download_apk_name, getPackageName());
            AppUtils.installApk(this, new File(downloadPath));
            checkForceUpdate();
            //否则开始下载
        } else {
            UpdateEventBusUtil.sendEvent(UpdateEventType.EVENT_ACTION_START_DOWNLOAD_APK);
        }
        finish();
    }

    private void showVersionDialog() {
        if (getDownloadConfig().getCustomVersionDialogListener() != null) {
            showCustomDialog();
        } else {
            showDefaultDialog();
        }
        mVersionDialog.setOnCancelListener(this);
    }

    @Override
    public void onReceiveEvent(CommonEvent commonEvent) {
        switch (commonEvent.getEventType()) {
            case UpdateEventType.EVENT_ACTION_SHOW_VERSION_DIALOG:
                showVersionDialog();
                break;
            default:
                break;
        }
    }
}
