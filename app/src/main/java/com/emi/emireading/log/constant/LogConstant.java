package com.emi.emireading.log.constant;

import com.emi.emireading.log.config.LogDefaultConfig;
import com.emi.emireading.log.parser.IParser;

import org.xml.sax.Parser;

import java.util.List;

/**
 * @author :zhoujian
 * @description : 日志常量
 * @company :翼迈科技
 * @date 2018年07月30日上午 11:35
 * @Email: 971613168@qq.com
 */

public class LogConstant {
    public static final String STRING_OBJECT_NULL = "Object[object is null]";

    // 每行最大日志长度
    public static final int LINE_MAX = 1024 * 3;

    // 解析属性最大层级
    public static final int MAX_CHILD_LEVEL = 2;

    public static final int MIN_STACK_OFFSET = 5;

    /**
     *  换行符
     */
    public static final String BR = System.getProperty("line.separator");

    /**
     *  分割线方位
     */
    public static final int DIVIDER_TOP = 1;
    public static final int DIVIDER_BOTTOM = 2;
    public static final int DIVIDER_CENTER = 4;
    public static final int DIVIDER_NORMAL = 3;

    /**
     * 默认支持解析库
     */
    public static final Class<? extends Parser>[] DEFAULT_PARSE_CLASS = new Class[]{
           /* BundleParse.class, IntentParse.class, CollectionParse.class,
            MapParse.class, ThrowableParse.class, ReferenceParse.class*/
    };

    /**
     * 获取默认解析类
     *
     * @return
     */
    public static List<IParser> getParsers() {
        return LogDefaultConfig.getInstance().getParseList();
    }
}
