package com.emi.emireading.adpter;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.core.adapter.BaseEmiAdapter;

import java.util.List;

/**
 * @author :zhoujian
 * @description : 蓝牙设备适配器
 * @company :翼迈科技
 * @date: 2017年11月01日下午 02:36
 * @Email: 971613168@qq.com
 */

public class SelectDeviceEmiAdapter extends BaseEmiAdapter<BluetoothDevice,BaseViewHolder> {
    public SelectDeviceEmiAdapter(@Nullable List<BluetoothDevice> data) {
        super(R.layout.item_seclect_device, data);
    }
    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice bluetoothDevice) {
        helper.setText(R.id.tvDeviceName,bluetoothDevice.getName());
        helper.setText(R.id.tvDeviceMAC,bluetoothDevice.getAddress());
    }


}
