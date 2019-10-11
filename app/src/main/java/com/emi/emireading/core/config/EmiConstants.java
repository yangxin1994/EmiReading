package com.emi.emireading.core.config;

import android.bluetooth.BluetoothSocket;

import com.emi.emireading.entities.UserInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * @author :zhoujian
 * @description : 抄表常量类
 * @company :翼迈科技
 * @date: 2017年07月08日下午 03:41
 * @Email: 971613168@qq.com
 */

public class EmiConstants {
    public static BluetoothSocket bluetoothSocket;
    public static ArrayList<UserInfo> userInfoArrayList;
    public final static String EXCEPTION_NOT_INIT = "You've to call static method init() first in Application";
    public final static String EXCEPTION_NOT_INIT_FAST_MANAGER = "You've to call static method init(Application) first in Application";
    public final static String EXCEPTION_EMPTY_URL = "You've configured an invalid url";
    public static final String FIRM_CODE_1001 = "1001";
    public static final String FIRM_CODE_7833 = "7833";
    public static final String FIRM_CODE_0110 = "0110";
    public static final String FIRM_CODE_3378 = "3378";
    /**
     * 开启蓝牙请求码
     */
    public static final int REQUEST_CODE_OPEN_BLUETOOTH = 1002;
    /**
     * 蓝牙已连接
     */
    public static final int MSG_BLUETOOTH_CONNECT = 100;
    public static final int MSG_ERROR = -1;
    public static final String IO_EXCEPTION = "IOException";
    public static final String EXCEPTION_NET_TIME_OUT = "SocketTimeoutException";
    public static final char ZERO = '0';
    public static final String VALUE_ZERO = "0";
    public static final char ONE = '1';
    public static final int TEN = 10;
    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String IMG_JPG = ".jpg";
    public static final String EMPTY_METER_DATA = "/";
    public static final String ERROR_METER_DATA = "FFFF";
    public static final String EXTRA_POSITION = "EXTRA_POSITION";
    public static final String EXTRA_CURRENT_TAG = "EXTRA_CURRENT_TAG";
    public static final String EXTRA_CHANNEL_NUMBER = "EXTRA_CHANNEL_NUMBER";
    public static final String EXTRA_FILE_NAME = "EXTRA_FILE_NAME";
    public static final String EXTRA_SKIP_TAG = "EXTRA_SKIP_TAG";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    /**
     * 表地址
     */
    public static final String EXTRA_METER_ID = "EXTRA_METER_ID";
    /**
     * 水表厂商代码
     */
    public static final String EXTRA_METER_FIRM_CODE = "EXTRA_METER_FIRM_CODE";
    public static final String EXTRA_USER_LIST = "EXTRA_USER_LIST";
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final int ERROR_CODE = -1;
    public static final int SUCCESS_CODE = 0;
    public static final String EMI_MERGE_FILE = "555.txt";
    public static final String FilePathKey = "filePath_key";
    public static final String EMI_RECEIVE_DIR = "EMIDJ";
    public static final String NEED_FILE_DIR = "翼迈抄表所需文件";
    public static final String GENERATE_DIR = "翼迈抄表生成文件";
    public static final String EMI_DIR_LOG = "log";
    public static final String DOWN_LOAD = "download";
    public static final String PHOTO_DIR_NAME = "翼迈图片";
    public static final String TEMP_DIR = "temp";
    public static final String PREF_CURRENT_CITY = "PREF_CURRENT_CITY";
    public static final String FILE_TYPE_EXCEL = "icon_file_type_excel";
    /**
     * 计时时间间隔
     */
    public static final int ONE_SECOND = 1000;
    /**
     * 已经导出文件
     */
    public static final int HAS_EXPORT = 1;

    /**
     * 未导出文件
     */
    public static final int NO_EXPORT = 0;
    /**
     * 文件夹
     */
    public static final String SUFFIX_FOLDER = "FOLDER";
    public static final String SUFFIX_XLS = ".xls";
    public static final String SUFFIX_TXT = ".txt";
    public static final String SUFFIX_APK = ".apk";
    public static final String SUFFIX_DBF = ".dbf";
    public static final String SUFFIX_EXCEL = ".xls";
    public static final String SUFFIX_EXCEL_2007 = ".xlsx";
    public static final String JSON_LOAD_FILE_NAME = "load_strategy.json";
    public static final String JSON_EXPORT_FILE_NAME = "export_strategy.json";
    public static final String JSON_EXPORT_ALIAS = "alias.json";
    public static final String JSON_EXPORT_MOULD_NAME = "export_mould.json";
    public static final String TXT_LOAD_FILE_NAME = "load_config.txt";
    public static final String TXT_EXPORT_FILE_NAME = "export_config.txt";
    public static final int READ_DELAY_DEFAULT = 1000;
    public static final String JSON_EXPORT_CITY_NAME = "cityName";
    public static final String JSON_TABLE_NAME_FILE_DEFAULT_NAME = "table_name_default.json";
    public static final String PREF_WATER_WARNING_LINE = "PREF_WATER_WARNING_LINE";
    /**
     * 补抄次数
     */
    public static final String PREF_AUTO_REPEAT_COUNT = "PREF_AUTO_REPEAT_COUNT";
    /**
     * 自动抄表类型（全部、未抄、异常）
     */
    public static final String PREF_AUTO_READ_TYPE = "PREF_AUTO_READ_TYPE";
    public static final String DEFAULT_CITY = "合肥";
    public static final String EMI_DEVICE_NAME = "EMI0001";
    public static final int METER_TYPE_DUI = 0;
    public static final int METER_TYPE_FAN = 1;
    /**
     * 水表数据标准长度
     */
    public static final int METER_INFO_LENGTH = 14;
    /**
     * 厂商代码标准长度
     */
    public static final int METER_FIRM_CODE_LENGTH = 4;
    public static final int METER_ID_LENGTH = 10;
    /**
     * 水表最大刻度
     */
    public static final int METER_MAX_METER_DATA = 9999;
    public static final int STANDARD_TYPE_SPECIAL = 1;
    public static final int STANDARD_TYPE_NORMAL = 2;
    public static final int CHANNEL_TYPE_SPECIAL = 1;
    public static final int CHANNEL_TYPE_NORMAL = 2;
    public static final String SPLIT_CHAR_DEFAULT = "\\$";
    public static final String NEW_LINE = "\r\n";

    /**
     * 未上传
     */
    public static final int STATE_NOT_UPLOAD = 9;
    /**
     * 上传状态
     */
    public static final String UPLOAD_STATE_KEY = "uploadState";
    public static final String DOT = ".";
    /**
     * 协议起始码
     */
    public static final String EMI_CALLBACK_CODE_BEGIN = "FE68";
    /**
     * 协议终止码
     */
    public static final String EMI_CALLBACK_CODE_END = "16";
    /**
     * 协议有效数据识别码
     */
    public static final String EMI_CALLBACK_CODE_VALID = "FE6810";

    /**
     * 协议有效数据识别码
     */
    public static final String EMI_SYSTEM_SETTING_CODE = "8403AAA0";

    /**
     * 正常、异常、补录
     */
    public static final int STATE_NORMAL = 10;
    public static final int STATE_ALL = 0;
    /**
     * 标记状态
     */
    public static final int STATE_TAG = 13;
    /**
     * 未抄
     */
    public static final int STATE_NO_READ = 5;
    /**
     * 抄表失败
     */
    public static final int STATE_FAILED = 3;
    /**
     * 抄表正常
     */
    public static final int STATE_SUCCESS = 1;
    /**
     * 读数异常
     */
    public static final int STATE_WARNING = 2;
    /**
     * 人工录入
     */
    public static final int STATE_PEOPLE_RECORDING = 4;
    /**
     * 单抄
     */
    public static final int STATE_SINGLE = 6;
    /**
     * 上传成功
     */
    public static final int STATE_UPLOAD_SUCCESS = 7;
    /**
     * 上传失败
     */
    public static final int STATE_UPLOAD_FAILED = 8;


    /**
     * 已抄（包括正常，失败，异常，补录）
     */
    public static final int STATE_HAS_READ = 12;
    /**
     * 跳转类型
     */
    public static final int SKIP_TYPE_1 = 1;
    public static final int SKIP_TYPE_2 = 2;
    /**
     * key
     */
    public final String PROGRESS_KEY = "progress_key";
    public static final String SHEET_NAME = "Sheet1";
    public static final String DBF_FILE_NAME = "dbase_a.dbf";
    public static final String QUERY_FLAG_KEY = "queryFlag";
    public static final String CURRENT_POSITION_KEY = "index";
    public static final String CURRENT_ITEM_TAG_KEY = "currentClickTag";
    public static final String CHANNEL_TYPE_KEY = "channel_type_key";
    public static final String DEBUG_MODE_KEY = "DEBUG_MODE_KEY";
    public static final String IS_NEED_CHANNEL_KEY = "IS_NEED_CHANNEL_KEY";
    public static final String EDIT_DATA_KEY = "edit_data_key";
    public static final String EDIT_LIST_KEY = "edit_list_key";
    public static final String METER_TYPE_KEY = "meter_type_key";
    public static final String STANDARD_TYPE_KEY = "STANDARD_TYPE_KEY";
    public static final String SKIP_TYPE_KEY = "SKIP_TYPE_KEY";


    public static final String USER_WATER_ID_KEY = "用水ID";
    public static final String KEY_USER_ID = "KEY_USER_ID";
    public static final String KEY_WATER_ID = "KEY_WATER_ID";
    public static final String KEY_USER_ADDRESS = "KEY_USER_ADDRESS";
    public static final String KEY_USER_NAME = "KEY_USER_NAME";
    public static final String KEY_LAST_DATA = "KEY_LAST_DATA";
    public static final String KEY_LAST_USAGE = "KEY_LAST_USAGE";
    public static final String KEY_CURRENT_DATA = "KEY_CURRENT_DATA";
    public static final String KEY_CURRENT_USAGE = "KEY_CURRENT_USAGE";
    public static final String KEY_READ_DATE = "KEY_READ_DATE";
    public static final String KEY_CHANNEL_NUMBER = "KEY_CHANNEL_NUMBER";
    public static final String KEY_METER_ADDRESS = "KEY_METER_ADDRESS";
    public static final String KEY_FIRM_CODE = "KEY_FIRM_CODE";
    public static final String KEY_CHANNEL_ADDRESS = "KEY_CHANNEL_ADDRESS";
    public static final String KEY_READ_STATE = "KEY_READ_STATE";
    public static final String PREF_PROFESSIONAL_MODE = "PREF_PROFESSIONAL_MODE";
    public static final String PREF_SHOW_PEOPLE_RECORD = "PREF_SHOW_PEOPLE_RECORD";
    public static final String PREF_ENABLE_EDIT_FILE = "PREF_ENABLE_EDIT_FILE";
    public static final String PREF_IS_SHOW_CREATE_DIALOG = "PREF_IS_SHOW_CREATE_DIALOG";
    public static final String PREF_CODED_FORMAT = "PREF_CODED_FORMAT";
    public static final String PREF_SKIP_TAG = "PREF_SKIP_TAG";
    public static final String PREF_READING_DELAY = "PREF_READING_DELAY";
    public static final String PREF_PHONE_NUMBER = "PREF_PHONE_NUMBER";
    public static final String PREF_REMARK = "PREF_REMARK";
    public static final String NULL = "null";

    public static final String EXTRA_PATH = "EXTRA_PATH";
    public static final String TENCENT = "tencent";
    public static final String QQ_PATH = "QQfile_recv";
    public static final String DOWNLOAD = "Download";
    public static final String EMI_ROOT_PATH = "EMICB";
    public static final String MICRO_MSG = "MicroMsg" + File.separator + DOWNLOAD;

    public static final String EXTRA_READING_PATH_FLAG = "EXTRA_READING_PATH_FLAG";

    /**
     * 后台输入的app包名
     */
    public static final String APP_NAME = "emireading";

}
