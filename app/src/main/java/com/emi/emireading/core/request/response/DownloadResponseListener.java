package com.emi.emireading.core.request.response;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 下载监听
 * @company :翼迈科技
 * @date: 2017年8月6日下午 04:02
 * @Email: 971613168@qq.com
 */
public abstract class DownloadResponseListener {
    /**
     * 下载完成监听
     * @param downloadFile
     */
    public abstract void onFinish(File downloadFile);
    /**
     * 下载过程中的文件大小
     * @param currentBytes
     * @param totalBytes
     */
    public abstract void onProgress(long currentBytes, long totalBytes);


    /**
     * 下载出错回调
     * @param errorMsg
     */
    public abstract void onError(String errorMsg);
}
