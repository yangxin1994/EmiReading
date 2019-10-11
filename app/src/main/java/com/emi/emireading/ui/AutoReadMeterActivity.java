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
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.ui.debug.SingleMeterDebugActivity;
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
import static com.emi.emireading.core.config.EmiConstants.EMI_CALLBACK_CODE_END;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_FIRM_CODE;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_ID;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_POSITION;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_SKIP_TAG;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_USER_LIST;
import static com.emi.emireading.core.config.EmiConstants.IO_EXCEPTION;
import static com.emi.emireading.core.config.EmiConstants.METER_INFO_LENGTH;
import static com.emi.emireading.core.config.EmiConstants.MSG_BLUETOOTH_CONNECT;
import static com.emi.emireading.core.config.EmiConstants.MSG_ERROR;
import static com.emi.emireading.core.config.EmiConstants.NEW_LINE;
import static com.emi.emireading.core.config.EmiConstants.ONE_SECOND;
import static com.emi.emireading.core.config.EmiConstants.PREF_READING_DELAY;
import static com.emi.emireading.core.config.EmiConstants.PREF_WATER_WARNING_LINE;
import static com.emi.emireading.core.config.EmiConstants.REQUEST_CODE_OPEN_BLUETOOTH;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.core.config.EmiConstants.userInfoArrayList;
import static com.emi.emireading.core.utils.EmiStringUtil.splitStrToList;
import static com.emi.emireading.ui.ChannelListActivity.EXTRA_CHANNEL;
import static com.emi.emireading.ui.MeterQueryActivity.REQUEST_CODE_DETAIL_INFO;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_BUNDLE;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_EDIT_DATA_LIST;
import static com.emi.emireading.ui.UserInfoListActivity.RESULT_CODE_CHANNEL_DATA;

/**
 * @author :zhoujian
 * @description : 自动抄表
 * @company :翼迈科技
 * @date 2018年06月14日下午 02:54
 * @Email: 971613168@qq.com
 */

public class AutoReadMeterActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_SINGLE_DEBUG = 101;
    public static final int SKIP_TAG_AUTO_READ_METER = 1;
    private Button btnConnect;
    private Button btnAutoRead;
    private BluetoothDevice bluetoothDevice = null;
    private Button btnReRead;
    private TitleView titleView;
    private byte[] cmd = {};
    private RecyclerView rvUserInfo;
    private Context mContext;
    private final static String FLAG_DATA_CODE_SPECIAL = "0189";
    private final static String FLAG_DATA_CODE_NORMAL = "AA";
    private final static String FLAG_DATA_METER_CALLBACK_CODE = "16";
    private final static String READ_CHANNEL_FAILED = "通道号读取失败";
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
    private static final int MSG_TOAST_INFO = 1001;
    private static final int MSG_CLEAR_UI_LIST = 1002;
    private static final int MSG_NOTIFY_UI = 1003;
    private static final int MSG_NOTIFY_TITLE = 1004;
    private static final int MSG_READ_FINISH = 1005;
    private static final int MSG_NOTIFY_ORIGINAL = 1006;
    private int receiveCount;
    private final int TAG_READ_CHANNEL = 1;
    private final int TAG_AUTO_READ_METER = 2;
    /**
     * 补抄标志
     */
    private final int TAG_REPEAT_READ_METER = 3;
    private AutoReadEmiAdapter adapter;
    private int mTag;
    private ArrayList<UserInfo> uiUserList = new ArrayList<>();
    private String callbackValue;
    private ArrayList<Disposable> disposableDataReceiveList = new ArrayList<>();
    /**
     * 页面是否已经销毁
     */
    private boolean isOnDestroy;
    /**
     * 正常、补录、异常的用户信息
     */
    private ArrayList<UserInfo> normalUserList = new ArrayList<>();
    private static final int REQUEST_CODE_USER_LIST = 10;
    private int readingDelay;
    /**
     * 收到回调的次数
     */
    private int tempCount = -1;
    private String channelCallback;
    private boolean isReadFinish = false;
    private boolean itemClickable = false;
    private int limitUsage;
    private String currentMeterId;
    private String currentUserId;
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
    /**
     * 是否是手动点击的补抄
     */
    private boolean isClickRepeat = false;
    private boolean hasClickAutoRead = false;
    private boolean isAutoRepeatRead = false;
    private boolean isReadChannelSuccess = false;
    private int repeatReadCount;
    private ArrayList<Disposable> disposableReadMeterList = new ArrayList<>();
    private ArrayList<Disposable> disposableReadChannelList = new ArrayList<>();

    private ArrayList<Disposable> disposableConnectedList = new ArrayList<>();

    @Override
    protected int getContentLayout() {
        return R.layout.emi_auto_read_meter_activity;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        EmiUtils.keepScreenLongLight(this, true);
        mFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        channelCallback = getIntent().getStringExtra(EXTRA_CHANNEL);
        REPEAT_COUNT = EmiUtils.getAutoRepeatCount();
        LogUtil.d(TAG, "设置的补抄次数：" + REPEAT_COUNT);
    }

    @Override
    protected void initUI() {
        btnConnect = findViewById(R.id.btnConnect);
        btnAutoRead = findViewById(R.id.btnAutoRead);
        btnReRead = findViewById(R.id.btnReRead);
        titleView = findViewById(R.id.titleView);
        btnConnect.setOnClickListener(this);
        btnAutoRead.setOnClickListener(this);
        rvUserInfo = findViewById(R.id.rvUserInfo);
        rvUserInfo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        btnReRead.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        limitUsage = PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50);
        READING_DELAY = PreferenceUtils.getInt(PREF_READING_DELAY, 1000);
        EmiConfig.AUTO_READ_TYPE = EmiUtils.getAutoReadType();
        initAdapter();
        resetDelayTime();
        titleView.setRightIconText("统计");
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UserInfoListActivity.class);
                intent.putExtra(EXTRA_CHANNEL, EmiStringUtil.clearFirstZero(channelCallback));
                intent.putExtra(EXTRA_FILE_NAME, mFileName);
                intent.putExtra(EXTRA_SKIP_TAG, SKIP_TAG_AUTO_READ_METER);
                startActivityForResult(intent, REQUEST_CODE_USER_LIST);
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                doConnectBlueTooth();
                break;
            case R.id.btnAutoRead:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                btnAutoRead.setText("准备抄表...");
                titleView.setTitle("开始抄表...");
                btnReRead.setText("补抄");
                hasClickAutoRead = true;
                isAutoRepeatRead = false;
                uiUserList.clear();
                isClickRepeat = false;
                makeButtonEnable(false);
                clearReceiveData();
                uiUserList.clear();
                notifyUItoBottom();
                repeatReadCount = 0;
                if (EmiUtils.isNeedChannel()) {
                    readingChannel();
                } else {
                    autoReadMeterByChannel(channelCallback);
                }
                break;
            case R.id.btnReRead:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                btnAutoRead.setText("开始抄表");
                btnReRead.setText("正在补抄...");
                titleView.setTitle("开始补抄...");
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
            final AutoReadMeterActivity activity = (AutoReadMeterActivity) mWeakReference.get();
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
                        if (READ_CHANNEL_FAILED.equals(msg.obj)) {
                            activity.showReadFinish();
                        }
                        if (activity.isStop) {
                            activity.makeButtonOriginal();
                        }
                        break;
                    case MSG_CLEAR_UI_LIST:
                        activity.uiUserList.clear();
                        activity.notifyUI();
                        break;
                    case MSG_NOTIFY_UI:
                        activity.notifyUI();
                        activity.notifyReadButton(msg.arg1, msg.arg2);
                        break;
                    case MSG_NOTIFY_TITLE:
                        activity.setTitleText((String) msg.obj);
                        break;
                    case MSG_READ_FINISH:
                        activity.showReadFinish();
                        activity.makeButtonEnable(true);
                        break;
                    case MSG_NOTIFY_ORIGINAL:
                        activity.makeButtonOriginal();
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
                if (itemClickable) {
                    Intent intent = new Intent(mContext, UserInfoDetailActivity.class);
                    EmiConstants.userInfoArrayList = new ArrayList<>();
                    userInfoArrayList.addAll(uiUserList);
                    intent.putExtra(EXTRA_CHANNEL_NUMBER, userInfoArrayList.get(position).channelNumber);
                    intent.putExtra(EXTRA_POSITION, position);
                    intent.putExtra(EXTRA_FILE_NAME, mFileName);
                    startActivityForResult(intent, REQUEST_CODE_DETAIL_INFO);
                } else {
                    ToastUtil.showShortToast("抄表中暂时不可点击哦");
                }
            }
        });

        if (EmiUtils.isDebugMode()) {
            adapter.setOnItemLongClickListener(new BaseEmiAdapter.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(BaseEmiAdapter adapter, View view, int position) {
                    doSkipSingleDebugActivity(uiUserList, position);
                    return true;
                }
            });
        }
    }

    /**
     * 蓝牙回调
     */
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
        while (!isStop) {
            try {
                if (socket != null) {
                    mmInStream = socket.getInputStream();
                }
                if (mmInStream == null) {
                    return;
                }
                if ((bytes = mmInStream.read(buffer)) > 0) {
                    if (receiveCount == 0) {
                        listenBlueToothDataReceive();
                    }
                    receiveCount++;
                    for (int i = 0; i < bytes; i++) {
                        callBackStringBuffer.append(DigitalTrans.byteToHexString(buffer[i]));
                    }
                }
            } catch (Exception e) {
                if (!e.toString().contains(IO_EXCEPTION)) {
                    sendErrorMsg(e.toString());
                }
                isStop = true;
                if (Thread.interrupted()) {
                    stopThread();
                } else {
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
        isConnect = true;
        isStop = false;
        closeDialog();
        showConnectSuccess();
        makeButtonEnable(true);
        clearReceiveData();
        ThreadPoolManager.EXECUTOR.execute(new BlueToothCallBackRunnable());
        listenBlueToothConnect();
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


    private void showDisconnect() {
        isConnect = false;
        makeButtonEnable(true);
        btnConnect.setBackgroundResource(R.drawable.btn_bg_red);
        btnConnect.setText("蓝牙未连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
    }

    private void makeButtonEnable(boolean b) {
        btnAutoRead.setEnabled(b);
        btnReRead.setEnabled(b);
        btnConnect.setEnabled(b);
        titleView.setRightButtonIsShow(b);
        rvUserInfo.setEnabled(b);
        itemClickable = b;
    }


    @Override
    protected void onDestroy() {
        EmiUtils.keepScreenLongLight(this, false);
        doDisConnect();
        isOnDestroy = true;
        removeFailedUser(uiUserList);
        updateReadResult(uiUserList);
        stopTimer(disposableReadMeterList);
        stopTimer(disposableReadChannelList);
        stopTimer(disposableDataReceiveList);
        stopTimer(disposableConnectedList);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private void sendEmptyMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }


    private void clearReceiveData() {
        receiveCount = 0;
        tempCount = -1;
        callBackStringBuffer.setLength(0);
        LogUtil.w(TAG, "数据已被清空");
    }

    /**
     * 检查回调数据是否正确
     *
     * @param callbackStr
     * @return
     */
    private boolean checkCallbackBlueToothDataCorrect(String callbackStr) {
        String endCode = EmiStringUtil.getLastStr(callbackStr, 2);
        if (callbackStr.contains(EMI_CALLBACK_CODE_BEGIN) && callbackStr.contains(endCode)) {
            int beginIndex = callbackStr.indexOf("68");
            int csIndex = callbackStr.lastIndexOf(EMI_CALLBACK_CODE_END) - 2;
            if (beginIndex > -1 && csIndex > -1) {
                String csValue = callbackStr.substring(csIndex, callbackStr.length() - 2);
                int csNumber = DigitalTrans.stringConvertInt(csValue, 16);
                String checkString = callbackStr.substring(beginIndex, csIndex);
                if (mTag != TAG_READ_CHANNEL) {
                    if (checkString.length() > INDEX_CHECK_METER_CALLBACK) {
                        String checkMeterFlag = checkString.substring(INDEX_CHECK_METER_CALLBACK, INDEX_CHECK_METER_CALLBACK + 2);
                        if (!checkMeterFlag.equals(FLAG_DATA_METER_CALLBACK_CODE)) {
                            return false;
                        }
                    }
                }
                byte[] checkArray = DigitalTrans.stringConvertBytes(checkString);
                return checkCs(checkArray, csNumber);
            }
        }
        return false;
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
            StringBuilder sbMeterInfo = new StringBuilder();
            StringBuilder sbMeterData = new StringBuilder();
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
                meterFirmCode = meterInfo.substring(0, 4);
                meterId = meterInfo.substring(4, meterInfoLength);
                //抄表成功
                LogUtil.w(TAG, "水表信息:" + meterInfo);
                LogUtil.i(TAG, "解析结果:" + meterId + "---" + meterFirmCode);
                LogUtil.i(TAG, "水表读数:" + meterData);
                UserInfo userInfo;
                if (hasClickAutoRead) {
                    LogUtil.i(TAG, "抄表成功回调(说明点击过)----->" + meterId);
                    userInfo = getUserInfoByMeterId(autoReadingUserList, meterInfo, meterId);
                } else {
                    LogUtil.d(TAG, "抄表成功回调(说明未点击过)----->" + meterId);
                    userInfo = getUserInfoByMeterId(failedUserList, meterInfo, meterId);
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
                    removeSameUser(uiUserList);
                    notifyUItoBottom();
                    isReadFinish = true;
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
        int dataIndex = callbackStr.indexOf(FLAG_DATA_CODE_NORMAL) + FLAG_DATA_CODE_NORMAL.length() + 2;
        String channel;
        if (dataIndex > -1) {
            channel = callbackStr.substring(dataIndex, dataIndex + 12);
            List<String> channelInfoList = EmiStringUtil.splitStrToList(channel);
            StringBuilder sbChannel = new StringBuilder();
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


    private UserInfo getUserInfoByMeterId(ArrayList<UserInfo> userInfoArrayList, String meterInfo, String meterId) {
        meterId = EmiStringUtil.clearFirstZero(meterId);
        for (UserInfo userInfo : userInfoArrayList) {
            if (meterId.equals(userInfo.meteraddr) || userInfo.meteraddr.equals(meterInfo)) {
                return userInfo;
            }
        }
        return null;
    }


    /**
     * 自动抄表结束
     */
    private void autoReadFinish() {
        for (UserInfo userInfo : autoReadingUserList) {
            LogUtil.w("列表数据：用户名：" + userInfo.accountnum + ",状态：" + userInfo.state + ",读数：" + userInfo.curdata);
        }
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
        //        replaceFailedUser(uiUserList);
        sendEmptyMsg(MSG_NOTIFY_UI);
        sendToastMsg("抄表结束", true);
        notifyTitleText("抄表结束", false);
        sendEmptyMsg(MSG_READ_FINISH);
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
        btnAutoRead.setText("开始抄表");
    }

    private void showStartFinish() {
        setTitleText("待抄表");
        btnReRead.setText("补抄");
        btnAutoRead.setText("开始抄表");
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
                    doEditFinishCallBack(editUserInfoList);
                }
                break;

            case REQUEST_CODE_USER_LIST:
                if (resultCode == RESULT_CODE_CHANNEL_DATA && data != null) {
                    ArrayList<UserInfo> editUserInfoList = (ArrayList<UserInfo>) data.getSerializableExtra(EXTRA_USER_LIST);
                    doEditFinishCallBack(editUserInfoList);
                }
                break;
            case REQUEST_CODE_OPEN_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    connectBlueTooth();
                }
                break;
            default:
                break;
        }
    }

    private void doEditFinishCallBack(ArrayList<UserInfo> callBackList) {
        if (callBackList != null) {
            LogUtil.i(TAG, "当前修改的用户数目：" + callBackList.size());
            for (UserInfo editUserInfo : callBackList) {
                LogUtil.i(TAG, "当前用户：" + editUserInfo.accountnum + "当前读数:" + editUserInfo.curdata);
                modifyUseList(uiUserList, editUserInfo);
                modifyUseList(normalUserList, editUserInfo);
            }
            List<UserInfo> saveList = new ArrayList<>();
            saveList.addAll(uiUserList);
            removeFailedUser(saveList);
            updateReadResult(saveList);
            adapter.notifyDataSetChanged();
        } else {
            LogUtil.i(TAG, "集合为空");
        }
    }

    private void listenBlueToothDataReceive() {
        doEventByInterval(90, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableDataReceiveList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (tempCount == receiveCount) {
                    LogUtil.i(TAG, "接收完毕~");
                    callbackValue = callBackStringBuffer.toString();
                    LogUtil.w(TAG, "蓝牙返回的字符数据：" + callbackValue);
                    boolean isCorrect = checkCallbackBlueToothDataCorrect(callbackValue);
                    if (isCorrect) {
                        readingDelay = EmiConfig.READING_DELAY;
                        switch (mTag) {
                            case TAG_READ_CHANNEL:
                                stopTimer(disposableReadChannelList);
                                resolveChannelNumber(callbackValue);
                                LogUtil.w(TAG, "读到的通道号:" + channelCallback);
                                //已经读取到通道号，开始自动抄表
                                if (!isClickRepeat) {
                                    //自动抄表
                                    autoReadMeterByChannel(channelCallback);
                                } else {
                                    //补抄
                                    repeatReadMeterByChannel(channelCallback);
                                }
                                break;
                            case TAG_AUTO_READ_METER:
                            case TAG_REPEAT_READ_METER:
                                stopTimer(disposableReadMeterList);
                                removeInvalidTimer(disposableReadMeterList);
                                LogUtil.i(TAG, "蓝牙返回的读表字符数据：" + callbackValue);
                                resolveMeterDataCallback(callbackValue);
                                LogUtil.i(TAG, "表地址：" + currentMeterId + "--->是否抄表成功:true");
                                isReadFinish = true;
                                break;
                            default:
                                break;
                        }
                    } else {
                        readingDelay = EmiConfig.READING_DELAY + 100;
                    }
                    onComplete();
                } else {
                    LogUtil.d(TAG, "正在接收中...");
                }
                tempCount = receiveCount;
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(disposableDataReceiveList);
                removeInvalidTimer(disposableDataReceiveList);
            }

            @Override
            public void onComplete() {
                stopTimer(disposableDataReceiveList);
                removeInvalidTimer(disposableDataReceiveList);
            }
        });
    }

    private void readingMeter(List<UserInfo> userInfoList) {
        if (!isClickRepeat) {
            selectUserInfoByReadType(userInfoList);
        }
        LogUtil.d(TAG, "准备抄表...");
        LogUtil.i(TAG, "userInfoList长度:" + userInfoList.size());
        selectCurrentUserList(channelCallback, failedUserList);
        String meterId;
        String firmCode;
        UserInfo userInfo;
        for (int i = 0; i < userInfoList.size(); i++) {
            if (!isStop) {
                userInfo = userInfoList.get(i);
                if (userInfo.meteraddr != null && userInfo.firmCode != null) {
                    LogUtil.i(TAG, "当前正在抄表地址:" + userInfo.meteraddr);
                    meterId = DigitalTrans.patchHexString(userInfo.meteraddr, 10);
                    currentUserId = userInfo.accountnum;
                    currentMeterId = userInfo.meteraddr;
                    if (userInfo.state == STATE_ALL || userInfo.state == STATE_NO_READ) {
                        userInfo.state = STATE_FAILED;
                    }
                    firmCode = EmiUtils.formatFirmCode(userInfo.firmCode);
                    byte[] cmdReadMeter = {0x00};
                    if (!TextUtils.isEmpty(userInfo.meteraddr)) {
                        if (userInfo.meteraddr.length() == METER_INFO_LENGTH) {
                            cmdReadMeter = EmiUtils.getReadingCmdWholeNormal(userInfo.meteraddr);
                            LogUtil.w(TAG, "发送的读表指令：" + DigitalTrans.byte2hex(cmdReadMeter));
                        } else {
                            cmdReadMeter = EmiUtils.getReadingCmdNormal(meterId, firmCode);
                            LogUtil.d(TAG, "发送的读表指令：" + DigitalTrans.byte2hex(cmdReadMeter));
                        }
                    }
                    clearReceiveData();
                    sendBTCmd(cmdReadMeter);
                    listenReadFailed();
                    if (i == 0) {
                        //抄第一只表时 增加2秒延时
                        delays(readingDelay + (ONE_SECOND*2));
                    } else {
                        delays(readingDelay);
                    }
                    showReading(currentMeterId);
                    int count = 0;
                    int delayTime = 50;
                    int timeUp = EmiConfig.READING_DELAY / delayTime;
                    LogUtil.e("超时时间：" + timeUp);
                    while (!isReadFinish) {
                        count++;
                        delays(delayTime);
                        if (count > timeUp) {
                            LogUtil.w("超时时间：已执行");
                            isReadFinish = true;
                            sendToastMsg("读表超时", false);
                        }
                        LogUtil.d("当前已执行" + count + "次");
                    }
                    isReadFinish = false;
                }
                Message message = mHandler.obtainMessage();
                message.what = MSG_NOTIFY_UI;
                message.arg1 = i + 1;
                message.arg2 = userInfoList.size();
                sendMsg(message);
            } else {
                if (!isOnDestroy) {
                    sendToastMsg("检测到蓝牙已断开,抄表中断！", true);
                }
                break;
            }
        }
        if (!isOnDestroy && !isStop) {
            LogUtil.w(TAG, "------抄表结束------");
            //抄表结束
            notifyTitleText("数据分析中...", false);
            delays(ONE_SECOND);
            sendEmptyMsg(MSG_NOTIFY_ORIGINAL);
            doReadFinishByTag();
        }
    }

    private void listenReadFailed() {
        doEventCountDown(readingDelay + 300, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableReadMeterList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (!isReadFinish) {
                    LogUtil.e(TAG, "时辰已到,抄表失败了");
                    LogUtil.e(TAG, "表地址：" + currentMeterId + "--->是否抄表成功：" + false);
                    UserInfo userInfo;
                    if (hasClickAutoRead) {
                        LogUtil.i("当前数据源是从待抄列表查询到的(抄表失败)");
                        userInfo = getUserInfoByMeterId(autoReadingUserList, EmiStringUtil.formatNull(currentMeterId), EmiStringUtil.formatNull(currentMeterId));
                    } else {
                        LogUtil.d("当前数据源是从失败列表查询到的(抄表失败)");
                        userInfo = getUserInfoByMeterId(failedUserList, EmiStringUtil.formatNull(currentMeterId), EmiStringUtil.formatNull(currentMeterId));
                    }
                    //抄表失败
                    if (userInfo != null) {
                        userInfo.channel = userInfo.channelNumber;
                        userInfo.data = 0;
                        userInfo.curyl = 0;
                        userInfo.state = STATE_FAILED;
                        uiUserList.add(userInfo);
                        userInfo.curreaddate = TimeUtil.getCurrentTime();
                        lastState = getSqOperator().queryStateByUserInfo(userInfo.accountnum, userInfo.meteraddr);
                        if (lastState == STATE_NO_READ || lastState == STATE_FAILED || lastState == STATE_ALL) {
                            updateReadResult(userInfo);
                        }
                        removeSameUser(uiUserList);
                        notifyUItoBottom();
                        isReadFinish = true;
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(disposableReadMeterList);
            }

            @Override
            public void onComplete() {
                LogUtil.w(TAG, "已执行--->onComplete");
                stopTimer(disposableReadMeterList);
                removeInvalidTimer(disposableReadMeterList);
            }
        });
    }


    /**
     * 读通道号
     */
    private void readingChannel() {
        mTag = TAG_READ_CHANNEL;
        isReadChannelSuccess = false;
        clearReceiveData();
        cmd = EmiUtils.getCmdReadChannel(EmiUtils.isNormalChannel());
        sendBTCmd(cmd);
        listenReadChannelTimer();
    }


    private void listenReadChannelTimer() {
        doEventCountDown(ONE_SECOND * 3, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableReadChannelList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (!isReadChannelSuccess) {
                    //读取通道号失败
                    sendToastMsg(READ_CHANNEL_FAILED, true);
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(disposableReadChannelList);
            }

            @Override
            public void onComplete() {
                stopTimer(disposableReadChannelList);
                removeInvalidTimer(disposableReadChannelList);
            }
        });
    }

    /**
     * 解析蓝牙回调的通道板号
     *
     * @param valueStr
     */
    private void resolveChannelNumber(String valueStr) {
        if (EmiUtils.isNormalChannel()) {
            resolveChannelNormalCallback(valueStr);
        } else {
            resolveChannelSpecialCallBack(valueStr);
        }
    }

    private void autoReadMeterByChannel(final String channelNumber) {
        mTag = TAG_AUTO_READ_METER;
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(channelNumber)) {
                    autoReadingUserList.clear();
                    normalUserList.clear();
                    autoReadingUserList.addAll(getUserListByChannel(EmiStringUtil.clearFirstZero(channelNumber)));
                    removeEmptyUserInfo(autoReadingUserList);
                    UserInfo copyUser;
                    for (UserInfo userInfo : autoReadingUserList) {
                        if (userInfo != null) {
                            if (userInfo.state == STATE_SUCCESS || userInfo.state == STATE_WARNING || userInfo.state == STATE_PEOPLE_RECORDING) {
                                copyUser = userInfo.copy();
                                normalUserList.add(copyUser);
                            }
                        }
                    }
                } else {
                    sendToastMsg("通道号错误", true);
                }
                if (autoReadingUserList.isEmpty()) {
                    sendToastMsg("通道号不匹配或待抄表数量为0", true);
                    sendEmptyMsg(MSG_READ_FINISH);
                } else {
                    mFileName = autoReadingUserList.get(0).filename;
                    readingMeter(autoReadingUserList);
                }
            }
        });
    }

    /**
     * 抄表结束
     */
    private void doReadFinishByTag() {
        switch (mTag) {
            case TAG_AUTO_READ_METER:
                if (REPEAT_COUNT > 0) {
                    isAutoRepeatRead = true;
                    repeatReadMeter();
                    LogUtil.d(TAG, "自动补抄");
                } else {
                    autoReadFinish();
                }
                break;
            case TAG_REPEAT_READ_METER:
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
                    selectCurrentUserList(channelCallback, failedUserList);
                    sendEmptyMsg(MSG_READ_FINISH);
                }
                break;
            default:
                break;
        }
        for (UserInfo info : currentSuccessList) {
            for (int i = failedUserList.size() - 1; i >= 0; i--) {
                if (info.equals(failedUserList.get(i))) {
                    failedUserList.remove(i);
                    break;
                }
            }
        }
    }


    private void repeatReadMeter() {
        removeSameUser(currentSuccessList);
        isClickRepeat = true;
        if (EmiUtils.isNeedChannel()) {
            readingChannel();
        } else {
            repeatReadMeterByChannel(channelCallback);
        }
    }

    /**
     * 补抄抄表
     */
    private void repeatReadMeterByChannel(final String channelNumber) {
        mTag = TAG_REPEAT_READ_METER;
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(channelNumber)) {
                    if (!hasClickAutoRead) {
                        //如果未点击过自动抄表，则失败列表将从数据库中获取
                        List<UserInfo> userInfoList = getUserListByChannel(EmiStringUtil.clearFirstZero(channelNumber));
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

    private void showReading(String meterId) {
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
            notifyTitleText("正 在读" + meterId + "...", true);
        }
    }

    private void notifyUI() {
        adapter.notifyDataSetChanged();
    }

    /**
     * 监听蓝牙连接情况
     */
    private void listenBlueToothConnect() {
        doEventByInterval(ONE_SECOND, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableConnectedList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (socket == null || (!socket.isConnected())) {
                    showDisconnect();
                    stopTimer(disposableConnectedList);
                    ToastUtil.showShortToast("蓝牙已断开");
                    isStop = true;
                    isConnect = false;
                    makeButtonEnable(true);
                    showStartFinish();
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(disposableConnectedList);
            }

            @Override
            public void onComplete() {
                stopTimer(disposableConnectedList);
            }
        });
    }


    private void doConnectBlueTooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            connectBlueTooth();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_OPEN_BLUETOOTH);
        }
    }

    private void notifyReadButton(int current, int total) {
        Button button;
        String buttonText;
        StringBuilder sb = new StringBuilder("");
        if (isClickRepeat && !isAutoRepeatRead) {
            button = btnReRead;
            buttonText = "补抄中...";
        } else {
            button = btnAutoRead;
            if (isAutoRepeatRead) {
                buttonText = "自动补抄中...";
            } else {
                buttonText = "抄表中...";
            }
        }
        sb.append(buttonText);
        sb.append(NEW_LINE);
        sb.append("(");
        sb.append(current);
        sb.append("/");
        sb.append(total);
        sb.append(")");
        button.setText(sb.toString());
    }

    private void sendMsg(Message message) {
        mHandler.sendMessage(message);
    }


    private void selectUserInfoByReadType(List<UserInfo> userInfoList) {
        if (EmiConfig.AUTO_READ_TYPE == STATE_ALL) {
            return;
        }
        LogUtil.w(TAG, "当前类型：" + EmiConfig.AUTO_READ_TYPE);
        if (EmiConfig.AUTO_READ_TYPE == STATE_NO_READ) {
            for (int i = userInfoList.size() - 1; i >= 0; i--) {
                if (userInfoList.get(i).state == STATE_SUCCESS || userInfoList.get(i).state == STATE_WARNING) {
                    userInfoList.remove(i);
                }
            }
        } else {
            for (int i = userInfoList.size() - 1; i >= 0; i--) {
                if (userInfoList.get(i).state != EmiConfig.AUTO_READ_TYPE) {
                    userInfoList.remove(i);
                }
            }
        }

    }

    /**
     * 更新回调回来的人工读数和状态
     *
     * @param callBackUserList
     * @param editUserInfo
     */
    private void modifyUseList(List<UserInfo> callBackUserList, UserInfo editUserInfo) {
        UserInfo currentUserInfo;
        for (int i = 0; i < callBackUserList.size(); i++) {
            currentUserInfo = callBackUserList.get(i);
            if (currentUserInfo.accountnum.equals(editUserInfo.accountnum) && currentUserInfo.meteraddr.equals(editUserInfo.meteraddr)) {
                currentUserInfo.state = editUserInfo.state;
                LogUtil.i(TAG, "修改的用户：" + editUserInfo.state);
                currentUserInfo.curdata = editUserInfo.curdata;
                currentUserInfo.curyl = editUserInfo.curyl;
                currentUserInfo.curreaddate = editUserInfo.curreaddate;
                break;
            }
        }
    }

    /**
     * 本次抄失败的表不覆盖原有数据
     */
    private void removeFailedUser(List<UserInfo> dataList) {
        UserInfo uiUserInfo;
        for (UserInfo normalUser : normalUserList) {
            for (int i = dataList.size() - 1; i >= 0; i--) {
                uiUserInfo = dataList.get(i);
                if (uiUserInfo.state == STATE_FAILED && uiUserInfo.equals(normalUser)) {
                    //本次抄表失败，但抄表之前已经有读数，不能覆盖原有数据,因此需要移除失败数据,不能往数据库写入
                    dataList.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * 使本次抄失败的表赋值上次正常的读数
     */
    private void replaceFailedUser(List<UserInfo> dataList) {
        UserInfo dataUserInfo;
        for (UserInfo normalUser : normalUserList) {
            for (int i = dataList.size() - 1; i >= 0; i--) {
                dataUserInfo = dataList.get(i);
                if (dataUserInfo.state == STATE_FAILED && dataUserInfo.equals(normalUser)) {
                    //本次抄表失败，但抄表之前已经有读数，不能覆盖原有数据,因此需要移除失败数据,不能往数据库写入
                    dataList.set(i, normalUser.copy());
                    break;
                }
            }
        }
    }

    private void doSkipSingleDebugActivity(List<UserInfo> userInfoList, int position) {
        if (userInfoList != null && !userInfoList.isEmpty()) {
            UserInfo userInfo = userInfoList.get(position);
            if (userInfo.meteraddr != null && userInfo.firmCode != null) {
                Intent intent = new Intent();
                intent.setClass(mContext, SingleMeterDebugActivity.class);
                intent.putExtra(EXTRA_METER_ID, userInfo.meteraddr);
                intent.putExtra(EXTRA_METER_FIRM_CODE, userInfo.firmCode);
                startActivityForResult(intent, REQUEST_CODE_SINGLE_DEBUG);
                doDisConnect();
            }
        }
    }

    private void makeButtonOriginal() {
        btnAutoRead.setText("开始抄表");
        btnReRead.setText("补抄");
    }


    private void selectCurrentUserList(String channelNumber, List<UserInfo> userInfoList) {
        if (TextUtils.isEmpty(channelNumber) || userInfoList.isEmpty()) {
            return;
        }
        String currentChannel = EmiStringUtil.clearFirstZero(channelNumber);
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            if (!userInfoList.get(i).channelNumber.equals(currentChannel)) {
                userInfoList.remove(i);
            }
        }
    }

    private void resetDelayTime() {
        if (READING_DELAY == 0) {
            READING_DELAY = 800;
        }
        readingDelay = READING_DELAY;
    }

}
