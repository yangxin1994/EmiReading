package com.emi.emireading.upgrade.entity;

import android.os.Bundle;

/**
 * @author :zhoujian
 * @description : App版本信息
 * @company :翼迈科技
 * @date 2018年08月17日下午 03:58
 * @Email: 971613168@qq.com
 */

public class AppVersionInfo {
    private final String TITLE = "title", CONTENT = "content", DOWNLOAD_URL = "download_url";
    private Bundle versionBundle;
    private boolean isForceUpdate;

    public boolean isForceUpdate() {
        return isForceUpdate;
    }

    public AppVersionInfo setForceUpdate(boolean forceUpdate) {
        isForceUpdate = forceUpdate;
        return this;
    }

    public static AppVersionInfo create() {
        return new AppVersionInfo();
    }

    private AppVersionInfo() {
        versionBundle = new Bundle();
        versionBundle.putString(TITLE, "by `AppVersionInfo.setTitle()` to set your update title");
        versionBundle.putString(CONTENT, "by `AppVersionInfo.setContent()` to set your update content ");
    }

    public AppVersionInfo setDownloadUrl(String downloadUrl) {
        versionBundle.putString(DOWNLOAD_URL, downloadUrl);
        return this;
    }

    public String getDownloadUrl() {
        return versionBundle.getString(DOWNLOAD_URL);
    }

    public AppVersionInfo setTitle(String title) {
        versionBundle.putString(TITLE, title);
        return this;
    }

    public AppVersionInfo setContent(String content) {
        versionBundle.putString(CONTENT, content);
        return this;
    }

    public Bundle getVersionBundle() {
        return versionBundle;
    }

    public String getTitle() {
        return versionBundle.getString(TITLE);
    }

    public String getContent() {
        return versionBundle.getString(CONTENT);
    }
}
