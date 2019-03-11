package com.emi.emireading.upgrade.event;

/**
 * @author :zhoujian
 * @description : 公共事件
 * @company :翼迈科技
 * @date :2018/8/18
 * @Email: 971613168@qq.com
 */
public class CommonEvent<T> extends BasicEvent {
    private boolean isSuccessful;
    private String message;
    private T data;
    private int responseCode;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public CommonEvent setSuccessful(boolean successful) {
        isSuccessful = successful;
        return this;
    }


    public int getResponseCode() {
        return responseCode;
    }

    public CommonEvent setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }


    public String getMessage() {
        return message;
    }

    public CommonEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public CommonEvent setData(T data) {
        this.data = data;
        return this;
    }
    public static CommonEvent getSimpleEvent(int type){
        CommonEvent commonEvent=new CommonEvent();
        commonEvent.setSuccessful(true);
        commonEvent.setEventType(type);
        return commonEvent;
    }
}
