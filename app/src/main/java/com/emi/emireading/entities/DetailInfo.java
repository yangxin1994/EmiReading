package com.emi.emireading.entities;

import com.emi.emireading.R;

/**
 * @author :zhoujian
 * @description : 详细信息
 * @company :翼迈科技
 * @date 2018年03月09日下午 04:51
 * @Email: 971613168@qq.com
 */

public class DetailInfo {
    private String label;
    private String value;
    /**
     * label字体颜色
     */
    private int labelColor;

    public int getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(int labelColor) {
        this.labelColor = labelColor;
    }

    /**
     * value字体颜色
     */
    private int valueColor;

    public String getLabel() {
        return label;
    }

    public void setLabelName(String labelName) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DetailInfo(String label, String value) {
        this.valueColor = R.color.text_black;
        this.labelColor = R.color.text_black;
        this.label = label;
        this.value = value;
    }

    public DetailInfo(String label, String value, int valueColor) {
        this.valueColor = valueColor;
        this.labelColor = R.color.text_black;
        this.label = label;
        this.value = value;
    }

    public int getValueColor() {
        return valueColor;
    }

    public void setValueColor(int color) {
        this.valueColor = color;
    }
}
