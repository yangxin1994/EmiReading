package com.emi.emireading.widget.filepicker.controller;

import com.emi.emireading.widget.view.EmiCheckBox;

/**
 * @author :zhoujian
 * @description : checkbox选择监听
 * @company :翼迈科技
 * @date 2019年01月29日上午 11:45
 * @Email: 971613168@qq.com
 */

public interface OnCheckedChangeListener {

    /**
     * 选择变化时回调该方法
     *
     * @param checkbox  复选框
     * @param isChecked 是否选中
     */
    void onCheckedChanged(EmiCheckBox checkbox, boolean isChecked);
}
