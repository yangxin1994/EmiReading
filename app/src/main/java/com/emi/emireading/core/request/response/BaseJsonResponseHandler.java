package com.emi.emireading.core.request.response;

import org.json.JSONObject;

/**
 * @author :zhoujian
 * @description : json类型的回调接口
 * @company :翼迈科技
 * @date: 2017年8月6日下午 04:02
 * @Email: 971613168@qq.com
 */

public abstract class BaseJsonResponseHandler implements IResponseListener {

    /**
     * 请求成功回调
     * @param statusCode
     * @param response
     */
    public abstract void onSuccess(int statusCode, JSONObject response);

    @Override
    public void onProgress(long currentBytes, long totalBytes) {

    }
}
