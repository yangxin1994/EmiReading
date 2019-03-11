package com.emi.emireading.upgrade.core;

import android.content.Context;
import android.content.Intent;

/**
 * @author :zhoujian
 * @description : 版本检测
 * @company :翼迈科技
 * @date 2018年08月18日上午 11:11
 * @Email: 971613168@qq.com
 */

public class VersionChecker {
    public static VersionChecker getInstance() {
        return VersionCheckerHolder.checker;
    }

    private VersionChecker() {
    }

    private static class VersionCheckerHolder {
        private static  VersionChecker checker = new VersionChecker();
    }

    public void cancelAllTask(Context context) {
        Intent intent = new Intent(context, CheckVersionService.class);
        context.stopService(intent);
    }

    /**
     * use request version function
     * @return requestVersionBuilder
     */
    public RequestVersion requestVersion() {
        return new RequestVersion();
    }
}
