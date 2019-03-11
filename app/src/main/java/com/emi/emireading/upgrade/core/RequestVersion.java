package com.emi.emireading.upgrade.core;


import com.emi.emireading.upgrade.callback.RequestVersionListener;
import com.emi.emireading.upgrade.helper.DownloadHelper;

/**
 * @author :zhoujian
 * @description : 请求版本信息
 * @company :翼迈科技
 * @date 2018年08月17日下午 03:06
 * @Email: 971613168@qq.com
 */

public class RequestVersion {
    private HttpRequestMethod requestMethod;
    private String requestUrl;
    private RequestVersionListener requestVersionListener;
    private RequestParams requestParams;

    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    public RequestVersion setRequestMethod(HttpRequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public RequestVersion setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    public RequestVersionListener getRequestVersionListener() {
        return requestVersionListener;
    }

    public RequestVersion setRequestVersionListener(RequestVersionListener requestVersionListener) {
        this.requestVersionListener = requestVersionListener;
        return this;
    }

    public RequestVersion() {
        requestMethod = HttpRequestMethod.GET;
    }


    public DownloadHelper request(RequestVersionListener requestVersionListener) {
        this.requestVersionListener = requestVersionListener;
        return new DownloadHelper(this, null);
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public RequestVersion setRequestParams(RequestParams requestParams) {
        this.requestParams = requestParams;
        return this;
    }

}
