package com.emi.emireading.ui.debug;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.LoadingDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.core.config.EmiConstants.bluetoothSocket;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date: 2017年11月03日下午 02:16
 * @Email: 971613168@qq.com
 */

public class ChannelDebugActivity extends BaseActivity implements View.OnClickListener {
    private static final int NO_EMI_DEVICE_CODE = 10;
    private static final int CONNECT_SUCCESS_CODE = 3;
    private static final int OpenBlueToothRequestCode = 1;
    private static final int CONNECT_FAILED_CODE = 5;
    private static final int EDIT_FINISH = 6;
    private static final int MAX_TIME = 20000;
    private static final int MIN_TIME = 3000;
    private static final int EXECUTE_TIME = 1000;
    private final int timeInterval = 1000;
    private Button btnConnect;
    private Button btnReadChannel;
    private BluetoothSocket socket;
    public boolean isStop = false;
    private byte[] _channelinfo;
    private EditText etInput;
    private boolean isConnect;
    private boolean readFlag = false;
    CountDownTimer countDownTimer;
    byte[] cmd = {};
    //蓝牙是否连接
    public boolean btConnect = false;
    private final static int INDEX_OUT = -1;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothDevice device1 = null;
    //修改通道号
    private Button btnReviseChannel;
    private TextView tvStatus;
    private ReadCallBackRunnable mReadCallBackRunnable = null;
    private EditText etDate;
    private LoadingDialog dialog;
    private Context mContext;
    private String strHex = "";
    String strChannelDate = "";
    String strChannelNum = "";
    boolean breadChannel = false;
    boolean breadEdited = false;
    public boolean isInterrupt = true;
    private ImageView ivClear;
    private Disposable mDisposable;
    @Override
    protected int getContentLayout() {
        return R.layout.activity_channel_debug;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        initView();
        initDateInputListener();
    }

    @Override
    protected void initData() {

    }

    private void sendReadChannelCmd() {
        if (isConnect) {
            breadChannel = false;
            breadEdited = true;
            if (!EmiUtils.isNormalChannel()) {
                //蓝牙指令
                cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x42, 0x00, 0x31, 0x03, 0x01, (byte) 0x89, 0x01, 0x69, 0x16};
            } else {
                cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, 0x6A, 0x10, 0x02, (byte) 0xAA, 0x01, 0x27, 0x16};
            }
            sendBTMessage(cmd);
            btnReadChannel.setText("正在读取中...");
        } else {
            ToastUtil.showShortToast("请先连接蓝牙");
        }
    }

    public void sendBTMessage(byte[] cmd) {
        if (bluetoothSocket == null) {
            return;
        }
        try {
            OutputStream os = bluetoothSocket.getOutputStream();
            os.write(cmd);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
       /* btnConnect = (Button) findViewById(R.id.btn_connect);
        etDate = (EditText) findViewById(R.id.et_date);
        btnReadChannel = (Button) findViewById(R.id.btn_read_channel);
        btnReviseChannel = (Button) findViewById(R.id.btn_revise_channel);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        ivClear = (ImageView) findViewById(R.id.iv_clear);
        etInput = (EditText) findViewById(R.id.et_input);
        btnConnect.setOnClickListener(this);
        btnReviseChannel.setOnClickListener(this);
        btnReadChannel.setOnClickListener(this);
        ivClear.setOnClickListener(this);*/
        if (!EmiUtils.isNormalChannel()) {
            setEditMaxLength(8);
        } else {
            setEditMaxLength(6);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
         /*   case R.id.btn_connect:
                if (!isConnect) {
                    doConnectDevice();
                } else {
                    doDisConnect();
                    showConnectSuccess();
                    ToastUtils.showShortToast("蓝牙已断开");
                }
                break;
            case R.id.btn_revise_channel:
                readFlag = false;
                countReadChannel();
                editChannel();
                break;
            case R.id.btn_read_channel:
                readFlag = false;
                countReadChannel();
                sendReadChannelCmd();
                break;
            case R.id.iv_clear:
                clearChannel();
                break;

            default:
                break;*/
        }
    }

    private void editChannel() {
        if (!(etInput.getText().toString().length() > 0)) {
            ToastUtil.showShortToast("请输入通道板号！");
            return;
        }
        if (isConnect) {
            breadEdited = false;
            breadChannel = true;
            editChannelNum();
            btnReviseChannel.setText("正在修改...");
        } else {
            Toast.makeText(ChannelDebugActivity.this, "请先连接蓝牙设备!!!", Toast.LENGTH_SHORT).show();
        }
    }



    private void editChannelNum() {
        byte hexcheck = 0x00;
        //前2
        if (!EmiUtils.isNormalChannel()) {
            cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x01, 0x10, 0x04, 0x0A, 0x02, (byte) 0x89, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
            String editbuf = etInput.getText().toString();
            int a = editbuf.length();
            if (a > 0) {
                for (; a < 8; a++) {
                    editbuf = "0" + editbuf;
                }
                byte[] srtbyte = {0, 0, 0, 0};
                srtbyte = stringToBytes(getHexString(editbuf));

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                year = year - 2000;
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                year = (year / 10) * 16 + year % 10;
                month = (month / 10) * 16 + month % 10;
                day = (day / 10) * 16 + day % 10;
                cmd[18] = srtbyte[3];
                cmd[19] = srtbyte[2];
                cmd[20] = srtbyte[1];
                cmd[21] = srtbyte[0];
                cmd[22] = (byte) day;
                cmd[23] = (byte) month;
                cmd[24] = (byte) year;
                for (int k = 4; k <= 24; k++) {
                    hexcheck += cmd[k];
                }
                cmd[25] = hexcheck;
                sendBTMessage(cmd);
            }
        } else {
            cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x6A, 0x10, 0x08, (byte) 0xAA, 0x02, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
            String _editbuf = etInput.getText().toString();
            int tdh_lenth = _editbuf.length();
            if (tdh_lenth > 0) {
                for (; tdh_lenth < 6; tdh_lenth++) {
                    _editbuf = "0" + _editbuf;
                }
            }
            byte[] tdh_hex = {0, 0, 0, 0, 0, 0};
            for (int i = 0; i < tdh_hex.length; i++) {
                try {
                    tdh_hex[i] = (byte) Integer.parseInt(_editbuf.substring(i, i + 1));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < tdh_hex.length; i++) {
                tdh_hex[i] += 0x30;
                //cmd[9+i] = tdh_hex[i];
            }
            for (int i = 5; i >= 0; i--) {
                cmd[9 + i] = tdh_hex[5 - i];
            }
            for (int k = 4; k <= 14; k++) {
                hexcheck += cmd[k];
            }
            cmd[15] = (byte) hexcheck;
            sendBTMessage(cmd);
        }
    }

    private void showEditFinish() {
        readFlag = true;
        ToastUtil.showShortToast("修改成功");
        btnReviseChannel.setText("修改通道号");
    }

    private void setEditMaxLength(int maxLength) {
        etDate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }


    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;
        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final ChannelDebugActivity activity = (ChannelDebugActivity) mWeakReference.get();
            switch (msg.what) {
                case CONNECT_SUCCESS_CODE:
                   activity.doConnectSuccess();
                    break;
                case CONNECT_FAILED_CODE:
                    activity.showDisConnect();
                    break;
                case 0:
                    activity.showReadSuccess();
                    break;
                case EDIT_FINISH:
                    activity.showEditFinish();
                    break;
                case INDEX_OUT:
                    LogUtil.e(TAG,"INDEX_OUT");
                    activity.showDisConnect();
                    ToastUtil.showShortToast("蓝牙已断开");
                    break;
                default:
                    break;
            }
        }
    }

    private void showReadSuccess() {
        readFlag = true;
        ToastUtil.showShortToast("读取成功");
        btnReadChannel.setText("读取通道号");
        etInput.setText(strChannelNum);
        etDate.setText(strChannelDate);

    }

    private void clearChannel() {
        etInput.setText("");
        etInput.setHint("请输入通道号，并点击修改");
        etDate.setText("");
        ivClear.setVisibility(View.GONE);
    }

    private void makeButtonEnable(boolean b) {
        btnReadChannel.setEnabled(b);
        btnReviseChannel.setEnabled(b);
        btnConnect.setEnabled(b);
    }

    private void initDateInputListener() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etInput.getText().toString().trim().equals("")) {
                    ivClear.setVisibility(View.GONE);
                } else {
                    ivClear.setVisibility(View.VISIBLE);
                }
            }
        });
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
                for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
                    final BluetoothDevice device = iterator.next();
                    LogUtil.w(TAG, "所有设备MAC：" + device.getAddress());
                    LogUtil.w(TAG, "所有设备名称：" + device.getName());
                    if (EmiConstants.EMI_DEVICE_NAME.equals(device.getName())) {
                        emiDeviceArrayList.add(device);
                    }
                }
                if (emiDeviceArrayList.size() == 0) {
                    ToastUtil.showShortToast("没有配对抄表设备，请先配对");
                    closeDialog();
                    return;
                } else if (emiDeviceArrayList.size() == Integer.MAX_VALUE) {
                    int emiDeviceIndex = getEmiDeviceIndex(emiDeviceArrayList);
                    if (emiDeviceIndex >= 0) {
                        connectByMac(emiDeviceArrayList.get(emiDeviceIndex));
                    }
                } else {
                    showSelectDialog(emiDeviceArrayList);
                }
            }
        }
    }

    private void showSelectDialog(final ArrayList<BluetoothDevice> emiDeviceArrayList) {
        SelectDeviceEmiAdapter deviceAdapter = new SelectDeviceEmiAdapter(emiDeviceArrayList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("请选择蓝牙设备");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelConnect();
                dialog.dismiss();
            }
        });
        final CommonSelectDialog deviceDialog = builder.create();
        deviceDialog.setCancelable(false);
        deviceDialog.setCanceledOnTouchOutside(false);
        deviceAdapter.bindToRecyclerView(builder.recyclerView);
        deviceAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                connectByMac(emiDeviceArrayList.get(position));
                countDownConnect();
                deviceDialog.dismiss();
            }
        });
        builder.setAdapter(deviceAdapter);
        deviceDialog.show();
    }

    private void connectByMac(BluetoothDevice device) {
        //得到BluetoothDevice对象,也就是说得到配对的蓝牙适配器
        //得到远程蓝牙设备的地址
        String address = device.getAddress();
        String name = device.getName();
        LogUtil.w(TAG, "已执行3");
        if (EmiConstants.EMI_DEVICE_NAME.equals(name)) {
            //EMI0001
            LogUtil.i(TAG, "MAC地址:" + address);
            device1 = mBluetoothAdapter.getRemoteDevice(address);
            ConnectThread connectThread = new ConnectThread();
            connectThread.start();
        } else {
            ToastUtil.showShortToast("该设备不是翼迈抄表设备");
            closeDialog();
            showDisConnect();
        }
    }


    private void closeDialog() {
        if (dialog != null) {
            dialog.close();
        }
    }


    private void showDisConnect() {
        makeButtonEnable(true);
        cancelTimer();
        btnConnect.setBackgroundResource(R.drawable.btn_bg_red);
        btnConnect.setText("蓝牙未连接");
        tvStatus.setTextColor(getResourcesColor(R.color.red_btn_bg_color));
        tvStatus.setText("蓝牙未连接");
        isConnect = false;
        closeDialog();
        reset();
        bluetoothSocket = null;
    }



    private void countDownConnect() {
        CountDownTimer countDownTimer = new CountDownTimer(MAX_TIME, EXECUTE_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                LogUtil.i(TAG, "执行onTick");
                if (dialog == null || !(dialog.isShowing())) {
                    this.cancel();
                    LogUtil.w(TAG, "计时器取消");
                }
            }

            @Override
            public void onFinish() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.close();
                    showDisConnect();
                }
                LogUtil.i(TAG, "countDownTimer--->onFinish");
            }
        };
        countDownTimer.start();
    }

    private int getResourcesColor(int colorId) {
        return ContextCompat.getColor(mContext, colorId);
    }

    private void cancelConnect() {
        closeDialog();
        showDisConnect();
        makeButtonEnable(true);
    }

    private class ReadCallBackRunnable implements Runnable {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream mmInStream = null;
            try {
                mmInStream = bluetoothSocket.getInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (!EmiUtils.isNormalChannel()) {
                while (isInterrupt) {
                    try {
                        if ((bytes = mmInStream.read(buffer)) != -1) {
                            byte[] buf_data = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                buf_data[i] = buffer[i];
                            }
                            strHex = strHex + DigitalTrans.byte2hex(buf_data);
                            if (breadChannel == false) {
                                if (strHex.length() >= 48) {
                                    byte[] channelinfo = DigitalTrans.hex2byte(strHex);
                                    int n = 0;
                                    while (channelinfo[n++] != 0x68) {
                                        if (n > 30) {
                                            break;
                                        }
                                    }
                                    if (n < 30) {
                                        strChannelNum = "";
                                        strChannelDate = "";
                                        for (int l = 0; l < 4; l++) {
                                            String str = DigitalTrans.algorismToHEXString(channelinfo[13 + n + l]);
                                            strChannelNum = str + strChannelNum;
                                        }

                                        for (int l = 0; l < 3; l++) {
                                            String str1 = DigitalTrans.algorismToHEXString(channelinfo[17 + n + l]);
                                            strChannelDate = str1 + strChannelDate;
                                        }

                                        Message message = new Message();
                                        message.what = 0;
                                        mHandler.sendMessage(message);
                                    }
                                    strHex = "";
                                    breadChannel = true;
                                }
                            }
                            if (breadEdited == false) {
                                if (strHex.length() >= 36) {
                                    byte[] Meterinfo = DigitalTrans.hex2byte(strHex);
                                    int m = 0;
                                    while (Meterinfo[m++] != 0x68)
                                        ;
                                    String st = "" + m;
                                    System.out.println(st);
                                    //0x84
                                    if (Meterinfo[8 + m] == -124) {
                                        Message message = new Message();
                                        message.what = 6;
                                        mHandler.sendMessage(message);
                                    }
                                    strHex = "";
                                    breadEdited = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.e(TAG, "设备异常：" + e.toString());
                        stopThread();
                        sendMsg(INDEX_OUT);
                        try {
                            mmInStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }
            } else {
                strHex = "";//拼接byte数组
                while (isInterrupt) {
                    try {
                        if ((bytes = mmInStream.read(buffer)) > 0) {
                            byte[] buf_data = new byte[bytes];
                            //-0x30
                            byte[] re_buffer = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                buf_data[i] = buffer[i];
                                re_buffer[i] = (byte) (buf_data[i] - 0x30);
                            }
                            strHex = strHex + DigitalTrans.byte2hex(re_buffer);
                            re_buffer = null;
                            if (breadChannel == false) {
                                if (strHex.length() >= 30) {
                                    _channelinfo = DigitalTrans.hex2byte(strHex);
                                    int n = 0;
                                    while (_channelinfo[n++] != 0x38) {
                                        if (n > _channelinfo.length) {
                                            break;
                                        }
                                    }
                                    if (n < _channelinfo.length) {
                                        strChannelNum = "";
                                        for (int l = 0; l < 6; l++) {
                                            if (7 + l < _channelinfo.length) {
                                                String str = String.valueOf(_channelinfo[7 + l]);
                                                strChannelNum = str + strChannelNum;
                                            }
                                        }
                                        Message message = new Message();
                                        message.what = 0;
                                        mHandler.sendMessage(message);
                                    }
                                    strHex = "";
                                    //                                    _channelinfo=null;//清空缓存
                                    breadChannel = true;
                                }
                            }

                            if (breadEdited == false) {
                                LogUtil.i(TAG, "读取通道号回调");
                                if (strHex.length() == 18) {
                                    byte[] Meterinfo = DigitalTrans.hex2byte(strHex);
                                    System.out.println(strHex);
                                    int m = 0;
                                    while (Meterinfo[m++] != 0x38) {
                                        if (m > Meterinfo.length) {
                                            break;
                                        }
                                    }
                                    String st = "" + m;
                                    System.out.println(st);
                                    if (Meterinfo[4 + m] == -10) {
                                        Message message = new Message();
                                        message.what = 6;
                                        mHandler.sendMessage(message);
                                    }
                                    strHex = "";
                                    breadEdited = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        sendMsg(INDEX_OUT);
                        stopThread();
                        LogUtil.e(TAG, "错误：" + getClass().getName() + "--->" + e.toString());
                        isInterrupt = false;
                        try {
                            mmInStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }

    private void sendMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OpenBlueToothRequestCode:
                if (resultCode == RESULT_OK) {
                    connectBlueTooth();
                }
                break;
            default:
                break;
        }
    }

    private void showConnectSuccess() {
        btnConnect.setText("设备已连接");
        btnConnect.setBackgroundColor(getResourcesColor(R.color.green));
        makeButtonEnable(true);
        isConnect = true;
        tvStatus.setTextColor(getResourcesColor(R.color.green));
        tvStatus.setText("蓝牙已连接");
        closeDialog();
    }

    private void connectBlueTooth() {
        if (!btConnect) {
            if (dialog != null) {
                dialog.show();
            } else {
                dialog = new LoadingDialog(mContext, "正在连接抄表蓝牙设备");
                dialog.show();
            }
            btnConnect.setText("正在连接...");
            btnConnect.setBackgroundResource(R.drawable.bt_select_bg);
            btnConnect.setTextColor(getResources().getColor(R.color.white_small));
            makeButtonEnable(false);
            connectToBlueDevice();
        } else {
            doDisConnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStop = true;
        doDisConnect();
    }


    private void doConnectDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            connectBlueTooth();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, OpenBlueToothRequestCode);
        }
    }

    private void doDisConnect() {
        try {
            cancelTimer();
            if (socket != null || EmiConstants.bluetoothSocket != null) {
                isStop = true;
                btConnect = false;
                if (EmiConstants.bluetoothSocket != null) {
                    try {
                        bluetoothSocket.close();
                    }catch (NullPointerException e){
                        LogUtil.e(TAG, "蓝牙已关闭");
                    }

                    LogUtil.w(TAG, "蓝牙已关闭");
                }
                socket = null;
                EmiConstants.bluetoothSocket = null;
                showDisConnect();
            }
        } catch (IOException e) {
            LogUtil.e(TAG, e.toString());
        }
    }

    private class ConnectThread extends Thread {
        @Override
        public void run() {
            try {
                socket = device1.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                socket.connect();
                EmiConstants.bluetoothSocket = socket;
                LogUtil.w(TAG, "socket.isConnected() = " + socket.isConnected());
                Message message = new Message();
                //蓝牙连接成功
                message.what = CONNECT_SUCCESS_CODE;
                mHandler.sendMessage(message);
            } catch (IOException e) {
                LogUtil.e(TAG, "connectToBlueDevice()--->" + e.toString());
                Message message = new Message();
                message.what = CONNECT_FAILED_CODE;
                mHandler.sendMessage(message);
                e.printStackTrace();
                interrupt();
            }
        }
    }

    private void stopThread() {
        try {
            Thread.interrupted();
            LogUtil.w("直接抛出异常");
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void doConnectSuccess(){
        interval(timeInterval);
        showConnectSuccess();
        if (EmiConstants.bluetoothSocket != null) {
            mReadCallBackRunnable = new ReadCallBackRunnable();
            ThreadPoolManager.EXECUTOR.execute(mReadCallBackRunnable);
        }
    }

    /**
     * 每隔milliseconds毫秒后执行next操作
     *
     * @param milliseconds
     */
    public void interval(long milliseconds) {
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (EmiConstants.bluetoothSocket != null && EmiConstants.bluetoothSocket.isConnected()) {
                        } else {
                            LogUtil.w(TAG,"蓝牙已断开");
                            showDisConnect();
                            ToastUtil.showShortToast("蓝牙已断开");
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        cancelTimer();
                    }

                    @Override
                    public void onComplete() {
                        cancelTimer();
                    }
                });
    }

    /**
     * 取消计时
     */
    public void cancelTimer() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            LogUtil.w("====定时器取消======");
        }
    }


    private void countReadChannel() {
        countDownTimer = new CountDownTimer(MIN_TIME, EXECUTE_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                    LogUtil.i(TAG, "执行onTick");
                    if (readFlag){
                        LogUtil.w(TAG, "计时器取消");
                        this.cancel();
                    }
            }

            @Override
            public void onFinish() {
                LogUtil.i(TAG, "onFinish");
                ToastUtil.showShortToast("读取失败");
                reset();
            }
        };
        countDownTimer.start();
    }

    private void reset(){
        btnReadChannel.setText("读取通道号");
    }
}
