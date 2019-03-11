package com.emi.emireading.widget.view.pickerview.listener;


import com.emi.emireading.widget.view.pickerview.TimePickerDialog;

/**
 * @author :zhoujian
 * @description :日期设置监听
 * @company :翼迈科技
 * @date: 2018年07月06日下午 01:17
 * @Email: 971613168@qq.com
 */
public interface OnDateSetListener {
    /**
     * 选择的时间
     *
     * @param timePickerView
     * @param millSeconds
     */
    void onDateSet(TimePickerDialog timePickerView, long millSeconds);
}
