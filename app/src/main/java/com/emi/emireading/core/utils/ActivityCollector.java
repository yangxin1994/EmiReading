package com.emi.emireading.core.utils;

import android.app.Activity;

import com.emi.emireading.core.log.LogUtil;

import java.util.LinkedList;
/**
 * @author :zhoujian
 * @description :
 * @company :翼迈科技
 * @date: 2017年12月25日下午 12:23
 * @Email: 971613168@qq.com
 */

public class ActivityCollector {
    private static final String TAG = "ActivityCollector";
	public static LinkedList<Activity> activities = new LinkedList<>();

    public static void addActivity(Activity activity) {
        activities.addLast(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static boolean isAnyActivityAlive() {
        return activities != null && activities.size() > 0;
    }

    public static void removeActivities() {
        while (!activities.isEmpty()) {
            Activity act = activities.removeLast();
            if (act != null) {
                act.finish();
            }
        }
    }

    public static Activity getTopActivity() {
        if (activities.size() == 0) {
            return null;
        }
        return activities.getLast();
    }

    public static void logActivities() {
        LogUtil.i(TAG, "=================  activities.size() = " + activities.size() + "  =================");
        for (int i = 0; i < activities.size(); i++) {
            LogUtil.i(TAG, (i + 1) + " : " + activities.get(i).getClass().getSimpleName());
        }
        LogUtil.i(TAG, "===================================================================================");
    }

    public static boolean isExist(String simplename){
        for (Activity activity : ActivityCollector.activities) {
            String activityName = activity.getClass().getSimpleName();
            if (activityName.equals(simplename)) {
                return true;
            }
        }
        return false;
    }


}
