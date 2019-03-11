package com.emi.emireading.log.config;

/**
 * @author :zhoujian
 * @description : 日志配置接口
 * @company :翼迈科技
 * @date 2018年07月30日上午 10:10
 * @Email: 971613168@qq.com
 */

public interface ILogConfig {
    /**
     * 设置是否输出日志
     * @param allowLog
     * @return
     */
    ILogConfig configAllowLog(boolean allowLog);


    /**
     * 设置标签前缀
     * @param prefix
     * @return
     */
    ILogConfig configTagPrefix(String prefix);

    /**
     * 设置需要格式化的标签
     * @param formatTag
     * @return
     */
    ILogConfig configFormatTag(String formatTag);

    /**
     * 设置是否显示排版线条
     * @param showBorder
     * @return
     */
    ILogConfig configShowBorders(boolean showBorder);

    /**
     * 设置日志最小显示级别
     * @param logLevel
     * @return
     */
    ILogConfig configLevel(int logLevel);


}
