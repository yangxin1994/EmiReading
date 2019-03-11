package com.emi.emireading.upgrade.helper;

import android.content.Context;

import com.emi.emireading.R;
import com.emi.emireading.upgrade.core.DownloadManager;
import com.emi.emireading.upgrade.core.VersionChecker;

import java.io.File;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年08月18日上午 10:58
 * @Email: 971613168@qq.com
 */

public class VersionHelper {
    private DownloadHelper mDownloadHelper;
    private Context mContext;

    public VersionHelper(Context context, DownloadHelper builder) {
        this.mContext = context;
        this.mDownloadHelper = builder;
    }

    /**
     * 验证安装包是否存在，并且在安装成功情况下删除安装包
     */
    public void checkAndDeleteAPK() {
        //判断versioncode与当前版本不一样的apk是否存在，存在删除安装包
        try {
            String downloadPath = mDownloadHelper.getDownloadApkPath() + mContext.getString(R.string.version_check_download_apk_name, mContext.getPackageName());
            if (!DownloadManager.checkAPKIsExists(mContext, downloadPath)) {
                new File(downloadPath).delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkForceUpdate() {
        if (mDownloadHelper.getForceUpdateListener() != null) {
            mDownloadHelper.getForceUpdateListener().onShouldForceUpdate();
            VersionChecker.getInstance().cancelAllTask(mContext);
        }
    }
}
