package com.emi.emireading.entities;

import org.litepal.LitePal;

/**
 * @author :zhoujian
 * @description : 含山小区数据
 * @company :翼迈科技
 * @date 2018年01月30日上午 10:15
 * @Email: 971613168@qq.com
 */

public class AreaInfo extends LitePal{
    private String xlmc;
    private int sjid;
    private int usexlid;
    private int xlid;
    private String readDate;

    public String getReadDate() {
        return readDate;
    }

    public void setReadDate(String readDate) {
        this.readDate = readDate;
    }

    /**
     * 抄表员id
     */
    private String readerId;

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public String getXlmc() {
        return xlmc;
    }

    public void setXlmc(String xlmc) {
        this.xlmc = xlmc;
    }

    public int getSjid() {
        return sjid;
    }

    public void setSjid(int sjid) {
        this.sjid = sjid;
    }

    public int getUsexlid() {
        return usexlid;
    }

    public void setUsexlid(int usexlid) {
        this.usexlid = usexlid;
    }

    public int getXlid() {
        return xlid;
    }

    public void setXlid(int xlid) {
        this.xlid = xlid;
    }

    public AreaInfo() {

    }


    @Override
    public boolean equals(Object obj) {
        AreaInfo dataBean = (AreaInfo) obj;
        return dataBean.sjid == this.sjid && dataBean.xlmc.equals(this.xlmc)&& dataBean.xlid== this.xlid;
    }
}
