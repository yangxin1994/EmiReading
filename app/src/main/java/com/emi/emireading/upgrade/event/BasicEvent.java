package com.emi.emireading.upgrade.event;

/**
 * @author :zhoujian
 * @description : 基类事件
 * @company :翼迈科技
 * @date :2018/8/18
 * @Email: 971613168@qq.com
 */
public class BasicEvent {
    private int eventType;

    public int getEventType() {
        return eventType;
    }

    public BasicEvent setEventType(int eventType) {
        this.eventType = eventType;
        return this;
    }
}
