package com.emi.emireading.upgrade.helper;

import android.content.Context;

import com.emi.emireading.upgrade.callback.CustomDownloadFailedListener;
import com.emi.emireading.upgrade.callback.CustomDownloadingDialogListener;
import com.emi.emireading.upgrade.callback.CustomVersionDialogListener;
import com.emi.emireading.upgrade.callback.DownloadFileListener;
import com.emi.emireading.upgrade.callback.ForceUpdateListener;
import com.emi.emireading.upgrade.callback.OnCancelListener;
import com.emi.emireading.upgrade.core.CheckVersionService;
import com.emi.emireading.upgrade.core.RequestVersion;
import com.emi.emireading.upgrade.entity.AppVersionInfo;
import com.emi.emireading.upgrade.entity.NotificationConfig;


/**
 * @author :zhoujian
 * @description : 下载配置信息
 * @company :翼迈科技
 * @date 2018年08月17日下午 03:34
 * @Email: 971613168@qq.com
 */

public class DownloadHelper {
    private RequestVersion requestVersion;
    private final static String TAG = "DownloadHelper";
    /**
     * 是否静默安装
     */
    private boolean silentDownload;
    private String downloadApkPath;
    /**
     * 强制重新下载
     */
    private boolean forceReDownload;

    private String downloadUrl;

    private boolean showDownloadingDialog;

    private boolean showNotification;
    private boolean showDownloadFailDialog;
    private boolean directDownload;
    private OnCancelListener onCancelListener;
    private CustomVersionDialogListener customVersionDialogListener;
    private CustomDownloadingDialogListener customDownloadingDialogListener;
    private CustomDownloadFailedListener customDownloadFailedListener;

    public CustomDownloadFailedListener getCustomDownloadFailedListener() {
        return customDownloadFailedListener;
    }

    public DownloadHelper setCustomDownloadFailedListener(CustomDownloadFailedListener customDownloadFailedListener) {
        this.customDownloadFailedListener = customDownloadFailedListener;
        return this;
    }

    public CustomDownloadingDialogListener getCustomDownloadingDialogListener() {
        return customDownloadingDialogListener;
    }

    public DownloadHelper setCustomDownloadingDialogListener(CustomDownloadingDialogListener customDownloadingDialogListener) {
        this.customDownloadingDialogListener = customDownloadingDialogListener;
        return this;
    }

    public CustomVersionDialogListener getCustomVersionDialogListener() {
        return customVersionDialogListener;
    }

    public DownloadHelper setCustomVersionDialogListener(CustomVersionDialogListener customVersionDialogListener) {
        this.customVersionDialogListener = customVersionDialogListener;
        return this;
    }

    public OnCancelListener getOnCancelListener() {
        return onCancelListener;
    }

    public DownloadHelper setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    public DownloadHelper setRequestVersion(RequestVersion requestVersion) {
        this.requestVersion = requestVersion;
        return this;
    }

    public boolean isShowDownloadFailDialog() {
        return showDownloadFailDialog;
    }

    public DownloadHelper setShowDownloadFailDialog(boolean showDownloadFailDialog) {
        this.showDownloadFailDialog = showDownloadFailDialog;
        return this;
    }

    public boolean isDirectDownload() {
        return directDownload;
    }

    public DownloadHelper setDirectDownload(boolean directDownload) {
        this.directDownload = directDownload;
        return this;
    }

    private DownloadFileListener downloadFileListener;
    private ForceUpdateListener forceUpdateListener;

    public DownloadHelper() {
        throw new RuntimeException("can not be instantiated from outside");
    }

    private NotificationConfig notificationConfig;
    private AppVersionInfo appVersionInfo;

    private void initialize() {
        silentDownload = false;
        //缓存
        downloadApkPath = FileHelper.getDownloadApkCachePath();
        forceReDownload = false;
        showDownloadingDialog = true;
        showNotification = true;
        directDownload = false;
        showDownloadFailDialog = true;
        notificationConfig = NotificationConfig.create();
    }

    public DownloadHelper(RequestVersion requestVersion, AppVersionInfo appVersionBundle) {
        this.requestVersion = requestVersion;
        this.appVersionInfo = appVersionBundle;
        initialize();
    }

    public DownloadHelper(RequestVersion requestVersion, boolean silentDownload, String downloadApkPath, boolean forceReDownload, String downloadUrl, boolean showDownloadingDialog, boolean showNotification, NotificationConfig notificationConfig, AppVersionInfo appVersionInfo) {
        this.requestVersion = requestVersion;
        this.silentDownload = silentDownload;
        this.downloadApkPath = downloadApkPath;
        this.forceReDownload = forceReDownload;
        this.downloadUrl = downloadUrl;
        this.showDownloadingDialog = showDownloadingDialog;
        this.showNotification = showNotification;
        this.notificationConfig = notificationConfig;
        this.appVersionInfo = appVersionInfo;
    }

    public RequestVersion getRequestVersion() {
        return requestVersion;
    }


    public boolean isSilentDownload() {
        return silentDownload;
    }

    public DownloadHelper setSilentDownload(boolean silentDownload) {
        this.silentDownload = silentDownload;
        return this;
    }

    public String getDownloadApkPath() {
        return downloadApkPath;
    }

    public DownloadHelper setDownloadApkPath(String downloadApkPath) {
        this.downloadApkPath = downloadApkPath;
        return this;
    }

    public boolean isForceReDownload() {
        return forceReDownload;
    }

    public DownloadHelper setForceReDownload(boolean forceReDownload) {
        this.forceReDownload = forceReDownload;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isShowDownloadingDialog() {
        return showDownloadingDialog;
    }

    public DownloadHelper setShowDownloadingDialog(boolean showDownloadingDialog) {
        this.showDownloadingDialog = showDownloadingDialog;
        return this;
    }

    public boolean isShowNotification() {
        return showNotification;
    }

    public DownloadHelper setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public DownloadFileListener getDownloadFileListener() {
        return downloadFileListener;
    }

    public DownloadHelper setDownloadFileListener(DownloadFileListener downloadFileListener) {
        this.downloadFileListener = downloadFileListener;
        return this;
    }

    public NotificationConfig getNotificationConfig() {
        return notificationConfig;
    }

    public DownloadHelper setNotificationConfig(NotificationConfig notificationConfig) {
        this.notificationConfig = notificationConfig;
        return this;
    }

    public AppVersionInfo getAppVersionInfo() {
        return appVersionInfo;
    }

    public DownloadHelper setAppVersionInfo(AppVersionInfo appVersionInfo) {
        this.appVersionInfo = appVersionInfo;
        return this;
    }

    public ForceUpdateListener getForceUpdateListener() {
        return forceUpdateListener;
    }

    public DownloadHelper setForceUpdateListener(ForceUpdateListener forceUpdateListener) {
        this.forceUpdateListener = forceUpdateListener;
        return this;
    }

    public void executeTask(Context context) {
        CheckVersionService.enqueueWork(context, this);
    }

}
