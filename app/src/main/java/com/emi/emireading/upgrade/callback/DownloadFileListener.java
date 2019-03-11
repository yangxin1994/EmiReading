package com.emi.emireading.upgrade.callback;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 下载文件监听
 * @company :翼迈科技
 * @date 2018年08月17日下午 03:44
 * @Email: 971613168@qq.com
 */

public interface DownloadFileListener {
    /**
     * 正在下载
     *
     * @param progress
     */
    void onDownloading(int progress);

    /**
     * 下载成功
     *
     * @param file
     */
    void onDownloadSuccess(File file);

    /**
     * 下载失败
     *
     * @param msg
     */
    void onDownloadFailed(String msg);
}
