package com.emi.emireading.listener;

/**
 * @author :zhoujian
 * @description : 蓝牙数据回调
 * @company :翼迈科技
 * @date 2018年01月19日上午 10:06
 * @Email: 971613168@qq.com
 */

public interface OnReceiveBlueToothDataListener {
    /**
     *蓝牙数据回调
     * @param bytesLength
     * @param data
     */
    void onReceiveData(int bytesLength, byte[] data);
}
