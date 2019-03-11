package com.emi.emireading.event;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年01月19日上午 10:23
 * @Email: 971613168@qq.com
 */

public class ServiceEvent {

    public enum NOTIFY_TYPE{
        /**
         * 服务创建
         */
        SERVICE_ON_CREATE,
        /**
         * 服务开始
         */
        SERVICE_ON_START,

        /**
         *服务销毁
         */
        SERVICE_ON_DESTROY,
        /**
         * 连接蓝牙
         */
        CONNECT_BLUETOOTH
    }

    public ServiceEvent(NOTIFY_TYPE mNotifyType) {
        this.mNotifyType = mNotifyType;
    }

    public NOTIFY_TYPE getNotifyType() {
        return mNotifyType;
    }

    public void setmNotifyType(NOTIFY_TYPE mNotifyType) {
        this.mNotifyType = mNotifyType;
    }

    public ServiceEvent() {

    }


    private NOTIFY_TYPE mNotifyType;

    private int connectState;

    public int getConnectState() {
        return connectState;
    }

    public void setConnectState(int connectState) {
        this.connectState = connectState;
    }

    public ServiceEvent(int connectState) {

        this.connectState = connectState;
    }

    public ServiceEvent(NOTIFY_TYPE mNotifyType, int connectState) {
        this.mNotifyType = mNotifyType;
        this.connectState = connectState;
    }
}
