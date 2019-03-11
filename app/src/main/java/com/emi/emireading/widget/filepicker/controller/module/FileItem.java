package com.emi.emireading.widget.filepicker.controller.module;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * @author :zhoujian
 * @description : 文件选择实体类
 * @company :翼迈科技
 * @date 2019年01月29日上午 10:44
 * @Email: 971613168@qq.com
 */

public class FileItem implements Comparable<FileItem> {
    /**
     * 文件名
     */
    private String filename;
    /**
     * 所在位置
     */
    private String location;
    /**
     * 是否是目录
     */
    private boolean directory;
    /**
     * 是否标记
     */
    private boolean marked;
    /**
     * 修改日期
     */
    private long time;

    @Override
    public int compareTo(@NonNull FileItem fileListItem) {
        //If the comparison is between two directories, return the directory with
        if (fileListItem.isDirectory() && isDirectory()) {
            //alphabetic order first.
            return filename.toLowerCase().compareTo(fileListItem.getFilename().toLowerCase(Locale.getDefault()));
            //If the comparison is not between two directories, return the file with
        } else if (!fileListItem.isDirectory() && !isDirectory()) {
            //alphabetic order first.
            return filename.toLowerCase().compareTo(fileListItem.getFilename().toLowerCase(Locale.getDefault()));
            //If the comparison is between a directory and a file, return the directory.
        } else if (fileListItem.isDirectory() && !isDirectory()) {
            return 1;
        } else {
            //Same as above but order of occurence is different.
            return -1;
        }
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}
