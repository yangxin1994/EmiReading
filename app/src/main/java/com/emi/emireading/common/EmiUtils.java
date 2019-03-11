package com.emi.emireading.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.WindowManager;

import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.entities.ExportStrategy;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.log.EmiLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.emi.emireading.core.config.EmiConstants.CHANNEL_TYPE_KEY;
import static com.emi.emireading.core.config.EmiConstants.CHANNEL_TYPE_NORMAL;
import static com.emi.emireading.core.config.EmiConstants.CHANNEL_TYPE_SPECIAL;
import static com.emi.emireading.core.config.EmiConstants.DEBUG_MODE_KEY;
import static com.emi.emireading.core.config.EmiConstants.DOT;
import static com.emi.emireading.core.config.EmiConstants.ERROR_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.FIRM_CODE_7833;
import static com.emi.emireading.core.config.EmiConstants.IS_NEED_CHANNEL_KEY;
import static com.emi.emireading.core.config.EmiConstants.METER_ID_LENGTH;
import static com.emi.emireading.core.config.EmiConstants.METER_MAX_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.METER_TYPE_DUI;
import static com.emi.emireading.core.config.EmiConstants.METER_TYPE_FAN;
import static com.emi.emireading.core.config.EmiConstants.METER_TYPE_KEY;
import static com.emi.emireading.core.config.EmiConstants.NULL;
import static com.emi.emireading.core.config.EmiConstants.PREF_AUTO_READ_TYPE;
import static com.emi.emireading.core.config.EmiConstants.PREF_AUTO_REPEAT_COUNT;
import static com.emi.emireading.core.config.EmiConstants.PREF_CODED_FORMAT;
import static com.emi.emireading.core.config.EmiConstants.PREF_ENABLE_EDIT_FILE;
import static com.emi.emireading.core.config.EmiConstants.PREF_IS_SHOW_CREATE_DIALOG;
import static com.emi.emireading.core.config.EmiConstants.PREF_PROFESSIONAL_MODE;
import static com.emi.emireading.core.config.EmiConstants.PREF_SHOW_PEOPLE_RECORD;
import static com.emi.emireading.core.config.EmiConstants.STANDARD_TYPE_KEY;
import static com.emi.emireading.core.config.EmiConstants.STANDARD_TYPE_NORMAL;
import static com.emi.emireading.core.config.EmiConstants.STANDARD_TYPE_SPECIAL;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NORMAL;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.utils.TimeUtil.getCurrentTime;


/**
 * @author :zhoujian
 * @description : EMI相关工具类
 * @company :翼迈科技
 * @date: 2017年08月08日下午 02:44
 * @Email: 971613168@qq.com
 */

public class EmiUtils {
    private final static String ZERO = "0";
    private static final String TAG = "EmiUtils";
    private static final String DAY = "日";
    private final static int CHECK_INDEX_START = 4;
    private final static int CHECK_INDEX_END = 17;
    private static final String PREF_FILTER_WATER = "PREF_FILTER_WATER";
    private static final String PREF_EXPORT_TYPE = "PREF_EXPORT_TYPE";
    private static final String PREF_EXPORT_CITY_NAME = "PREF_EXPORT_CITY_NAME";
    private static final String PREF_EXPORT_FILE_TYPE = "PREF_EXPORT_FILE_TYPE";
    private static final String PREF_EXPORT_FORMAT_JSON = "PREF_EXPORT_FORMAT_JSON";
    private static final String PREF_EXPORT_TABLE_ALIAS_JSON = "PREF_EXPORT_TABLE_ALIAS_JSON";

    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    /**
     * 检查某个应用是否前台运行
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isRunningForeground(Context context, String packageName) {
        if (context == null) {
            return false;
        }
        if (TextUtils.isEmpty(packageName)) {
            packageName = context.getPackageName();
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRunningForeground(Context context) {
        return isRunningForeground(context, null);
    }

    /**
     * 校验位
     */
    private final static int CHECK_INDEX = 18;

    public static void saveDebugMode(boolean isDebug) {
        PreferenceUtils.putBoolean(DEBUG_MODE_KEY, isDebug);
    }

    public static boolean isDebugMode() {
        return PreferenceUtils.getBoolean(DEBUG_MODE_KEY, false);
    }

    public static boolean isNeedChannel() {
        return PreferenceUtils.getBoolean(IS_NEED_CHANNEL_KEY, true);
    }

    public static void saveChannelSetting(boolean isNeedChannel) {
        PreferenceUtils.putBoolean(IS_NEED_CHANNEL_KEY, isNeedChannel);
    }


    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String dateNow = sdf.format(date);
        return dateNow;
    }

    /**
     * short整数转换为2字节的byte数组
     *
     * @param s short整数
     * @return byte数组
     */
    public static byte[] unsignedShortToByte2(int s) {
        byte[] targets = new byte[2];
        targets[0] = (byte) (s >> 8 & 0xFF);
        targets[1] = (byte) (s & 0xFF);
        return targets;
    }


    public static boolean isEmpty(@Nullable CharSequence str) {
        if (str == null || str.length() == 0 || NULL.equalsIgnoreCase(str.toString())) {
            return true;
        } else {
            return false;
        }
    }


    public static String changeDateFormat(String date) {
        int dayIndex;
        String result = "";
        if (date != null && date.contains(DAY)) {
            dayIndex = date.indexOf(DAY);
            result = date.substring(0, dayIndex + 1);
            result = result.replace("年", "");
            result = result.replace("月", "");
            result = result.replace("日", "");
        }
        return result;
    }


    public static byte[] getReadingCmdSpecial(String meterId, String firmCode) {
        int length = meterId.length();
        LogUtil.w(TAG, "length长度：" + length);
        StringBuilder sb = new StringBuilder(meterId);
        for (; length < METER_ID_LENGTH; length++) {
            sb.append(ZERO);
            sb.append(meterId);
            LogUtil.w(TAG, "meterId--->" + meterId);
        }
        meterId = sb.toString();
        LogUtil.i(TAG, "meterId = " + meterId);
        //表地址字符串转化为字节数组
        byte[] hexMeterId = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(meterId));
        byte[] hexFirmCode = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(firmCode));
        byte hexCheck = 0x00;
        byte[] cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFE, 0x68, 0x10, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x01, 0x10, 0x01, 0x03, (byte) 0x90, 0x1F,
                0x00, 0x00, 0x16};
        cmd[6] = hexMeterId[4];
        cmd[7] = hexMeterId[3];
        cmd[8] = hexMeterId[2];
        cmd[9] = hexMeterId[1];
        cmd[10] = hexMeterId[0];
        cmd[11] = hexFirmCode[1];
        cmd[12] = hexFirmCode[0];
        for (int k = CHECK_INDEX_START; k <= CHECK_INDEX_END; k++) {
            hexCheck += cmd[k];
        }
        cmd[CHECK_INDEX] = hexCheck;
        return cmd;
    }


    public static byte[] getReadingCmdNormal(String meterId, String firmCode) {
        byte[] empty = {0x00};
        if (TextUtils.isEmpty(meterId) || TextUtils.isEmpty(firmCode)) {
            return empty;
        }
        if (!TextUtils.isDigitsOnly(meterId)) {
            return empty;
        }
        if (TextUtils.isDigitsOnly(firmCode) || EmiStringUtil.isEnglish(firmCode)) {
            byte[] hexAddress = DigitalTrans.hex2byte(meterId);
            byte[] hexCode = DigitalTrans.hexStringToByte(firmCode);
            byte hexCheck = 0x00;
            byte[] cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                    (byte) 0xFE, 0x68, 0x10, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x01, 0x03, 0x1F, (byte) 0x90,
                    0x00, 0x00, 0x16};
            for (int i = 0; i < hexAddress.length; i++) {
                cmd[6 + i] = hexAddress[hexAddress.length - 1 - i];
            }
            for (int j = 0; j < hexCode.length; j++) {
                cmd[11 + j] = hexCode[hexCode.length - j - 1];
            }
            for (int k = CHECK_INDEX_START; k <= CHECK_INDEX_END; k++) {
                hexCheck += cmd[k];
            }
            cmd[CHECK_INDEX] = hexCheck;
            return cmd;
        }
        return empty;
    }

    /**
     * 表地址和厂商代码混合在一起(普通市场协议(前提是长度必须是14位且厂商代码在前)
     *
     * @param meterInfo
     * @return
     */
    public static byte[] getReadingCmdWholeNormal(String meterInfo) {
        int wholeLength = 14;
        byte[] cmd = {};
        if (!TextUtils.isEmpty(meterInfo) && meterInfo.length() == wholeLength) {
            String meterId = meterInfo.substring(4, meterInfo.length());
            String firmCode = meterInfo.substring(0, 4);
            return getReadingCmdNormal(meterId, firmCode);
        }
        return cmd;
    }


    /**
     * 表地址和厂商代码混合在一起(特殊市场协议）(前提是长度必须是14位且厂商代码在前)
     *
     * @param meterInfo
     * @return
     */
    public static byte[] getReadingCmdWholeSpecial(String meterInfo) {
        int wholeLength = 14;
        byte[] cmd = {};
        if (!TextUtils.isEmpty(meterInfo) && meterInfo.length() == wholeLength) {
            String meterId = meterInfo.substring(4, meterInfo.length());
            String firmCode = meterInfo.substring(0, 4);
            LogUtil.w(TAG, "测试结果：" + meterId);
            LogUtil.w(TAG, "测试结果：" + firmCode);
            return getReadingCmdSpecial(meterId, firmCode);
        }
        return cmd;
    }

    public static int getMeterType() {
        return PreferenceUtils.getInt(METER_TYPE_KEY, METER_TYPE_DUI);
    }


    public static void saveMeterType(int meterType) {
        PreferenceUtils.putInt(METER_TYPE_KEY, meterType);
    }

    /**
     * 获取水表的市场类型（特殊市场：老表使用的协议（极少量），普通市场：新表采用的协议（大量））
     *
     * @return
     */
    public static int getStandardType() {
        return PreferenceUtils.getInt(STANDARD_TYPE_KEY, STANDARD_TYPE_NORMAL);
    }

    public static void saveStandardType(int meterType) {
        PreferenceUtils.putInt(STANDARD_TYPE_KEY, meterType);
    }

    /**
     * 判断是否是普通市场
     *
     * @return
     */
    public static boolean isNormalStandardType() {
        switch (getStandardType()) {
            case STANDARD_TYPE_SPECIAL:
                return false;
            case STANDARD_TYPE_NORMAL:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否是普通通道板
     *
     * @return
     */
    public static boolean isNormalChannel() {
        int type = PreferenceUtils.getInt(CHANNEL_TYPE_KEY, CHANNEL_TYPE_SPECIAL);
        switch (type) {
            case CHANNEL_TYPE_SPECIAL:
                return false;
            case CHANNEL_TYPE_NORMAL:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否是对射是水表
     *
     * @return
     */
    public static boolean isMeterTypeDui() {
        switch (PreferenceUtils.getInt(METER_TYPE_KEY, METER_TYPE_DUI)) {
            case METER_TYPE_DUI:
                return true;
            case METER_TYPE_FAN:
                return false;
            default:
                return false;
        }

    }


    public static List<String> removeSameData(List<String> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = i + 1; j < dataList.size(); j++) {
                if (dataList.get(i).equals(dataList.get(j))) {
                    dataList.remove(j);
                    j--;
                }
            }
        }
        return dataList;
    }


    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static String formatFirmCode(String firmCode) {
        if (EmiStringUtil.isNotEmpty(firmCode)) {
            return firmCode;
        } else {
            return FIRM_CODE_7833;
        }
    }

    /**
     * 设置是否过滤用水量
     *
     * @param isFilter
     */
    public static void setIsFilterWaterUsage(boolean isFilter) {
        PreferenceUtils.putBoolean(PREF_FILTER_WATER, isFilter);
    }


    public static boolean getIsFilter() {
        return PreferenceUtils.getBoolean(PREF_FILTER_WATER, true);
    }

    public static void saveExportType(int state) {
        PreferenceUtils.putInt(PREF_EXPORT_TYPE, state);
    }

    /**
     * 获取导出文件类型（全部、异常、失败 、等...）
     *
     * @return
     */
    public static int getExportType() {
        return PreferenceUtils.getInt(PREF_EXPORT_TYPE, STATE_NORMAL);
    }


    /**
     * 使用正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s) {
        //去掉多余的0
        if (s.indexOf(DOT) > 0) {
            s = s.replaceAll("0+?$", "");
            //如最后一位是.则去掉
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }

    /**
     * 是否是专业模式
     *
     * @return
     */
    public static boolean isProfessionalMode() {
        return PreferenceUtils.getBoolean(PREF_PROFESSIONAL_MODE, false);
    }

    public static void saveProfessionalMode(boolean isProfessional) {
        PreferenceUtils.putBoolean(PREF_PROFESSIONAL_MODE, isProfessional);
    }

    /**
     * 是否显示人工补录
     */
    public static boolean isShowPeopleRecord() {
        return PreferenceUtils.getBoolean(PREF_SHOW_PEOPLE_RECORD, false);
    }

    /**
     * @param isShowPeopleSetting
     */
    public static void savePeopleRecordSetting(boolean isShowPeopleSetting) {
        PreferenceUtils.putBoolean(PREF_SHOW_PEOPLE_RECORD, isShowPeopleSetting);
    }

    public static boolean isGBK() {
        return PreferenceUtils.getBoolean(PREF_CODED_FORMAT, true);
    }

    /**
     * 保存编码格式
     *
     * @param isGBK
     */
    public static void saveCodeFormat(boolean isGBK) {
        PreferenceUtils.putBoolean(PREF_CODED_FORMAT, isGBK);
    }


    /**
     * 是否可以修改数据文件
     *
     * @param isEnable
     */
    public static void saveIsEnableModifyFile(boolean isEnable) {
        PreferenceUtils.putBoolean(PREF_ENABLE_EDIT_FILE, isEnable);
    }

    public static boolean isEnableModifyFile() {
        return PreferenceUtils.getBoolean(PREF_ENABLE_EDIT_FILE, false);
    }


    public static boolean isShowCreateDate() {
        return PreferenceUtils.getBoolean(PREF_IS_SHOW_CREATE_DIALOG, true);
    }


    public static void setIsShowCreateDate(boolean isShow) {
        PreferenceUtils.putBoolean(PREF_IS_SHOW_CREATE_DIALOG, isShow);
    }


    public static boolean isPhoneNumber(String phone) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }


    public static byte[] getCmdReadChannel(boolean isNormal) {
        if (isNormal) {
            return new byte[]{(byte) 0xFE, (byte) 0xFE, 0x6A, 0x10, 0x02, (byte) 0xAA, 0x01, 0x27, 0x16};
        } else {
            return new byte[]{(byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x42, 0x00, 0x31, 0x03, 0x01, (byte) 0x89, 0x01, 0x69, 0x16};
        }
    }


    public static void setAutoRepeatCount(int count) {
        PreferenceUtils.putInt(PREF_AUTO_REPEAT_COUNT, count);
    }


    public static int getAutoRepeatCount() {
        return PreferenceUtils.getInt(PREF_AUTO_REPEAT_COUNT, 0);
    }

    /**
     * 设置自动抄表类型（全部、未抄、异常等）
     *
     * @param state
     */
    public static void setAutoReadType(int state) {
        PreferenceUtils.putInt(PREF_AUTO_READ_TYPE, state);
    }


    /**
     * 获取自动抄表类型（全部、未抄、异常等）
     */
    public static int getAutoReadType() {
        return PreferenceUtils.getInt(PREF_AUTO_READ_TYPE, STATE_ALL);
    }

    /**
     * 是否使屏幕常亮
     *
     * @param activity
     */
    public static void keepScreenLongLight(Activity activity, boolean isOpenLight) {
        if (isOpenLight) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    /**
     * /获取CPU名字
     *
     * @return
     */
    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存导出策略
     *
     * @param exportStrategy
     */
    public static void saveExportStrategy(ExportStrategy exportStrategy) {
        PreferenceUtils.putString(PREF_EXPORT_CITY_NAME, exportStrategy.cityName);
        PreferenceUtils.putString(PREF_EXPORT_FILE_TYPE, exportStrategy.exportFileType);
        PreferenceUtils.putString(PREF_EXPORT_FORMAT_JSON, exportStrategy.exportFormatJson);
        PreferenceUtils.putString(PREF_EXPORT_TABLE_ALIAS_JSON, exportStrategy.tableNameJson);
    }

    /**
     * 获取导出策略
     *
     * @return
     */
    public static ExportStrategy getExportStrategy() {
        String cityName = PreferenceUtils.getString(PREF_EXPORT_CITY_NAME, null);
        String fileType = PreferenceUtils.getString(PREF_EXPORT_FILE_TYPE, null);
        String formatJson = PreferenceUtils.getString(PREF_EXPORT_FORMAT_JSON, null);
        if (TextUtils.isEmpty(cityName) || (!EmiStringUtil.isJSONValid(formatJson)) || TextUtils.isEmpty(fileType)) {
            return null;
        }
        ExportStrategy exportStrategy = new ExportStrategy();
        exportStrategy.cityName = cityName;
        exportStrategy.exportFileType = fileType;
        exportStrategy.exportFormatJson = formatJson;
        exportStrategy.tableNameJson = PreferenceUtils.getString(PREF_EXPORT_TABLE_ALIAS_JSON, null);
        return exportStrategy;
    }


    /**
     * 获取读表地址指令(普通市场)
     */
    public static byte[] getCmdReadAddressNormal() {
        return new byte[]{(byte) 0xFE, (byte) 0xFE, 0x68, 0x10, (byte) 0xAA,
                (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
                (byte) 0xAA, (byte) 0xAA, 0x03, 0x03, (byte) 0x0A, (byte) 0x81,
                0x01, (byte) 0xB0, 0x16};
    }

    /**
     * 获取读表地址指令(特殊市场)
     */
    public static byte[] getCmdReadAddressSpecial() {
        return new byte[]{(byte) 0xFE, (byte) 0xFE, 0x68, 0x10, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, 0x03, 0x03,
                (byte) 0x81, (byte) 0x0A, 0x01, (byte) 0xB0, 0x16};
    }

    /**
     * 获取使能指令
     *
     * @param meterId
     * @param firmCode
     * @return
     */
    public static byte[] getSettingCmd(String meterId, String firmCode) {
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04,
                0x07, (byte) 0xAA, (byte) 0xA0, 0x00, (byte) 0x87, 0x56, 0x20, (byte) 0x88, 0x00, 0x16};
        if (TextUtils.isEmpty(meterId) || TextUtils.isEmpty(firmCode)) {
            return new byte[]{};
        }
        int checkBeginIndex = 4;
        int checkEndIndex = 21;
        byte hexCheck = 0x00;
        meterId = EmiStringUtil.appendZero(meterId, 10);
        firmCode = EmiStringUtil.appendZero(firmCode, 4);
        byte[] meterIdBytes = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(meterId));
        byte[] firmCodeBytes = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(firmCode));
        cmd[6] = meterIdBytes[4];
        cmd[7] = meterIdBytes[3];
        cmd[8] = meterIdBytes[2];
        cmd[9] = meterIdBytes[1];
        cmd[10] = meterIdBytes[0];
        cmd[11] = firmCodeBytes[1];
        cmd[12] = firmCodeBytes[0];
        for (int k = checkBeginIndex; k <= checkEndIndex; k++) {
            hexCheck += cmd[k];
        }
        cmd[checkEndIndex + 1] = hexCheck;
        return cmd;
    }


    public static byte[] getClearThCmd(String meterId, String mInputThStr) {
        if (TextUtils.isEmpty(meterId)) {
            return new byte[]{};
        }
        meterId = EmiStringUtil.appendZero(meterId, 10);
        byte hexCheck = 0x00;
        int checkBeginIndex = 4;
        int modifyThEndIndex = 22;
        //修改千分位
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x16,
                0x08, (byte) 0x16, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
        LogUtil.d(TAG, "测试" + meterId);
        LogUtil.d(TAG, "测试mInputThStr:" + meterId);
        byte[] meterIdByte;
        byte[] thByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(mInputThStr));
        meterIdByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(meterId));
        cmd[6] = meterIdByte[4];
        cmd[7] = meterIdByte[3];
        cmd[8] = meterIdByte[2];
        cmd[9] = meterIdByte[1];
        cmd[10] = meterIdByte[0];
        cmd[20] = thByte[0];
        for (int k = checkBeginIndex; k <= modifyThEndIndex; k++) {
            hexCheck += cmd[k];
        }
        cmd[modifyThEndIndex + 1] = hexCheck;
        return cmd;
    }

    /**
     * 特殊市场水表
     *
     * @param oldWholeMeterInfo:旧的水表信息（厂商代码+水表地址）
     * @param newWholeMeterInfo:新的水表信息（厂商代码+水表地址）
     * @return
     */
    public static byte[] getEditMeterAddressCmdSpecial(String oldWholeMeterInfo, String newWholeMeterInfo) {
        if (TextUtils.isEmpty(oldWholeMeterInfo) || TextUtils.isEmpty(newWholeMeterInfo)) {
            return new byte[]{};
        }
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x15, 0x0A, (byte) 0xA0, 0x18
                , 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
        oldWholeMeterInfo = EmiStringUtil.appendZero(oldWholeMeterInfo, 14);
        newWholeMeterInfo = EmiStringUtil.appendZero(newWholeMeterInfo, 14);
        byte[] oldMeterIdByte;
        byte[] oldFirmCodeByte;
        byte hexCheck = 0x00;
        int checkBeginIndex = 4;
        int checkEndIndex = 24;
        int csIndex = 25;
        String oldMeterId = oldWholeMeterInfo.substring(4, oldWholeMeterInfo.length());
        String oldFirmCode = oldWholeMeterInfo.substring(0, 4);
        String newMeterId = newWholeMeterInfo.substring(4, oldWholeMeterInfo.length());
        String newFirmCode = newWholeMeterInfo.substring(0, 4);
        LogUtil.d("旧当前的厂商代码为：" + oldFirmCode);
        byte[] newMeterIdByte;
        byte[] newFirmCodeByte;
        oldMeterIdByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(oldMeterId));
        oldFirmCodeByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(oldFirmCode));
        newMeterIdByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(newMeterId));
        newFirmCodeByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(newFirmCode));
        cmd[6] = oldMeterIdByte[4];
        cmd[7] = oldMeterIdByte[3];
        cmd[8] = oldMeterIdByte[2];
        cmd[9] = oldMeterIdByte[1];
        cmd[10] = oldMeterIdByte[0];
        cmd[11] = oldFirmCodeByte[1];
        cmd[12] = oldFirmCodeByte[0];
        cmd[18] = newMeterIdByte[4];
        cmd[19] = newMeterIdByte[3];
        cmd[20] = newMeterIdByte[2];
        cmd[21] = newMeterIdByte[1];
        cmd[22] = newMeterIdByte[0];
        cmd[23] = newFirmCodeByte[1];
        cmd[24] = newFirmCodeByte[0];
        for (int k = checkBeginIndex; k <= checkEndIndex; k++) {
            hexCheck += cmd[k];
        }
        cmd[csIndex] = hexCheck;
        return cmd;
    }


    /**
     * 特殊市场水表
     *
     * @param oldWholeMeterInfo:旧的水表信息（厂商代码+水表地址）
     * @param newWholeMeterInfo:新的水表信息（厂商代码+水表地址）
     * @return
     */
    public static byte[] getEditMeterAddressCmdNormal(String oldWholeMeterInfo, String newWholeMeterInfo) {
        if (TextUtils.isEmpty(oldWholeMeterInfo) || TextUtils.isEmpty(newWholeMeterInfo)) {
            return new byte[]{};
        }
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x15, 0x0A, 0x18,
                (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
        oldWholeMeterInfo = EmiStringUtil.appendZero(oldWholeMeterInfo, 14);
        newWholeMeterInfo = EmiStringUtil.appendZero(newWholeMeterInfo, 14);
        byte[] oldMeterIdByte;
        byte[] oldFirmCodeByte;
        byte hexCheck = 0x00;
        int checkBeginIndex = 4;
        int checkEndIndex = 24;
        int csIndex = 25;
        String oldMeterId = oldWholeMeterInfo.substring(4, oldWholeMeterInfo.length());
        String oldFirmCode = oldWholeMeterInfo.substring(0, 4);
        String newMeterId = newWholeMeterInfo.substring(4, oldWholeMeterInfo.length());
        String newFirmCode = newWholeMeterInfo.substring(0, 4);
        LogUtil.d("旧当前的厂商代码为：" + oldFirmCode);
        byte[] newMeterIdByte;
        byte[] newFirmCodeByte;
        oldMeterIdByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(oldMeterId));
        oldFirmCodeByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(oldFirmCode));
        newMeterIdByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(newMeterId));
        newFirmCodeByte = EmiStringUtil.stringToBytes(EmiStringUtil.getHexString(newFirmCode));
        cmd[6] = oldMeterIdByte[4];
        cmd[7] = oldMeterIdByte[3];
        cmd[8] = oldMeterIdByte[2];
        cmd[9] = oldMeterIdByte[1];
        cmd[10] = oldMeterIdByte[0];
        cmd[11] = oldFirmCodeByte[1];
        cmd[12] = oldFirmCodeByte[0];
        cmd[18] = newMeterIdByte[4];
        cmd[19] = newMeterIdByte[3];
        cmd[20] = newMeterIdByte[2];
        cmd[21] = newMeterIdByte[1];
        cmd[22] = newMeterIdByte[0];
        cmd[23] = newFirmCodeByte[1];
        cmd[24] = newFirmCodeByte[0];
        for (int k = checkBeginIndex; k <= checkEndIndex; k++) {
            hexCheck += cmd[k];
        }
        cmd[csIndex] = hexCheck;
        return cmd;
    }


    /**
     * 读取集中器ID指令
     *
     * @return
     */
    public static byte[] getConcentratorIdCmd() {
        return new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x04, 0x68, 0x02, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x16};
    }

    /**
     * @param userInfoList
     * @param concentratorId
     * @param collectorId
     * @return
     */
    public static byte[] getWriteToConcentratorCmd(List<UserInfo> userInfoList, String concentratorId, String collectorId) {
        List<Byte> byteList = new ArrayList<>();
        byte[] currentBytes = new byte[]{};
        boolean isEmpty = TextUtils.isEmpty(concentratorId) || TextUtils.isEmpty(collectorId) || userInfoList.isEmpty();
        if (isEmpty) {
            return currentBytes;
        }
        boolean isCorrect = TextUtils.isDigitsOnly(concentratorId) && TextUtils.isDigitsOnly(collectorId);
        if (!isCorrect) {
            return currentBytes;
        }
        byte[] temp = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, (byte) 0x00, (byte) 0x00, (byte) 0x68, (byte) 0x19};
        for (byte b : temp) {
            byteList.add(b);
        }
        for (UserInfo userInfo : userInfoList) {
            if (TextUtils.isEmpty(userInfo.meteraddr) || !TextUtils.isDigitsOnly(userInfo.meteraddr)) {
                return currentBytes;
            }
        }
        //写入集中器集器ID
        byteList.add(numberToHexByte(concentratorId));
        //写入采集器编号
        byteList.add(numberToHexByte(collectorId));
        //当前写入第几包数据
        byteList.add(numberToHexByte("01"));
        //写入的水表数量
        byteList.add(numberToHexByte(String.valueOf(userInfoList.size())));
        for (UserInfo userInfo : userInfoList) {
            addByteToList(meterIdToHexBytes(userInfo.meteraddr), byteList);
            addByteToList(meterDataToHexBytes(userInfo.curdata), byteList);
        }
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, byteList.size()));
        byteList.add((byte) 0x16);
        int length = userInfoList.size() * 6 + 5;
        byte[] lengthBytes = DigitalTrans.intToByteArray(length);
        byteList.set(5, lengthBytes[0]);
        byteList.set(6, lengthBytes[1]);
        LogUtil.d("长度：" + length);
        currentBytes = byteListToByteArray(byteList);
        String result = DigitalTrans.byte2hex(currentBytes);
        EmiLog.i(TAG, "转换后的结果：" + result);
        return currentBytes;
    }


    private static void addByteToList(byte[] bytes, List<Byte> byteList) {
        for (int i = 0; i < bytes.length; i++) {
            byteList.add(bytes[i]);
        }
    }

    public static byte[] byteListToByteArray(List<Byte> byteList) {
        if (byteList == null || byteList.isEmpty()) {
            return new byte[]{};
        }
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = byteList.get(i);
        }
        return bytes;
    }

    public static List<Byte> byteArrayToByteList(byte[] byteArray) {
        List<Byte> byteList = new ArrayList<>();
        if (byteArray == null || byteArray.length == 0) {
            return byteList;
        }
        for (byte b : byteArray) {
            byteList.add(b);
        }
        return byteList;
    }

    /**
     * 将水表地址转换成16进制字节数组
     *
     * @param meterId
     * @return
     */
    public static byte[] meterIdToHexBytes(String meterId) {
        if (TextUtils.isEmpty(meterId) || !TextUtils.isDigitsOnly(meterId) || meterId.length() > METER_ID_LENGTH) {
            return new byte[0];
        }
        byte[] meterIdBytes = new byte[4];
        int number = Integer.parseInt(meterId);
        meterIdBytes[0] = (byte) (number >> 24);
        meterIdBytes[1] = (byte) (number >> 16);
        meterIdBytes[2] = (byte) (number >> 8);
        meterIdBytes[3] = (byte) (number);
        return meterIdBytes;
    }

    /**
     * 将水表读数转换成16进制字节数组
     *
     * @param meterData
     * @return
     */
    public static byte[] meterDataToHexBytes(int meterData) {
        if (meterData > METER_MAX_METER_DATA) {
            return new byte[0];
        }
        byte[] meterDataBytes = new byte[2];
        meterDataBytes[0] = (byte) (meterData >> 8);
        meterDataBytes[1] = (byte) (meterData);
        return meterDataBytes;
    }

    /**
     * 将十进制字符串转成十六进制字节且必须是数字字符（不能超过255）
     *
     * @param number
     * @return
     */
    public static byte numberToHexByte(String number) {
        int max = 255;
        if (TextUtils.isEmpty(number) || !TextUtils.isDigitsOnly(number) || Integer.parseInt(number) > max) {
            return 0x00;
        }
        return (byte) Integer.parseInt(number);
    }

    /**
     * 获取集中器管辖的采集器设备编号指令
     *
     * @param deviceId
     * @return
     */
    public static byte[] getCmdDeviceId(String deviceId) {
        if (!isValidNumber(deviceId)) {
            return emptyBytes();
        }
        byte[] temp = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x04, 0x68, 0x22, 0x00, 0x00, 0x00};
        temp[9] = numberToHexByte(deviceId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, temp.length));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(temp));
        return byteListToByteArray(byteList);
    }


    /**
     * @param concentratorId 十进制
     * @param deviceId       十进制
     * @return
     */
    public static byte[] getCmdDeleteDeviceData(String concentratorId, String deviceId) {
        //FE FE FE FE 68 00 05 68 1C 00 00 01 00
        if (!isValidNumber(concentratorId) || !isValidNumber(deviceId)) {
            return emptyBytes();
        }
        byte[] temp = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x03, 0x68, 0x1C, 0x00, 0x00};

        temp[9] = numberToHexByte(concentratorId);
        temp[10] = numberToHexByte(deviceId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, temp.length));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }

    private static boolean isValidNumber(String value) {
        if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value) && Integer.parseInt(value) < 256) {
            return true;
        }
        return false;
    }

    private static byte[] emptyBytes() {
        return new byte[]{};
    }

    /**
     * 获取指定采集器数据指令
     *
     * @param concentratorId
     * @param deviceId
     * @return
     */
    public static byte[] getCmdReadDeviceData(String concentratorId, String deviceId) {
        if (!isValidNumber(concentratorId) || !isValidNumber(deviceId)) {
            return emptyBytes();
        }
        //        FE FE FE FE 68 00 06 68 34 00 02 00 【01 01】
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x06, 0x68, 0x34, 0x00, 0x00, 0x00, 0x01, 0x01};
        temp[9] = numberToHexByte(concentratorId);
        temp[10] = numberToHexByte(deviceId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, temp.length));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }


    /**
     * 获取指定采集器设备信息
     *
     * @param concentratorId
     * @return
     */
    public static byte[] getCmdReadDeviceInfo(String concentratorId) {
        if (!isValidNumber(concentratorId)) {
            return emptyBytes();
        }
        //      FE FE FE FE 68 00 04 68 06 00 02 00 07 16
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x04, 0x68, 0x06, 0x00, 0x00, 0x00};
        temp[9] = numberToHexByte(concentratorId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, temp.length));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }

    /**
     * 写采集器设备编号指令
     *
     * @param concentratorId
     * @return
     */
    public static byte[] getCmdWriteDeviceNumber(String concentratorId, int deviceCount) {
        if (!isValidNumber(concentratorId)) {
            return emptyBytes();
        }
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x00, 0x68, 0x23, 0x00, 0x00};
        temp[9] = numberToHexByte(concentratorId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        int length = deviceCount + 4;
        byte[] lengthBytes = DigitalTrans.intToByteArray(length);
        byteList.set(5, lengthBytes[0]);
        byteList.set(6, lengthBytes[1]);
        byteList.add(numberToHexByte(deviceCount + ""));
        LogUtil.d("长度：" + length);
        LogUtil.d("集合长度：" + byteList.size());
        byteList.addAll(createNumberHexBytes(deviceCount));
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, byteList.size()));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }

    public static List<Byte> createNumberHexBytes(int count) {
        List<Byte> byteList = new ArrayList<>();
        StringBuilder sb = new StringBuilder("");
        int tenDigit = 10;
        for (int i = 0; i < count; i++) {
            sb.setLength(0);
            if (i + 1 < tenDigit) {
                sb.append("0");
            }
            sb.append(i + 1);
            byteList.add(numberToHexByte(sb.toString()));
        }
        return byteList;
    }


    /**
     * 写采集器设备编号指令
     *
     * @param oldConcentratorId
     * @return
     */
    public static byte[] getCmdEditConcentratorId(String oldConcentratorId, String newConcentratorId) {
        //        FE FE FE FE 68 00 06 68 12 00 02  01 03  00 18 16
        if (!isValidNumber(oldConcentratorId) || !isValidNumber(newConcentratorId)) {
            return emptyBytes();
        }
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x06, 0x68, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00};
        temp[9] = numberToHexByte(oldConcentratorId);
        temp[11] = numberToHexByte(newConcentratorId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, temp.length));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }


    /**
     * @param concentratorId 集中器开始集抄下辖采集器冻结数据  27H
     * @return
     */
    public static byte[] getCmdStartReadDeviceFreezeData(String concentratorId) {
        if (!isValidNumber(concentratorId)) {
            return emptyBytes();
        }
        //     FE FE FE FE 68 00 04 68 28 02 00 00 29 16
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x04, 0x68, 0x28, 0x00, 0x00, 0x00};
        temp[9] = numberToHexByte(concentratorId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, temp.length));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }

    /**
     * @param concentratorId 集中器抄下辖采集器数据  28H
     * @return
     */
    public static byte[] getCmdStartReadDeviceData(String concentratorId) {
        if (!isValidNumber(concentratorId)) {
            return emptyBytes();
        }
        //     FE FE FE FE 68 00 04 68 27 02 00 00 29 16
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x04, 0x68, 0x27, 0x00, 0x00, 0x00};
        temp[9] = numberToHexByte(concentratorId);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, temp.length));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }


    /**
     * 写同步时间    11H（Ⅰ、Ⅱ代采集器、集中器）
     *
     * @param concentratorId
     * @return
     */
    public static byte[] getCmdWriteSycTime(String concentratorId, String deviceNumber) {
        if (!isValidNumber(concentratorId)) {
            return emptyBytes();
        }
        //    FE FE FE FE 68 00 0A 68 11 00 01 14 0F 06 06 0A 18 00 63 16
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x0A, 0x68, 0x11, 0x00, 0x00};
        temp[9] = numberToHexByte(concentratorId);
        temp[10] = numberToHexByte(deviceNumber);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        String time = TimeUtil.getTimeStringNoSplit(getCurrentTime());
        List<String> timeList = EmiStringUtil.getStrList(time, 2);
        for (String s : timeList) {
            byteList.add(numberToHexByte(s));
        }
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, byteList.size()));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }


    public static byte[] getCmdSetDeviceRunInfo(String concentratorId, String deviceNumber, String freezeTime, List<Integer> freezeDateList) {
        if (!isValidNumber(concentratorId) || !isValidNumber(deviceNumber)) {
            return emptyBytes();
        }
        int timeLength = 4;
        if (TextUtils.isEmpty(freezeTime) || !TextUtils.isDigitsOnly(freezeTime) || freezeTime.length() != timeLength) {
            return emptyBytes();
        }
        //        发送命令：FE FE FE FE 68 00 09 68 14 00 01 10 2E 03 0F 12 18 43 16
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x00, 0x68, 0x14, 0x00, 0x00};
        temp[9] = numberToHexByte(concentratorId);
        temp[10] = numberToHexByte(deviceNumber);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        int length = freezeDateList.size() + 6;
        byte[] lengthBytes = DigitalTrans.intToByteArray(length);
        byteList.set(5, lengthBytes[0]);
        byteList.set(6, lengthBytes[1]);
        List<String> timeList = EmiStringUtil.getStrList(freezeTime, 2);
        for (String time : timeList) {
            byteList.add(numberToHexByte(time));
        }
        byteList.add(numberToHexByte(freezeDateList.size() + ""));
        for (Integer date : freezeDateList) {
            byteList.add(numberToHexByte(date + ""));
        }
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, byteList.size()));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }

    /**
     * 组网指令
     *
     * @param concentratorId
     * @param deviceNumber
     * @return
     */
    public static byte[] getCmdReadActualData(String concentratorId, String deviceNumber) {
        //        FE FE FE FE 68 00 04 68 32 01 00 00 33 16
        if (!isValidNumber(concentratorId) || !isValidNumber(deviceNumber)) {
            return emptyBytes();
        }
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x04, 0x68, 0x32, 0x00, 0x00, 0x00};
        temp[9] = numberToHexByte(concentratorId);
        temp[10] = numberToHexByte(deviceNumber);
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, byteList.size()));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }


    public static byte[] getCmdWriteDataToDevice(List<UserInfo> userInfoList) {
        if (userInfoList == null || userInfoList.isEmpty()) {
            return emptyBytes();
        }
        //        FE FE FE FE 68 00 0B 68 19 FF FF 01
        byte[] temp = {
                (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0x68, 0x00, 0x00, 0x68, 0x19, (byte) 0xFF, (byte) 0xFF, 0x00};
        List<Byte> byteList = new ArrayList<>();
        addByteToList(temp, byteList);
        byteList.set(11, numberToHexByte(userInfoList.size() + ""));
        int length = userInfoList.size() * 5 + userInfoList.size() * 2 + 4;
        LogUtil.d("长度：" + length);
        byte[] lengthBytes = DigitalTrans.intToByteArray(length);
        byteList.set(5, lengthBytes[0]);
        byteList.set(6, lengthBytes[1]);
        for (UserInfo userInfo : userInfoList) {
            byteList.addAll(createMeterInfoBytes(userInfo));
        }
        byteList.add(DigitalTrans.getCsNumber(byteList, 8, byteList.size()));
        byteList.add((byte) 0x16);
        LogUtil.w("发送的指令：" + DigitalTrans.byte2hex(byteListToByteArray(byteList)));
        return byteListToByteArray(byteList);
    }


    public static List<Byte> createMeterInfoBytes(UserInfo userInfo) {
        List<Byte> byteList = new ArrayList<>();
        if (userInfo == null) {
            return byteList;
        }
        String meterId;
        String meterData;
        meterId = EmiStringUtil.appendZero(userInfo.meteraddr, 10);
        meterData = EmiStringUtil.appendZero(userInfo.curdata + "", 4);
        EmiStringUtil.stringToBytes(meterId, 10);
        byteList.addAll(byteArrayToByteList(EmiStringUtil.stringToBytes(meterId, 16)));
        LogUtil.w(TAG, "已执行：状态" + userInfo.state);
        if (userInfo.curdata < 0 || userInfo.state == STATE_FAILED || userInfo.state == STATE_NO_READ || userInfo.state == STATE_ALL) {
            LogUtil.w(TAG, "已执行：" + meterData);
            byteList.addAll(byteArrayToByteList(EmiStringUtil.stringToBytes(ERROR_METER_DATA, 16)));
        } else {
            LogUtil.e(TAG, "已执行：" + meterData);
            byteList.addAll(byteArrayToByteList(EmiStringUtil.stringToBytes(meterData, 16)));
        }
        return byteList;
    }

    public static List<String> stringArrayToStringList(String[] stringArray) {
        List<String> stringList = new ArrayList<>();
        if (stringArray == null) {
            return stringList;
        }
        for (int i = 0; i < stringArray.length; i++) {
            stringList.add(stringArray[i]);
        }
        return stringList;
    }

}





