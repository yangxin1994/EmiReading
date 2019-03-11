package com.emi.emireading.widget.view.pickerview.data.source;


import com.emi.emireading.widget.view.pickerview.data.WheelCalendar;

/**
 * @author :zhoujian
 * @description : 时间获取接口
 * @company :翼迈科技
 * @date 2018年04月25日下午 02:30
 * @Email: 971613168@qq.com
 */
public interface TimeDataSource {

    int getMinYear();

    int getMaxYear();

    int getMinMonth(int currentYear);

    int getMaxMonth(int currentYear);

    int getMinDay(int year, int month);

    int getMaxDay(int year, int month);

    int getMinHour(int year, int month, int day);

    int getMaxHour(int year, int month, int day);

    int getMinMinute(int year, int month, int day, int hour);

    int getMaxMinute(int year, int month, int day, int hour);

    boolean isMinYear(int year);

    boolean isMinMonth(int year, int month);

    boolean isMinDay(int year, int month, int day);

    boolean isMinHour(int year, int month, int day, int hour);
//
//    boolean isMaxYear(int year);
//
//    boolean isMaxMonth(int year, int month);
//
//    boolean isMaxDay(int year, int month, int day);
//
//    boolean isMaxMinute(int year, int month, int day, int hour);

    WheelCalendar getDefaultCalendar();

}
