package com.emi.emireading.core.config;

import com.emi.emireading.entities.ExportStrategy;
import com.emi.emireading.entities.LoadStrategy;
import com.emi.emireading.entities.UserInfo;

import java.util.ArrayList;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date: 2017年12月25日下午 03:23
 * @Email: 971613168@qq.com
 */

public class EmiConfig {
    /**
     * 是否是debug模式
     */
    public static boolean isDebug = false;
    public static String RootPath = "";
    public static int MeterType;
    public static boolean isFilter;
    /**
     * 是否显示生成日期对话框
     */
    public static boolean isShowCreateDateDialog;
    public static ArrayList<UserInfo> userInfoArrayList;
    public static boolean bluetoothConnectStatus;

    /**
     * 导入策略
     */
    public static LoadStrategy loadStrategy;
    /**
     * 导出策略
     */
    public static ExportStrategy exportStrategy;

    /**
     * apk存储路径
     */
    public static final String APK_PATH_DOWNLOAD = "/storage/emulated/0/Android/data/com.emi.reading/files/Download/";

    /**
     * 翼迈抄表所需文件文件夹
     */
    public static String NeedFilePath;
    /**
     * 翼迈抄表所需文件文件夹
     */
    public static String DOWN_LOAD_PATH;
    /**
     * 翼迈抄表生成文件文件夹
     */
    public static String GeneratePath;
    /**
     * 翼迈抄表txt文件文件夹
     */
    public static String TempPath;
    /**
     * 翼迈抄表对接文件文件夹
     */
    public static String ReceievePath;
    /**
     * 拍摄图片文件夹
     */
    public static String EMI_PHOTO_PATH;

    /**
     * 日志路径
     */
    public static String EMI_PATH_LOG;
    /**
     * 555文件夹路径
     */
    public static String EMI_MERGE_PATH;


    /**
     * 日志缓存大小
     */
    public static int CacheSize = 3 * 1024 * 1024;
    public static final String MERGE_FILE_DIR_NAME = "555MergeFiles";
    public static final String ROOT_PATH_NAME = "EMICB";

    public static final String NULL = "null";
    /**
     * 当前城市名称
     */
    public static String CURRENT_CITY = "";
    /**
     * 文件类型
     */
    public static String FILE_TYPE = "";

    /**
     * 文件后缀名
     */
    public static String FILE_SUFFIX = "";
    /**
     * 字段数量
     */
    public static int FIELD_COUNT = -1;
    /**
     * 用户编号
     */
    public static int USER_ID_INDEX = -1;

    /**
     * 用水id（正常情况下和用户编号一致）
     */
    public static int WATER_ID_INDEX = -1;
    /**
     * 用户名
     */
    public static int USER_NAME_INDEX = -1;
    /**
     * 用户地址
     */
    public static int USER_ADDRESS_INDEX = -1;
    /**
     * 上次用量
     */
    public static int LAST_USAGE_INDEX = -1;
    /**
     * 上次读数
     */
    public static int LAST_READING_INDEX = -1;
    /**
     * 通道板
     */
    public static int CHANNEL_INDEX = -1;
    /**
     * 水表id（表地址）
     */
    public static int METER_ID_INDEX = -1;
    /**
     * 厂商代码
     */
    public static int FIRM_CODE_INDEX = -1;

    /**
     * 人工录入的读数
     */
    public static int PEOPLE_RECORDING_INDEX = -1;


    public static int EXPORT_TYPE;
    /**
     * 分割字符
     */
    public static String SPLIT_CHAR;

    public static boolean IS_MERGE = false;

    public static int MERGE_INFO_INDEX = -1;
    public static int CHANNEL_ADDRESS_INDEX = -1;
    /**
     * 支持数据上传
     */
    public static boolean IS_SUPPORT_UPLOAD;
    public static boolean IS_GBK;
    public static final String GBK = "gbk";
    public static final String UTF = "utf-8";
    /**
     * 是否只加载匹配到的,true表示只显示555和表册文件一致的数据
     */
    public static boolean IS_MATCH_SAME = false;
    /**
     * 专业模式
     */
    public static boolean PROFESSTIONAL_MODE;
    /**
     * 是否跳过文件选择
     */
    public static boolean IS_SKIP_SELECT_FILE;
    /**
     * 是否按源文件格式输出
     */
    public static boolean IS_KEEP_SAME = false;

    public static int READING_DELAY;
    /**
     * 自动补抄次数设置（默认为补抄1次,可以自己设置补抄次数）
     */
    public static int REPEAT_COUNT = 1;

    /**
     * 当前模式下文件后缀名
     */
    public static String CURRENT_SUFFIX = "";


    /**
     * 导出的用户编号索引
     */
    public static int EXPORT_USER_ID_INDEX = -1;

    /**
     * 导出的用水id索引（正常情况下和用户编号一致）
     */
    public static int EXPORT_WATER_ID_INDEX = -1;
    /**
     * 导出的用户名索引
     */
    public static int EXPORT_USER_NAME_INDEX = -1;
    /**
     * 导出的用户地址索引
     */
    public static int EXPORT_USER_ADDRESS_INDEX = -1;
    /**
     * 导出的上次用量索引
     */
    public static int EXPORT_LAST_USAGE_INDEX = -1;
    /**
     * 导出的上次读数索引
     */
    public static int EXPORT_LAST_READING_INDEX = -1;
    /**
     * 导出的通道板索引
     */
    public static int EXPORT_CHANNEL_INDEX = -1;
    /**
     * 导出的水表id（表地址）索引
     */
    public static int EXPORT_METER_ID_INDEX = -1;
    /**
     * 导出的厂商代码索引
     */
    public static int EXPORT_FIRM_CODE_INDEX = -1;
    /**
     * 售后联系方式
     */
    public static String EMI_PHONE;
    /**
     * 备注
     */
    public static String EMI_REMARK;

    /**
     * 自动抄表类型（抄全部、未抄、异常）
     */
    public static int AUTO_READ_TYPE;
}
