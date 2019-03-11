package com.emi.emireading.entities;


import com.emi.emireading.core.config.UrlConstants;

/**
 * @author :zhoujian
 * @description : 版本信息实体类
 * @company :翼迈科技
 * @date: 2017年10月31日上午 09:44
 * @Email: 971613168@qq.com
 */

public class VersionInfo {
    private String id;
    private String versionName;
    private int versionCode;
    /**
     * 是否强制升级 0是 1否
     */
    private String isMustUpgrade;
    private String upgradeDesc;
    private String apkPath;
    private String iosAppStorePath;
    private String createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getIsMustUpgrade() {
        return isMustUpgrade;
    }

    public void setIsMustUpgrade(String isMustUpgrade) {
        this.isMustUpgrade = isMustUpgrade;
    }

    public String getUpgradeDesc() {
        return upgradeDesc;
    }

    public void setUpgradeDesc(String upgradeDesc) {
        this.upgradeDesc = upgradeDesc;
    }

    public String getApkPath() {
        return UrlConstants.HOST + apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getIosAppStorePath() {
        return iosAppStorePath;
    }

    public void setIosAppStorePath(String iosAppStorePath) {
        this.iosAppStorePath = iosAppStorePath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AppVersionDO [id=" + id + ", versionName=" + versionName
                + ", versionCode=" + versionCode + ", isMustUpgrade="
                + isMustUpgrade + ", upgradeDesc=" + upgradeDesc + ", apkPath="
                + apkPath + ", iosAppStorePath=" + iosAppStorePath
                + ", createTime=" + createTime + "]";
    }
}
