package com.emi.emireading.widget.filepicker.controller;

/**
 * @author :zhoujian
 * @description : 选择监听接口
 * @company :翼迈科技
 * @date 2019年01月25日下午 05:11
 * @Email: 971613168@qq.com
 */

public interface DialogSelectListener {

    /**
     * 当文件或目录被选择时回调此方法
     *
     * @param files 被选中的文件数组
     */
    void onSelectFilePaths(String[] files);
}
