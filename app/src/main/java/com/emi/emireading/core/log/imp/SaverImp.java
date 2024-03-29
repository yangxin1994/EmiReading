package com.emi.emireading.core.log.imp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.log.LogTool;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.log.encryption.IEncryption;
import com.emi.emireading.core.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;


/**
 * @author :zhoujian
 * @description : 日志保存具体实现类
 * @company :翼迈科技
 * @date: 2017年07月3日下午 04:17
 * @Email: 971613168@qq.com
 */
public abstract class SaverImp implements ISave {
    private final static String TAG = "SaverImp";
    private IEncryption mEncryption;
    /**
     * 使用线程池对异步的日志写入做管理，提高性能
     */
    protected ExecutorService mThreadPool = ThreadPoolManager.EXECUTOR;
    /**
     * 根据日期创建文件夹,文件夹的名称以日期命名,下面是日期的格式
     */
    public final static SimpleDateFormat yyyy_mm_dd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public void setmEncryption(IEncryption mEncryption) {
        this.mEncryption = mEncryption;
    }

    /**
     * 在每一条log前面增加一个时间戳
     */
    public final static SimpleDateFormat yyyy_MM_dd_HH_mm_ss_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS", Locale.getDefault());

    /**
     * 日志的保存的类型
     */
    public static final String SAVE_FILE_TYPE = ".log";

    /**
     * 日志命名的其中一部分：时间戳
     */
    public final static String LOG_CREATE_TIME = yyyy_mm_dd.format(new Date(System.currentTimeMillis()));

    public static String TimeLogFolder;

    /**
     * 操作日志全名拼接
     */
    public final static String LOG_FILE_NAME_MONITOR = "MonitorLog" + LOG_CREATE_TIME + SAVE_FILE_TYPE;

    public Context mContext;

    public SaverImp(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 用于在每条log前面，增加更多的文本信息，包括时间，线程名字等等
     */
    public static String formatLogMsg(String tag, String tips) {
        String timeStr = yyyy_MM_dd_HH_mm_ss_SS.format(Calendar.getInstance().getTime());
        Thread currThread = Thread.currentThread();
        StringBuilder sb = new StringBuilder();
        sb.append("Thread ID: ")
                .append(currThread.getId())
                .append(" Thread Name:　")
                .append(currThread.getName())
                .append(" Time: ")
                .append(timeStr)
                .append(" FromClass: ")
                .append(tag)
                .append(" > ")
                .append(tips);
        LogUtil.d("添加的内容是:\n" + sb.toString());
        return sb.toString();
    }


    /**
     * 写入设备的各种参数信息之前，请确保File文件以及他的父路径是存在的
     *
     * @param file 需要创建的文件
     */
    public File createFile(File file, Context context) {
        StringBuilder sb = new StringBuilder();
        LogUtil.d("创建的设备信息（加密前） = \n" + sb.toString());
        //加密信息
        sb = new StringBuilder(encodeString(sb.toString()));
        LogUtil.d("创建的设备信息（加密后） = \n" + sb.toString());
        try {
            if (!file.exists()) {
                boolean successCreate = file.createNewFile();
                if (!successCreate) {
                    return null;
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    @Override
    public void setEncodeType(IEncryption encodeType) {

    }

    public String encodeString(String content) {
        if (mEncryption != null) {
            try {
                return mEncryption.encrypt(content);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                return content;
            }
        }

        return content;

    }

    public String decodeString(String content) {
        if (mEncryption != null) {
            try {
                return mEncryption.decrypt(content);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                return content;
            }
        }
        return content;
    }

    /**
     * 异步操作，务必加锁
     *
     * @param tag     Log的标签
     * @param content Log的内容
     */
    @Override
    public void writeLog(final String tag, final String content) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (SaverImp.class) {
                    TimeLogFolder = LogTool.getInstance().getROOT() + "/Log/" + yyyy_mm_dd.format(new Date(System.currentTimeMillis())) + "/";
                    final File logsDir = new File(TimeLogFolder);
                    final File logFile = new File(logsDir, LOG_FILE_NAME_MONITOR);
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        LogUtil.d("SDcard 不可用");
                        return;
                    }
                    if (!logsDir.exists()) {
                        Log.d(TAG, "logsDir.mkdirs() =  +　" + logsDir.mkdirs());
                    }
                    if (!logFile.exists()) {
                        createFile(logFile, mContext);
                    }
                    //long startTime = System.nanoTime();
                    //long endTime = System.nanoTime();
                    //Log.d("wenming", "解密耗时为 = ： " + String.valueOf((double) (endTime - startTime) / 1000000) + "ms");
                    //Log.d("wenming", "读取本地的Log文件，并且解密 = \n" + preContent.toString());
                    //Log.d("wenming", "即将保存的Log文件内容 = \n" + preContent.toString());
                    writeText(logFile, decodeString(FileUtil.getText(logFile)) + formatLogMsg(tag, content) + "\n");
                }

            }
        });
    }

    public void writeText(final File logFile, final String content) {
        FileOutputStream outputStream = null;
        try {
            //long startTime = System.nanoTime();
            String encoderesult = encodeString(content);
            //long endTime = System.nanoTime();
            //Log.d("wenming", "加密耗时为 = ： " + String.valueOf((double) (endTime - startTime) / 1000000) + "ms");
            LogUtil.d("最终写到文本的Log：\n" + content);
            outputStream = new FileOutputStream(logFile);
            outputStream.write(encoderesult.getBytes());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
