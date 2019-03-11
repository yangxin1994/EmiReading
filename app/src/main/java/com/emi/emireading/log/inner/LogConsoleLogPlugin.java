package com.emi.emireading.log.inner;

/**
 * @author :zhoujian
 * @description : 控制台打印
 * @company :翼迈科技
 * @date 2018年07月30日下午 02:09
 * @Email: 971613168@qq.com
 */

public class LogConsoleLogPlugin extends BaseLogPlugin {
    @Override
    protected void log(int type, String tag, String message) {
        System.out.println(tag + "\t" + message);
    }
}
