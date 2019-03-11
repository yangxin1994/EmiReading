package com.emi.emireading.core.utils;

import android.text.TextUtils;

import com.emi.emireading.common.DigitalTrans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author :zhoujian
 * @description : 时间工具类
 * @company :翼迈科技
 * @date 2018年03月13日下午 04:22
 * @Email: 971613168@qq.com
 */

public class TimeUtil {
    private final static String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final static String PATTERN_NO_SPLIT = "yyyyMMddHHmmss";
    private final static String PATTERN_DATE = "yyyy-MM-dd";
    private final static int TIME_LENGTH = 14;

    /**
     * 获取当前时间戳格式的字符串
     */
    public static String getCurrentTime() {
        long timeStamp = System.currentTimeMillis();
        return String.valueOf(timeStamp);
    }


    /**
     * 将时间戳转换为时间格式的字符串
     */
    public static String getTimeString(String timeMillis) {
        final FastDateFormat df = FastDateFormat.getInstance(PATTERN);
        if (StringUtils.isNotEmpty(timeMillis)) {
            long time = Long.parseLong(timeMillis);
            return df.format(new Date(time));
        } else {
            return "";
        }
    }

    /**
     * 将时间戳转换为时间格式的字符串
     */
    public static String getTimeStringNoSplit(String timeMillis) {
        final FastDateFormat df = FastDateFormat.getInstance(PATTERN_NO_SPLIT);
        if (StringUtils.isNotEmpty(timeMillis)) {
            long time = Long.parseLong(timeMillis);
            return df.format(new Date(time));
        } else {
            return "";
        }
    }


    public static String getDateString(String timeMillis) {
        final FastDateFormat df = FastDateFormat.getInstance(PATTERN_DATE);
        if (StringUtils.isNotEmpty(timeMillis)) {
            long time = Long.parseLong(timeMillis);
            return df.format(new Date(time));
        } else {
            return "";
        }
    }

    public static String getTimeChineseCharacter(String timeValue) {
        if (TextUtils.isEmpty(timeValue)) {
            return "";
        }
        if (timeValue.length() == TIME_LENGTH) {
            StringBuilder sb = new StringBuilder("");
            sb.append(timeValue.substring(0, 4));
            sb.append("年");
            sb.append(timeValue.substring(4, 6));
            sb.append("月");
            sb.append(timeValue.substring(6, 8));
            sb.append("日");
            sb.append(timeValue.substring(8, 10));
            sb.append("时");
            sb.append(timeValue.substring(10, 12));
            sb.append("分");
            sb.append(timeValue.substring(12, 14));
            sb.append("秒");
            return sb.toString();
        }
        return "";
    }

    /**
     * 将十六进制时间字符串集合转成时间戳
     *
     * @param timeList
     */
    public static String dateListParseTimeMillis(List<String> timeList) {
        int size = 7;
        if (timeList == null || timeList.isEmpty() || timeList.size() != size) {
            return "";
        }
        String[] timeArray = new String[timeList.size()];
        String dateString;
        int date;
        for (int i = 0; i < timeList.size(); i++) {
            date = DigitalTrans.hexStringToAlgorism(timeList.get(i));
            if (date < 10) {
                dateString = "0" + date;
            } else {
                dateString = date + "";
            }
            timeArray[i] = dateString;
        }
        StringBuilder timeStringBuilder = new StringBuilder("");
        timeStringBuilder.append(timeArray[0]);
        timeStringBuilder.append(timeArray[1]);
        timeStringBuilder.append("-");
        //月
        timeStringBuilder.append(timeArray[2]);
        timeStringBuilder.append("-");
        //日
        timeStringBuilder.append(timeArray[3]);
        timeStringBuilder.append("-");
        //小时
        timeStringBuilder.append(timeArray[4]);
        timeStringBuilder.append("-");
        //分钟
        timeStringBuilder.append(timeArray[5]);
        timeStringBuilder.append("-");
        //秒
        timeStringBuilder.append(timeArray[6]);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date d;
        try {
            d = sdf.parse(timeStringBuilder.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        return d.getTime() + "";
    }
}
