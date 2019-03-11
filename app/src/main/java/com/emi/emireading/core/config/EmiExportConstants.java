package com.emi.emireading.core.config;

/**
 * @author :zhoujian
 * @description : 导出文件配置常量
 * @company :翼迈科技
 * @date 2018年03月29日下午 02:09
 * @Email: 971613168@qq.com
 */
public class EmiExportConstants {
    public static final int INIT_CAPACITY = 32;

    public static final int DEFAULT_SIZE = 1000;
    /**
     * 文件导出分段间隔
     */
    public static int SPLIT_INTERVAL = -1;
    /**
     * 源格式导出
     */
    public static boolean IS_KEEP_ORIGINAL = true;


    /**
     * 是否生成日期
     */
    public static boolean IS_CREATE_DATE = false;

    /**
     * 是否生成数字类型
     */
    public static boolean IS_NUMBER = false;

    /**
     * 是否生成小数
     */
    public static boolean IS_DOUBLE = false;
}
