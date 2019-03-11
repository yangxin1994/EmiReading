package com.emi.emireading.common;

import android.text.TextUtils;

import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据类型转换
 */
public class DigitalTrans {
    private final static int MAX_NUMBER = 65535;

    /**
     * 数字字符串转ASCII码字符串
     *
     * @param content 字符串
     * @return ASCII字符串
     */
    public static String StringToAsciiString(String content) {
        String result = "";
        int max = content.length();
        for (int i = 0; i < max; i++) {
            char c = content.charAt(i);
            String b = Integer.toHexString(c);
            result = result + b;
        }
        return result;
    }

    /**
     * 十六进制转字符串
     *
     * @param hexString  十六进制字符串
     * @param encodeType 编码类型4：Unicode，2：普通编码
     * @return 字符串
     */
    public static String hexStringToString(String hexString, int encodeType) {
        String result = "";
        int max = hexString.length() / encodeType;
        for (int i = 0; i < max; i++) {
            char c = (char) DigitalTrans.hexStringToAlgorism(hexString
                    .substring(i * encodeType, (i + 1) * encodeType));
            result += c;
        }
        return result;
    }

    /**
     * 十六进制字符串转十进制数字
     *
     * @param hex 十六进制字符串
     * @return 十进制数值
     */
    public static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    /**
     * 十六转二进制
     *
     * @param hex 十六进制字符串
     * @return 二进制字符串
     */
    public static String hexStringToBinary(String hex) {
        hex = hex.toUpperCase();
        String result = "";
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
                case '0':
                    result += "0000";
                    break;
                case '1':
                    result += "0001";
                    break;
                case '2':
                    result += "0010";
                    break;
                case '3':
                    result += "0011";
                    break;
                case '4':
                    result += "0100";
                    break;
                case '5':
                    result += "0101";
                    break;
                case '6':
                    result += "0110";
                    break;
                case '7':
                    result += "0111";
                    break;
                case '8':
                    result += "1000";
                    break;
                case '9':
                    result += "1001";
                    break;
                case 'A':
                    result += "1010";
                    break;
                case 'B':
                    result += "1011";
                    break;
                case 'C':
                    result += "1100";
                    break;
                case 'D':
                    result += "1101";
                    break;
                case 'E':
                    result += "1110";
                    break;
                case 'F':
                    result += "1111";
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * ASCII码字符串转数字字符串
     *
     * @param content ASCII字符串
     * @return 字符串
     */
    public static String AsciiStringToString(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    /**
     * 将十进制转换为指定长度的十六进制字符串
     *
     * @param algorism  int 十进制数字
     * @param maxLength int 转换后的十六进制字符串长度
     * @return String 转换后的十六进制字符串
     */
    public static String algorismToHEXString(int algorism, int maxLength) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        return patchHexString(result.toUpperCase(), maxLength);
    }

    /**
     * 字节数组转为普通字符串（ASCII对应的字符）
     *
     * @param bytearray byte[]
     * @return String
     */
    public static String bytetoString(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }

    /**
     * 二进制字符串转十进制
     *
     * @param binary 二进制字符串
     * @return 十进制数值
     */
    public static int binaryToAlgorism(String binary) {
        int max = binary.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = binary.charAt(i - 1);
            int algorism = c - '0';
            result += Math.pow(2, max - i) * algorism;
        }
        return result;
    }

    /**
     * 十进制转换为十六进制字符串
     *
     * @param algorism int 十进制的数字
     * @return String 对应的十六进制字符串
     */
    public static String algorismToHEXString(int algorism) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;

        }
        if (result.length() > 2) {
            result = result.substring(result.length() - 2, result.length());
        }
        result = result.toUpperCase();

        return result;
    }


    /**
     * HEX字符串前补0，主要用于长度位数不足。
     *
     * @param str       String 需要补充长度的十六进制字符串
     * @param maxLength int 补充后十六进制字符串的长度
     * @return 补充结果
     */
    public static String patchHexString(String str, int maxLength) {
        String temp = "";
        for (int i = 0; i < maxLength - str.length(); i++) {
            temp = "0" + temp;
        }
        str = (temp + str).substring(0, maxLength);
        return str;
    }

    /**
     * 将一个字符串转换为int
     *
     * @param s          String 要转换的字符串
     * @param defaultInt int 如果出现异常,默认返回的数字
     * @param radix      int 要转换的字符串是什么进制的,如16 8 10.
     * @return int 转换后的数字
     */
    public static int parseToInt(String s, int defaultInt, int radix) {
        int i = 0;
        try {
            i = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * 将一个十进制形式的数字字符串转换为int
     *
     * @param s          String 要转换的字符串
     * @param defaultInt int 如果出现异常,默认返回的数字
     * @return int 转换后的数字
     */
    public static int parseToInt(String s, int defaultInt) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * 十六进制字符串转为Byte数组,每两个十六进制字符转为一个Byte
     *
     * @param hex 十六进制字符串
     * @return byte 转换结果
     */
    public static byte[] hexStringToByte(String hex) {
        int max = hex.length() / 2;
        byte[] bytes = new byte[max];
        String binarys = DigitalTrans.hexStringToBinary(hex);
        for (int i = 0; i < max; i++) {
            bytes[i] = (byte) DigitalTrans.binaryToAlgorism(binarys.substring(
                    i * 8 + 1, (i + 1) * 8));
            if (binarys.charAt(8 * i) == '1') {
                bytes[i] = (byte) (0 - bytes[i]);
            }
        }
        return bytes;
    }

    /**
     * 十六进制串转化为byte数组
     *
     * @return the array of byte
     */
    public static final byte[] hex2byte(String hex)
            throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    /**
     * 字节数组转换为十六进制字符串
     *
     * @param b byte[] 需要转换的字节数组
     * @return String 十六进制字符串
     */
    public static String byte2hex(byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }


    public static final String byteToHexString(byte b) {
        String temp;
        temp = Integer.toHexString(b & 0xff);
        if (temp.length() == 1) {
            temp = "0" + temp;
        }
        return temp.toUpperCase();
    }

    /**
     * 65535以内的数字转成byte数组
     *
     * @param number
     * @return
     */
    public static byte[] intToByteArray(int number) {
        byte[] bytes = {0x00, 0x00};
        int high = number / 256;
        int low = number % 256;
        if (number < MAX_NUMBER) {
            bytes[0] = (byte) high;
            bytes[1] = (byte) low;
        }
        return bytes;
    }


    public static final String byte2hexWithSpace(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            stmp += EmiConstants.NEW_LINE;
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }


    /**
     * 判断是否是16进制数
     *
     * @param strHex
     * @return
     */
    public static boolean isHex(String strHex) {
        int i = 0;
        if (strHex.length() > 2) {
            if (strHex.charAt(0) == '0' && (strHex.charAt(1) == 'X' || strHex.charAt(1) == 'x')) {
                i = 2;
            }
        }
        for (; i < strHex.length(); ++i) {
            char ch = strHex.charAt(i);
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) {
                continue;
            }
            return false;
        }
        return true;
    }


    /**
     * 16进制转10进制
     *
     * @param strHex
     * @return
     */
    public static int hexToInt(String strHex) {
        int nResult = 0;
        if (!isHex(strHex)) {
            return nResult;
        }
        String str = strHex.toUpperCase();
        if (str.length() > 2) {
            if (str.charAt(0) == '0' && str.charAt(1) == 'X') {
                str = str.substring(2);
            }
        }
        int nLen = str.length();
        for (int i = 0; i < nLen; ++i) {
            char ch = str.charAt(nLen - i - 1);
            try {
                nResult += (getHex(ch) * getPower(16, i));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return nResult;
    }

    /**
     * 计算16进制对应的数值
     *
     * @param ch
     * @return
     * @throws Exception
     */
    public static int getHex(char ch) throws Exception {
        if (ch >= '0' && ch <= '9') {
            return (int) (ch - '0');
        }
        if (ch >= 'a' && ch <= 'f') {
            return (int) (ch - 'a' + 10);
        }
        if (ch >= 'A' && ch <= 'F') {
            return (int) (ch - 'A' + 10);
        }
        throw new Exception("error param");
    }


    /**
     * 计算幂
     *
     * @param nValue
     * @param nCount
     * @return
     * @throws Exception
     */
    private static int getPower(int nValue, int nCount) throws Exception {
        if (nCount < 0) {
            throw new Exception("nCount can't small than 1!");
        }
        if (nCount == 0) {
            return 1;
        }
        int nSum = 1;
        for (int i = 0; i < nCount; ++i) {
            nSum = nSum * nValue;
        }
        return nSum;
    }

    /**
     * 字符串转16进制字节数组
     *
     * @param stringValue
     * @return
     */
    public static byte[] stringConvertBytes(String stringValue) {
        boolean isEvenNumber = stringValue.length() % 2 == 0;
        if (!isEvenNumber) {
            return new byte[]{0x00, 0x00};
        }
        List<Byte> list = new ArrayList<>();
        for (int i = 0; i < stringValue.length(); i = i + 2) {
            String msg = stringValue.substring(i, i + 2);
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

    /**
     * 字节数组转16进制字符串
     *
     * @param b
     * @return
     */
    public static String bytesConvertString(byte[] b) {
        int length = b.length;
        StringBuffer str = new StringBuffer();
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
                a = "0" + a;
            }
            str.append(a + " ");
        }
        return str.toString().toUpperCase();
    }


    public static int stringConvertInt(String csNumber, int radix) {
        try {
            return Integer.parseInt(csNumber, radix);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 获取校验位
     *
     * @param data
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static byte getCsNumber(List<Byte> data, int startIndex, int endIndex) {
        int count = 0;
        for (int i = startIndex; i < endIndex; i++) {
            count += byte2Int(data.get(i));
            count = count & 0xFF;
        }
        LogUtil.d("getCs:校验和：" + count);
        return (byte) count;
    }



    public static int byte2Int(byte b) {
        return b & 0xFF;
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

}