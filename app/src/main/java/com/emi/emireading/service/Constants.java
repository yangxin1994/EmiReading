

package com.emi.emireading.service;

/**
 * /**
 * @author :zhoujian
 * @description :
 * @company :翼迈科技
 * @date 2018年01月12日下午 01:59
 * @Email: 971613168@qq.com
 * Defines several constants used between {@link EmiBlueToothService} and the UI.
 */
public interface Constants {

    /**
     * Message types sent from the BluetoothChatService Handler
     */
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

}
