package com.emi.emireading.entities;

import java.util.List;

/**
 * @author :zhoujian
 * @description :含山对接相关实体
 * @company :翼迈科技
 * @date 2018年01月30日上午 09:23
 * @Email: 971613168@qq.com
 */

public class NetData {

    /**
     * RET : 100
     * MSG : 成功
     * data : [{"xlmc":"东大门南门面","sjid":"2014","usexlid":"-1","xlid":"2016"},{"xlmc":"东大门北门面","sjid":"2014","usexlid":"-1","xlid":"2017"},{"xlmc":"一单元","sjid":"1999","usexlid":"-1","xlid":"2018"},{"xlmc":"二单元","sjid":"1999","usexlid":"-1","xlid":"2019"},{"xlmc":"一单元","sjid":"2000","usexlid":"-1","xlid":"2020"},{"xlmc":"二单元","sjid":"2000","usexlid":"-1","xlid":"2021"},{"xlmc":"一单元","sjid":"2001","usexlid":"-1","xlid":"2022"}]
     */

    private String RET;
    private String MSG;
    private List<AreaInfo> data;

    public String getRET() {
        return RET;
    }

    public void setRET(String RET) {
        this.RET = RET;
    }

    public String getMSG() {
        return MSG;
    }

    public void setMSG(String MSG) {
        this.MSG = MSG;
    }

    public List<AreaInfo> getData() {
        return data;
    }

    public void setData(List<AreaInfo> data) {
        this.data = data;
    }


}
