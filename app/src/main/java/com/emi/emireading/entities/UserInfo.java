package com.emi.emireading.entities;

import com.emi.emireading.core.config.EmiConstants;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

import static com.emi.emireading.core.config.EmiConstants.STATE_NOT_UPLOAD;

/**
 * 描述:
 * Created by chx on 2017/3/13.
 * 邮箱:snake_chenhx@163.com
 */

public class UserInfo extends LitePalSupport implements Serializable, Cloneable {
    /**
     * 该属性只针对合肥
     */
    public String dirname = "";
    /**
     * 用水id,一般与用户编号相同
     */
    public String waterId = "";

    public String getDirname() {
        return dirname;
    }

    public void setDirname(String dirname) {
        this.dirname = dirname;
    }

    public String getWaterId() {
        return waterId;
    }

    public void setWaterId(String waterId) {
        this.waterId = waterId;
    }


    public String filename = "";
    public String accountnum = "";
    public String meteraddr = "";
    public int curdata;
    public int lastdata;
    public int curyl;
    public int readmonth;
    public String useraddr = "";
    public int hasExport;
    /**
     * 加载策略json
     */
    public String loadStrategyJson = "";
    /**
     * 水表读数是否异常 1 没有异常 2 数据有异常  3 未抄到读数 4 补录
     */
    public int state;
    /**
     * 用户名
     */
    public String username = "";
    //上次用水量
    public int lastyl;
    public String curreaddate;
    /**
     * 已读通道号，为空则表示未读
     */
    public String channel;
    /**
     * 补抄标记位
     */
    public int rereadflag;

    public String getFirmCode() {
        return firmCode;
    }

    public void setFirmCode(String firmCode) {
        this.firmCode = firmCode;
    }

    public String firmCode = EmiConstants.FIRM_CODE_7833;
    public int data;
    public String fileType;
    public String channelAddress = "";

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 表册文件文件路径
     */
    public String filePath;
    /**
     * 标记用户的水表状态
     */
    public int meterTag;

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    /**
     * 全部通道号(默认：2147483647)
     */
    public String channelNumber = String.valueOf(Integer.MAX_VALUE);

    public String getChannelAddress() {
        return channelAddress;
    }

    public void setChannelAddress(String channelAddress) {
        this.channelAddress = channelAddress;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }

    public int uploadState = STATE_NOT_UPLOAD;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAccountnum() {
        return accountnum;
    }

    public void setAccountnum(String accountnum) {
        this.accountnum = accountnum;
    }

    public String getMeteraddr() {
        return meteraddr;
    }

    public void setMeteraddr(String meteraddr) {
        this.meteraddr = meteraddr;
    }

    public int getCurdata() {
        return curdata;
    }

    public void setCurdata(int curdata) {
        this.curdata = curdata;
    }

    public int getLastdata() {
        return lastdata;
    }

    public void setLastdata(int lastdata) {
        this.lastdata = lastdata;
    }

    public int getCuryl() {
        return curyl;
    }

    public void setCuryl(int curyl) {
        this.curyl = curyl;
    }

    public int getReadmonth() {
        return readmonth;
    }

    public void setReadmonth(int readmonth) {
        this.readmonth = readmonth;
    }

    public String getUseraddr() {
        return useraddr;
    }

    public void setUseraddr(String useraddr) {
        this.useraddr = useraddr;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLastyl() {
        return lastyl;
    }

    public void setLastyl(int lastyl) {
        this.lastyl = lastyl;
    }

    public String getCurreaddate() {
        return curreaddate;
    }

    public void setCurreaddate(String curreaddate) {
        this.curreaddate = curreaddate;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getRereadflag() {
        return rereadflag;
    }

    public void setRereadflag(int rereadflag) {
        this.rereadflag = rereadflag;
    }


    /**
     * 重写equals方法，用于判断连个UserInfo对象是否相同
     *
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        UserInfo userInfo = (UserInfo) obj;
        return this.filename.equals(userInfo.filename) && this.accountnum.equals(userInfo.accountnum) && this.meteraddr.equals(userInfo.meteraddr) && this.channelNumber.equals(userInfo.channelNumber);
    }

    public UserInfo copy() {
        UserInfo copyUserInfo = null;
        try {
            copyUserInfo = (UserInfo) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return copyUserInfo;
    }
}
