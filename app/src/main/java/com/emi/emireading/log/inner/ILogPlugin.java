package com.emi.emireading.log.inner;

/**
 * @author :zhoujian
 * @description : 打印插件接口
 * @company :翼迈科技
 * @date 2018年07月30日上午 10:46
 * @Email: 971613168@qq.com
 */

public interface ILogPlugin {
    void wtf(String message, Object... args);

    void wtf(Object object);

    /**
     * 打印异常
     *
     * @param message
     * @param args
     */

    void e(String message, Object... args);

    /**
     * 打印异常
     *
     * @param tag
     * @param message
     */
    void d(String tag, String message);

    /**
     * 打印警告
     *
     * @param tag
     * @param message
     */
    void w(String tag, String message);


    /**
     * 打印info
     *
     * @param tag
     * @param message
     */
    void i(String tag, String message);


    /**
     * 打印异常
     *
     * @param tag
     * @param message
     */
    void e(String tag, String message);

    /**
     * 打印异常
     *
     * @param object
     */
    void e(Object object);

    /**
     * 打印警告
     *
     * @param message:过滤信息
     * @param args:要打印的参数
     */
    void w(String message, Object... args);

    /**
     * 打印警告
     *
     * @param object
     */
    void w(Object object);

    /**
     * 打印d
     *
     * @param message
     * @param args
     */
    void d(String message, Object... args);

    /**
     * 打印警告
     *
     * @param object
     */
    void d(Object object);

    /**
     * 打印info
     *
     * @param message
     * @param args
     */
    void i(String message, Object... args);

    /**
     * 打印警告
     *
     * @param object
     */
    void i(Object object);

    /**
     * 打印v
     *
     * @param message
     * @param args
     */
    void v(String message, Object... args);

    /**
     * 打印v
     *
     * @param object
     */
    void v(Object object);

    /**
     * 打印json
     *
     * @param json
     */
    void json(String json);

    /**
     * 打印xml
     *
     * @param xml
     */
    void xml(String xml);
}
