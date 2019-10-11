package com.emi.emireading.entities;

import org.litepal.crud.LitePalSupport;

/**
 * @author :zhoujian
 * @description : 修改文件的信息实体类
 * @company :翼迈科技
 * @date 2018年05月17日下午 03:24
 * @Email: 971613168@qq.com
 */
@SuppressWarnings("unchecked")
public class FileEditInfo extends LitePalSupport {
    public String fileName = "";
    public String originalUserId;
    public String originalMeterId;
    public String originalChannelNumber;
    public String originalFirmCode;
    public String newUserId;
    public String newMeterId;
    public String newChannelNumber;
    public String newFirmCode;
    public String userAddress = "";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String userName = "";

    public String getFileName() {
        return fileName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalUserId() {
        return originalUserId;
    }

    public void setOriginalUserId(String originalUserId) {
        this.originalUserId = originalUserId;
    }

    public String getOriginalMeterId() {
        return originalMeterId;
    }

    public void setOriginalMeterId(String originalMeterId) {
        this.originalMeterId = originalMeterId;
    }

    public String getOriginalChannelNumber() {
        return originalChannelNumber;
    }

    public void setOriginalChannelNumber(String originalChannelNumber) {
        this.originalChannelNumber = originalChannelNumber;
    }

    public String getOriginalFirmCode() {
        return originalFirmCode;
    }

    public void setOriginalFirmCode(String originalFirmCode) {
        this.originalFirmCode = originalFirmCode;
    }

    public String getNewUserId() {
        return newUserId;
    }

    public void setNewUserId(String newUserId) {
        this.newUserId = newUserId;
    }

    public String getNewMeterId() {
        return newMeterId;
    }

    public void setNewMeterId(String newMeterId) {
        this.newMeterId = newMeterId;
    }

    public String getNewChannelNumber() {
        return newChannelNumber;
    }

    public void setNewChannelNumber(String newChannelNumber) {
        this.newChannelNumber = newChannelNumber;
    }

    public String getNewFirmCode() {
        return newFirmCode;
    }

    public void setNewFirmCode(String newFirmCode) {
        this.newFirmCode = newFirmCode;
    }

    public FileEditInfo() {

    }


    @Override
    public boolean equals(Object obj) {
        FileEditInfo fileEditInfo = (FileEditInfo) obj;
        return this.fileName.equals(fileEditInfo.fileName) && this.userAddress.equals(fileEditInfo.userAddress)&&this.userName.equals(fileEditInfo.userName);
    }
}
