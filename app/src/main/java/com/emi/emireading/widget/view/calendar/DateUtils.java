package com.emi.emireading.widget.view.calendar;

import java.util.Calendar;

/**
 * @author :zhoujian
 * @description : 日期工具类
 * @company :翼迈科技
 * @date 2018年07月05日下午 01:36
 * @Email: 971613168@qq.com
 */
public class DateUtils {

    private DateUtils() {
        // 工具类, 禁止实例化
        throw new AssertionError();
    }

    /**
     * 通过指定的年份和月份获取当月有多少天.
     *
     * @param year  年.
     * @param month 月.
     * @return 天数.
     */
    public static int getMonthDays(int year, int month) {
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                boolean lubricant = ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
                if (lubricant) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                return -1;
        }
    }

    /**
     * 获取指定年月的 1 号位于周几.
     *
     * @param year  年.
     * @param month 月.
     * @return 周.
     */
    public static int getFirstDayWeek(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 0);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

}
