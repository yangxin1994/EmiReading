package com.emi.emireading.upgrade.helper;

import android.os.Environment;

import java.io.File;

import static com.emi.emireading.core.config.EmiConfig.ROOT_PATH_NAME;
import static com.emi.emireading.core.config.EmiConstants.DOWN_LOAD;

/**
 * @author :zhoujian
 * @description : 文件帮助类
 * @company :翼迈科技
 * @date 2018年08月17日下午 04:16
 * @Email: 971613168@qq.com
 */

public class FileHelper {
    private static final String DIRECTORY = ROOT_PATH_NAME + File.separator + DOWN_LOAD+File.separator;

    public static String getDownloadApkCachePath() {
        String appCachePath;
        if (checkSDCardEnable()) {
            appCachePath = Environment.getExternalStorageDirectory() + File.separator + DIRECTORY;
        } else {
            appCachePath = Environment.getDataDirectory().getPath() + File.separator + DIRECTORY;
        }
        File file = new File(appCachePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return appCachePath;
    }


    /**
     * 检测SD卡是否可用
     */
    public static boolean checkSDCardEnable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


}
