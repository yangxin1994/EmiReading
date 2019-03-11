package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;

/**
 * @author :zhoujian
 * @description : dbf字段名实体类
 * @company :翼迈科技
 * @date: 2017年09月18日下午 08:12
 * @Email: 971613168@qq.com
 */

public class DbfDataContent extends LitePalSupport {
    private String fileName;
    private ArrayList<String> dbfFieldList;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<String> getDbfFieldList() {
        return dbfFieldList;
    }

    public void setDbfFieldList(ArrayList<String> dbfFieldList) {
        this.dbfFieldList = dbfFieldList;
    }
}
