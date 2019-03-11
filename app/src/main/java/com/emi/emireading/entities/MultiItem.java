package com.emi.emireading.entities;


import com.emi.emireading.core.bean.MultiItemEntity;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2019年01月23日下午 05:16
 * @Email: 971613168@qq.com
 */

public class MultiItem implements MultiItemEntity {
    public static final int FOLD = 1;
    public static final int FILE = 2;
    private int itemType;
    private FileInfo data;

    @Override
    public int getItemType() {
        return itemType;
    }

    public FileInfo getData() {
        return data;
    }

    public MultiItem(int itemType, FileInfo data) {
        this.itemType = itemType;
        this.data = data;
    }
}
