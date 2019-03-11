package com.emi.emireading.upgrade.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.emi.emireading.R;
import com.emi.emireading.upgrade.core.VersionFileProvider;
import com.emi.emireading.upgrade.entity.NotificationConfig;
import com.emi.emireading.upgrade.ui.UpdatePermissionActivity;

import java.io.File;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * @author :zhoujian
 * @description : 通知管理类
 * @company :翼迈科技
 * @date 2018年08月18日上午 10:52
 * @Email: 971613168@qq.com
 */

public class NotificationHelper {
    private Context mContext;
    private DownloadHelper mDownloadHelper;
    NotificationCompat.Builder notificationBuilder = null;
    NotificationManager manager = null;
    private boolean downloadSuccess = false, downloadFailed = false;
    private int currentProgress = 0;
    private String contentText;
    private final int NOTIFICATION_ID = 0;
    private static final int PROGRESS_MIN = 5;

    public NotificationHelper(Context context, DownloadHelper downloadHelper) {
        this.mContext = context;
        this.mDownloadHelper = downloadHelper;
        currentProgress = 0;
    }

    public void onDestroy() {
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
    }

    /**
     * update notification progress
     *
     * @param progress the progress of notification
     */
    public void updateNotification(int progress) {
        if (mDownloadHelper.isShowNotification()) {
            if ((progress - currentProgress) > PROGRESS_MIN && !downloadSuccess && !downloadFailed) {
                notificationBuilder.setContentIntent(null);
                notificationBuilder.setContentText(String.format(contentText, progress));
                notificationBuilder.setProgress(100, progress, false);
                manager.notify(NOTIFICATION_ID, notificationBuilder.build());
                currentProgress = progress;
            }
        }
    }


    private NotificationCompat.Builder createNotification() {
        final String channelId = "0", channelName = "LIB_NOTIFICATION";
        NotificationCompat.Builder builder;
        NotificationConfig notificationConfig = mDownloadHelper.getNotificationConfig();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            NotificationManager manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
        builder = new NotificationCompat.Builder(mContext, channelId);
        builder.setAutoCancel(true);
        builder.setSmallIcon(mDownloadHelper.getNotificationConfig().getIcon());
        //set content title
        String contentTitle = mContext.getString(R.string.app_name);
        if (notificationConfig.getContentTitle() != null) {
            contentTitle = notificationConfig.getContentTitle();
        }
        builder.setContentTitle(contentTitle);
        //set ticker
        String ticker = mContext.getString(R.string.version_check_downloading);
        if (notificationConfig.getTicker() != null) {
            ticker = notificationConfig.getTicker();
        }
        builder.setTicker(ticker);
        //set content text
        contentText = mContext.getString(R.string.version_check_download_progress);
        if (notificationConfig.getContentText() != null) {
            contentText = notificationConfig.getContentText();
        }
        builder.setContentText(String.format(contentText, 0));
        if (notificationConfig.ringtoneEnable()) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mContext, notification);
            r.play();
        }
        return builder;
    }


    /**
     * show download success notification
     */
    public void showDownloadCompleteNotification(File file) {
        downloadSuccess = true;
        if (!mDownloadHelper.isShowNotification()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = VersionFileProvider.getUriForFile(mContext, mContext.getPackageName() + ".versionProvider", file);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        //设置intent的类型
        i.setDataAndType(uri,
                "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, 0);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setContentText(mContext.getString(R.string.version_check_download_finish));
        notificationBuilder.setProgress(100, 100, false);
        manager.cancelAll();
        manager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void showDownloadFailedNotification() {
        downloadSuccess = false;
        downloadFailed = true;
        if (mDownloadHelper.isShowNotification()) {
            Intent intent = new Intent(mContext, UpdatePermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);
            notificationBuilder.setContentText(mContext.getString(R.string.version_check_download_fail));
            notificationBuilder.setProgress(100, 0, false);
            manager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }


    /**
     * show notification
     */
    public void showNotification() {
        downloadSuccess = false;
        downloadFailed = false;
        if (mDownloadHelper.isShowNotification()) {
            manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            notificationBuilder = createNotification();
            manager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
