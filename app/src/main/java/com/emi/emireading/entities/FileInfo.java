package com.emi.emireading.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author :zhoujian
 * @description : 文件信息实体
 * @company :翼迈科技
 * @date 2019年01月23日下午 05:18
 * @Email: 971613168@qq.com
 */

public class FileInfo implements Parcelable {
    public String filePath;
    public String fileName;
    public String suffix;

    public boolean isDirectory() {
        return directory;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean directory;
    public String date;
    /**
     * 是否被选中
     */
    public boolean select;
    public long fileSize;

    public FileInfo() {
    }

    private FileInfo(Parcel in) {
        filePath = in.readString();
        fileName = in.readString();
        suffix = in.readString();
        directory = in.readByte() != 0;
        date = in.readString();
        select = in.readByte() != 0;
        fileSize = in.readLong();


    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(fileName);
        dest.writeString(suffix);
        dest.writeByte((byte) (directory ? 1 : 0));
        dest.writeString(date);
        dest.writeByte((byte) (select ? 1 : 0));
        dest.writeLong(fileSize);
    }
}
