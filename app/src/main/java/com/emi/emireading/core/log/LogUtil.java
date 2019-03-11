package com.emi.emireading.core.log;

import android.util.Log;

import com.emi.emireading.BuildConfig;


/**
 * @author :zhoujian
 * @description :Log打印类
 * @company :翼迈科技
 * @date: 2017年7月3日下午 01:08
 * @Email: 971613168@qq.com
 */
public class LogUtil {
    private static final String LOG_POSITION_FORMAT = "[(%1$s:%2$d)#%3$s]";
    private static final int LOG_MAX_LENGTH = 3 * 1024;
    /**
     * 是否需要打印bug，可以在application的onCreate函数里面初始化
     */
    private static boolean isDebug = BuildConfig.DEBUG;
    private static String TAG = "LogUtil";

    public static void i(String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.i(TAG, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.i(TAG, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.i(TAG, msg);
            }
        }

    }

    public static void d(String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.d(TAG, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.d(TAG, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.d(TAG, msg);
            }
        }
    }

    public static void w(String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.w(TAG, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.w(TAG, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.w(TAG, msg);
            }
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.e(TAG, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.e(TAG, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.e(TAG, msg);
            }
        }
    }

    public static void v(String msg) {
        if (isDebug) {
            v(TAG, msg);
        }
    }

    /**
     * 下面是传入自定义tag的函数
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.i(tag, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.i(tag, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.i(tag, msg);
            }
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.w(tag, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.w(tag, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.w(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg) {
        long length;
        if (isDebug) {
            if (msg == null) {
                length = 0;
            } else {
                length = msg.length();
            }
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.d(tag, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.d(tag, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.d(tag, msg);
            }
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.e(tag, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.e(tag, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.e(tag, msg);
            }
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            long length = msg.length();
            // 长度小于等于限制直接打印
            if (length <= LOG_MAX_LENGTH) {
                Log.v(tag, getLogPosition() + " " + msg);
            } else {
                // 循环分段打印日志
                while (msg.length() > LOG_MAX_LENGTH) {
                    String logContent = msg.substring(0, LOG_MAX_LENGTH);
                    msg = msg.replace(logContent, "");
                    Log.v(tag, getLogPosition() + " " + logContent);
                }
                // 打印剩余日志
                Log.v(tag, msg);
            }
        }
    }


    /**
     * 获取调用log的位置
     *
     * @return
     */
    private static String getLogPosition() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
        String caller = "<unknown>";
        if (trace != null && trace.length >= 3) {
            String methodName = trace[2].getMethodName();
            int lineNumber = trace[2].getLineNumber();
            String fileName = trace[2].getFileName();
            caller = String.format(LOG_POSITION_FORMAT, fileName, lineNumber, methodName);
        }
        return caller;
    }
}