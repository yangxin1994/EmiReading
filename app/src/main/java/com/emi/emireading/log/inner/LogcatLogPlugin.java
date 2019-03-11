package com.emi.emireading.log.inner;

import android.util.Log;

/**
 * @author :zhoujian
 * @description : logcat打印插件
 * @company :翼迈科技
 * @date 2018年07月30日下午 02:07
 * @Email: 971613168@qq.com
 */

public class LogcatLogPlugin extends BaseLogPlugin {

    @Override
    protected void log(int type, String tag, String message) {
        switch (type) {
            case Log.VERBOSE:
                Log.v(tag, message);
                break;
            case Log.INFO:
                Log.i(tag, message);
                break;
            case Log.DEBUG:
                Log.d(tag, message);
                break;
            case Log.WARN:
                Log.w(tag, message);
                break;
            case Log.ERROR:
                Log.e(tag, message);
                break;
            case Log.ASSERT:
                Log.wtf(tag, message);
                break;
            default:
                break;
        }
    }


}
