package com.emi.emireading.core.request.response;


/**
 * @author :zhoujian
 * @description : 字符串结果回调
 * @company :翼迈科技
 * @date: 2017年8月6日下午 04:05
 * @Email: 971613168@qq.com
 */
public abstract class RawResponseHandler implements IResponseListener {

    public abstract void onSuccess(int statusCode, String response);

    @Override
    public void onProgress(long currentBytes, long totalBytes) {

    }
}
