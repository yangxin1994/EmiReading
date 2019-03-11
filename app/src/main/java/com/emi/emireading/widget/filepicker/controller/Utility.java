

package com.emi.emireading.widget.filepicker.controller;

import android.content.Context;
import android.content.pm.PackageManager;

import com.emi.emireading.widget.filepicker.controller.module.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author :zhoujian
 * @description : 权限请求类
 * @company :翼迈科技
 * @date 2019年01月25日下午 05:15
 * @Email: 971613168@qq.com
 */
public class Utility {
    /**
     * 存储权限检测
     *
     * @param context
     * @return
     */
    public static boolean checkStorageAccessPermissions(Context context) {
        //仅在6.0或 6.0以上执行
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String permission = "android.permission.READ_EXTERNAL_STORAGE";
            int res = context.checkCallingOrSelfPermission(permission);
            return (res == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    /**
     * 获取文件实体
     * @param internalList
     * @param inter
     * @param filter
     * @return
     */
    public static ArrayList<FileItem> prepareFileListEntries(ArrayList<FileItem> internalList, File inter, ExtensionFilter filter) {
        try {
            for (File name : inter.listFiles(filter)) {
                if (name.canRead()) {
                    FileItem item = new FileItem();
                    item.setFilename(name.getName());
                    item.setDirectory(name.isDirectory());
                    item.setLocation(name.getAbsolutePath());
                    item.setTime(name.lastModified());
                    //Add row to the List of directories/files
                    internalList.add(item);
                }
            }
           //排序
            Collections.sort(internalList);
        } catch (NullPointerException e) {
            e.printStackTrace();
            internalList = new ArrayList<>();
        }
        return internalList;
    }

    /**
     * 判断是否存在V7库
     * @return
     */
    private boolean hasSupportLibraryInClasspath() {
        try {
            Class.forName("com.android.support:appcompat-v7");
            return true;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
