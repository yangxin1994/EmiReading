

package com.emi.emireading.widget.view.pickerview.wheel;


/**
 * @author :zhoujian
 * @description :滚轮滚动监听
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:19
 * @Email: 971613168@qq.com
 */
public interface OnWheelScrollListener {
    /**
     * Callback method to be invoked when scrolling started.
     *
     * @param wheel the wheel view whose state has changed.
     */
    void onScrollingStarted(WheelView wheel);

    /**
     * Callback method to be invoked when scrolling ended.
     *
     * @param wheel the wheel view whose state has changed.
     */
    void onScrollingFinished(WheelView wheel);
}
