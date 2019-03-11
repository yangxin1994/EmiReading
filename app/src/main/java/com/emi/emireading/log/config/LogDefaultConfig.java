package com.emi.emireading.log.config;

import android.text.TextUtils;
import android.util.Log;

import com.emi.emireading.log.constant.LogPattern;
import com.emi.emireading.log.parser.IParser;

import java.util.List;

/**
 * @author :zhoujian
 * @description : 打印格式默认配置
 * @company :翼迈科技
 * @date 2018年07月30日上午 10:27
 * @Email: 971613168@qq.com
 */

public class LogDefaultConfig implements ILogConfig {
    private boolean logEnable;
    private String prefix;
    private String tagPrefix;
    private boolean showBorder = false;
    private String formatTag;
    private int logLevel = Log.VERBOSE;
    private List<IParser> parseList;
    public boolean isLogEnable() {
        return logEnable;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getTagPrefix() {
        if (TextUtils.isEmpty(tagPrefix)) {
            return "EmiLog";
        }
        return tagPrefix;
    }

    public boolean isShowBorder() {
        return showBorder;
    }

    public int getLogLevel() {
        return logLevel;
    }

    private LogDefaultConfig() {

    }

    public static LogDefaultConfig getInstance() {
        return LogConfigDefaultHolder.INSTANCE;
    }

    private static class LogConfigDefaultHolder {
        private final static LogDefaultConfig INSTANCE = new LogDefaultConfig();
    }


    @Override
    public ILogConfig configAllowLog(boolean allowLog) {
        logEnable = allowLog;
        return this;
    }

    @Override
    public ILogConfig configTagPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public ILogConfig configFormatTag(String formatTag) {
        this.formatTag = formatTag;
        return this;
    }

    @Override
    public ILogConfig configShowBorders(boolean showBorder) {
        this.showBorder = showBorder;
        return this;
    }

    @Override
    public ILogConfig configLevel(int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public List<IParser> getParseList() {
        return parseList;
    }

    public String getFormatTag(StackTraceElement caller) {
        if (TextUtils.isEmpty(formatTag) || caller == null) {
            return null;
        }
        LogPattern logPattern = LogPattern.compile(formatTag);
        if (logPattern != null) {
            return logPattern.apply(caller);
        } else {
            return null;
        }
    }


}
