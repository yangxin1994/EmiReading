package com.emi.emireading.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;

import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年01月11日下午 12:24
 * @Email: 971613168@qq.com
 */

public class ReadService extends BaseService {
    public BluetoothDevice device1 = null;
    public BluetoothAdapter mBluetoothAdapter = getDefaultAdapter();
    private BluetoothSocket socket;
    private final int CONNECT_SUCCESS_CODE = 3;
    private final int CONNECT_FAILED_CODE = 5;
    private MessageHandler mHandler = new MessageHandler(this);
    final Messenger mMessenger = new Messenger(mHandler);

    @Override
    protected void initOnCreate() {
        LogUtil.i(tag, "initOnCreate--->");
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<ReadService> mService;

        MessageHandler(ReadService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }


    private void connectToBlueDevice() {
        //得到BluetoothAdapter对象
        BluetoothAdapter adapter = getDefaultAdapter();
        //判断BluetoothAdapter对象是否为空，如果为空，则表明本机没有蓝牙设备
        if (adapter != null) {
            //调用isEnabled()方法判断当前蓝牙设备是否可用
            if (!adapter.isEnabled()) {
                //如果蓝牙设备不可用的话,创建一个intent对象,该对象用于启动一个Activity,提示用户启动蓝牙适配器
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
            //得到所有已经配对的蓝牙适配器对象
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            final ArrayList<BluetoothDevice> emiDeviceArrayList = new ArrayList<>();
            if (devices.size() > 0) {
                //用迭代
                for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
                    final BluetoothDevice device = iterator.next();
                    if (EmiConstants.EMI_DEVICE_NAME.equals(device.getName())) {
                        emiDeviceArrayList.add(device);
                    }
                }
                int emiDeviceCount = checkEmiDeviceCount(emiDeviceArrayList);
                LogUtil.w(tag, "emiDeviceCount：" + emiDeviceCount);
                if (emiDeviceCount == 0) {
                    return;
                } else if (emiDeviceCount == Integer.MAX_VALUE) {
                    int emiDeviceIndex = getEmiDeviceIndex(emiDeviceArrayList);
                    connectByMac(emiDeviceArrayList.get(emiDeviceIndex));
                } else {
                    connectByMac(emiDeviceArrayList.get(0));
                }
            }
        }
    }


    private int checkEmiDeviceCount(ArrayList<BluetoothDevice> deviceArrayList) {
        int count = 0;
        for (int i = 0; i < deviceArrayList.size(); i++) {
            if (EmiConstants.EMI_DEVICE_NAME.equals(deviceArrayList.get(i).getName())) {
                count++;
            }
        }
        return count;
    }

    private int getEmiDeviceIndex(ArrayList<BluetoothDevice> deviceArrayList) {
        int index = -1;
        for (int i = 0; i < deviceArrayList.size(); i++) {
            if (EmiConstants.EMI_DEVICE_NAME.equals(deviceArrayList.get(i).getName())) {
                index = i;
            }
        }
        return index;
    }


    private void connectByMac(BluetoothDevice device) {
        //得到BluetoothDevice对象,也就是说得到配对的蓝牙适配器
        //得到远程蓝牙设备的地址
        String address = device.getAddress();
        String name = device.getName();
        if (EmiConstants.EMI_DEVICE_NAME.equals(name)) {
            LogUtil.i(tag, "MAC地址:" + address);
            device1 = mBluetoothAdapter.getRemoteDevice(address);
            try {
                socket = device1.createRfcommSocketToServiceRecord(
                        UUID.fromString(EmiConstants.UUID));
                socket.connect();
                EmiConstants.bluetoothSocket = socket;
                LogUtil.w(tag, "socket.isConnected() = " + socket.isConnected());
                Message message = new Message();
                //蓝牙连接成功
                message.what = CONNECT_SUCCESS_CODE;
                mHandler.sendMessage(message);
            } catch (IOException e) {
                LogUtil.e(tag, "connectToBlueDevice()--->" + e.toString());
                Message message = new Message();
                message.what = CONNECT_FAILED_CODE;
                mHandler.sendMessage(message);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.w(tag,"ReadService---->onDestroy()");
        super.onDestroy();
    }



}





