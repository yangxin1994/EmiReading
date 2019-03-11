package com.emi.emireading.core.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * @author :zhoujian
 * @description : 字符串工具类
 * @company :翼迈科技
 * @date: 2017年12月28日上午 09:18
 * @Email: 971613168@qq.com
 */

public class EmiStringUtil {
    private static final String TAG = "EmiStringUtil";
    private static final String EMPTY = "null";
    private static final String E = "E";
    private static final String DEC = "0.0";
    public static final String DOT = ".";

    /**
     * 解决解析dbf或excel文件出现“显示科学计数法问题”
     *
     * @param str
     */
    public static String handleString(String str) {
        String string;
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if ((!str.contains(E)) && (!str.contains(DOT))) {
            return str;
        }
        try {
            double temp = Double.parseDouble(str);
            string = (int) temp + "";
        } catch (NumberFormatException e) {
            string = str;
        }
        if (DEC.equals(str)) {
            return "0";
        }
        if (str.contains(E)) {
            int index = str.indexOf(E);
            int dotIndex = str.indexOf(".");
            string = str.substring(0, index);
            int numberLength = parseInt(str.substring(index + 1));
            String decimal = str.substring(dotIndex + 1, index);
            int i = numberLength - decimal.length();
            string = string.replace(".", "");
            if (i > 0) {
                for (int j = 0; j < i; j++) {
                    string += "0";
                }
            }
        }
        return string;
    }

    /**
     * 根据路径获取文件名（仅保留文件名，不保留后缀名）
     */
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        int start = filePath.lastIndexOf("/");
        int end = filePath.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return filePath.substring(start + 1, end);
        } else {
            return null;
        }
    }

    /**
     * 根据路径获取文件名（保留后缀名）
     */
    public static String getFileNameWithSuffix(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        int start = filePath.lastIndexOf("/");
        if (start != -1) {
            return filePath.substring(start + 1);
        } else {
            return null;
        }
    }


    public static int stringConvertInt(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        if (TextUtils.isDigitsOnly(str)) {
            return Integer.parseInt(str);
        }
        if (isDecimal(str)) {
            return (int) Double.parseDouble(str);
        }
        return -1;
    }


    public static boolean isEmpty(@Nullable CharSequence str) {
        if (str == null || str.length() == 0 || EMPTY.equalsIgnoreCase(str.toString().trim())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNotEmpty(@Nullable CharSequence str) {
        if (str == null || str.length() == 0 || EMPTY.equalsIgnoreCase(str.toString().trim())) {
            return false;
        } else {
            return true;
        }
    }

    public static double getDoubleValue(String number) {
        try {
            LogUtil.w(TAG, "传入的number：" + number);
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    public static String subZeroAndDot(String s) {
        if (s == null) {
            return null;
        }
        if (s.indexOf(DOT) > 0) {
            //去掉多余的0
            s = s.replaceAll("0+?$", "");
            //如最后一位是.则去掉
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }

    public static byte[] stringToBytes(String s) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) parseInt(s.substring(i * 2, i * 2 + 2), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return buf;
    }


    public static String getHexString(String buf) {
        String s = buf;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (('0' <= c && c <= '9') || ('a' <= c && c <= 'f')
                    || ('A' <= c && c <= 'F')) {
                sb.append(c);
            }
        }
        if ((sb.length() % 2) != 0) {
            sb.deleteCharAt(sb.length());
        }
        return sb.toString();
    }


    public static String clearFirstZero(String str) {
        if (str != null) {
            return str.replaceFirst("^0*", "");
        } else {
            return null;
        }
    }


    public static byte[] stringToByteArray(String strData) {
        strData = strData.replace(";", "");
        List<Byte> list = new ArrayList<Byte>();
        for (int i = 0; i < strData.length(); i = i + 2) {
            String msg = strData.substring(i, i + 2);
            Integer n = Integer.parseInt(msg, 16);
            byte msg2Cluster = n.byteValue();
            list.add(msg2Cluster);
        }
        byte[] msg2Cluster = new byte[list.size()];
        for (int i = 0; i < msg2Cluster.length; i++) {
            msg2Cluster[i] = list.get(i);
        }
        return msg2Cluster;
    }

    public static String byteAttaryToString(@NonNull byte[] b) {
        int length = b.length;
        StringBuilder str = new StringBuilder();
        StringBuilder aSb = new StringBuilder("");
        byte[] array = new byte[1];
        String a;
        for (int i = 0; i < length; i++) {
            array[0] = b[i];
            a = Arrays.toString(array);
            a = a.substring(1, a.length() - 1);
            int temp = Integer.parseInt(a);
            a = Integer.toHexString(temp);
            if (a.length() > 2) {
                a = a.substring(a.length() - 2, a.length());
            }
            if (a.length() < 2) {
                aSb.append("0");
                aSb.append(a);
                //                a = "0" + a;
            }
            str.append(aSb.toString());
        }
        return str.toString();
    }

    public static String clearNullStr(String str) {
        if (null != str) {
            return str.replace("null", "");
        }
        return null;
    }

    /***
     * @param value
     * @return
     */
    public static String appendZero(String value, int maxLength) {
        if (value != null) {
            StringBuilder stringBuilder = new StringBuilder("");
            int a = value.length();
            for (; a < maxLength; a++) {
                stringBuilder.append(EmiConstants.ZERO);
            }
            stringBuilder.append(value);
            return stringBuilder.toString();
        }
        return null;
    }


    public static String formatNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }


    public static boolean isDecimal(String original) {
        return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", original);
    }

    public static boolean isWholeNumber(String original) {
        return isMatch("^\\+{0,1}[1-9]\\d*", original);
    }

    private static boolean isMatch(String regex, String original) {
        if (original == null || "".equals(original.trim())) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher isNum = pattern.matcher(original);
        return isNum.matches();
    }

    public static String convertNumber(String value) {
        String temp = value;
        if (value == null) {
            return null;
        } else {
            if (value.contains(E)) {
                LogUtil.e("解析的数字：" + value);
                LogUtil.w("出现科学计数法，需要转换");
                BigDecimal bd = new BigDecimal(value);
                temp = bd.toPlainString();
            }
            if (value.contains(EmiConstants.DOT)) {
                temp = value.substring(0, value.indexOf(EmiConstants.DOT));
            }
            return temp.trim();
        }
    }

    /**
     * 获取字符串 XX位字符串
     *
     * @param digit:截取字符串后面第几位
     * @return
     */
    public static String getLastStr(String value, int digit) {
        if (value == null) {
            return null;
        } else {
            if (digit <= 0 || digit > value.length()) {
                return value;
            }
        }
        return value.substring(value.length() - digit, value.length());
    }


    public static String getMiddleStr(String value, int start, int end) {
        if (value == null) {
            return null;
        } else {
            boolean startFlag = (start <= 0 || start > value.length());
            boolean endFlag = (end <= 0 || end > value.length());
            if (startFlag || endFlag || start > end) {
                return value;
            }
        }
        return value.substring(value.length() - start, value.length());
    }


    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @param size        指定列表大小
     * @return
     */
    public static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }


    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     *
     * @param str 原始字符串
     * @param f   开始位置
     * @param t   结束位置
     * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length()) {
            return "";
        }
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    /**
     * 将字符串两两分割后添加到集合
     *
     * @param str
     * @return
     */
    public static List<String> splitStrToList(String str) {
        List<String> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }
        int m = str.length() / 2;
        if (m * 2 < str.length()) {
            m++;
        }
        String[] strArray = new String[m];
        int j = 0;
        for (int i = 0; i < str.length(); i++) {
            if (i % 2 == 0) {
                strArray[j] = "" + str.charAt(i);
            } else {
                strArray[j] = strArray[j] + str.charAt(i);
                j++;
            }
        }
        for (String s : strArray) {
            list.add(s);
        }
        return list;
    }


    public static int stringToInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isJSONValid(String value) {
        try {
            JSONObject.parseObject(value);
        } catch (Exception ex) {
            try {
                JSONObject.parseArray(value);
            } catch (Exception ex1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEnglish(String charaString) {
        return charaString.matches("^[a-zA-Z]*");

    }

    /**
     * 拆分集合
     *
     * @param <T>
     * @param resList 要拆分的集合
     * @param count   每个集合的元素个数
     * @return 返回拆分后的各个集合
     */
    public static <T> List<List<T>> splitList(List<T> resList, int count) {
        if (resList == null || count < 1) {
            return null;
        }
        List<List<T>> ret = new ArrayList<>();
        int size = resList.size();
        //数据量不足count指定的大小
        if (size <= count) {
            ret.add(resList);
        } else {
            int pre = size / count;
            int last = size % count;
            //前面pre个集合，每个大小都是count个元素
            for (int i = 0; i < pre; i++) {
                List<T> itemList = new ArrayList<>();
                for (int j = 0; j < count; j++) {
                    itemList.add(resList.get(i * count + j));
                }
                ret.add(itemList);
            }
            //last的进行处理
            if (last > 0) {
                List<T> itemList = new ArrayList<>();
                for (int i = 0; i < last; i++) {
                    itemList.add(resList.get(pre * count + i));
                }
                ret.add(itemList);
            }
        }
        return ret;
    }

    /**
     * 判断字符串是否全部是某个字符
     *
     * @return
     */
    public static boolean checkStringIsSame(String value, char charValue) {
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (charValue != value.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static void test(List<String> testList) {
        //第二种方法：利用map.containsKey()
        Map<String, Integer> map = new HashMap<>(10);
        //用于存放重复的元素的list
        List<String> repeatList = new ArrayList<>();
        for (String s : testList) {
            //1:map.containsKey()   检测key是否重复
            if (map.containsKey(s)) {
                //获取重复的数据
                repeatList.add(s);
                Integer num = map.get(s);
                map.put(s, num + 1);
            } else {
                map.put(s, 1);
            }
            //2: 这个key是不是存在对应的value(key是否在map中)
            //          Integer count = map.get(s.getStuName());//这种写法也可以，异曲同工
            //          if (count == null) {
            //              map.put(s.getStuName(), 1);
            //          } else {
            //              map.put(s.getStuName(), (count + 1));
            //          }
        }
        //      for(Student s : repeatList){
        //          System.out.println("相同的元素:" + s.getStuName());
        //      }
        //      for(Map.Entry<String, Integer> entry : map.entrySet()){
        //          System.out.println("学生:" + entry.getKey() + "的名字出现了：" + entry.getValue() + "次");
        //      }
    }


    public static String appendString(String data, String character, int length) {
        if (data == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        if (data.length() >= length) {
            sb.append(data);
            return sb.toString();
        } else {
            for (int i = 0; i < length - data.length(); i++) {
                sb.append(character);
            }
            sb.append(data);
            return sb.toString();
        }
    }

    public static byte[] stringToBytes(String s, int radix) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) parseInt(s.substring(i * 2, i * 2 + 2), radix);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return buf;
    }

    public static String subStringByFrequency(String value, String subString, int frequency) {
        int i = 0;
        int s = 0;
        while (i++ < frequency) {
            s = value.indexOf(subString, s + 1);
            if (s == -1) {
                return value;
            }
        }
        return value.substring(0, s);
    }

    /**
     * 获取字符串出现的频率
     *
     * @param value
     * @param compareStr
     * @return
     */
    public static int getValueFrequency(String value, String compareStr) {
        //字符串查找初始从0开始查找
        int indexStart = 0;
        int compareStrLength = compareStr.length();
        int count = 0;
        while (true) {
            int tm = value.indexOf(compareStr, indexStart);
            if (tm >= 0) {
                count++;
                //  没查找一次就从新计算下次开始查找的位置
                indexStart = tm + compareStrLength;
            } else {
                //直到没有匹配结果为止
                break;
            }
        }
        return count;
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
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @return
     */
    public static List<String> getSplitStrListByLength(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getSplitStrListByLength(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @param size        指定列表大小
     * @return
     */
    public static List<String> getSplitStrListByLength(String inputString, int length,
                                                       int size) {
        List<String> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }
}

