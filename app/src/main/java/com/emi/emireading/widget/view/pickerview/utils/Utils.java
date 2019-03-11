package com.emi.emireading.widget.view.pickerview.utils;

import android.view.View;

import com.emi.emireading.widget.view.pickerview.data.WheelCalendar;


/**
 * @author :zhoujian
 * @description :日历相关工具类
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:17
 * @Email: 971613168@qq.com
 */
public class Utils {

    public static boolean isTimeEquals(WheelCalendar calendar, int... params) {
        switch (params.length) {
            case 1:
                return calendar.year == params[0];
            case 2:
                return calendar.year == params[0] &&
                        calendar.month == params[1];
            case 3:
                return calendar.year == params[0] &&
                        calendar.month == params[1] &&
                        calendar.day == params[2];
            case 4:
                return calendar.year == params[0] &&
                        calendar.month == params[1] &&
                        calendar.day == params[2] &&
                        calendar.hour == params[3];
            default:
                break;
        }
        return false;
    }

    public static void hideViews(View... views) {
        for (int i = 0; i < views.length; i++) {
            views[i].setVisibility(View.GONE);
        }
    }
}
