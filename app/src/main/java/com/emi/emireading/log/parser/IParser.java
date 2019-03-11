package com.emi.emireading.log.parser;

import com.emi.emireading.log.constant.LogConstant;

import org.apache.poi.ss.formula.functions.T;

/**
 * @author :zhoujian
 * @description : 解析器接口
 * @company :翼迈科技
 * @date 2018年07月30日上午 11:56
 * @Email: 971613168@qq.com
 */

public interface IParser {
    String LINE_SEPARATOR = LogConstant.BR;

    /**
     * 解析类型
     * @return 返回类类型
     */
    Class<T> parseClassType();

    /**
     * 解析String
     *
     * @param t
     * @return
     */
    String parseString(T t);
}
