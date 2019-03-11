package com.emi.emireading.core.utils;

import android.widget.Toast;

import com.emi.emireading.EmiReadingApplication;

/**
 * @author :zhoujian
 * @description : 吐司封装（避免短期内弹出相同吐司）
 * @company :翼迈科技
 * @date: 2017年7月03日下午 02:18
 * @Email: 971613168@qq.com
 */

public class ToastUtil {
    /**
     * 时间间隔
     */
    private static final int TIME_INTERVAL = 2000;
    private static String oldMsg;
    private static long time;

    /**
     * 短吐司
     *
     * @param msg
     */
    public static void showShortToast(String msg) {
        // 当显示的内容不一样时，即断定为不是同一个Toast
        if (!msg.equals(oldMsg)) {
            Toast.makeText(EmiReadingApplication.getAppContext(), msg, Toast.LENGTH_SHORT).show();
            time = System.currentTimeMillis();
        } else {
            // 显示内容一样时，只有间隔时间大于2秒时才显示
            if (System.currentTimeMillis() - time > TIME_INTERVAL) {
                Toast.makeText(EmiReadingApplication.getAppContext(), msg, Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            }
        }
        oldMsg = msg;
    }

    /**
     * 长吐司
     *
     * @param msg
     */
    public static void showLongToast(String msg) {
        // 当显示的内容不一样时，即断定为不是同一个Toast
        if (!msg.equals(oldMsg)) {
            Toast.makeText(EmiReadingApplication.getAppContext(), msg, Toast.LENGTH_LONG).show();
            time = System.currentTimeMillis();
        } else {
            // 显示内容一样时，只有间隔时间大于2秒时才显示
            if (System.currentTimeMillis() - time > TIME_INTERVAL) {
                Toast.makeText(EmiReadingApplication.getAppContext(), msg, Toast.LENGTH_LONG).show();
                time = System.currentTimeMillis();
            }
        }
        oldMsg = msg;
    }
}
