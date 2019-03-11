package com.emi.emireading.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.calendar.EmiCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.emi.emireading.core.config.EmiConstants.TEN;

/**
 * @author :zhoujian
 * @description : 时间选择页面
 * @company :翼迈科技
 * @date 2018年07月05日下午 05:01
 * @Email: 971613168@qq.com
 */

public class TimePickerActivity extends BaseActivity implements View.OnClickListener {
    private EmiCalendarView calendarView;
    private TextView tvCurrentDate;
    private Button btnLastMonth;
    private Button btnNextMonth;
    private ArrayList<Integer> dateList = new ArrayList<>();
    private TitleView titleView;
    public static final String EXTRA_SELECT_DATE = "EXTRA_SELECT_DATE";
    public static final String EXTRA_SELECT_DATE_LIST = "EXTRA_SELECT_DATE_LIST";
    public static final String EXTRA_SELECT_COUNT = "EXTRA_SELECT_COUNT";
    public static final String EXTRA_BUNDLE_DATE = "EXTRA_BUNDLE_DATE";

    @Override
    protected int getContentLayout() {
        return R.layout.activity_time_picker;
    }

    @Override
    protected void initIntent() {
    }

    @Override
    protected void initUI() {
        calendarView = findViewById(R.id.calendarView);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnLastMonth = findViewById(R.id.btnLastMonth);
        titleView = findViewById(R.id.titleView);
        btnLastMonth.setOnClickListener(this);
        btnNextMonth.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        // 设置是否能够改变日期状态
        titleView.setRightIconText("确定");
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(dateList);
                StringBuilder stringBuilder = new StringBuilder("");
                for (Integer date : dateList) {
                    if (date < TEN) {
                        stringBuilder.append(EmiConstants.ZERO);
                    }
                    stringBuilder.append(date);
                }
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECT_COUNT, dateList.size());
                Bundle bundle = new Bundle();
                bundle.putSerializable(EXTRA_SELECT_DATE_LIST,dateList);
                if(dateList.isEmpty()){
                    intent.putExtra(EXTRA_SELECT_DATE, "");
                }
                LogUtil.w(TAG,"选择的日期个数："+dateList.size());
                for (int i = 0; i < dateList.size(); i++) {
                    LogUtil.w(TAG,"选择的日期："+dateList.get(i));
                }
                intent.putExtra(EXTRA_SELECT_DATE, stringBuilder.toString());
                intent.putExtra(EXTRA_BUNDLE_DATE,bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        calendarView.setChangeDateStatus(true);
        // 设置是否能够点击
        calendarView.setClickable(true);
        // 设置已选的日期
        calendarView.setSelectDate(initDate());
        // 指定显示的日期, 如当前月的下个月
        Calendar calendar = calendarView.getCalendar();
        calendar.add(Calendar.MONTH, 0);
        calendarView.setCalendar(calendar);
        // 设置字体
        calendarView.setTypeface(Typeface.DEFAULT);
        // 设置日期状态改变监听
        calendarView.setOnDateChangeListener(new EmiCalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull EmiCalendarView view, boolean select, int year, int month, int day) {
                StringBuilder stringBuilder = new StringBuilder("");
                stringBuilder.append(day);
                Log.e(TAG, "select: " + select);
                Log.e(TAG, "year: " + year);
                Log.e(TAG, "month,: " + (month + 1));
                Log.e(TAG, "day: " + day);
                if (select) {
                    dateList.add(day);
                } else {
                    dateList.remove((Integer) day);
                }
                LogUtil.d(TAG, "当前选中的个数：" + dateList.size());
            }

        });
        // 设置是否能够改变日期状态
        calendarView.setChangeDateStatus(true);
        // 设置是否能够点击
        calendarView.setClickable(true);
        // 设置日期点击监听
        calendarView.setOnDataClickListener(new EmiCalendarView.OnDataClickListener() {
            @Override
            public void onDataClick(@NonNull EmiCalendarView view, int year, int month, int day) {
                Log.e(TAG, "year: " + year);
                Log.e(TAG, "month,: " + (month + 1));
                Log.e(TAG, "day: " + day);
            }
        });
        setCurDate();
    }


    private List<String> initDate() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        SimpleDateFormat sdf = new SimpleDateFormat(calendarView.getDateFormatPattern(), Locale.CHINA);
        Date date = calendar.getTime();
        sdf.format(date);
        dates.add(sdf.format(calendar.getTime()));
        int time = getDay(date);
        LogUtil.d("添加的日期："+time);
        dateList.add(time);
        return dates;
    }

    public static int getDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public void next(View v) {
        calendarView.nextMonth();
        setCurDate();
    }

    public void last(View v) {
        calendarView.lastMonth();
        setCurDate();
    }

    private void setCurDate() {
        tvCurrentDate.setText(calendarView.getYear() + "年" + (calendarView.getMonth() + 1) + "月");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLastMonth:
                calendarView.lastMonth();
                setCurDate();
                break;
            case R.id.btnNextMonth:
                calendarView.nextMonth();
                setCurDate();
                break;
            default:
                break;
        }
    }
}
