package com.emi.emireading.log;

import com.emi.emireading.log.config.ILogConfig;
import com.emi.emireading.log.config.LogDefaultConfig;
import com.emi.emireading.log.inner.BaseLogPlugin;
import com.emi.emireading.log.inner.LogMajorLogPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * @author :zhoujian
 * @description : 日志工具
 * @company :翼迈科技
 * @date 2018年07月30日上午 10:08
 * @Email: 971613168@qq.com
 */

public class EmiLog {
    private static final List<BaseLogPlugin> PLUGIN_LIST = new ArrayList<>();
    /**
     * 主打印插件
     */
    private static final BaseLogPlugin LOG_PLUGIN = new LogMajorLogPlugin();
    /**
     * 默认配置
     */
    private static final LogDefaultConfig LOG_DEFAULT_CONFIG = LogDefaultConfig.getInstance();

    private EmiLog() {
        throw new AssertionError("No instances.");
    }

    public static void wtf(String message, Object... args) {
        LOG_PLUGIN.wtf(message, args);
    }

    public static void wtf(Object object) {
        LOG_PLUGIN.wtf(object);
    }

    public static void e(String message, Object... args) {
        LOG_PLUGIN.e(message, args);
    }

    public static void e(Object object) {
        LOG_PLUGIN.e(object);
    }

    public static void w(String message, Object... args) {
        LOG_PLUGIN.w(message, args);
    }

    public static void w(Object object) {
        LOG_PLUGIN.w(object);
    }

    public static void d(String message, Object... args) {
        LOG_PLUGIN.d(message, args);
    }

    public static void d(Object object) {
        LOG_PLUGIN.d(object);
    }

    public static void i(String message, Object... args) {
        LOG_PLUGIN.i(message, args);
    }

    public static void i(String tag, String message) {
        LOG_PLUGIN.i(tag, message);
    }

    public static void d(String tag, String message) {
        LOG_PLUGIN.d(tag, message);
    }

    public static void w(String tag, String message) {
        LOG_PLUGIN.w(tag, message);
    }

    public static void e(String tag, String message) {
        LOG_PLUGIN.e(tag, message);
    }

    public static void i(Object object) {
        LOG_PLUGIN.i(object);
    }

    public static void v(String message, Object... args) {
        LOG_PLUGIN.v(message, args);
    }

    public static void v(Object object) {
        LOG_PLUGIN.v(object);
    }

    public static void json(String json) {
        LOG_PLUGIN.json(json);
    }

    public static void xml(String xml) {
        LOG_PLUGIN.xml(xml);
    }

    public static BaseLogPlugin asPlugin() {
        return LOG_PLUGIN;
    }

    /**
     * 获取配置信息，可重新进行设置
     *
     * @return
     */
    public static ILogConfig getLogConfig() {
        return LOG_DEFAULT_CONFIG;
    }

    /**
     * 设置标签
     *
     * @param tag
     * @return
     */
    public static BaseLogPlugin setTag(String tag) {
        BaseLogPlugin[] forest = ((LogMajorLogPlugin) LOG_PLUGIN).getPluginAsArray();
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].setTag(tag);
        }
        return LOG_PLUGIN;
    }

    /**
     * 安装插件
     *
     * @param logPlugin
     */
    public static void install(BaseLogPlugin logPlugin) {
        if (logPlugin == null) {
            throw new NullPointerException("tree == null");
        }
        if (logPlugin == LOG_PLUGIN) {
            throw new IllegalArgumentException("Cannot plant Timber into itself.");
        }
        synchronized (PLUGIN_LIST) {
            PLUGIN_LIST.add(logPlugin);
            ((LogMajorLogPlugin) LOG_PLUGIN).setPluginsAsArray(PLUGIN_LIST.toArray(new BaseLogPlugin[PLUGIN_LIST.size()]));
        }
    }

    /**
     * 安装插件
     *
     * @param
     */
    public static void install(BaseLogPlugin... plugins) {
        if (plugins == null) {
            throw new NullPointerException("plugins == null");
        }
        for (BaseLogPlugin plugin : plugins) {
            if (plugin == null) {
                throw new NullPointerException("install contains null");
            }
            if (plugin == LOG_PLUGIN) {
                throw new IllegalArgumentException("Cannot install Timber into itself.");
            }
        }
        synchronized (PLUGIN_LIST) {
            Collections.addAll(PLUGIN_LIST, plugins);
            ((LogMajorLogPlugin) LOG_PLUGIN).setPluginsAsArray(PLUGIN_LIST.toArray(new BaseLogPlugin[PLUGIN_LIST.size()]));
        }
    }

    /**
     * 卸载插件
     *
     * @param plugin
     */
    public static void unInstall(BaseLogPlugin plugin) {
        synchronized (PLUGIN_LIST) {
            if (!PLUGIN_LIST.remove(plugin)) {
                throw new IllegalArgumentException("Cannot remove plugin which is not installed: " +
                        plugin);
            }
            ((LogMajorLogPlugin) LOG_PLUGIN).setPluginsAsArray(PLUGIN_LIST.toArray(new BaseLogPlugin[PLUGIN_LIST.size()]));
        }
    }

    /**
     * 卸载所以打印插件
     */
    public static void unInstallAll() {
        synchronized (PLUGIN_LIST) {
            PLUGIN_LIST.clear();
            ((LogMajorLogPlugin) LOG_PLUGIN).setPluginsAsArray(new BaseLogPlugin[0]);
        }
    }

    /**
     * 获取插件集合
     *
     * @return
     */
    public static List<BaseLogPlugin> getPluginList() {
        synchronized (PLUGIN_LIST) {
            return unmodifiableList(new ArrayList<>(PLUGIN_LIST));
        }
    }

    /**
     * 获取当前森林有几颗树
     *
     * @return
     */
    public static int pluginCount() {
        synchronized (PLUGIN_LIST) {
            return PLUGIN_LIST.size();
        }
    }
}
