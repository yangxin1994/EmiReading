package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @author :zhoujian
 * @description : 保存的文件信息
 * @company :翼迈科技
 * @date 2018年07月26日上午 11:18
 * @Email: 971613168@qq.com
 */

public class SavedFileInfo extends LitePalSupport implements Serializable {
    public String getSavedFileName() {
        return savedFileName;
    }

    public SavedFileInfo() {
    }

    public SavedFileInfo(String savedFileName, String savedTime, String savedPath) {
        this.savedFileName = savedFileName;
        this.savedTime = savedTime;
        this.savedPath = savedPath;
    }

    public void setSavedFileName(String savedFileName) {
        this.savedFileName = savedFileName;
    }

    public String getSavedTime() {
        return savedTime;
    }

    public void setSavedTime(String savedTime) {
        this.savedTime = savedTime;
    }

    public String getSavedPath() {
        return savedPath;
    }

    public void setSavedPath(String savedPath) {
        this.savedPath = savedPath;
    }

    /**
     * 保存的文件名
     */
    public String savedFileName = "";
    /**
     * 保存的时间
     */
    public String savedTime = "";
    /**
     * 保存的文件路径
     */
    public String savedPath = "";
}
