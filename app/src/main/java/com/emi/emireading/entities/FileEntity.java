package com.emi.emireading.entities;


/**
 * @author :zhoujian
 * @description : 文件实体类
 * @company :翼迈科技
 * @date: 2017年08月08日上午 11:19
 * @Email: 971613168@qq.com
 */

public class FileEntity {
    public String fileName;
    public String filePath;
    public String fileSuffix;
    public FileEntity() {
    }

    public FileEntity(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
