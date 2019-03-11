package com.emi.emireading.upgrade.entity;


import com.emi.emireading.R;

/**
 * @author :zhoujian
 * @description : 通知配置
 * @company :翼迈科技
 * @date 2018年08月17日下午 04:23
 * @Email: 971613168@qq.com
 */

public class NotificationConfig {
    private int icon;
    private String contentTitle;
    private String ticker;
    private String contentText;
    private boolean ringtoneEnable;

    private NotificationConfig() {
        icon = R.mipmap.download;
        ringtoneEnable = true;
    }

    public static NotificationConfig create() {
        return new NotificationConfig();
    }

    public int getIcon() {
        return icon;
    }

    public NotificationConfig setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public NotificationConfig setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        return this;
    }

    public String getTicker() {
        return ticker;
    }

    public NotificationConfig setTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public String getContentText() {
        return contentText;
    }

    public NotificationConfig setContentText(String contentText) {
        this.contentText = contentText;
        return this;
    }

    public boolean ringtoneEnable() {
        return ringtoneEnable;
    }

    public NotificationConfig setRingtone(boolean ringtoneEnable) {
        this.ringtoneEnable = ringtoneEnable;
        return this;
    }


}
