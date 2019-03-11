
package com.emi.emireading.widget.view.pickerview.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

import com.emi.emireading.widget.view.pickerview.config.PickerConfig;


/**
 * @author :zhoujian
 * @description :滚轮控件适配接口
 * @company :翼迈科技
 * @date: 2018年07月06日下午 01:57
 * @Email: 971613168@qq.com
 */
public interface WheelViewAdapter {
    /**
     * 获取item数量
     *
     * @return the count of wheel items
     */
    int getItemsCount();

    /**
     * 获取item
     *
     * @param index
     * @param convertView
     * @param parent
     * @return
     */
    View getItem(int index, View convertView, ViewGroup parent);

    /**
     * 获取空的item
     *
     * @param convertView
     * @param parent
     * @return
     */
    View getEmptyItem(View convertView, ViewGroup parent);

    /**
     * 注册数据设置观察者
     *
     * @param observer
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * 取消注册
     *
     * @param observer
     */
    void unregisterDataSetObserver(DataSetObserver observer);

    /**
     * 获取配置实例
     *
     * @return
     */
    PickerConfig getConfig();

    /**
     * 设置配置
     *
     * @param config
     */
    void setConfig(PickerConfig config);

}
