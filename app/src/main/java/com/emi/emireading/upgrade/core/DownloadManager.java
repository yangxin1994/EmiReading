package com.emi.emireading.upgrade.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.OkHttpUtils;
import com.emi.emireading.core.request.response.DownloadResponseListener;
import com.emi.emireading.upgrade.callback.DownloadCheckListener;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 下载管理
 * @company :翼迈科技
 * @date 2018年08月17日下午 04:49
 * @Email: 971613168@qq.com
 */

public class DownloadManager {
    private static final String ERROR_URL = "下载链接不正确";
    private static final String ERROR_PATH = "路径有误";
    private static final String ERROR_FILE_NAME = "文件名为空";

    public static void downloadFile(final String downloadUrl, final String downloadPath, final String fileName, final DownloadCheckListener listener) {
        if (TextUtils.isEmpty(downloadUrl)) {
            handleFailed(listener, ERROR_URL);
            return;
        }
        if (TextUtils.isEmpty(downloadPath)) {
            handleFailed(listener, ERROR_PATH);
            return;
        }
        if (TextUtils.isEmpty(fileName)) {
            handleFailed(listener, ERROR_FILE_NAME);
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onCheckerStartDownload();
                }
            }
        });
        OkHttpUtils.getInstance().download(downloadUrl, downloadPath, fileName, new DownloadResponseListener() {
            @Override
            public void onFinish(final File downloadFile) {
                handleResultOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onCheckerDownloadSuccess(downloadFile);
                        }
                    }
                });
            }

            @Override
            public void onProgress(long currentBytes, long totalBytes) {
                final int progress = (int) (((double) currentBytes / totalBytes) * 100);
                if (listener != null) {
                    listener.onCheckerDownloading(progress);
                }
            }

            @Override
            public void onError(final String errorMsg) {
                handleResultOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onCheckerDownloadFail(errorMsg);
                        }
                    }
                });
            }
        });


    }


    private static void handleFailed(final DownloadCheckListener listener, final String msg) {
        handleResultOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onCheckerDownloadFail(msg);
                }
            }
        });
    }


    private static void handleResultOnUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }


    public static boolean checkAPKIsExists(Context context, String downloadPath) {
        File file = new File(downloadPath);
        boolean result = false;
        if (file.exists()) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(downloadPath,
                        PackageManager.GET_ACTIVITIES);
                //判断安装包存在并且包名一样并且版本号不一样
                if (info != null) {
                    LogUtil.d("context.getPackageName()：" + info.packageName + "版本号:" + info.versionCode);
                }
                LogUtil.i("context.getPackageName()：" + context.getPackageName() + "版本号:" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
                if (info != null && context.getPackageName().equalsIgnoreCase(info.packageName) && context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode != info.versionCode) {
                    result = true;
                }
            } catch (Exception e) {
                result = false;
            }
        }
        return result;

    }
}
