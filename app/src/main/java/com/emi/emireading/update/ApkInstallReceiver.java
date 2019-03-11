package com.emi.emireading.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.ToastUtil;

import java.io.File;

import static com.emi.emireading.core.config.EmiConstants.SUFFIX_APK;


/**
 * @author :zhoujian
 * @description : APK安装广播
 * @company :翼迈科技
 * @date: 2017年10月30日上午 10:03
 * @Email: 971613168@qq.com
 */

public class ApkInstallReceiver extends BroadcastReceiver {
    private static final String TAG = "ApkInstallReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())){
            long downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadManager downloadManager = DownLoadUtils.getInstance(context).getDownloadManager();
            Uri downloadUri = downloadManager.getUriForDownloadedFile(downloadApkId);
            String apkPath;
            if (downloadUri != null){
                apkPath = downloadUri.getPath();
                LogUtil.d(TAG,"apkPath="+apkPath);
                String apkName = getApkName(apkPath);
                String newApkPath = EmiConfig.DOWN_LOAD_PATH+"/"+apkName+SUFFIX_APK;
                boolean finish = FileUtil.copyFile(apkPath, newApkPath);
                if (finish){
                    notifyFileExplore(context,newApkPath);
                    notifyFileExplore(context,apkPath);
                }
            }
            ToastUtil.showShortToast("下载完成，请点击通知栏安装");
        }
    }
    protected void notifyFileExplore(Context context, String filePath){
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(intent);
    }

    /**
     * 安装apk
     */
    private void installApk(Context context, long downloadId) {
        long downId = PreferenceUtils.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
        if(downloadId == downId) {
            DownloadManager downManager= (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = downManager.getUriForDownloadedFile(downloadId);
          PreferenceUtils.putString(DownLoadConstance.DOWN_LOAD_APK_KEY,downloadUri.getPath());
            if (downloadUri != null) {
                Intent install= new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(downloadUri, DownLoadConstance.DOWN_LOAD_TYPE);
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(install);
            } else {
                ToastUtil.showShortToast(DownLoadConstance.DOWN_LOAD_MSG_FAILED);
            }
        }
    }


    private String getApkName(String filePath){
        if (filePath.contains(SUFFIX_APK)){
            int last = filePath.lastIndexOf("/");
            int lastPoint = filePath.lastIndexOf(".");
            LogUtil.i(TAG,"最后一个/索引："+last);
            String apkName = filePath.substring(last+1,lastPoint);
            LogUtil.i(TAG,"apkName："+apkName);
            return apkName;
        }
        return "";
    }



    private void installFile(Context context, String apkPath){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        //  String type = getMIMEType(f);
        //  intent.setDataAndType(Uri.fromFile(f), type);
        intent.setDataAndType(Uri.parse(apkPath),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
