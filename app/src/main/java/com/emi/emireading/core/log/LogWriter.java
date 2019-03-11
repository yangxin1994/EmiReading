package com.emi.emireading.core.log;


import com.emi.emireading.core.log.imp.ISave;

/**
 * @author :zhoujian
 * @description : 日志写入类
 * @company :翼迈科技
 * @date: 2017年7月06日下午 03:05
 * @Email: 971613168@qq.com
 */
public class LogWriter {
    private static LogWriter mLogWriter;
    private static ISave mSave;

    private LogWriter() {
    }


    public static LogWriter getInstance() {
        if (mLogWriter == null) {
            synchronized (LogTool.class) {
                if (mLogWriter == null) {
                    mLogWriter = new LogWriter();
                }
            }
        }
        return mLogWriter;
    }


    public LogWriter init(ISave save) {
        mSave = save;
        return this;
    }

    public void writeLog(String tag, String content) {
        LogUtil.d(tag, content);
        mSave.writeLog(tag, content);
    }
}