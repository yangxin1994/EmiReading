package com.emi.emireading.ui.debug;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.core.config.EmiConstants.IO_EXCEPTION;
import static com.emi.emireading.core.config.EmiConstants.MSG_BLUETOOTH_CONNECT;

/**
 * @author :zhoujian
 * @description : 通道板调试
 * @company :翼迈科技
 * @date 2018年05月31日上午 10:29
 * @Email: 971613168@qq.com
 */

public class ChannelDebugActivityNew extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChannelDebugActivityNew";
    public BluetoothDevice bluetoothDevice = null;
    private Button btnConnect;
    private EmiMultipleProgressDialog dialog;
    private Context mContext;
    private boolean isConnect;
    private TextView tvStatus;
    private Button btnReadChannel;
    private Button btnEditChannel;
    private BluetoothSocket socket;
    private boolean isStop;
    private StringBuffer callBackStringBuffer = new StringBuffer("");
    private byte[] cmd = {};
    boolean breadChannel = false;
    boolean breadEdited = false;
    private Disposable blueToothDisposable;
    private Disposable dataReceiveDisposable;
    private Disposable readAndEditDisposable;
    /**
     * 时间间隔(毫秒)
     */
    private static final int TIME_INTERVAL = 1000;
    private static final int MSG_ERROR = -1;
    /**
     * 蓝牙已连接
     */
    private Handler handler = new MyHandler(this);

    private int receiveCount;
    private EditText etInputChannel;
    private EditText etDate;
    private int maxLengthSpecial = 8;
    private int maxLengthNormal = 6;

    /**
     * 收到回调的次数
     */
    private int tempCount = -1;

    private int mTag;

    /**
     * 读取或修改是否成功
     */
    private boolean isCallBackSuccess;

    private final int TAG_READ_NORMAL = 1;

    private final int TAG_READ_SPECIAL = 2;

    private final int TAG_EDIT = 3;
    private final static String FLAG_DATA_CODE_SPECIAL = "0189";
    private final static String FLAG_DATA_CODE_NORMAL = "AA";
    private String mChannel;
    private String mDate;
    private String mEditDate;
    private ImageView ivClear;
    private String mEditChannel;

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
        btnConnect = findViewById(R.id.btnConnect);
        btnReadChannel = findViewById(R.id.btnReadChannel);
        btnEditChannel = findViewById(R.id.btnEditChannel);
        etInputChannel = findViewById(R.id.etInputChannel);
        tvStatus = findViewById(R.id.tvStatus);
        etDate = findViewById(R.id.etDate);
        ivClear = findViewById(R.id.ivClear);
    }

    @Override
    protected void initData() {
        btnConnect.setOnClickListener(this);
        btnReadChannel.setOnClickListener(this);
        btnEditChannel.setOnClickListener(this);
        ivClear.setOnClickListener(this);
        initDateInputListener();
    }


    /**
     * 监听蓝牙连接情况
     */
    public void observeBlueToothConnection() {
        doEventByInterval(TIME_INTERVAL, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                blueToothDisposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                if (socket == null || (!socket.isConnected())) {
                    doConnectFailed();
                    ToastUtil.showShortToast("蓝牙已断开");
                    stopTimer(blueToothDisposable);
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(blueToothDisposable);
            }

            @Override
            public void onComplete() {
                stopTimer(blueToothDisposable);
            }
        });
    }

    private void connectBlueTooth() {
        if (!isConnect) {
            if (dialog != null) {
                dialog.show();
            } else {
                showDialog("正在连接蓝牙...");
            }
            btnConnect.setText("正在连接...");
            btnConnect.setBackgroundResource(R.drawable.bt_select_bg);
            btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white_small));
            makeButtonEnable(false);
            connectToBlueDevice();
        } else {
            doDisConnect();
            ToastUtil.showShortToast("蓝牙已断开");
        }
    }

    private void doDisConnect() {
        cancelTimers();
        if (socket != null || EmiConstants.bluetoothSocket != null) {
            isStop = true;
            EmiConstants.bluetoothSocket = socket;
            showDisconnect();
            isConnect = false;
            if (socket != null && socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    socket = null;
                }
            }
            socket = null;
        }
    }

    private void doConnectFailed() {
        isConnect = false;
        makeButtonEnable(true);
        stopTimer(blueToothDisposable);
        showDisconnect();
        closeDialog();
    }

    private void showDialog(String text) {
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setLabel(text)
                .setCancellable(false)
                .show();
    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    private void makeButtonEnable(boolean b) {
        btnReadChannel.setEnabled(b);
        btnEditChannel.setEnabled(b);
        btnConnect.setEnabled(b);
    }


    private void showDisconnect() {
        isConnect = false;
        makeButtonEnable(true);
        btnConnect.setBackgroundResource(R.drawable.btn_bg_red);
        btnConnect.setText("蓝牙未连接");
        tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red_btn_bg_color));
        tvStatus.setText("蓝牙未连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
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
                    btnConnect.setText("蓝牙未连接");
                    makeButtonEnable(true);
                    closeDialog();
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
                deviceDialog.dismiss();
            }
        });
        builder.setAdapter(deviceAdapter);
        deviceDialog.show();
    }

    private void connectByMac(BluetoothDevice device) {
        //得到BluetoothDevice对象
        //得到远程蓝牙设备的地址
        String address = device.getAddress();
        String name = device.getName();
        if (EmiConstants.EMI_DEVICE_NAME.equals(name)) {
            LogUtil.i(TAG, "MAC地址:" + address);
            bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            ThreadPoolManager.EXECUTOR.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(
                                        UUID.fromString(EmiConstants.UUID));
                                socket.connect();
                                EmiConstants.bluetoothSocket = socket;
                                if (EmiConstants.bluetoothSocket != null) {
                                    LogUtil.w(TAG, "socket.isConnected() = " + socket.isConnected());
                                }
                                //蓝牙连接成功
                                sendEmptyMsg(MSG_BLUETOOTH_CONNECT);
                            } catch (IOException e) {
                                LogUtil.e(TAG, "connectToBlueDevice()--->" + e.toString());
                                sendErrorMsg("当前蓝牙设备可能关闭或被占用");
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } else {
            ToastUtil.showShortToast("该设备不是翼迈抄表设备");
            closeDialog();
            showDisconnect();
        }
    }

    private void sendEmptyMsg(int what) {
        handler.sendEmptyMessage(what);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                connectBlueTooth();
                break;
            case R.id.btnReadChannel:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                clearReceiveData();
                clearResult();
                isCallBackSuccess = false;
                sendReadChannelCmd();
                listenReadAndEdit();
                break;
            case R.id.ivClear:
                clearResult();
                break;
            case R.id.btnEditChannel:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                if (TextUtils.isEmpty(etInputChannel.getText().toString())) {
                    ToastUtil.showShortToast("请输入要修改的通道号");
                    return;
                }
                isCallBackSuccess = false;
                clearReceiveData();
                sendEditEmd();
                clearResult();
                listenReadAndEdit();
                break;
            default:
                break;
        }
    }


    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ChannelDebugActivityNew activity = (ChannelDebugActivityNew) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_BLUETOOTH_CONNECT:
                        activity.doConnectSuccess();
                        break;
                    case MSG_ERROR:
                        String errorMsg = (String) msg.obj;
                        LogUtil.e("有异常：" + errorMsg);
                        activity.doConnectFailed();
                        ToastUtil.showShortToast(errorMsg);
                        activity.closeDialog();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void cancelConnect() {
        closeDialog();
        showDisconnect();
        makeButtonEnable(true);
    }

    private void doConnectSuccess() {
        observeBlueToothConnection();
        isConnect = true;
        isStop = false;
        closeDialog();
        showConnectSuccess();
        makeButtonEnable(true);
        ThreadPoolManager.EXECUTOR.execute(new ReceiverDataRunnable());
    }

    private void sendErrorMsg(String errorMsg) {
        Message message = handler.obtainMessage();
        message.what = MSG_ERROR;
        message.obj = errorMsg;
        handler.sendMessage(message);
    }

    private void showConnectSuccess() {
        btnConnect.setText("蓝牙已连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        btnConnect.setBackgroundResource(R.drawable.btn_bg_green_sel);
        tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        tvStatus.setText("蓝牙已连接");
    }


    @Override
    protected void onDestroy() {
        stopTimer(blueToothDisposable);
        stopTimer(dataReceiveDisposable);
        stopTimer(readAndEditDisposable);
        doDisConnect();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private class ReceiverDataRunnable implements Runnable {
        @Override
        public void run() {
            blueToothReceiveCallBack();
        }
    }


    /**
     * 获取通道板回传回来的指令
     */
    private void blueToothReceiveCallBack() {
        byte[] buffer = new byte[1024];
        int bytes;
        InputStream mmInStream = null;
        LogUtil.i(TAG, "线程已启动");
        while (!isStop) {
            try {
                if (EmiConstants.bluetoothSocket != null) {
                    mmInStream = EmiConstants.bluetoothSocket.getInputStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mmInStream == null) {
                return;
            }
            try {
                if ((bytes = mmInStream.read(buffer)) > 0) {
                    Log.w(TAG, "已执行");
                    if (receiveCount == 0) {
                        LogUtil.i(TAG, "表示已经第一次收到回调");
                        listenBlueToothDataReceive();
                    } else {
                        LogUtil.d(TAG, "表示已经第" + receiveCount + "次收到回调");
                    }
                    receiveCount++;
                    byte[] bufData = new byte[bytes];
                    for (int i = 0; i < bytes; i++) {
                        bufData[i] = buffer[i];
                        Log.e(TAG, "返回的数据：" + buffer[i]);
                        callBackStringBuffer.append(DigitalTrans.byteToHexString(buffer[i]));
                    }
                    Log.e(TAG, "返回的字符数据：" + callBackStringBuffer.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "错误：" + e.toString());
                if (!e.toString().contains(IO_EXCEPTION)) {
                    sendErrorMsg(e.toString());
                }
                isStop = true;
                if (Thread.interrupted()) {
                    stopThread();
                } else {
                    LogUtil.i("interrupted = false");
                    stopThread();
                }
                try {
                    mmInStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    private void stopThread() {
        try {
            isConnect = false;
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }


    private void sendReadChannelCmd() {
        if (isConnect) {
            breadChannel = false;
            breadEdited = true;
            if (!EmiUtils.isNormalChannel()) {
                //蓝牙指令
                mTag = TAG_READ_SPECIAL;
                cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x42, 0x00, 0x31, 0x03, 0x01, (byte) 0x89, 0x01, 0x69, 0x16};
            } else {
                mTag = TAG_READ_NORMAL;
                cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, 0x6A, 0x10, 0x02, (byte) 0xAA, 0x01, 0x27, 0x16};
            }
            sendBTCmd(cmd);
            btnReadChannel.setText("正在读取中...");
        } else {
            ToastUtil.showShortToast("请先连接蓝牙");
        }
    }

    /**
     * 监听蓝牙数据接收
     */
    private void listenBlueToothDataReceive() {
        doEventByInterval(100, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                dataReceiveDisposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                if (tempCount == receiveCount) {
                    LogUtil.i(TAG, "接收完毕~");
                    LogUtil.d("当前次数：" + receiveCount);
                    LogUtil.i(TAG, "蓝牙返回的字符数据：" + callBackStringBuffer.toString());
                    boolean isCorrect = checkCallbackDataCorrect(callBackStringBuffer.toString());
                    if (isCorrect) {
                        isCallBackSuccess = true;
                        resolveCallBackData(mTag);
                    }
                    stopTimer(dataReceiveDisposable);
                } else {
                    LogUtil.d(TAG, "正在接收中...");
                }
                tempCount = receiveCount;
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(dataReceiveDisposable);
            }

            @Override
            public void onComplete() {
                stopTimer(dataReceiveDisposable);
            }
        });
    }


    private void clearReceiveData() {
        receiveCount = 0;
        tempCount = -1;
        callBackStringBuffer.setLength(0);
    }



    /**
     * 解析回调数据
     *
     * @param tag
     */
    private void resolveCallBackData(int tag) {
        switch (tag) {
            case TAG_READ_NORMAL:
                LogUtil.d("普通市场");
                resolveChannelNormalCallback(callBackStringBuffer.toString());
                showReadFinish();
                break;
            case TAG_READ_SPECIAL:
                LogUtil.i("特殊市场");
                resolveChannelSpecialCallBack(callBackStringBuffer.toString());
                showReadFinish();
                break;
            case TAG_EDIT:
                showEditFinish();
                break;
            default:
                break;
        }
    }

    /**
     * 特殊市场通道号回调解析
     *
     * @param callbackStr
     */
    private void resolveChannelSpecialCallBack(String callbackStr) {
        int dataIndex = callbackStr.indexOf(FLAG_DATA_CODE_SPECIAL) + FLAG_DATA_CODE_SPECIAL.length() + 2;
        String channel;
        String date;
        if (dataIndex > -1) {
            channel = callbackStr.substring(dataIndex, dataIndex + 8);
            date = callbackStr.substring(dataIndex + 8, dataIndex + 8 + 6);
            List<String> channelInfoList = EmiStringUtil.splitStrToList(channel);
            List<String> dateInfoList = EmiStringUtil.splitStrToList(date);
            StringBuilder sbChannel = new StringBuilder("");
            StringBuilder sbDate = new StringBuilder("");
            for (int i = channelInfoList.size() - 1; i >= 0; i--) {
                sbChannel.append(channelInfoList.get(i));
            }
            for (int i = dateInfoList.size() - 1; i >= 0; i--) {
                sbDate.append(dateInfoList.get(i));
            }
            mChannel = sbChannel.toString();
            mDate = sbDate.toString();
        }
    }

    /**
     * 普通市场通道号回调解析
     *
     * @param callbackStr
     */
    private void resolveChannelNormalCallback(String callbackStr) {
        int dataIndex = callbackStr.indexOf(FLAG_DATA_CODE_NORMAL) + FLAG_DATA_CODE_NORMAL.length() + 2;
        String channel;
        String date;
        if (dataIndex > -1) {
            channel = callbackStr.substring(dataIndex, dataIndex + 12);
            LogUtil.w(TAG, "channel:" + channel);
            date = "";
            List<String> channelInfoList = EmiStringUtil.splitStrToList(channel);
            List<String> dateInfoList = EmiStringUtil.splitStrToList(date);
            StringBuilder sbChannel = new StringBuilder("");
            StringBuilder sbDate = new StringBuilder("");
            String tempStr;
            for (int i = channelInfoList.size() - 1; i >= 0; i--) {
                tempStr = channelInfoList.get(i);
                tempStr = tempStr.substring(tempStr.length() - 1, tempStr.length());
                sbChannel.append(tempStr);
            }
            for (int i = dateInfoList.size() - 1; i >= 0; i--) {
                sbDate.append(dateInfoList.get(i));
            }
            mChannel = sbChannel.toString();
            mDate = sbDate.toString();
        }
    }

    private void showReadFinish() {
        makeButtonEnable(true);
        ToastUtil.showShortToast("读取成功");
        btnReadChannel.setText("读取通道号");
        btnEditChannel.setText("修改通道号");
        etInputChannel.setText(EmiStringUtil.formatNull(mChannel));
        etDate.setText(EmiStringUtil.formatNull(mDate));
    }

    private void clearResult() {
        etInputChannel.setText("");
        etDate.setText("");
        mChannel = "";
        mDate = "";
        ivClear.setVisibility(View.GONE);
    }


    private void initDateInputListener() {
        if (EmiUtils.isNormalChannel()) {
            etInputChannel.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLengthNormal)});
        } else {
            etInputChannel.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLengthSpecial)});
        }
        etInputChannel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if ("".equals(etInputChannel.getText().toString().trim())) {
                    ivClear.setVisibility(View.GONE);
                } else {
                    ivClear.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void listenReadAndEdit() {
        stopTimer(readAndEditDisposable);
        doEventCountDown(TIME_INTERVAL * 3, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                readAndEditDisposable = d;
            }

            @Override
            public void onNext(Long o) {
                LogUtil.d(TAG, "是否读取成功：" + isCallBackSuccess);
                if (isCallBackSuccess) {
                    stopTimer(readAndEditDisposable);
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(readAndEditDisposable);
            }

            @Override
            public void onComplete() {
                if (TAG_EDIT == mTag) {
                    ToastUtil.showShortToast("修改失败");
                } else {
                    ToastUtil.showShortToast("读取失败");
                }
                showOperateFailed();
                clearResult();
                stopTimer(readAndEditDisposable);
            }
        });
    }


    private void sendEditEmd() {
        byte hexcheck = 0x00;
        btnEditChannel.setText("正在修改中...");
        mTag = TAG_EDIT;
        if (!EmiUtils.isNormalChannel()) {
            etInputChannel.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLengthSpecial)});
            cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x01, 0x10, 0x04, 0x0A, 0x02, (byte) 0x89, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
            String editbuf = etInputChannel.getText().toString();
            int a = editbuf.length();
            if (a > 0) {
                for (; a < maxLengthSpecial; a++) {
                    editbuf = "0" + editbuf;
                }
                mEditChannel = editbuf;
                byte[] strBytes;
                strBytes = stringToBytes(getHexString(editbuf));
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                year = year - 2000;
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                StringBuilder stringBuilder = new StringBuilder("");
                DecimalFormat df = new DecimalFormat("00");
                stringBuilder.append(year);
                stringBuilder.append(df.format(month));
                stringBuilder.append(df.format(day));
                year = (year / 10) * 16 + year % 10;
                month = (month / 10) * 16 + month % 10;
                day = (day / 10) * 16 + day % 10;
                mEditDate = stringBuilder.toString();
                cmd[18] = strBytes[3];
                cmd[19] = strBytes[2];
                cmd[20] = strBytes[1];
                cmd[21] = strBytes[0];
                cmd[22] = (byte) day;
                cmd[23] = (byte) month;
                cmd[24] = (byte) year;
                for (int k = 4; k <= 24; k++) {
                    hexcheck += cmd[k];
                }
                cmd[25] = hexcheck;
                sendBTCmd(cmd);
            }
        } else {
            cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x6A, 0x10, 0x08, (byte) 0xAA, 0x02, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
            String editBuffer = etInputChannel.getText().toString();
            int tdh_lenth = editBuffer.length();
            mEditChannel = editBuffer;
            if (tdh_lenth > 0) {
                for (; tdh_lenth < maxLengthNormal; tdh_lenth++) {
                    editBuffer = "0" + editBuffer;
                }
            }
            byte[] tdh_hex = {0, 0, 0, 0, 0, 0};
            for (int i = 0; i < tdh_hex.length; i++) {
                try {
                    tdh_hex[i] = (byte) Integer.parseInt(editBuffer.substring(i, i + 1));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < tdh_hex.length; i++) {
                tdh_hex[i] += 0x30;
            }
            for (int i = 5; i >= 0; i--) {
                cmd[9 + i] = tdh_hex[5 - i];
            }
            for (int k = 4; k <= 14; k++) {
                hexcheck += cmd[k];
            }
            cmd[15] = hexcheck;
            sendBTCmd(cmd);
        }
    }


    private void showEditFinish() {
        makeButtonEnable(true);
        btnReadChannel.setText("读取通道号");
        btnEditChannel.setText("修改通道号");
        ToastUtil.showShortToast("修改成功");
        etInputChannel.setText(EmiStringUtil.formatNull(mEditChannel));
        etDate.setText(EmiStringUtil.formatNull(mEditDate));
    }

    private void showOperateFailed() {
        makeButtonEnable(true);
        btnReadChannel.setText("读取通道号");
        btnEditChannel.setText("修改通道号");
        etInputChannel.setText(EmiStringUtil.formatNull(mEditChannel));
        etDate.setText(EmiStringUtil.formatNull(mEditDate));
    }
}
