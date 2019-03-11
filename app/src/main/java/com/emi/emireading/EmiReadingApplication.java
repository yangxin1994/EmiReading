package com.emi.emireading;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.log.LogTool;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.FileEntity;
import com.emi.emireading.log.EmiLog;
import com.emi.emireading.log.inner.LogcatLogPlugin;
import com.squareup.leakcanary.LeakCanary;

import org.litepal.LitePalApplication;

import java.io.File;
import java.util.ArrayList;

import static com.emi.emireading.core.config.EmiConfig.DOWN_LOAD_PATH;
import static com.emi.emireading.core.config.EmiConfig.EMI_MERGE_PATH;
import static com.emi.emireading.core.config.EmiConfig.GeneratePath;
import static com.emi.emireading.core.config.EmiConfig.MERGE_FILE_DIR_NAME;
import static com.emi.emireading.core.config.EmiConfig.NeedFilePath;
import static com.emi.emireading.core.config.EmiConfig.ROOT_PATH_NAME;
import static com.emi.emireading.core.config.EmiConfig.ReceievePath;
import static com.emi.emireading.core.config.EmiConfig.RootPath;
import static com.emi.emireading.core.config.EmiConfig.TempPath;
import static com.emi.emireading.core.config.EmiConstants.DOWN_LOAD;
import static com.emi.emireading.core.config.EmiConstants.EMI_RECEIVE_DIR;
import static com.emi.emireading.core.config.EmiConstants.GENERATE_DIR;
import static com.emi.emireading.core.config.EmiConstants.NEED_FILE_DIR;
import static com.emi.emireading.core.config.EmiConstants.PHOTO_DIR_NAME;
import static com.emi.emireading.core.config.EmiConstants.TEMP_DIR;
import static com.emi.emireading.core.crash.CrashManager.init;

/**
 * @author :zhoujian
 * @description : Application
 * @company :翼迈科技
 * @date: 2017年12月25日下午 02:00
 * @Email: 971613168@qq.com
 */

public class EmiReadingApplication extends LitePalApplication {
    private static EmiReadingApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ToastUtils.init(mInstance);
        EmiLog.getLogConfig().configAllowLog(true)
                .configShowBorders(false);
        EmiLog.install(new LogcatLogPlugin());
        try {
            if (getApplicationContext().getExternalCacheDir() != null && existSDCard()) {
                LogTool.cacheDir = getApplicationContext().getExternalCacheDir().toString();
            } else {
                LogTool.cacheDir = getApplicationContext().getCacheDir().toString();
            }
            initFileFolder();
        } catch (Exception e) {
            LogTool.cacheDir = getApplicationContext().getCacheDir().toString();
        }
        initCrashHandle();
        if (EmiConfig.isDebug && BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            }
            LeakCanary.install(this);
        }

    }

    public static EmiReadingApplication getAppContext() {
        return mInstance;
    }

    private void initCrashHandle() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //日志存放的根目录
            LogTool.LogPath = RootPath + "log/";
        }
        init(mInstance);
        if (!TextUtils.isEmpty(RootPath)) {
            LogTool.getInstance().setLogDir(mInstance, LogTool.LogPath).setCacheSize(EmiConfig.CacheSize).init(mInstance);
        } else {
            ToastUtil.showShortToast("日志初始化异常");
        }
    }

    /**
     * 初始化文件夹
     */
    private void initFileFolder() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //手机存储根目录
            RootPath = Environment.getExternalStorageDirectory().getPath() + "/" + ROOT_PATH_NAME + "/";
            EMI_MERGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + MERGE_FILE_DIR_NAME;
            NeedFilePath = RootPath + NEED_FILE_DIR;
            GeneratePath = RootPath + GENERATE_DIR;
            TempPath = RootPath + TEMP_DIR;
            DOWN_LOAD_PATH = RootPath + "/" + DOWN_LOAD;
            EmiConfig.EMI_PHOTO_PATH = RootPath + PHOTO_DIR_NAME;
            ReceievePath = Environment.getExternalStorageDirectory().getPath() + "/" + EMI_RECEIVE_DIR;
            File destDir = new File(NeedFilePath);
            File generateDir = new File(GeneratePath);
            File tempDir = new File(TempPath);
            File receiveDir = new File(ReceievePath);
            File downloadDir = new File(DOWN_LOAD_PATH);
            File mergeDir = new File(EMI_MERGE_PATH);
            File photoDir = new File(EmiConfig.EMI_PHOTO_PATH);
            if (!destDir.exists()) {
                destDir.mkdirs();
                notifyFileExplore(NeedFilePath);
            }
            if (!generateDir.exists()) {
                generateDir.mkdirs();
                notifyFileExplore(GeneratePath);
            }
            if (!tempDir.exists()) {
                tempDir.mkdirs();
                notifyFileExplore(TempPath);
            }
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
                notifyFileExplore(DOWN_LOAD_PATH);
            }
            if (!mergeDir.exists()) {
                mergeDir.mkdirs();
                notifyFileExplore(EMI_MERGE_PATH);
            }
            if (!photoDir.exists()) {
                photoDir.mkdirs();
                notifyFileExplore(EmiConfig.EMI_PHOTO_PATH);
            }
            if (!receiveDir.exists()) {
                receiveDir.mkdirs();
                notifyFileExplore(ReceievePath);
                String path = Environment.getExternalStorageDirectory().getPath() + "/" + getString(R.string.app_name);
                ArrayList<FileEntity> receiveList = FileUtil.getFilePathListFromDir(path);
                for (FileEntity oldFileEntity : receiveList) {
                    String copyFilePath = NeedFilePath + "/" + oldFileEntity.getFileName();
                    FileUtil.copyFile(oldFileEntity.getFilePath(), copyFilePath);
                    notifyFileExplore(copyFilePath);
                    FileUtil.deleteFile(oldFileEntity.getFilePath());
                }
            }
        }
    }

    private void notifyFileExplore(String filePath) {
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(intent);
    }

    private boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
