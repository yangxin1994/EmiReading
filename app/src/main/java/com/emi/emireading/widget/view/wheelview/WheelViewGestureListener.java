package com.emi.emireading.widget.view.wheelview;

import android.view.MotionEvent;

/**
 * @description : 滚轮手势监听
 * @author :zhoujian
 * @company :翼迈科技
 * @date: 2017年08月09日下午 02:18
 * @Email: 971613168@qq.com
 */

public final class WheelViewGestureListener extends android.view.GestureDetector.SimpleOnGestureListener{

    final WheelView wheelView;

    WheelViewGestureListener(WheelView wheelView) {
        this.wheelView = wheelView;
    }

    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        wheelView.scrollBy(velocityY);
        return true;
    }
}
