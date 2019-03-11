package com.emi.emireading.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.emi.emireading.R;
import com.emi.emireading.adpter.AutoReadEmiAdapter;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.EmiRecycleViewDivider;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.core.config.EmiConfig.READING_DELAY;
import static com.emi.emireading.core.config.EmiConfig.REPEAT_COUNT;
import static com.emi.emireading.core.config.EmiConstants.EMI_CALLBACK_CODE_BEGIN;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_POSITION;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_USER_LIST;
import static com.emi.emireading.core.config.EmiConstants.MSG_ERROR;
import static com.emi.emireading.core.config.EmiConstants.ONE_SECOND;
import static com.emi.emireading.core.config.EmiConstants.PREF_READING_DELAY;
import static com.emi.emireading.core.config.EmiConstants.PREF_WATER_WARNING_LINE;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.core.config.EmiConstants.userInfoArrayList;
import static com.emi.emireading.core.utils.EmiStringUtil.splitStrToList;
import static com.emi.emireading.ui.ChannelListActivity.EXTRA_CHANNEL;
import static com.emi.emireading.ui.MeterQueryActivityNew.REQUEST_CODE_DETAIL_INFO;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_BUNDLE;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_EDIT_DATA_LIST;
import static com.emi.emireading.ui.UserInfoListActivity.RESULT_CODE_CHANNEL_DATA;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年06月01日下午 05:21
 * @Email: 971613168@qq.com
 */
@Deprecated
public class AutoReadMeterActivityOld extends BaseActivity implements View.OnClickListener {
    private Button btnConnect;
    private Button btnRead;
    private BluetoothDevice bluetoothDevice = null;
    private Button btnReRead;
    private TitleView titleView;
    private byte[] cmd = {};
    private RecyclerView rvUserInfo;
    private Context mContext;
    private final static String FLAG_DATA_CODE_SPECIAL = "0189";
    private final static String FLAG_DATA_CODE_NORMAL = "AA";
    private final static String FLAG_DATA_METER_CALLBACK_CODE = "16";
    /**
     * 水表返回数据的校验位索引
     */
    private final static int INDEX_CHECK_METER_CALLBACK = 20;
    private BluetoothSocket socket;
    public boolean isStop = false;
    private Handler mHandler = new MyHandler(this);
    private String mFileName;
    private StringBuffer callBackStringBuffer = new StringBuffer("");
    private EmiMultipleProgressDialog dialog;
    private boolean isConnect;
    private static final int MSG_CONNECT = 100;
    private static final int MSG_TOAST_INFO = 1001;
    private static final int MSG_CLEAR_UI_LIST = 1002;
    private static final int MSG_NOTIFY_UI = 1003;
    private static final int MSG_NOTIFY_TITLE = 1004;
    private static final int MSG_READ_FINISH = 1005;
    private int receiveCount;
    private final int TAG_READ_CHANNEL = 1;
    private final int TAG_AUTO_READ_METER = 2;
    /**
     * 补抄标志
     */
    private final int TAG_REPEAT_READ_METER = 3;
    private boolean isReadChannelSuccess = false;
    private AutoReadEmiAdapter adapter;
    private int mTag;
    private ArrayList<UserInfo> uiUserList = new ArrayList<>();
    private UserInfo currentUserInfo;
    private ArrayList<Disposable> disposableReadMeterList = new ArrayList<>();

    private ArrayList<Disposable> disposableReadChannelList = new ArrayList<>();
    private ArrayList<Disposable> disposableDataReceiveList = new ArrayList<>();
    private static final int REQUEST_CODE_USER_LIST = 10;
    /**
     * 收到回调的次数
     */
    private int tempCount = -1;
    private String channelCallback;
    private boolean isCallBackSuccess;
    private boolean isReadFinish = false;
    private int limitUsage;
    /**
     * 待自动抄表的用户信息集合
     */
    private ArrayList<UserInfo> autoReadingUserList = new ArrayList<>();
    /**
     * 失败抄表的用户信息集合
     */
    private ArrayList<UserInfo> failedUserList = new ArrayList<>();
    private ArrayList<UserInfo> currentSuccessList = new ArrayList<>();
    private int lastState;
    private String failedMeterId;
    /**
     * 是否是手动点击的补抄
     */
    private boolean isClickRepeat = false;
    private boolean hasClickAutoRead = false;
    private boolean isAutoRepeatRead = false;
    private int repeatReadCount;

    @Override
    protected int getContentLayout() {
        return R.layout.emi_auto_read_meter_activity;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        mFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        channelCallback = getIntent().getStringExtra(EXTRA_CHANNEL);
    }

    @Override
    protected void initUI() {
        btnConnect = findViewById(R.id.btnConnect);
        btnRead = findViewById(R.id.btnRead);
        btnReRead = findViewById(R.id.btnReRead);
        titleView = findViewById(R.id.titleView);
        btnConnect.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        rvUserInfo = findViewById(R.id.rvUserInfo);
        rvUserInfo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        btnReRead.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        EmiConfig.REPEAT_COUNT = EmiUtils.getAutoRepeatCount();
        limitUsage = PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50);
        READING_DELAY = PreferenceUtils.getInt(PREF_READING_DELAY, 1000);
        initAdapter();
        if(READING_DELAY ==0){
            READING_DELAY = 500;
        }
        LogUtil.w(TAG, "延时时间：" + READING_DELAY);
        titleView.setRightIconText("统计");
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UserInfoListActivity.class);
                intent.putExtra(EXTRA_CHANNEL, EmiStringUtil.clearFirstZero(channelCallback));
                intent.putExtra(EXTRA_FILE_NAME, mFileName);
                startActivityForResult(intent, REQUEST_CODE_USER_LIST);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                connectBlueTooth();
                break;
            case R.id.btnRead:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                hasClickAutoRead = true;
                isAutoRepeatRead = true;
                mTag = TAG_AUTO_READ_METER;
                isClickRepeat = false;
                makeButtonEnable(false);
                clearReceiveData();
                uiUserList.clear();
                notifyUItoBottom();
                repeatReadCount = 0;
                stopTimerList(disposableDataReceiveList);
                if (EmiUtils.isNeedChannel()) {
                    notifyTitleText("正在读取通道号...", false);
                    readChannel();
                } else {
                    LogUtil.d(TAG, "传递的通道号：" + channelCallback);
                    autoReadMeterByChannel();
                }
                break;
            case R.id.btnReRead:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                setTitleText("开始补抄...");
                isAutoRepeatRead = false;
                isReadChannelSuccess = false;
                clearReceiveData();
                repeatReadMeter();
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
            final AutoReadMeterActivityOld activity = (AutoReadMeterActivityOld) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_CONNECT:
                        activity.doConnectSuccess();
                        break;
                    case MSG_ERROR:
                        String errorMsg = (String) msg.obj;
                        LogUtil.e("有异常：" + errorMsg);
                        activity.doConnectFailed();
                        ToastUtil.showShortToast(errorMsg);
                        activity.closeDialog();
                        break;
                    case MSG_TOAST_INFO:
                        switch (msg.arg1) {
                            case 1:
                                activity.makeButtonEnable(true);
                                break;
                            case 2:
                                activity.makeButtonEnable(false);
                                break;
                            default:
                                break;
                        }
                        ToastUtil.showShortToast((String) msg.obj);
                        break;
                    case MSG_CLEAR_UI_LIST:
                        activity.uiUserList.clear();
                        activity.notifyUI();
                        break;
                    case MSG_NOTIFY_UI:
                        activity.notifyUI();
                        break;
                    case MSG_NOTIFY_TITLE:
                        activity.setTitleText((String) msg.obj);
                        break;
                    case MSG_READ_FINISH:
                        activity.showReadFinish();
                        activity.makeButtonEnable(true);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void initAdapter() {
        adapter = new AutoReadEmiAdapter(uiUserList);
        EmiRecycleViewDivider divider = new EmiRecycleViewDivider(
                this, LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(this, R.color.progress_dialog_gray_color));
        divider.setDividerMarginLeft(10);
        divider.setDividerMarginRight(10);
        rvUserInfo.addItemDecoration(divider);
        adapter.bindToRecyclerView(rvUserInfo);
        adapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                Intent intent = new Intent(mContext, UserInfoDetailActivity.class);
                EmiConstants.userInfoArrayList = new ArrayList<>();
                userInfoArrayList.addAll(uiUserList);
                intent.putExtra(EXTRA_CHANNEL_NUMBER, userInfoArrayList.get(position).channelNumber);
                intent.putExtra(EXTRA_POSITION, position);
                intent.putExtra(EXTRA_FILE_NAME, mFileName);
                startActivityForResult(intent, REQUEST_CODE_DETAIL_INFO);
            }
        });
    }

    private class BlueToothCallBackRunnable implements Runnable {
        @Override
        public void run() {
            blueToothReceiveCallBack();
        }
    }

    private void blueToothReceiveCallBack() {
        byte[] buffer = new byte[1024];
        int bytes;
        InputStream mmInStream = null;
        LogUtil.i(TAG, "线程已启动");
        while (!isStop) {
            try {
                if (socket != null) {
                    mmInStream = socket.getInputStream();
                }
                if (mmInStream == null) {
                    return;
                }
                if ((bytes = mmInStream.read(buffer)) > 0) {
                    Log.w(TAG, "已执行");
                    if (receiveCount == 0) {
                        listenBlueToothDataReceive();
                    }
                    receiveCount++;
                    for (int i = 0; i < bytes; i++) {
                        Log.w(TAG, "返回的数据：" + buffer[i]);
                        callBackStringBuffer.append(DigitalTrans.byteToHexString(buffer[i]));
                    }
                    Log.e(TAG, "返回的字符数据：" + callBackStringBuffer.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "错误：" + e.toString());
                if (!e.toString().contains("IOEx")) {
                    sendErrorMsg(e.toString());
                }
                isStop = true;
                if (Thread.interrupted()) {
                    stopThread();
                } else {
                    LogUtil.e("interrupted = false");
                    stopThread();
                }
                if (mmInStream != null) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private void sendErrorMsg(String errorMsg) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_ERROR;
        message.obj = errorMsg;
        mHandler.sendMessage(message);
    }

    /**
     * @param msg：需要显示的文本
     * @param clickEnable:按钮是否允许点击
     */
    private void sendToastMsg(String msg, boolean clickEnable) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_TOAST_INFO;
        message.obj = msg;
        if (clickEnable) {
            //表示按钮允许点击
            message.arg1 = 1;
        } else {
            //表示按钮不允许点击
            message.arg1 = 2;
        }
        mHandler.sendMessage(message);
    }

    private void stopThread() {
        try {
            LogUtil.w("直接抛出异常");
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void cancelConnect() {
        closeDialog();
        showDisconnect();
        clearReceiveData();
        makeButtonEnable(true);
    }


    private void showDialog(String text) {
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setLabel(text)
                .setCancellable(false)
                .show();
    }

    private void doConnectFailed() {
        isConnect = false;
        makeButtonEnable(true);
        //        stopTimer(blueToothDisposable);
        showDisconnect();
        closeDialog();
    }

    private void doConnectSuccess() {
        //        observeBlueToothConnection();
        isConnect = true;
        isStop = false;
        closeDialog();
        showConnectSuccess();
        makeButtonEnable(true);
        clearReceiveData();
        ThreadPoolManager.EXECUTOR.execute(new BlueToothCallBackRunnable());
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

    private void showConnectSuccess() {
        btnConnect.setText("蓝牙已连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        btnConnect.setBackgroundResource(R.drawable.btn_bg_green_sel);
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

    private void doDisConnect() {
        cancelTimers();
        if (socket != null) {
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
                    EmiConstants.bluetoothSocket = null;
                }
            }
            socket = null;
        }
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
                                sendEmptyMsg(MSG_CONNECT);
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


    private void showDisconnect() {
        isConnect = false;
        makeButtonEnable(true);
        btnConnect.setBackgroundResource(R.drawable.btn_bg_red);
        btnConnect.setText("蓝牙未连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
    }

    private void makeButtonEnable(boolean b) {
        btnRead.setEnabled(b);
        btnReRead.setEnabled(b);
        btnConnect.setEnabled(b);
        titleView.setEnabled(b);
        rvUserInfo.setEnabled(b);
    }


    @Override
    protected void onDestroy() {
        doDisConnect();
        updateReadResult(uiUserList);
        stopTimerList(disposableReadMeterList);
        stopTimerList(disposableReadChannelList);
        stopTimerList(disposableDataReceiveList);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private void sendEmptyMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }


    /**
     * 监听蓝牙数据接收
     */
    private void listenBlueToothDataReceive() {
        int delays;
        delays = 200;
        doEventByInterval(delays, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableDataReceiveList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (tempCount == receiveCount) {
                    LogUtil.i(TAG, "接收完毕~");
                    LogUtil.d("当前次数：" + receiveCount);
                    if (TextUtils.isEmpty(callBackStringBuffer.toString())) {
                        stopTimerList(disposableDataReceiveList);
                    }
                    LogUtil.i(TAG, "蓝牙返回的字符数据：" + callBackStringBuffer.toString());
                    boolean correct = checkCallbackDataCorrect(callBackStringBuffer.toString());
                    LogUtil.w(TAG, "是否正确:" + correct);
                    if (correct) {
                        isCallBackSuccess = true;
                        resolveCallBackData(mTag);
                    } else {
                        clearReceiveData();
                    }
                    onComplete();
                } else {
                    LogUtil.d(TAG, "正在接收中...");
                }
                tempCount = receiveCount;
            }

            @Override
            public void onError(Throwable e) {
                stopTimerList(disposableDataReceiveList);
                removeInvalidTimer(disposableDataReceiveList);
            }

            @Override
            public void onComplete() {
                stopTimerList(disposableDataReceiveList);
                removeInvalidTimer(disposableDataReceiveList);
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
        isCallBackSuccess = true;
        switch (tag) {
            case TAG_READ_CHANNEL:
                LogUtil.i("读通道号回调成功");
                if (EmiUtils.isNormalChannel()) {
                    resolveChannelNormalCallback(callBackStringBuffer.toString());
                } else {
                    resolveChannelSpecialCallBack(callBackStringBuffer.toString());
                }
                LogUtil.i(TAG, "读到的通道号：" + channelCallback);
                isReadChannelSuccess = true;
                clearReceiveData();
                //已经读取到通道号，开始自动抄表
                if (!isClickRepeat) {
                    //自动抄表
                    autoReadMeterByChannel();
                } else {
                    //补抄
                    repeatReadMeterByChannel();
                }
                break;
            case TAG_AUTO_READ_METER:
            case TAG_REPEAT_READ_METER:
                LogUtil.i("解析水表读数回调数据：" + callBackStringBuffer.toString());
                stopTimerList(disposableReadMeterList);
                removeInvalidTimer(disposableReadMeterList);
                resolveMeterDataCallback(callBackStringBuffer.toString());
                break;
            default:
                break;
        }
        clearReceiveData();
    }


    private void resolveMeterDataCallback(String callbackStr) {
        String meterInfo = "";
        int meterInfoLength = 14;
        String meterId;
        String meterFirmCode;
        String meterData = "";
        int currentData;
        int currentUsage;
        int beginIndex = callbackStr.indexOf(EMI_CALLBACK_CODE_BEGIN) + EMI_CALLBACK_CODE_BEGIN.length() + 2;
        if (beginIndex > -1) {
            if (callbackStr.length() > beginIndex + meterInfoLength) {
                meterInfo = callbackStr.substring(beginIndex, beginIndex + meterInfoLength);
                meterData = callbackStr.substring(beginIndex + meterInfoLength + 12, beginIndex + meterInfoLength + 12 + 4);
            }
            StringBuilder sbMeterInfo = new StringBuilder("");
            StringBuilder sbMeterData = new StringBuilder("");
            List<String> meterInfoList = splitStrToList(meterInfo);
            List<String> meterDataList = splitStrToList(meterData);
            for (int i = meterInfoList.size() - 1; i >= 0; i--) {
                sbMeterInfo.append(meterInfoList.get(i));
            }
            for (int i = meterDataList.size() - 1; i >= 0; i--) {
                sbMeterData.append(meterDataList.get(i));
            }
            meterInfo = sbMeterInfo.toString();
            meterData = sbMeterData.toString();
            if (meterInfo.length() == meterInfoLength) {
                isReadFinish = true;
                clearReceiveData();
                meterFirmCode = meterInfo.substring(0, 4);
                meterId = meterInfo.substring(4, meterInfoLength);
                //抄表成功
                LogUtil.w(TAG, "水表信息:" + meterInfo);
                LogUtil.i(TAG, "解析结果:" + meterId + "---" + meterFirmCode);
                LogUtil.i(TAG, "水表读数:" + meterData);
                UserInfo userInfo;
                if (hasClickAutoRead) {
                    LogUtil.i(TAG, "抄表成功回调(说明点击过)----->" + meterId);
                    userInfo = getUserInfoByMeterId(autoReadingUserList, meterId);
                } else {
                    LogUtil.d(TAG, "抄表成功回调(说明未点击过)----->" + meterId);
                    userInfo = getUserInfoByMeterId(failedUserList, meterId);
                }
                if (userInfo != null) {
                    try {
                        currentData = Integer.parseInt(meterData);
                    } catch (NumberFormatException e) {
                        currentData = 0;
                    }
                    //本次用水量
                    currentUsage = currentData - userInfo.lastdata;
                    if ((currentUsage > limitUsage) || currentUsage < 0) {
                        userInfo.state = STATE_WARNING;
                    } else {
                        userInfo.state = STATE_SUCCESS;
                    }
                    userInfo.curdata = currentData;
                    userInfo.channel = userInfo.channelNumber;
                    userInfo.curyl = currentUsage;
                    userInfo.curreaddate = TimeUtil.getCurrentTime();
                    updateReadResult(userInfo);
                    uiUserList.add(userInfo);
                    currentSuccessList.add(userInfo);
                    LogUtil.d(TAG, "列表数目：" + uiUserList.size());
                    removeSameUser(uiUserList);
                    notifyUItoBottom();
                }

            }
        }
    }


    /**
     * 普通市场通道号回调解析
     *
     * @param callbackStr
     */
    private void resolveChannelNormalCallback(String callbackStr) {
        int dataIndex = callbackStr.indexOf(FLAG_DATA_CODE_SPECIAL) + FLAG_DATA_CODE_NORMAL.length() + 2;
        String channel;
        if (dataIndex > -1) {
            channel = callbackStr.substring(dataIndex, dataIndex + 12);
            List<String> channelInfoList = EmiStringUtil.splitStrToList(channel);
            StringBuilder sbChannel = new StringBuilder("");
            String tempStr;
            for (int i = channelInfoList.size() - 1; i >= 0; i--) {
                tempStr = channelInfoList.get(i);
                tempStr = tempStr.substring(tempStr.length() - 1, tempStr.length());
                sbChannel.append(tempStr);
            }
            channelCallback = sbChannel.toString();
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
        if (dataIndex > -1) {
            channel = callbackStr.substring(dataIndex, dataIndex + 8);
            List<String> channelInfoList = EmiStringUtil.splitStrToList(channel);
            StringBuilder sbChannel = new StringBuilder("");
            for (int i = channelInfoList.size() - 1; i >= 0; i--) {
                sbChannel.append(channelInfoList.get(i));
            }
            channelCallback = sbChannel.toString();
        }
    }


    private List<UserInfo> getUserListByChannel(String channelNumber) {
        List<UserInfo> userInfoList = new ArrayList<>();
        if (!TextUtils.isEmpty(mFileName)) {
            if (channelNumber != null) {
                userInfoList.addAll(getSqOperator().findByChannel(mFileName, channelNumber));
            }
        } else {
            if (channelNumber != null) {
                userInfoList.addAll(getSqOperator().findByChannel(channelNumber));
            }
        }
        return userInfoList;
    }

    /**
     * 倒计时
     */
    private void countDownTimeReadChannel() {
        doEventByInterval(ONE_SECOND, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableReadChannelList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (isReadChannelSuccess) {
                    LogUtil.d(TAG, "已经读到通道号，本次计时取消");
                    stopTimerList(disposableReadChannelList);
                    onComplete();
                }
                if (aLong * ONE_SECOND > ONE_SECOND * 3) {
                    LogUtil.e(TAG, "未读到通道号，本次计时取消");
                    stopTimerList(disposableReadChannelList);
                    removeInvalidTimer(disposableReadChannelList);
                    ToastUtil.showShortToast("读取通道号失败");
                    makeButtonEnable(true);
                    setTitleText("通道号读取失败");
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimerList(disposableReadChannelList);
            }

            @Override
            public void onComplete() {
                stopTimerList(disposableReadChannelList);
                removeInvalidTimer(disposableReadChannelList);
                LogUtil.d(TAG, "倒计时已结束");
                clearReceiveData();
            }
        });
    }

    private void readingMeter(final List<UserInfo> userInfoList) {
        LogUtil.d(TAG, "开始抄表...");
        LogUtil.i(TAG, "userInfoList长度:" + userInfoList.size());
        String meterId;
        String firmCode;
        UserInfo userInfo;
        for (int i = 0; i < userInfoList.size(); i++) {
            userInfo = userInfoList.get(i);
            clearReceiveData();
            isReadFinish = false;
            isCallBackSuccess = false;
            if (userInfo.meteraddr != null && userInfo.firmCode != null) {
                meterId = DigitalTrans.patchHexString(userInfo.meteraddr, 10);
                firmCode = EmiUtils.formatFirmCode(userInfo.firmCode);
                byte[] cmdReadMeter = EmiUtils.getReadingCmdNormal(meterId, firmCode);
                clearReceiveData();
                LogUtil.i(TAG, "当前发送的读表指令：" + DigitalTrans.bytesConvertString(cmdReadMeter));
                if (isAutoRepeatRead) {
                    if (isClickRepeat) {
                        if (REPEAT_COUNT == 1) {
                            notifyTitleText("正在自动" + "补抄...", true);
                        } else {
                            int count = repeatReadCount + 1;
                            notifyTitleText("正在第" + count + "次自动" + "补抄...", true);
                        }
                    } else {
                        notifyTitleText("正在读" + meterId + "...", true);
                    }
                } else {
                    notifyTitleText("正在读" + meterId + "...", true);
                }
                currentUserInfo = userInfo;
                sendBTCmd(cmdReadMeter);
                if (i == 0) {
                    //读第一只水表时，额外延迟，避免第一只水表抄不到
                    delays(500);
                }
                delays(READING_DELAY);
                LogUtil.d("当前已延迟" + READING_DELAY + "次");
                stopTimerList(disposableReadMeterList);
                countDownTimeReadMeter();
                int count = 0;
                while (!isReadFinish) {
                    count++;
                    delays(50);
                    LogUtil.d("当前已执行" + count + "次");
                }
            }
        }
        //抄表结束
        delays(ONE_SECOND);
        if (mTag == TAG_AUTO_READ_METER) {
            if (REPEAT_COUNT > 0) {
                repeatReadMeter();
            } else {
                autoReadFinish();
            }
        } else if (mTag == TAG_REPEAT_READ_METER) {
            if (isAutoRepeatRead) {
                ++repeatReadCount;
                if (repeatReadCount < REPEAT_COUNT) {
                    repeatReadMeter();
                } else {
                    //自动抄表结束，说明此时肯定有未抄到的表，需要合并显示列表
                    autoReadFinish();
                }
            } else {
                sendToastMsg("补抄结束", true);
                notifyTitleText("补抄结束", false);
            }

        }
        for (UserInfo info : currentSuccessList) {
            for (int i = failedUserList.size() - 1; i >= 0; i--) {
                if (info.equals(failedUserList.get(i))) {
                    failedUserList.remove(i);
                    LogUtil.w(TAG, "已经移除了");
                    break;
                }
            }
        }
    }

    private void countDownTimeReadMeter() {
        doEventByInterval(100, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableReadMeterList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (isReadFinish) {
                    onComplete();
                }else {
                    if (aLong * 100 > READING_DELAY - 200) {
                        isReadFinish = true;
                        LogUtil.e(TAG, "失败表地址：" + currentUserInfo.meteraddr);
                        failedMeterId = currentUserInfo.meteraddr;
                        LogUtil.e(TAG, "读表失败,本次计时取消");
                        stopTimerList(disposableReadMeterList);
                        stopTimerList(disposableReadChannelList);
                        removeInvalidTimer(disposableReadMeterList);
                        removeInvalidTimer(disposableReadChannelList);
                        UserInfo userInfo;
                        if (hasClickAutoRead) {
                            LogUtil.i("当前数据源是从待抄列表查询到的(抄表失败)");
                            userInfo = getUserInfoByMeterId(autoReadingUserList, EmiStringUtil.formatNull(failedMeterId));
                        } else {
                            LogUtil.d("当前数据源是从失败列表查询到的(抄表失败)");
                            userInfo = getUserInfoByMeterId(failedUserList, EmiStringUtil.formatNull(failedMeterId));
                        }
                        //抄表失败
                        if (userInfo != null) {
                            userInfo.channel = userInfo.channelNumber;
                            userInfo.state = STATE_FAILED;
                            userInfo.data = 0;
                            userInfo.curyl = 0;
                            uiUserList.add(userInfo);
                            userInfo.curreaddate = TimeUtil.getCurrentTime();
                            lastState = getSqOperator().queryStateByUserInfo(userInfo.accountnum, userInfo.meteraddr);
                            LogUtil.d(TAG, "当前用户的上一次状态：" + lastState);
                            if (lastState == STATE_NO_READ || lastState == STATE_FAILED) {
                                updateReadResult(userInfo);
                            }
                            removeSameUser(uiUserList);
                            notifyUItoBottom();
                        }
                        onComplete();
                    }
                }
                LogUtil.d(TAG, "当前已执行次数：" + aLong);
                //todo:该处需要后期优化

            }

            @Override
            public void onError(Throwable e) {
                stopTimerList(disposableReadMeterList);
            }

            @Override
            public void onComplete() {
                stopTimerList(disposableReadMeterList);
                removeInvalidTimer(disposableReadMeterList);
                LogUtil.i(TAG, "本次计时取消");
            }
        });
    }


    private UserInfo getUserInfoByMeterId(ArrayList<UserInfo> userInfoArrayList, String meterId) {
        meterId = EmiStringUtil.clearFirstZero(meterId);
        LogUtil.d(TAG, "需要查询的水表id：" + meterId);
        for (UserInfo userInfo : userInfoArrayList) {
            if (meterId.equals(userInfo.meteraddr)) {
                LogUtil.i(TAG, "获取的信息:" + userInfo.username);
                LogUtil.i(TAG, "获取的信息:" + userInfo.accountnum);
                LogUtil.i(TAG, "获取的信息:" + userInfo.channelNumber);
                return userInfo;
            }
        }
        return null;
    }

    /**
     * 根据通道号自动抄表
     */
    private void autoReadMeterByChannel() {
        mTag = TAG_AUTO_READ_METER;
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(channelCallback)) {
                    autoReadingUserList.clear();
                    autoReadingUserList.addAll(getUserListByChannel(EmiStringUtil.clearFirstZero(channelCallback)));
                    removeEmptyUserInfo(autoReadingUserList);
                } else {
                    sendErrorMsg("通道号有误");
                }
                if (autoReadingUserList.isEmpty()) {
                    sendToastMsg("当前通道无匹配水表信息", true);
                    sendEmptyMsg(MSG_READ_FINISH);
                } else {
                    mFileName = autoReadingUserList.get(0).filename;
                    LogUtil.d(TAG, "查询到的文件名:" + mFileName);
                    readingMeter(autoReadingUserList);
                }
            }
        });
    }

    /**
     * 补抄抄表
     */
    private void repeatReadMeterByChannel() {
        mTag = TAG_REPEAT_READ_METER;
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(channelCallback)) {
                    if (!hasClickAutoRead) {
                        //如果未点击过自动抄表，则失败列表将从数据库中获取
                        List<UserInfo> userInfoList = getUserListByChannel(EmiStringUtil.clearFirstZero(channelCallback));
                        failedUserList.addAll(selectUserByState(userInfoList, STATE_FAILED));
                    } else {
                        //点击过自动抄表需要从自动抄表中获取失败列表
                        failedUserList.addAll(selectUserByState(autoReadingUserList, STATE_FAILED));
                    }
                } else {
                    sendErrorMsg("通道号有误");
                }
                removeSameUser(failedUserList);
                if (failedUserList.isEmpty()) {
                    if (isAutoRepeatRead) {
                        //自动补抄全部抄到
                        autoReadFinish();
                    } else {
                        //点击按钮的补抄
                        sendToastMsg("无需补抄", true);
                        sendEmptyMsg(MSG_READ_FINISH);
                    }
                } else {
                    sendEmptyMsg(MSG_CLEAR_UI_LIST);
                    mFileName = failedUserList.get(0).filename;
                    readingMeter(failedUserList);
                }
            }
        });
    }


    /**
     * 读通道号
     */
    private void readChannel() {
        mTag = TAG_READ_CHANNEL;
        isCallBackSuccess = false;
        isReadChannelSuccess = false;
        cmd = EmiUtils.getCmdReadChannel(EmiUtils.isNormalChannel());
        sendBTCmd(cmd);
        countDownTimeReadChannel();
    }

    private void notifyUItoBottom() {
        rvUserInfo.scrollToPosition(adapter.getItemCount() - 1);
        adapter.notifyDataSetChanged();
    }


    private void updateReadResult(UserInfo userInfo) {
        getSqOperator().updateData(mFileName, userInfo.accountnum, userInfo.curdata, userInfo.curyl, userInfo.state, TimeUtil.getCurrentTime(), userInfo.channel);
    }

    /**
     * 批量更新数据库数据
     *
     * @param userInfoList
     */
    private void updateReadResult(List<UserInfo> userInfoList) {
        getSqOperator().updateData(userInfoList);
    }

    private void stopTimerList(List<Disposable> disposableList) {
        for (Disposable disposable : disposableList) {
            stopTimer(disposable);
        }
    }




    private void repeatReadMeter() {
        removeSameUser(currentSuccessList);
        isClickRepeat = true;
        if (EmiUtils.isNeedChannel()) {
            readChannel();
        } else {
            repeatReadMeterByChannel();
        }
    }

    private void notifyUI() {
        adapter.notifyDataSetChanged();
    }

    /**
     * 自动抄表结束
     */
    private void autoReadFinish() {
        for (UserInfo info : currentSuccessList) {
            for (int i = autoReadingUserList.size() - 1; i >= 0; i--) {
                if (info.equals(autoReadingUserList.get(i))) {
                    autoReadingUserList.set(i, info);
                    break;
                }
            }
        }
        uiUserList.clear();
        uiUserList.addAll(autoReadingUserList);
        removeSameUser(uiUserList);
        delays(ONE_SECOND);
        sendEmptyMsg(MSG_NOTIFY_UI);
        sendToastMsg("抄表结束", true);
        notifyTitleText("抄表结束", false);
        sendEmptyMsg(MSG_READ_FINISH);
    }


    private void notifyTitleText(String text, boolean useNewMessage) {
        Message message;
        if (useNewMessage) {
            message = new Message();
        } else {
            message = mHandler.obtainMessage();
        }
        message.what = MSG_NOTIFY_TITLE;
        message.obj = text;
        mHandler.sendMessage(message);
    }


    private void setTitleText(String text) {
        titleView.setTitle(text);
    }

    private void showReadFinish() {
        setTitleText("抄表结束");
        btnReRead.setText("补抄");
        btnRead.setText("开始抄表");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d("resultCode====" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_DETAIL_INFO:
                if (resultCode == REQUEST_CODE_DETAIL_INFO && data != null) {
                    Bundle bundle = data.getBundleExtra(EXTRA_BUNDLE);
                    ArrayList<UserInfo> editUserInfoList = (ArrayList<UserInfo>) bundle.getSerializable(EXTRA_EDIT_DATA_LIST);
                    doFinishCallBack(editUserInfoList);
                }
                break;

            case REQUEST_CODE_USER_LIST:
                if (resultCode == RESULT_CODE_CHANNEL_DATA && data != null) {
                    ArrayList<UserInfo> editUserInfoList = (ArrayList<UserInfo>) data.getSerializableExtra(EXTRA_USER_LIST);
                    doFinishCallBack(editUserInfoList);
                }
                break;
            default:
                break;
        }
    }

    private void doFinishCallBack(ArrayList<UserInfo> callBackList) {
        UserInfo currentUserInfo;
        if (callBackList != null) {
            for (UserInfo editUserInfo : callBackList) {
                LogUtil.i(TAG, "当前用户：" + editUserInfo.accountnum + "当前读数:" + editUserInfo.curdata);
                for (int i = 0; i < uiUserList.size(); i++) {
                    currentUserInfo = uiUserList.get(i);
                    if (currentUserInfo.accountnum.equals(editUserInfo.accountnum) && currentUserInfo.meteraddr.equals(editUserInfo.meteraddr)) {
                        currentUserInfo.state = editUserInfo.state;
                        LogUtil.i(TAG, "修改的用户：" + editUserInfo.state);
                        currentUserInfo.curdata = editUserInfo.curdata;
                        currentUserInfo.curyl = editUserInfo.curyl;
                        currentUserInfo.curreaddate = editUserInfo.curreaddate;
                    }
                }
            }
            updateReadResult(uiUserList);
            adapter.notifyDataSetChanged();
        } else {
            LogUtil.i(TAG, "集合为空");
        }
    }

}
