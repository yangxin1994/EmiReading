package com.emi.emireading.widget.filepicker.controller.module;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 文件选择对话框配置
 * @company :翼迈科技
 * @date 2019年01月25日下午 05:19
 * @Email: 971613168@qq.com
 */

public abstract class BaseDialogConfig {
    /**
     * 单选模式
     */
    public static final int SINGLE_MODE = 0;

    /**
     * 多选模式
     */
    public static final int MULTI_MODE = 1;

    /**
     * 只选文件
     */
    public static final int SELECT_FILE = 0;
    /**
     * 只选目录
     */
    public static final int SELECT_DIR = 1;

    /**
     * 选文件和目录
     */
    public static final int SELECT_FILE_AND_DIR = 2;

    /**
     * 文件分割符
     */
    private static final String DIRECTORY_SEPARATOR = File.separator;

    public static final String STORAGE_DIR = "mnt";

    /**
     * 默认根目录
     */
    public static final String DEFAULT_DIR = DIRECTORY_SEPARATOR + STORAGE_DIR;
}
