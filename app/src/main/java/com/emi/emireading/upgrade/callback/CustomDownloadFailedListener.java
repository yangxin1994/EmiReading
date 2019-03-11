package com.emi.emireading.upgrade.callback;

import android.app.Dialog;
import android.content.Context;

import com.emi.emireading.upgrade.entity.AppVersionInfo;


/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年08月20日上午 08:57
 * @Email: 971613168@qq.com
 */

public interface CustomDownloadFailedListener {
    /**
     * 自定义失败对话框接口
     *
     * @param context
     * @param versionBundle
     * @return
     */
    Dialog getCustomDownloadFailed(Context context, AppVersionInfo versionBundle);
}
