package com.emi.emireading.core.log.imp;

import android.content.Context;
import android.os.Environment;

import com.emi.emireading.core.log.LogTool;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.FileUtil;

import java.io.File;
import java.util.Date;


/**
 * @author :zhoujian
 * @description : 在崩溃之后，马上异步保存崩溃信息，完成后退出线程，并且将崩溃信息都写在一个文件中
 * @company :翼迈科技
 * @date: 2017年07月3日下午 05:19
 * @Email: 971613168@qq.com
 */
public class CrashWriter extends SaverImp {
    private final static String TAG = "CrashWriter";
    private String divideLine = "------------------------------------------------------------------------------------";
    /**
     * 崩溃日志文件的文件名：eg： CrashLog2017-07-3.log
     */
    public final static String LOG_FILE_NAME_EXCEPTION = "错误日志" + LOG_CREATE_TIME + SAVE_FILE_TYPE;

    /**
     * 初始化，继承父类
     *
     * @param context 上下文
     */
    public CrashWriter(Context context) {
        super(context);
    }

    /**
     * 异步的写操作，使用线程池对异步操作做统一的管理
     *
     * @param thread  发生崩溃的线程
     * @param ex      崩溃的错误信息
     * @param tag     标签，用于区别Log和Crash，一并写入文件中
     * @param content 写入的Crash内容
     */
    @Override
    public synchronized void writeCrash(final Thread thread, final Throwable ex, final String tag, final String content) {

    }

    public void saveCrashLog(final String crashLog) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (SaverImp.class) {
                    TimeLogFolder = LogTool.getInstance().getROOT() + yyyy_mm_dd.format(new Date(System.currentTimeMillis())) + "/";
                    File logsDir = new File(TimeLogFolder);
                    LogUtil.e(TAG, TimeLogFolder);
                    File crashFile = new File(logsDir, LOG_FILE_NAME_EXCEPTION);
                    LogUtil.e(TAG, LOG_FILE_NAME_EXCEPTION);
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        return;
                    }
                    if (!logsDir.exists()) {
                        logsDir.mkdirs();
                    }
                    if (!crashFile.exists()) {
                        createFile(crashFile, mContext);
                    }
                    String crashTemp = decodeString(FileUtil.getText(crashFile));
                    if (null != crashTemp) {
                        StringBuilder preContent = new StringBuilder();
                        preContent.append(formatLogMsg(TAG, crashLog)).append("\n");
                        preContent.append(divideLine);
                        writeText(crashFile, preContent.toString());
                    }
                }
            }
        });


    }


}
