package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

/**
 * @author :zhoujian
 * @description : dbf数据实体类
 * @company :翼迈科技
 * @date: 2017年09月18日下午 08:24
 * @Email: 971613168@qq.com
 */

public class DbfField extends LitePalSupport {
    /**
     * 字段名
     */
    private String fileName;
    private String fieldName;
    private byte fieldType;
    private int fieldLength;

    public Object[] getRowValues() {
        return rowValues;
    }

    public void setRowValues(Object[] rowValues) {
        this.rowValues = rowValues;
    }

    private Object[] rowValues;

    public byte getFieldType() {
        return fieldType;
    }

    public void setFieldType(byte fieldType) {
        this.fieldType = fieldType;
    }

    public int getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(int fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public DbfField() {

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
