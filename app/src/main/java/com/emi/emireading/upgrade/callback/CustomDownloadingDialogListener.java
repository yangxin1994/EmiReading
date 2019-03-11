package com.emi.emireading.upgrade.callback;

import android.app.Dialog;
import android.content.Context;

import com.emi.emireading.upgrade.entity.AppVersionInfo;


/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date :2018/8/19
 * @Email: 971613168@qq.com
 */
public interface CustomDownloadingDialogListener {

    /**
     * 自定义下载进度对话框
     *
     * @param context
     * @param progress
     * @param versionBundle
     * @return
     */
    Dialog getCustomDownloadingDialog(Context context, int progress, AppVersionInfo versionBundle);

    /**
     * 更新进度
     *
     * @param dialog
     * @param progress
     * @param versionBundle
     */
    void updateProgress(Dialog dialog, int progress, AppVersionInfo versionBundle);
}
