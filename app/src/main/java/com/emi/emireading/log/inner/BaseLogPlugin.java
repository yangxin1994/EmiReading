package com.emi.emireading.log.inner;

import android.text.TextUtils;
import android.util.Log;

import com.emi.emireading.log.EmiLog;
import com.emi.emireading.log.config.LogDefaultConfig;
import com.emi.emireading.log.constant.LogConstant;
import com.emi.emireading.log.constant.LogConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.MissingFormatArgumentException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author :zhoujian
 * @description : 日志基类插件
 * @company :翼迈科技
 * @date 2018年07月30日上午 11:19
 * @Email: 971613168@qq.com
 */

public abstract class BaseLogPlugin implements ILogPlugin {
    private LogDefaultConfig mLogConfig;
    private final ThreadLocal<String> localTags = new ThreadLocal<>();

    protected BaseLogPlugin() {
        mLogConfig = LogDefaultConfig.getInstance();
        //        mLogConfig.addParserClass(LogConstant.DEFAULT_PARSE_CLASS);
    }

    @Override
    public void wtf(String message, Object... args) {
        logString(Log.ASSERT, generateTag(), message, args);
    }

    @Override
    public void wtf(Object object) {
        logObject(Log.ASSERT, object);
    }

    @Override
    public void e(String message, Object... args) {
        logString(Log.ERROR, generateTag(), message, args);
    }

    @Override
    public void d(String tag, String message) {
        logString(Log.DEBUG, tag, message, "");
    }

    @Override
    public void w(String tag, String message) {
        logString(Log.WARN, tag, message, "");
    }

    @Override
    public void i(String tag, String message) {
        logString(Log.INFO, tag, message, "");
    }

    @Override
    public void e(String tag, String message) {
        logString(Log.ERROR, tag, message, "");
    }

    @Override
    public void e(Object object) {
        logObject(Log.ERROR, object);
    }

    @Override
    public void w(String message, Object... args) {
        logString(Log.WARN, generateTag(), message, args);
    }

    @Override
    public void w(Object object) {
        logObject(Log.WARN, object);
    }

    @Override
    public void d(String message, Object... args) {
        logString(Log.DEBUG, generateTag(), message, args);
    }

    @Override
    public void d(Object object) {
        logObject(Log.DEBUG, object);
    }

    @Override
    public void i(String message, Object... args) {
        logString(Log.INFO, generateTag(), message, args);
    }

    @Override
    public void i(Object object) {
        logObject(Log.INFO, object);
    }

    @Override
    public void v(String message, Object... args) {
        logString(Log.VERBOSE, generateTag(), message, args);
    }

    @Override
    public void v(Object object) {
        logObject(Log.VERBOSE, object);
    }

    @Override
    public void xml(String xml) {
        if (TextUtils.isEmpty(xml)) {
            w("XML{xml is empty}");
            return;
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            d(xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerException e) {
            e(e.toString() + "\n\nxml = " + xml);
        }
    }

    @Override
    public void json(String json) {
        int indent = 4;
        if (TextUtils.isEmpty(json)) {
            d("JSON{json is empty}");
            return;
        }
        try {
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String msg = jsonObject.toString(indent);
                d(msg);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String msg = jsonArray.toString(indent);
                d(msg);
            }
        } catch (JSONException e) {
            e(e.toString() + "\n\njson = " + json);
        }
    }

    private void logObject(int type, Object object) {
        logString(type, generateTag(), LogConvert.objectToString(object));
    }

    private synchronized void logString(int type, String tag, String msg, Object... args) {
        logString(type, tag, msg, false, args);
    }

    private void logString(int type, String tag, String msg, boolean isPart, Object... args) {
        //判定是否显示日志
        if (!mLogConfig.isLogEnable()) {
            return;
        }
        //判断日志显示最小级别
        if (type < mLogConfig.getLogLevel()) {
            return;
        }
        //        String tag = generateTag();
        //判断信息是否超过一行最大显示
        //超过一行
        if (msg.length() > LogConstant.LINE_MAX) {
            if (mLogConfig.isShowBorder()) {
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_TOP));
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_NORMAL) +
                        getTopStackInfo());
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_CENTER));
            }
            for (String subMsg : LogConvert.largeStringToList(msg)) {
                logString(type, tag, subMsg, true, args);
            }
            if (mLogConfig.isShowBorder()) {
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_BOTTOM));
            }
            return;
        }
        //有格式化参数
        if (args.length > 0) {
            try {
                msg = String.format(msg, args);
            } catch (MissingFormatArgumentException e) {
                e.printStackTrace();
            }
        }
        //判断是否显示排版线条
        //显示排版线条
        if (mLogConfig.isShowBorder()) {
            //判定是否需要分段显示
            //需要分段显示
            if (isPart) {
                for (String sub : msg.split(LogConstant.BR)) {
                    printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_NORMAL)
                            + sub);
                }
            } else {//不需要分段显示
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_TOP));
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_NORMAL) +
                        getTopStackInfo());
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_CENTER));
                for (String sub : msg.split(LogConstant.BR)) {
                    printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_NORMAL)
                            + sub);
                }
                printLog(type, tag, LogConvert.printDividingLine(LogConstant.DIVIDER_BOTTOM));
            }
        } else {//直接显示
            printLog(type, tag, msg);
        }
    }

    /**
     * 生成标签
     *
     * @return
     */
    private String generateTag() {
        String tempTag = localTags.get();
        if (!TextUtils.isEmpty(tempTag)) {
            localTags.remove();
            return tempTag;
        }
        return mLogConfig.getTagPrefix();
    }

    /**
     * 获取当前堆栈信息
     *
     * @return
     */
    private StackTraceElement getCurrentStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int stackOffset = getStackOffset(trace, EmiLog.class);
        if (stackOffset == -1) {
            return null;
        }
        return trace[stackOffset];
    }

    /**
     * 获取顶部堆栈信息
     *
     * @return
     */
    private String getTopStackInfo() {
        String customTag = mLogConfig.getFormatTag(getCurrentStackTrace());
        if (customTag != null) {
            return customTag;
        }
        StackTraceElement caller = getCurrentStackTrace();
        if (caller == null) {
            return "";
        }
        String stackTrace = caller.toString();
        stackTrace = stackTrace.substring(stackTrace.lastIndexOf('('), stackTrace.length());
        String tag = "%s.%s%s";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), stackTrace);
        return tag;
    }

    /**
     * 获取堆栈信息下标
     *
     * @param trace
     * @param cla
     * @return
     */
    private int getStackOffset(StackTraceElement[] trace, Class cla) {
        for (int i = LogConstant.MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (cla.equals(EmiLog.class) && i < trace.length - 1 && trace[i + 1].getClassName()
                    .equals(EmiLog.class.getName())) {
                continue;
            }
            if (name.equals(cla.getName())) {
                return ++i;
            }
        }
        return -1;
    }

    /**
     * 输出日志
     *
     * @param type
     * @param tag
     * @param msg
     */
    private void printLog(int type, String tag, String msg) {
        if (!mLogConfig.isShowBorder()) {
            msg = getTopStackInfo() + ": " + msg;
        }
        log(type, tag, msg);
    }

    /**
     * 日志具体实现方式，可以是打印、文件存储等
     *
     * @param type
     * @param tag
     * @param message
     */
    protected abstract void log(int type, String tag, String message);

    public ILogPlugin setTag(String tag) {
        if (!TextUtils.isEmpty(tag) && mLogConfig.isLogEnable()) {
            localTags.set(tag);
        }
        return this;
    }
}
