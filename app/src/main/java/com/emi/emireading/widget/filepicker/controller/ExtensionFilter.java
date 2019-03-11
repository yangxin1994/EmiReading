package com.emi.emireading.widget.filepicker.controller;

import com.emi.emireading.widget.filepicker.controller.module.BaseDialogConfig;
import com.emi.emireading.widget.filepicker.controller.module.DialogProperties;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

/**
 * @author :zhoujian
 * @description : 文件扩展名过滤器
 * @company :翼迈科技
 * @date 2019年01月29日上午 10:54
 * @Email: 971613168@qq.com
 */

public class ExtensionFilter implements FileFilter {
    private final String[] validExtensions;
    private DialogProperties properties;

    public ExtensionFilter(DialogProperties properties) {
        if (properties.extensions != null) {
            this.validExtensions = properties.extensions;
        } else {
            this.validExtensions = new String[]{""};
        }
        this.properties = properties;
    }

    /**
     * 基类过滤文件规则
     */
    @Override
    public boolean accept(File file) {
        //All directories are added in the least that can be read by the Application
        if (file.isDirectory() && file.canRead()) {
            return true;
        } else if (properties.selectType == BaseDialogConfig.SELECT_DIR) {   /*  True for files, If the selection type is Directory type, ie.
             *  Only directory has to be selected from the list, then all files are
             *  ignored.
             */
            return false;
        } else {   /*  Check whether name of the file ends with the extension. Added if it
             *  does.
             */
            String name = file.getName().toLowerCase(Locale.getDefault());
            for (String ext : validExtensions) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }
}
