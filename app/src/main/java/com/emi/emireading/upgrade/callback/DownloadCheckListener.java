package com.emi.emireading.upgrade.callback;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 检测下载监听
 * @company :翼迈科技
 * @date 2018年08月18日上午 10:11
 * @Email: 971613168@qq.com
 */

public interface DownloadCheckListener {
    /**
     * 下载进度回调
     * @param progress
     */
    void onCheckerDownloading(int progress);

    /**
     * 下载成功
     * @param file
     */
    void onCheckerDownloadSuccess(File file);

    /**
     * 下载失败
     * @param msg
     */
    void onCheckerDownloadFail(String msg);

    /**
     * 开始下载
     * @param
     */
    void onCheckerStartDownload();
}
