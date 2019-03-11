package com.emi.emireading.widget.view.pickerview.wheel;


/**
 * @author :zhoujian
 * @description :滚轮改变监听
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:19
 * @Email: 971613168@qq.com
 */
public interface OnWheelChangedListener {
    /**
     * Callback method to be invoked when current item changed
     *
     * @param wheel    the wheel view whose state has changed
     * @param oldValue the old value of current item
     * @param newValue the new value of current item
     */
    void onChanged(WheelView wheel, int oldValue, int newValue);
}
