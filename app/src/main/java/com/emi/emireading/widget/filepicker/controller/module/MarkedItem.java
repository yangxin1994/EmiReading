package com.emi.emireading.widget.filepicker.controller.module;

import java.util.HashMap;
import java.util.Set;

/**
 * @author :zhoujian
 * @description : 标记的条目实体
 * @company :翼迈科技
 * @date 2019年01月29日上午 10:51
 * @Email: 971613168@qq.com
 */

public class MarkedItem {

    private static HashMap<String, FileItem> ourInstance = new HashMap<>();

    public static void addSelectedItem(FileItem item) {
        ourInstance.put(item.getLocation(), item);
    }

    public static void removeSelectedItem(String key) {
        ourInstance.remove(key);
    }

    public static boolean hasItem(String key) {
        return ourInstance.containsKey(key);
    }

    public static void clearSelectionList() {
        ourInstance = new HashMap<>(10);
    }

    public static void addSingleFile(FileItem item) {
        ourInstance = new HashMap<>(10);
        ourInstance.put(item.getLocation(), item);
    }

    public static String[] getSelectedPaths() {
        Set<String> paths = ourInstance.keySet();
        String[] filePaths = new String[paths.size()];
        int i = 0;
        for (String path : paths) {
            filePaths[i++] = path;
        }
        return filePaths;
    }

    public static int getFileCount() {
        return ourInstance.size();
    }
}
