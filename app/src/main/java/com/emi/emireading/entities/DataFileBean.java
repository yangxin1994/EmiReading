package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;

/**
 * @author :zhoujian
 * @description : 文件实体类
 * @company :翼迈科技
 * @date 2018年02月23日上午 10:53
 * @Email: 971613168@qq.com
 */

public class DataFileBean extends LitePalSupport {
    private String filePath;
    private ArrayList<Integer> cellTypeList;
    private String fileName;
    private ArrayList<String> cellValueList;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public ArrayList<Integer> getCellTypeList() {
        return cellTypeList;
    }

    public void setCellTypeList(ArrayList<Integer> cellTypeList) {
        this.cellTypeList = cellTypeList;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ArrayList<String> getCellValueList() {
        return cellValueList;
    }

    public void setCellValueList(ArrayList<String> cellValueList) {
        this.cellValueList = cellValueList;
    }


    public DataFileBean() {

    }


}
