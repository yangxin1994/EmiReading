package com.emi.emireading.core.log;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.emi.emireading.core.log.encryption.IEncryption;
import com.emi.emireading.core.log.imp.ISave;

import java.io.File;
import java.util.ArrayList;

/**
 * @author :zhoujian
 * @description : 日志管理框架
 * @company :翼迈科技
 * @date: 2017年7月03日上午 10:06
 * @Email: 971613168@qq.com
 */
public class LogTool {
    private static final String TAG = "LogTool";
    private static LogTool mLogTool;
    public static String cacheDir = "";
    public static String LogPath;
    /**
     * 设置缓存文件夹的大小,默认是10MB
     */
    private long mCacheSize = 10 * 1024 * 1024;

    /**
     * 设置日志保存的路径
     */
    private String mROOT;

    /**
     * 设置加密方式
     */
    private IEncryption mEncryption;

    /**
     * 设置日志的保存方式
     */
    private ISave mLogSaver;


    private LogTool() {
    }


    public static LogTool getInstance() {
        if (mLogTool == null) {
            synchronized (LogTool.class) {
                if (mLogTool == null) {
                    mLogTool = new LogTool();
                }
            }
        }
        return mLogTool;
    }

    public LogTool setCacheSize(long cacheSize) {
        this.mCacheSize = cacheSize;
        return this;
    }

    public LogTool setEncryption(IEncryption encryption) {
        this.mEncryption = encryption;
        return this;
    }


    public LogTool setLogDir(Context context, String logDir) {
        if (TextUtils.isEmpty(logDir)) {
            //如果SD不可用，则存储在沙盒中
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mROOT = context.getExternalCacheDir().getAbsolutePath();
            } else {
                mROOT = context.getCacheDir().getAbsolutePath();
            }
        } else {
            mROOT = logDir;
        }
        return this;
    }

    public LogTool setLogSaver(ISave logSaver) {
        this.mLogSaver = logSaver;
        return this;
    }


    public String getROOT() {
        return mROOT;
    }

    public void init(Context context) {
        if (TextUtils.isEmpty(mROOT)) {
            //如果SD不可用，则存储在沙盒中
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mROOT = context.getExternalCacheDir().getAbsolutePath();
            } else {
                mROOT = context.getCacheDir().getAbsolutePath();
            }
        }
        if (mEncryption != null) {
            mLogSaver.setEncodeType(mEncryption);
        }
        LogWriter.getInstance().init(mLogSaver);
    }


    public long getCacheSize() {
        return mCacheSize;
    }


    public boolean checkCacheSize(File dir) {
        long dirSize = FileUtil.folderSize(dir);
        return dirSize >= LogTool.getInstance().getCacheSize() && FileUtil.deleteDir(dir);
    }

    public boolean clearLog() {
        final File logfolder = new File(LogPath);
        if (!logfolder.exists() || logfolder.listFiles().length == 0) {
            Log.w(TAG, "Log文件夹不存在，删除失败");
            return false;
        } else {
            if (logfolder.isFile()) {
                ArrayList<File> crashFileList = FileUtil.getCrashList(logfolder);
                if (crashFileList.size() == 0) {
                    LogUtil.w(TAG, "只存在log文件夹，但是不存在崩溃日志");
                    return false;
                } else {
                    LogUtil.w(TAG, "删除");
                    return FileUtil.deleteDir(logfolder);
                }
            } else {
                File[] files = logfolder.listFiles();
                if (files != null && files.length > 0) {
                    return FileUtil.deleteDir(logfolder);
                }
                return false;
            }
        }
    }
}
