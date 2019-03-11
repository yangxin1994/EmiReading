package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @author :zhoujian
 * @description : 加载策略实体类
 * @company :翼迈科技
 * @date: 2017年12月26日上午 09:22
 * @Email: 971613168@qq.com
 */

public class LoadStrategy extends LitePalSupport implements Serializable {
    /**
     * 城市
     */
    private String city;
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 字段数目
     */
    private int fieldCount;
    /**
     * 是否支持数据上传
     */
    public boolean supportUpload;

    public String getSplitChar() {
        return splitChar;
    }

    public void setSplitChar(String splitChar) {
        this.splitChar = splitChar;
    }

    private String splitChar;
    /**
     * 抄表字段是否合并
     */
    private boolean isMerge;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public boolean isMerge() {
        return isMerge;
    }

    public void setMerge(boolean merge) {
        isMerge = merge;
    }

    public int getUserIdIndex() {
        return userIdIndex;
    }

    public void setUserIdIndex(int userIdIndex) {
        this.userIdIndex = userIdIndex;
    }

    public int getUserNameIndex() {
        return userNameIndex;
    }

    public void setUserNameIndex(int userNameIndex) {
        this.userNameIndex = userNameIndex;
    }

    public int getUserAddressIndex() {
        return userAddressIndex;
    }

    public void setUserAddressIndex(int userAddressIndex) {
        this.userAddressIndex = userAddressIndex;
    }

    public int getMergeInfoIndex() {
        return mergeInfoIndex;
    }

    public void setMergeInfoIndex(int mergeInfoIndex) {
        this.mergeInfoIndex = mergeInfoIndex;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    public int getMeterIdIndex() {
        return meterIdIndex;
    }

    public void setMeterIdIndex(int meterIdIndex) {
        this.meterIdIndex = meterIdIndex;
    }

    public int getFirmCodeIndex() {
        return firmCodeIndex;
    }

    public void setFirmCodeIndex(int firmCodeIndex) {
        this.firmCodeIndex = firmCodeIndex;
    }

    public int getLastReadingIndex() {
        return lastReadingIndex;
    }

    public void setLastReadingIndex(int lastReadingIndex) {
        this.lastReadingIndex = lastReadingIndex;
    }

    public int getLastUsageIndex() {
        return lastUsageIndex;
    }

    public void setLastUsageIndex(int lastUsageIndex) {
        this.lastUsageIndex = lastUsageIndex;
    }

    public LoadStrategy() {

    }

    private int userIdIndex = -1;
    private int userNameIndex = -1;
    private int userAddressIndex = -1;
    private int channelAddressIndex = -1;
    private int mergeInfoIndex = -1;
    private int channelIndex = -1;
    private int meterIdIndex = -1;
    private int firmCodeIndex = -1;
    private int lastReadingIndex = -1;
    private int lastUsageIndex = -1;
    private int peopleRecordingIndex = -1;

    public int getPeopleRecordingIndex() {
        return peopleRecordingIndex;
    }

    public int getChannelAddressIndex() {
        return channelAddressIndex;
    }

    public void setChannelAddressIndex(int channelAddressIndex) {
        this.channelAddressIndex = channelAddressIndex;
    }

    public void setPeopleRecordingIndex(int peopleRecordingIndex) {
        this.peopleRecordingIndex = peopleRecordingIndex;
    }

    public boolean isSupportUpload() {
        return supportUpload;
    }

    @Override
    public String toString() {
        return "LoadStrategy{" +
                "city='" + city + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fieldCount=" + fieldCount +
                ", supportUpload=" + supportUpload +
                ", splitChar='" + splitChar + '\'' +
                ", isMerge=" + isMerge +
                ", userIdIndex=" + userIdIndex +
                ", userNameIndex=" + userNameIndex +
                ", userAddressIndex=" + userAddressIndex +
                ", channelAddressIndex=" + channelAddressIndex +
                ", mergeInfoIndex=" + mergeInfoIndex +
                ", channelIndex=" + channelIndex +
                ", meterIdIndex=" + meterIdIndex +
                ", firmCodeIndex=" + firmCodeIndex +
                ", lastReadingIndex=" + lastReadingIndex +
                ", lastUsageIndex=" + lastUsageIndex +
                ", peopleRecordingIndex=" + peopleRecordingIndex +
                '}';
    }

    public void setSupportUpload(boolean supportUpload) {
        this.supportUpload = supportUpload;
    }
}
