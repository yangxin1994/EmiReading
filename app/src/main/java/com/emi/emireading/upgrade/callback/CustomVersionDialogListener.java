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

public interface CustomVersionDialogListener {
    /**
     * 自定义版本更新对话框接口
     *
     * @param context
     * @param appVersionInfo
     * @return
     */
    Dialog getCustomVersionDialog(Context context, AppVersionInfo appVersionInfo);
}
