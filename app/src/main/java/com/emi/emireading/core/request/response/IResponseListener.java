package com.emi.emireading.core.request.response;


/**
 * @author :zhoujian
 * @description : 网络回调接口
 * @company :翼迈科技
 * @date: 2017年8月6日下午 04:01
 * @Email: 971613168@qq.com
 */
public interface IResponseListener {
    /**
     * 请求错误回调
     * @param statusCode
     * @param errorMsg
     */
    void onError(int statusCode, String errorMsg);
    /**
     * 进度回调
     * @param currentBytes
     * @param totalBytes
     */
    void onProgress(long currentBytes, long totalBytes);

}
