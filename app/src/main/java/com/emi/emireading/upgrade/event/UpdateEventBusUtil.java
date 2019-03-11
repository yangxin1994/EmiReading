package com.emi.emireading.upgrade.event;

import org.greenrobot.eventbus.EventBus;

/**
 * @author :zhoujian
 * @description : 版本更新事件
 * @company :翼迈科技
 * @date :2018/8/18
 * @Email: 971613168@qq.com
 */
public class UpdateEventBusUtil {

    public static void sendEvent(int eventType) {
        CommonEvent commonEvent = new CommonEvent();
        commonEvent.setSuccessful(true);
        commonEvent.setEventType(eventType);
        EventBus.getDefault().post(commonEvent);
    }
}
