package com.emi.emireading.widget.view.wheelview;

/**
 * @author :zhoujian
 * @description : 惯性效果
 * @company :翼迈科技
 * @date: 2017年08月09日下午 02:07
 * @Email: 971613168@qq.com
 */

public class InertiaTimerTask  implements Runnable {

    float a;
    final float velocityY;
    final WheelView wheelView;

    InertiaTimerTask(WheelView wheelView, float velocityY) {
        super();
        this.wheelView = wheelView;
        this.velocityY = velocityY;
        a = Integer.MAX_VALUE;
    }

    @Override
    public final void run() {
        if (a == Integer.MAX_VALUE) {
            if (Math.abs(velocityY) > 2000F) {
                if (velocityY > 0.0F) {
                    a = 2000F;
                } else {
                    a = -2000F;
                }
            } else {
                a = velocityY;
            }
        }
        if (Math.abs(a) >= 0.0F && Math.abs(a) <= 20F) {
            wheelView.cancelFuture();
            wheelView.handler.sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL);
            return;
        }
        int i = (int) ((a * 10F) / 1000F);
        WheelView wheelView = this.wheelView;
        wheelView.totalScrollY = wheelView.totalScrollY - i;
        if (!wheelView.isLoop) {
            float itemHeight = wheelView.lineSpacingMultiplier * wheelView.maxTextHeight;
            if (wheelView.totalScrollY <= (int) ((float) (-wheelView.initPosition) * itemHeight)) {
                a = 40F;
                wheelView.totalScrollY = (int) ((float) (-wheelView.initPosition) * itemHeight);
            } else if (wheelView.totalScrollY >= (int) ((float) (wheelView.items.size() - 1 - wheelView.initPosition) * itemHeight)) {
                wheelView.totalScrollY = (int) ((float) (wheelView.items.size() - 1 - wheelView.initPosition) * itemHeight);
                a = -40F;
            }
        }
        if (a < 0.0F) {
            a = a + 20F;
        } else {
            a = a - 20F;
        }
        wheelView.handler.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
    }
}
