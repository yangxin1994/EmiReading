package com.emi.emireading.upgrade.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.OkHttpUtils;
import com.emi.emireading.core.request.response.BaseResponseHandler;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.utils.AppUtils;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.log.EmiLog;
import com.emi.emireading.upgrade.callback.DownloadCheckListener;
import com.emi.emireading.upgrade.callback.RequestVersionListener;
import com.emi.emireading.upgrade.entity.AppVersionInfo;
import com.emi.emireading.upgrade.event.CommonEvent;
import com.emi.emireading.upgrade.event.UpdateEventBusUtil;
import com.emi.emireading.upgrade.event.UpdateEventType;
import com.emi.emireading.upgrade.helper.DownloadHelper;
import com.emi.emireading.upgrade.helper.NotificationHelper;
import com.emi.emireading.upgrade.helper.VersionHelper;
import com.emi.emireading.upgrade.ui.UpdateDialogActivity;
import com.emi.emireading.upgrade.ui.UpdateDownloadFailedActivity;
import com.emi.emireading.upgrade.ui.UpdateDownloadingActivity;
import com.emi.emireading.upgrade.ui.UpdatePermissionActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;


/**
 * @author :zhoujian
 * @description : 版本检测服务
 * @company :翼迈科技
 * @date 2018年08月18日上午 10:44
 * @Email: 971613168@qq.com
 */

public class CheckVersionService extends Service {
    public static DownloadHelper mDownloadHelper;
    private VersionHelper versionHelper;
    private NotificationHelper notificationHelper;
    private boolean serviceAlive = false;
    private static final String TAG = "CheckVersionService";

    @Override
    public void onCreate() {
        super.onCreate();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Log.e(TAG, "CheckVersionService-->init已执行");
        init();
        LogUtil.d("测试消耗时间---->", "已执行4");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    private void init() {
        if (mDownloadHelper == null) {
            return;
        }
        serviceAlive = true;
        versionHelper = new VersionHelper(getApplicationContext(), mDownloadHelper);
        notificationHelper = new NotificationHelper(getApplicationContext(), mDownloadHelper);
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                onHandleWork();
            }
        });
    }


    protected void onHandleWork() {
        if (checkWhetherNeedRequestVersion()) {
            requestVersion();
        } else {
            downloadAPK();
        }
    }

    private boolean checkWhetherNeedRequestVersion() {
        return mDownloadHelper.getRequestVersion() != null;
    }

    /**
     * 请求版本接口
     */
    private void requestVersion() {
        LogUtil.i(TAG, "url =" + mDownloadHelper.getDownloadUrl());
        RequestVersion requestVersion = mDownloadHelper.getRequestVersion();
        HttpRequestMethod requestMethod = requestVersion.getRequestMethod();
        final RequestVersionListener requestVersionListener = requestVersion.getRequestVersionListener();
        RequestCallback requestCallback = new RequestCallback(requestVersionListener);
        switch (requestMethod) {
            case GET:
                OkHttpUtils.getInstance().get(requestVersion.getRequestUrl(), requestVersion.getRequestParams(), requestCallback);
                break;
            case POST:
                OkHttpUtils.getInstance().post(requestVersion.getRequestUrl(), requestVersion.getRequestParams(), requestCallback);
                break;
            default:
                OkHttpUtils.getInstance().get(requestVersion.getRequestUrl(), requestVersion.getRequestParams(), requestCallback);
                break;
        }

    }

    private class RequestCallback extends BaseResponseHandler {
        private RequestVersionListener requestVersionListener;
        private Handler handler = new Handler(Looper.getMainLooper());

        public RequestCallback(RequestVersionListener requestVersionListener) {
            this.requestVersionListener = requestVersionListener;
        }

        @Override
        public void onError(int statusCode, final String errorMsg) {
            if (!serviceAlive) {
                return;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    requestVersionListener.onRequestVersionFailed(errorMsg);
                    VersionChecker.getInstance().cancelAllTask(getApplicationContext());
                }
            });
        }

        @Override
        public void onSuccess(int statusCode, final Object response) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    LogUtil.e(TAG, "奇葩问题:" + response.toString());
                    if (requestVersionListener == null) {
                        Toast.makeText(getApplicationContext(), "未设置版本监听!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AppVersionInfo versionInfo = requestVersionListener.onRequestVersionSuccess(response.toString());
                    if (versionInfo != null) {
                        mDownloadHelper.setAppVersionInfo(versionInfo);
                        downloadAPK();
                    } else {
                        ToastUtils.showToastNormal("当前已经是最新版本");
                    }
                }
            });
        }
    }


    private void downloadAPK() {
        if (mDownloadHelper.getAppVersionInfo() != null) {
            if (mDownloadHelper.isDirectDownload()) {
                UpdateEventBusUtil.sendEvent(UpdateEventType.EVENT_ACTION_START_DOWNLOAD_APK);
                EmiLog.d(TAG, "已执行" + "  UpdateEventBusUtil.sendEvent");
            } else {
                if (mDownloadHelper.isSilentDownload()) {
                    requestPermissionAndDownload();
                } else {
                    showVersionDialog();
                }
            }
        } else {
            VersionChecker.getInstance().cancelAllTask(getApplicationContext());
        }
        EmiLog.d(TAG, "已执行5");
    }


    private void requestPermissionAndDownload() {
        Intent intent = new Intent(this, UpdatePermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    /**
     * 开启UI展示界面
     */
    private void showVersionDialog() {
        Intent intent = new Intent(this, UpdateDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveEvent(CommonEvent commonEvent) {
        switch (commonEvent.getEventType()) {
            case UpdateEventType.EVENT_ACTION_START_DOWNLOAD_APK:
                requestPermissionAndDownload();
                break;
            case UpdateEventType.EVENT_ACTION_REQUEST_PERMISSION:
                boolean permissionResult = (boolean) commonEvent.getData();
                if (permissionResult) {
                    startDownloadApk();
                } else {
                    stopSelf();
                }
                break;
            default:
                break;
        }

    }

    @WorkerThread
    private void startDownloadApk() {
        //判断是否缓存并且是否强制重新下载
        mDownloadHelper.setForceReDownload(true);
        final String downloadPath = mDownloadHelper.getDownloadApkPath() + getString(R.string.version_check_download_apk_name, "emi3.0");
        LogUtil.d("下载的路径：" + downloadPath);
        if (DownloadManager.checkAPKIsExists(getApplicationContext(), downloadPath) && !mDownloadHelper.isForceReDownload()) {
            install(downloadPath);
            return;
        }
        versionHelper.checkAndDeleteAPK();
        String downloadUrl = mDownloadHelper.getAppVersionInfo().getDownloadUrl();
        if (downloadUrl == null && mDownloadHelper.getAppVersionInfo() != null) {
            downloadUrl = mDownloadHelper.getAppVersionInfo().getDownloadUrl();
        }
        if (downloadUrl == null) {
            VersionChecker.getInstance().cancelAllTask(getApplicationContext());
            throw new RuntimeException("you must set a download url for download function using");
        }
        String fileName = EmiStringUtil.getFileNameWithSuffix(downloadUrl);
        LogUtil.d("下载的文件名：" + fileName);
        final String apkPath = mDownloadHelper.getDownloadApkPath() + File.separator + fileName;
        LogUtil.e("下载的文件名：" + apkPath);
        DownloadManager.downloadFile(downloadUrl, mDownloadHelper.getDownloadApkPath(), fileName, new DownloadCheckListener() {
            @Override
            public void onCheckerDownloading(int progress) {
                if (serviceAlive) {
                    if (!mDownloadHelper.isSilentDownload()) {
                        notificationHelper.updateNotification(progress);
                        updateDownloadingDialogProgress(progress);
                    }
                }

            }

            @Override
            public void onCheckerDownloadSuccess(File file) {
                if (serviceAlive) {
                    if (!mDownloadHelper.isSilentDownload()) {
                        if(file != null){
                            notificationHelper.showDownloadCompleteNotification(file);
                        }
                    }
                    install(apkPath);
                }
            }

            @Override
            public void onCheckerDownloadFail(String msg) {
                if (!serviceAlive) {
                    return;
                }
                if (!mDownloadHelper.isSilentDownload()) {
                    UpdateEventBusUtil.sendEvent(UpdateEventType.EVENT_ACTION_CLOSE_DOWNLOADING_ACTIVITY);
                    if (mDownloadHelper.isShowDownloadFailDialog()) {
                        showDownloadFailedDialog();
                    }
                    notificationHelper.showDownloadFailedNotification();
                } else {
                    VersionChecker.getInstance().cancelAllTask(getApplicationContext());
                }
            }

            @Override
            public void onCheckerStartDownload() {
                if (!mDownloadHelper.isSilentDownload()) {
                    notificationHelper.showNotification();
                    showDownloadingDialog();
                }
            }
        });

    }

    private void install(final String apkPath) {
        UpdateEventBusUtil.sendEvent(UpdateEventType.EVENT_ACTION_DOWNLOAD_COMPLETE);
        if (mDownloadHelper.isSilentDownload()) {
            showVersionDialog();
        } else {
            versionHelper.checkForceUpdate();
            AppUtils.installApk(getApplicationContext(), new File(apkPath));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateDownloadingDialogProgress(int progress) {
        CommonEvent commonEvent = new CommonEvent();
        commonEvent.setEventType(UpdateEventType.EVENT_ACTION_UPDATE_DOWNLOADING_PROGRESS);
        commonEvent.setData(progress);
        commonEvent.setSuccessful(true);
        EventBus.getDefault().post(commonEvent);
    }


    @Override
    public void onDestroy() {
        mDownloadHelper = null;
        versionHelper = null;
        if (notificationHelper != null) {
            notificationHelper.onDestroy();
        }
        notificationHelper = null;
        serviceAlive = false;
        OkHttpUtils.getInstance().getOkHttpClient().dispatcher().cancelAll();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    private void showDownloadFailedDialog() {
        Intent intent = new Intent(this, UpdateDownloadFailedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static void enqueueWork(final Context context, final DownloadHelper downloadHelper) {
        //清除之前的任务，如果有
        VersionChecker.getInstance().cancelAllTask(context);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDownloadHelper == null) {
                    mDownloadHelper = downloadHelper;
                }
                Intent intent = new Intent(context, CheckVersionService.class);
                context.startService(intent);
            }
        }, 0);
    }


    private void showDownloadingDialog() {
        if (mDownloadHelper.isShowDownloadingDialog()) {
            Intent intent = new Intent(this, UpdateDownloadingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
