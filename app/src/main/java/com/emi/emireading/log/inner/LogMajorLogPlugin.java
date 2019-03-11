package com.emi.emireading.log.inner;

/**
 * @author :zhoujian
 * @description : 主打印实现
 * @company :翼迈科技
 * @date 2018年07月30日下午 01:57
 * @Email: 971613168@qq.com
 */

public class LogMajorLogPlugin extends BaseLogPlugin {
    private volatile BaseLogPlugin[] basePluginAsArray = new BaseLogPlugin[0];

    @Override
    public void wtf(String message, Object... args) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].wtf(message, args);
        }
    }

    @Override
    public void wtf(Object object) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].wtf(object);
        }
    }

    @Override
    public void e(String message, Object... args) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].e(message, args);
        }
    }

    @Override
    public void e(Object object) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].e(object);
        }
    }

    @Override
    public void d(String tag, String message) {
        for (BaseLogPlugin logPlugin : basePluginAsArray) {
            logPlugin.d(tag,message);
        }
    }

    @Override
    public void w(String tag, String message) {
        for (BaseLogPlugin logPlugin : basePluginAsArray) {
            logPlugin.w(tag,message);
        }
    }

    @Override
    public void i(String tag, String message) {
        for (BaseLogPlugin logPlugin : basePluginAsArray) {
            logPlugin.i(tag,message);
        }
    }

    @Override
    public void e(String tag, String message) {
        for (BaseLogPlugin logPlugin : basePluginAsArray) {
            logPlugin.e(tag,message);
        }
    }

    @Override
    public void w(String message, Object... args) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].w(message, args);
        }
    }

    @Override
    public void w(Object object) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].w(object);
        }
    }

    @Override
    public void d(String message, Object... args) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].d(message, args);
        }
    }

    @Override
    public void d(Object object) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].d(object);
        }
    }

    @Override
    public void i(String message, Object... args) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].i(message, args);
        }
    }

    @Override
    public void i(Object object) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].i(object);
        }
    }

    @Override
    public void v(String message, Object... args) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].v(message, args);
        }
    }

    @Override
    public void v(Object object) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].v(object);
        }
    }

    @Override
    public void json(String json) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].json(json);
        }
    }

    @Override
    public void xml(String xml) {
        BaseLogPlugin[] forest = basePluginAsArray;
        for (int i = 0, count = forest.length; i < count; i++) {
            forest[i].xml(xml);
        }
    }

    @Override
    protected void log(int type, String tag, String message) {
        throw new AssertionError("缺少 override for log method.");
    }

    public BaseLogPlugin[] getPluginAsArray() {
        return basePluginAsArray;
    }

    public void setPluginsAsArray(BaseLogPlugin[] forestAsArray) {
        this.basePluginAsArray = forestAsArray;
    }

}
