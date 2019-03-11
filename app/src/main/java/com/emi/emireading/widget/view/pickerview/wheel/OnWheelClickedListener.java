package com.emi.emireading.widget.view.pickerview.wheel;

/**
 * @author :zhoujian
 * @description :滚轮点击监听
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:19
 * @Email: 971613168@qq.com
 */
public interface OnWheelClickedListener {
    /**
     * Callback method to be invoked when current item clicked
     *
     * @param wheel     the wheel view
     * @param itemIndex the index of clicked item
     */
    void onItemClicked(WheelView wheel, int itemIndex);
}
