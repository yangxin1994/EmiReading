
package com.emi.emireading.widget.view.wheelview;

import android.os.Handler;
import android.os.Message;

/**
 * @author :zhoujian
 * @description : 自定义handler
 * @company :翼迈科技
 * @date: 2017年08月09日下午 02:07
 * @Email: 971613168@qq.com
 */
final class MessageHandler extends Handler {
    public static final int WHAT_INVALIDATE_LOOP_VIEW = 1000;
    public static final int WHAT_SMOOTH_SCROLL = 2000;
    public static final int WHAT_ITEM_SELECTED = 3000;

    final WheelView wheelView;

    MessageHandler(WheelView wheelView) {
        this.wheelView = wheelView;
    }

    @Override
    public final void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_INVALIDATE_LOOP_VIEW:
                wheelView.invalidate();
                break;

            case WHAT_SMOOTH_SCROLL:
                wheelView.smoothScroll(WheelView.ACTION.FLING);
                break;

            case WHAT_ITEM_SELECTED:
                wheelView.onItemSelected();
                break;
            default:
                break;
        }
    }

}
