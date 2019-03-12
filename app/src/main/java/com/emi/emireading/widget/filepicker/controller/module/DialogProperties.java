package com.emi.emireading.widget.filepicker.controller.module;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 文件选择对话框属性类
 * @company :翼迈科技
 * @date 2019年01月28日上午 11:06
 * @Email: 971613168@qq.com
 */

public class DialogProperties {
    /**
     * 选择模式
     */
    public int selectMode;

    public int selectType;
    public File root;

    public File errorDir;

    public File offset;

    /**
     * 扩展名
     */
    public String[] extensions;


    public DialogProperties() {
        selectMode = BaseDialogConfig.SINGLE_MODE;
        selectType = BaseDialogConfig.SELECT_FILE;
        root = new File(BaseDialogConfig.DEFAULT_DIR);
        errorDir = new File(BaseDialogConfig.DEFAULT_DIR);
        offset = new File(BaseDialogConfig.DEFAULT_DIR);
        extensions = null;
    }



}
