package com.emi.emireading.core.log.imp;


import com.emi.emireading.core.log.encryption.IEncryption;

/**
 * @author :zhoujian
 * @description : 保存日志接口
 * @company :翼迈科技
 * @date: 2017年07月3日下午 03:30
 * @Email: 971613168@qq.com
 */
public interface ISave {
    /**
     * 普通日志写入
     *
     * @param tag
     * @param content
     */
    void writeLog(String tag, String content);

    /**
     * 崩溃日志写入
     *
     * @param thread
     * @param ex
     * @param tag
     * @param content
     */
    void writeCrash(Thread thread, Throwable ex, String tag, String content);

    /**
     * 设置加密类型
     *
     * @param encodeType
     */
    void setEncodeType(IEncryption encodeType);
}
