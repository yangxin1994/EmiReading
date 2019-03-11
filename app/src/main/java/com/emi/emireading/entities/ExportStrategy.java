package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @author :zhoujian
 * @description : 导出策略实体类
 * @company :翼迈科技
 * @date 2018年03月22日上午 10:43
 * @Email: 971613168@qq.com
 */

public class ExportStrategy extends LitePalSupport implements Serializable {


    public String cityName = "";
    public String exportFormatJson;
    public String tableNameJson;
    public String splitChar;
    public String exportFileType;


    public String getCityName() {
        return cityName;
    }


    public String getExportFileType() {
        return exportFileType;
    }

    public void setExportFileType(String exportFileType) {
        this.exportFileType = exportFileType;
    }


    public void setCityName(String cityName) {
        this.cityName = cityName;
    }


    public String getExportFormateJson() {
        return exportFormatJson;
    }

    public void setExportFormatJson(String exportFormatJson) {
        this.exportFormatJson = exportFormatJson;
    }

    public String getTableNameJson() {
        return tableNameJson;
    }

    public void setTableNameJson(String tableNameJson) {
        this.tableNameJson = tableNameJson;
    }


    public String getSplitChar() {
        return splitChar;
    }

    public void setSplitChar(String splitChar) {
        this.splitChar = splitChar;
    }

    public ExportStrategy() {


    }


    public static class ExportFormatJsonBean {
    }
}
