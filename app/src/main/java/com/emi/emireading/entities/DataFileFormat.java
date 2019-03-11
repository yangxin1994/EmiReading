package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;

/**
 * @author :zhoujian
 * @description : 文件格式
 * @company :翼迈科技
 * @date 2018年02月23日上午 11:32
 * @Email: 971613168@qq.com
 */

public class DataFileFormat extends LitePalSupport {
    private ArrayList<String> tableNameList;
    private String filePath;
    private ArrayList<Integer> cellTypeList;

    public ArrayList<Integer> getCellTypeList() {
        return cellTypeList;
    }

    public void setCellTypeList(ArrayList<Integer> cellTypeList) {
        this.cellTypeList = cellTypeList;
    }

    public ArrayList<String> getTableNameList() {
        return tableNameList;
    }

    public void setTableNameList(ArrayList<String> tableNameList) {
        this.tableNameList = tableNameList;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public DataFileFormat() {

    }
}
