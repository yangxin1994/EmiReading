package com.emi.emireading.entities;

/**
 * @author :zhoujian
 * @description : 翼迈网络实体类
 * @company :翼迈科技
 * @date 2018年04月26日上午 11:36
 * @Email: 971613168@qq.com
 */

public class EmiNetData {
    /**
     * channelNumber
     */
    private String coCode;

    public String getCoCode() {
        return coCode;
    }

    public void setCoCode(String coCode) {
        this.coCode = coCode;
    }

    public String getReadDate() {
        return readDate;
    }

    public void setReadDate(String readDate) {
        this.readDate = readDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    private String userNo = "";
    private String wmCode = "";
    private String readDate = "";
    private String data = "";

    public EmiNetData() {
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getWmCode() {
        return wmCode;
    }

    public void setWmCode(String wmCode) {
        this.wmCode = wmCode;
    }
}
