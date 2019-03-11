package com.emi.emireading.core.request.response;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年08月20日下午 03:35
 * @Email: 971613168@qq.com
 */

public abstract class BaseResponseHandler implements IResponseListener {
    /**
     * 请求成功回调
     * @param statusCode
     * @param response
     */
    public abstract void onSuccess(int statusCode, Object response);

    @Override
    public void onProgress(long currentBytes, long totalBytes) {

    }

}
