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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;

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
import static com.emi.emireading.core.config.EmiConstants.EMI_CALLBACK_CODE_VALID;
import static com.emi.emireading.core.config.EmiConstants.EMI_SYSTEM_SETTING_CODE;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_FIRM_CODE;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_ID;
import static com.emi.emireading.core.config.EmiConstants.IO_EXCEPTION;
import static com.emi.emireading.core.config.EmiConstants.METER_FIRM_CODE_LENGTH;
import static com.emi.emireading.core.config.EmiConstants.METER_INFO_LENGTH;
import static com.emi.emireading.core.config.EmiConstants.MSG_BLUETOOTH_CONNECT;
import static com.emi.emireading.core.config.EmiConstants.ONE_SECOND;
import static com.emi.emireading.core.config.EmiConstants.REQUEST_CODE_OPEN_BLUETOOTH;
import static com.emi.emireading.core.config.EmiConstants.ZERO;

/**
 * @author :zhoujian
 * @description : 单表维护
 * @company :翼迈科技
 * @date 2018年07月11日下午 01:08
 * @Email: 971613168@qq.com
 */

public class SingleMeterDebugActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private TextView tvStatus;
    public BluetoothDevice bluetoothDevice = null;
    private EditText etMeterAddress;
    private EditText editTextFirmCode;
    private Button btnConnect;
    private Button btnRead;
    private Button btnClear;
    private EmiMultipleProgressDialog dialog;
    private Context mContext;
    private boolean isConnect;
    private BluetoothSocket socket;
    private boolean isStop;
    private List<Disposable> blueToothDisposableList = new ArrayList<>();
    private List<Disposable> dataReceiveDisposableList = new ArrayList<>();
    private ArrayList<Disposable> disposableConnectedList = new ArrayList<>();

    private boolean isReadSuccess;
    /**
     * 读水表地址（单表）
     */
    private static final String ACTION_READ_METER_ADDRESS = "ACTION_READ_METER_ADDRESS";


    /**
     * 读取水表读数（单表）
     */
    private static final String ACTION_READ_METER_DATA = "ACTION_READ_METER_DATA";
    /**
     * 使能设置
     */
    private static final String ACTION_SYSTEM_SETTING = "ACTION_SYSTEM_SETTING";

    /**
     * 清空千分位（单表）
     */
    private static final String ACTION_CLEAR_TH = "ACTION_CLEAR_TH";

    /**
     * 修改水表地址（单表）
     */
    private static final String ACTION_EDIT_METER_ADDRESS = "ACTION_EDIT_METER_ADDRESS";
    /**
     * 时间间隔(毫秒)
     */
    private static final int TIME_INTERVAL = 1000;
    private static final int MSG_ERROR = -1;

    private int receiveCount;
    /**
     * 是否是修改表地址
     */
    private int mTag;
    private static final int TAG_EDIT_METER_ADDRESS = 1;
    private static final int TAG_CLEAR_TH = 2;


    /**
     * 收到回调的次数
     */
    private int tempCount = -1;

    private String mCurrentAction = "";

    private StringBuffer callBackStringBuffer = new StringBuffer("");
    private byte[] cmd = {};

    private TextView tvMeterData;

    private TextView tvLocation;

    private TextView tvChannel;

    private Button btnClearTh;
    private Button btnEditMeterAddress;

    private String inputAddress = "";
    private String inputFirmCode = "";
    private String mInputThStr = "";
    private String newInputFirmCode = "";
    private String newInputMeterAddress = "";
    private final int MaxInputThLength = 2;
    /**
     * 蓝牙已连接
     */
    private Handler handler = new MyHandler(this);
    private CheckBox cBoxMeterAddress;
    private CheckBox cBoxMeterFirmCode;
    private List<Disposable> disposableReadingList = new ArrayList<>();

    private CheckBox cbMarketHF, cbMarketOther, cbMeterTypeDui, cbMeterTypeFan;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_singlemeter_debug;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        inputAddress = getIntent().getStringExtra(EXTRA_METER_ID);
        inputFirmCode = getIntent().getStringExtra(EXTRA_METER_FIRM_CODE);
    }

    @Override
    protected void initUI() {
        tvStatus = findViewById(R.id.tv_status);
        tvChannel = findViewById(R.id.tvChannel);
        etMeterAddress = findViewById(R.id.et_meter_address);
        editTextFirmCode = findViewById(R.id.editTextFirmCode);
        tvChannel = findViewById(R.id.tvChannel);
        btnClear = findViewById(R.id.bt_clear);
        btnRead = findViewById(R.id.bt_read);
        tvLocation = findViewById(R.id.tv_location);
        cBoxMeterAddress = findViewById(R.id.cb_switch);
        cBoxMeterFirmCode = findViewById(R.id.cbFirmCode);
        cbMarketHF = findViewById(R.id.cbMarketHF);
        cbMarketOther = findViewById(R.id.cbMarketOther);
        cbMeterTypeDui = findViewById(R.id.cbMeterTypeDui);
        cbMeterTypeFan = findViewById(R.id.cbMeterTypeFan);
        cbMarketHF.setOnCheckedChangeListener(this);
        cbMarketOther.setOnCheckedChangeListener(this);
        cbMeterTypeDui.setOnCheckedChangeListener(this);
        cbMeterTypeFan.setOnCheckedChangeListener(this);
        tvMeterData = findViewById(R.id.tv_meter_data);
        btnConnect = findViewById(R.id.bt_connect);
        btnClearTh = findViewById(R.id.btnClearTh);
        btnEditMeterAddress = findViewById(R.id.btnEditMeterAddress);
        btnConnect.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnClearTh.setOnClickListener(this);
        btnEditMeterAddress.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        isShowClearTh();
        if (inputAddress == null) {
            inputAddress = "";
        }
        if (inputFirmCode == null) {
            inputFirmCode = "";
        }
        etMeterAddress.setText(inputAddress);
        editTextFirmCode.setText(inputFirmCode);
        cBoxMeterAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    cBoxMeterFirmCode.setChecked(false);
                }
            }
        });
        cBoxMeterFirmCode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!isAppointAddress()) {
                    cBoxMeterFirmCode.setChecked(false);
                }
                if (!b) {
                    editTextFirmCode.setText("");
                }
            }
        });
    }

    /**
     * 是否指定表地址
     *
     * @return
     */
    private boolean isAppointAddress() {
        return cBoxMeterAddress.isChecked();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_connect:
                doConnectBlueTooth();
                break;
            case R.id.bt_read:
                if (isAppointAddress()) {
                    //指定水表地址
                    if (!isConnect) {
                        ToastUtil.showShortToast("请先连接蓝牙");
                        return;
                    }
                    if (etMeterAddress.getText().toString().isEmpty()) {
                        ToastUtil.showShortToast("请先输入表地址");
                        return;
                    }
                    inputAddress = etMeterAddress.getText().toString();
                    inputAddress = EmiStringUtil.appendZero(inputAddress, 10);
                    if (isAppointMeterFirmCode()) {
                        //指定厂商代码
                        if (editTextFirmCode.getText().toString().length() != METER_FIRM_CODE_LENGTH) {
                            ToastUtil.showShortToast("请输入四位厂商代码");
                            return;
                        }
                        inputFirmCode = editTextFirmCode.getText().toString();
                    } else {
                        inputFirmCode = editTextFirmCode.getText().toString();
                        if (TextUtils.isEmpty(inputFirmCode) || inputFirmCode.length() != METER_FIRM_CODE_LENGTH || !TextUtils.isDigitsOnly(inputFirmCode)) {
                            inputFirmCode = EmiConstants.FIRM_CODE_7833;
                        }
                    }
                    LogUtil.w("指定表地址---" + "输入的表地址：" + inputAddress + "厂商代码：" + inputFirmCode);
                    showReading();
                    readMeterData(inputFirmCode + inputAddress, false, !isAppointMeterFirmCode());
                } else {
                    showReading();
                    makeButtonEnable(false);
                    doReadMeterAddress();
                }
                break;
            case R.id.bt_clear:
                clearInput(true, true);
                break;
            case R.id.btnClearTh:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                if (TextUtils.isEmpty(etMeterAddress.getText().toString())) {
                    ToastUtil.showShortToast("请先输入水表地址");
                    return;
                }
                showInputThDialog();
                break;

            case R.id.btnEditMeterAddress:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                doEditMeterAddress();
                break;
            default:
                break;
        }
    }

    /**
     * 是否指定厂商代码
     *
     * @return
     */
    private boolean isAppointMeterFirmCode() {
        return cBoxMeterFirmCode.isChecked();
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

    private void showDialog(String text) {
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setLabel(text)
                .setCancellable(false)
                .show();
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


    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
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


    private void showDisconnect() {
        isConnect = false;
        makeButtonEnable(true);
        btnConnect.setBackgroundResource(R.drawable.btn_bg_red);
        btnConnect.setText("蓝牙未连接");
        tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red_btn_bg_color));
        tvStatus.setText("蓝牙未连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
    }


    private void makeButtonEnable(boolean b) {
        btnClear.setEnabled(b);
        btnRead.setEnabled(b);
        btnConnect.setEnabled(b);
        btnEditMeterAddress.setEnabled(b);
        btnClearTh.setEnabled(b);
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

    private void cancelConnect() {
        closeDialog();
        showDisconnect();
        makeButtonEnable(true);
    }

    private void sendEmptyMsg(int what) {
        handler.sendEmptyMessage(what);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.cbMarketHF:
                if (b) {
                    cbMarketOther.setChecked(false);
                } else {
                    cbMarketOther.setChecked(true);
                }
                break;
            case R.id.cbMarketOther:
                if (b) {
                    cbMarketHF.setChecked(false);
                } else {
                    cbMarketHF.setChecked(true);
                }
                break;
            case R.id.cbMeterTypeDui:
                if (b) {
                    cbMeterTypeFan.setChecked(false);
                } else {
                    cbMeterTypeFan.setChecked(true);
                }
                isShowClearTh();
                break;
            case R.id.cbMeterTypeFan:
                if (b) {
                    cbMeterTypeDui.setChecked(false);
                } else {
                    cbMeterTypeDui.setChecked(true);
                }
                isShowClearTh();
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
            final SingleMeterDebugActivity activity = (SingleMeterDebugActivity) mWeakReference.get();
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


    private void doConnectSuccess() {
        observeBlueToothConnection();
        isConnect = true;
        isStop = false;
        closeDialog();
        showConnectSuccess();
        makeButtonEnable(true);
        ThreadPoolManager.EXECUTOR.execute(new ReceiverDataRunnable());
    }


    /**
     * 监听蓝牙连接情况
     */
    public void observeBlueToothConnection() {
        doEventByInterval(TIME_INTERVAL, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                blueToothDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (socket == null || (!socket.isConnected())) {
                    doConnectFailed();
                    ToastUtil.showShortToast("蓝牙已断开");
                    stopTimer(blueToothDisposableList);
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(blueToothDisposableList);
            }

            @Override
            public void onComplete() {
                stopTimer(blueToothDisposableList);
            }
        });
    }


    private void sendErrorMsg(String errorMsg) {
        Message message = handler.obtainMessage();
        message.what = MSG_ERROR;
        message.obj = errorMsg;
        handler.sendMessage(message);
    }


    private void doConnectFailed() {
        isConnect = false;
        makeButtonEnable(true);
        stopTimer(blueToothDisposableList);
        showDisconnect();
        closeDialog();
    }


    private void showConnectSuccess() {
        btnConnect.setText("蓝牙已连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        btnConnect.setBackgroundResource(R.drawable.btn_bg_green_sel);
        tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        tvStatus.setText("蓝牙已连接");
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
        LogUtil.i(TAG, "蓝牙线程已启动");
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
                }
            } catch (Exception e) {
                Log.e(TAG, "错误：" + e.toString());
                if (!e.toString().contains(IO_EXCEPTION)) {
                    sendErrorMsg(e.toString());
                }
                isStop = true;
                if (Thread.interrupted()) {
                    doDisConnect();
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


    /**
     * 监听蓝牙数据接收
     */
    private void listenBlueToothDataReceive() {
        doEventByInterval(100, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                dataReceiveDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (tempCount == receiveCount) {
                    LogUtil.i(TAG, "接收完毕~");
                    LogUtil.d("当前次数：" + receiveCount);
                    LogUtil.i(TAG, "蓝牙返回的字符数据：" + callBackStringBuffer.toString());
                    resolveCallBackData(callBackStringBuffer.toString());
                    closeDialog();
                    makeButtonEnable(true);
                    stopTimer(dataReceiveDisposableList);
                    removeInvalidTimer(dataReceiveDisposableList);
                } else {
                    LogUtil.d(TAG, "正在接收中...");
                }
                tempCount = receiveCount;
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(dataReceiveDisposableList);
            }

            @Override
            public void onComplete() {
                stopTimer(dataReceiveDisposableList);
            }
        });
    }

    /**
     * 解析蓝牙回调数据
     *
     * @param callbackString
     */
    private void resolveCallBackData(String callbackString) {
        switch (mCurrentAction) {
            case ACTION_READ_METER_ADDRESS:
                UserInfo userInfo = parseMeterInfo(callbackString);
                if (userInfo != null) {
                    isReadSuccess = true;
                    readMeterData(userInfo.firmCode + userInfo.meteraddr, true, true);
                } else {
                    ToastUtil.showShortToast("读取水表地址失败");
                    makeButtonOriginal();
                    makeButtonEnable(true);
                }
                break;
            case ACTION_READ_METER_DATA:
                UserInfo user = parseMeterDataCallback(callbackString);
                if (user != null) {
                    isReadSuccess = true;
                    UserInfo localUser = getSqOperator().findUserByMeterInfo(EmiStringUtil.clearFirstZero(user.meteraddr));
                    boolean isExist = localUser != null;
                    if (isExist) {
                        localUser.curdata = user.curdata;
                        localUser.firmCode = user.firmCode;
                        showReadDataSuccess(localUser);
                        showIsSaveDialog(localUser);
                    } else {
                        showReadDataSuccess(user);
                    }
                } else {
                    showReadFailed();
                }
                makeButtonOriginal();
                break;
            case ACTION_SYSTEM_SETTING:
                boolean isSetting = callbackString.contains(EMI_SYSTEM_SETTING_CODE);
                boolean isCorrect = checkCallbackDataCorrect(callbackString);
                LogUtil.w(TAG, "正确性：" + isSetting + "---" + isCorrect);
                if (isSetting && isCorrect) {
                    //设置使能返回正确，开始执行后续操作
                    switch (mTag) {
                        case TAG_EDIT_METER_ADDRESS:
                            //修改表地址
                            makeButtonEnable(false);
                            editMeterAddress();
                            break;
                        case TAG_CLEAR_TH:
                            //修改千分位
                            makeButtonEnable(false);
                            clearTh(etMeterAddress.getText().toString(), mInputThStr);
                            break;
                        default:
                            break;
                    }
                } else {
                    makeButtonOriginal();
                    ToastUtil.showShortToast("修改失败");
                }
                break;
            case ACTION_CLEAR_TH:
                if (checkCallbackDataCorrect(callbackString)) {
                    isReadSuccess = true;
                    ToastUtil.showShortToast("修改成功");
                }
                makeButtonOriginal();
                break;
            case ACTION_EDIT_METER_ADDRESS:
                if (checkCallbackDataCorrect(callbackString)) {
                    ToastUtil.showShortToast("修改成功");
                    isReadSuccess = true;
                    etMeterAddress.setText(EmiStringUtil.clearFirstZero(newInputMeterAddress));
                    editTextFirmCode.setText(newInputFirmCode);
                }
                makeButtonOriginal();
                break;
            default:
                break;
        }
    }


    private void doReadMeterAddress() {
        //读水表地址
        mCurrentAction = ACTION_READ_METER_ADDRESS;
        if (cbMarketOther.isChecked()) {
            cmd = EmiUtils.getCmdReadAddressNormal();
        } else {
            cmd = EmiUtils.getCmdReadAddressSpecial();
        }
        isReadSuccess = false;
        clearReceiveData();
        sendBTCmd(cmd);
        listen();
        clearInput(true, true);
    }

    /**
     * 解析水表地址和厂商代码
     *
     * @param callbackString
     * @return
     */
    private UserInfo parseMeterInfo(String callbackString) {
        if (TextUtils.isEmpty(callbackString)) {
            return null;
        }
        boolean isCorrect = checkCallbackDataCorrect(callbackString);
        if (isCorrect) {
            int validIndex = callbackString.indexOf(EMI_CALLBACK_CODE_VALID);
            int endIndex = validIndex + EMI_CALLBACK_CODE_VALID.length() + METER_INFO_LENGTH;
            if (callbackString.length() > endIndex) {
                String meterInfoString = callbackString.substring(validIndex + EMI_CALLBACK_CODE_VALID.length(), endIndex);
                String meterInfo = convertMeterInfoByList(EmiStringUtil.splitStrToList(meterInfoString));
                if (meterInfo.length() == EmiConstants.METER_INFO_LENGTH) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.firmCode = meterInfo.substring(0, 4);
                    userInfo.meteraddr = meterInfo.substring(4, meterInfo.length());
                    return userInfo;
                }
            }
        }
        return null;
    }

    /**
     * 显示初始状态
     */
    private void makeButtonOriginal() {
        btnRead.setText("读取");
        btnClear.setText("清除");
        btnClearTh.setText("修改千分位");
        btnEditMeterAddress.setText("修改表地址");
        makeButtonEnable(true);
    }


    private void clearReceiveData() {
        receiveCount = 0;
        tempCount = -1;
        callBackStringBuffer.setLength(0);
        LogUtil.w(TAG, "数据已被清空");
    }

    /**
     * 根据完整的水表地址读取水表读数
     *
     * @param wholeMeterInfo:厂商代码+水表地址(长度：14)
     * @param isClearMeterAddress:是否清空表地址输入框
     */
    private void readMeterData(String wholeMeterInfo, boolean isClearMeterAddress, boolean isClearFirmCode) {
        mCurrentAction = ACTION_READ_METER_DATA;
        isReadSuccess = false;
        if (cbMarketOther.isChecked()) {
            cmd = EmiUtils.getReadingCmdWholeNormal(wholeMeterInfo);
        } else {
            cmd = EmiUtils.getReadingCmdWholeSpecial(wholeMeterInfo);
        }
        clearReceiveData();
        clearInput(isClearMeterAddress, isClearFirmCode);
        sendBTCmd(cmd);
        listen();
    }

    private void showReading() {
        btnRead.setText(getResources().getString(R.string.reading));
        btnClear.setText("清除");
    }


    private void clearInput(boolean isClearMeterAddress, boolean clearFirmCode) {
        if (isClearMeterAddress) {
            etMeterAddress.setText("");
        }
        if (clearFirmCode) {
            editTextFirmCode.setText("");
        }
        tvLocation.setText("");
        tvChannel.setText("");
        tvMeterData.setText("");
        inputAddress = "";
        inputFirmCode = "";
        newInputFirmCode = "";
        newInputMeterAddress = "";
    }


    private void showReadFailed() {
        etMeterAddress.setText("");
        editTextFirmCode.setText("");
        tvMeterData.setText("");
        tvChannel.setText("");
        tvChannel.setText("");
    }


    private void showReadDataSuccess(UserInfo user) {
        if (user != null) {
            if (EmiStringUtil.checkStringIsSame(user.meteraddr, ZERO)) {
                etMeterAddress.setText(user.meteraddr);
            } else {
                etMeterAddress.setText(EmiStringUtil.clearFirstZero(user.meteraddr));
            }
            editTextFirmCode.setText(user.firmCode);
            tvMeterData.setText(String.valueOf(user.curdata));
            if (TextUtils.isEmpty(user.channelNumber) || String.valueOf(Integer.MAX_VALUE).equals(user.channelNumber)) {
                tvChannel.setText("无");
            } else {
                tvChannel.setText(user.channelNumber);
            }
            if (TextUtils.isEmpty(user.useraddr)) {
                tvLocation.setText("无");
            } else {
                tvLocation.setText(user.useraddr);
            }
        }
    }

    @Override
    protected void onDestroy() {
        doDisConnect();
        stopThread();
        super.onDestroy();
    }

    /**
     * 监听
     */
    private void listen() {
        stopTimer(disposableReadingList);
        doEventCountDown(ONE_SECOND * 3, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableReadingList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (!isReadSuccess) {
                    //读取失败
                    ToastUtil.showShortToast("操作失败");
                    makeButtonOriginal();
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(disposableReadingList);
            }

            @Override
            public void onComplete() {
                stopTimer(disposableReadingList);
                removeInvalidTimer(disposableReadingList);
            }
        });
    }


    private void showInputThDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("请输入千位和百位");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInputThStr = builder.editText.getText().toString();
                mCurrentAction = ACTION_CLEAR_TH;
                if (TextUtils.isEmpty(mInputThStr) || mInputThStr.length() != MaxInputThLength) {
                    ToastUtil.showShortToast("请输入千位和百位");
                    return;
                }
                mCurrentAction = ACTION_SYSTEM_SETTING;
                mTag = TAG_CLEAR_TH;
                btnClearTh.setText("修改中...");
                doSystemSetting();
                //修改千分位
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog inputDialog = builder.create();
        builder.setInputMaxLength(MaxInputThLength);
        inputDialog.show();
    }

    private void showInputFirmCodeDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("输入要修改的厂商代码");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newInputFirmCode = builder.editText.getText().toString();
                if (TextUtils.isEmpty(newInputFirmCode) || newInputFirmCode.length() != METER_FIRM_CODE_LENGTH) {
                    ToastUtil.showShortToast("请输入4位数的厂商代码");
                    return;
                }
                dialog.dismiss();
                mTag = TAG_EDIT_METER_ADDRESS;
                mCurrentAction = ACTION_SYSTEM_SETTING;
                btnEditMeterAddress.setText("修改中...");
                doSystemSetting();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog inputDialog = builder.create();
        builder.setInputMaxLength(4);
        inputDialog.show();
    }

    /**
     * 设置设能
     */
    private void doSystemSetting() {
        if (TextUtils.isEmpty(etMeterAddress.getText().toString())) {
            ToastUtil.showShortToast("请先输入表地址");
            return;
        }
        isReadSuccess = false;
        cmd = EmiUtils.getSettingCmd(etMeterAddress.getText().toString(), editTextFirmCode.getText().toString());
        clearReceiveData();
        listen();
        sendBTCmd(cmd);
    }


    private void clearTh(String meterId, String th) {
        mTag = TAG_CLEAR_TH;
        mCurrentAction = ACTION_CLEAR_TH;
        isReadSuccess = false;
        cmd = EmiUtils.getClearThCmd(meterId, th);
        clearReceiveData();
        listen();
        sendBTCmd(cmd);
    }


    private void doEditMeterAddress() {
        if (etMeterAddress.getText().toString().isEmpty()) {
            ToastUtil.showShortToast("请输入水表地址");
            return;
        }
        if (editTextFirmCode.getText().toString().length() != METER_FIRM_CODE_LENGTH) {
            ToastUtil.showShortToast("请输入正确的厂商代码");
            return;
        }
        showInputAddressDialog();
    }

    private void editMeterAddress() {
        mCurrentAction = ACTION_EDIT_METER_ADDRESS;
        isReadSuccess = false;
        String oldFirmCode;
        oldFirmCode = editTextFirmCode.getText().toString();
        String oldMeterAddress = etMeterAddress.getText().toString();
        oldFirmCode = EmiStringUtil.appendZero(oldFirmCode, 4);
        oldMeterAddress = EmiStringUtil.appendZero(oldMeterAddress, 10);
        newInputFirmCode = EmiStringUtil.appendZero(newInputFirmCode, 4);
        newInputMeterAddress = EmiStringUtil.appendZero(newInputMeterAddress, 10);
        String oldMeterInfo = oldFirmCode + oldMeterAddress;
        String newMeterInfo = newInputFirmCode + newInputMeterAddress;
        LogUtil.w(TAG, "旧表信息奇葩：" + oldMeterInfo);
        LogUtil.i(TAG, "新表信息奇葩：" + newMeterInfo);
        if (cbMarketOther.isChecked()) {
            LogUtil.i(TAG, "新表信息奇葩：普通市场");
            cmd = EmiUtils.getEditMeterAddressCmdNormal(oldMeterInfo, newMeterInfo);
        } else {
            LogUtil.e(TAG, "新表信息奇葩：特殊市场");
            cmd = EmiUtils.getEditMeterAddressCmdSpecial(oldMeterInfo, newMeterInfo);
        }
        clearReceiveData();
        listen();
        sendBTCmd(cmd);
    }


    private void showInputAddressDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("输入新表地址");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newInputMeterAddress = builder.editText.getText().toString();
                if (TextUtils.isEmpty(newInputMeterAddress)) {
                    ToastUtil.showShortToast("请输入新表地址");
                    return;
                }
                showInputFirmCodeDialog();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog inputDialog = builder.create();
        builder.setInputMaxLength(10);
        inputDialog.show();
    }

    private void isShowClearTh() {
        if (cbMeterTypeFan.isChecked()) {
            btnClearTh.setVisibility(View.VISIBLE);
        } else {
            btnClearTh.setVisibility(View.GONE);
        }
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
                    isConnect = false;
                    makeButtonOriginal();
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

    /**
     * 将水表读数保存到数据库
     *
     * @param userInfo
     */
    private void saveMeterData(UserInfo userInfo) {
        if (userInfo != null) {
            userInfo.state = EmiConstants.STATE_SUCCESS;
            userInfo.curreaddate = TimeUtil.getCurrentTime();
            getSqOperator().updateData(userInfo);
        } else {
            LogUtil.e(TAG, "用户信息为空，无法保存到数据库");
        }
    }

    /**
     * 显示是否保存对话框
     */
    private void showIsSaveDialog(final UserInfo userInfo) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("保存数据")
                .setContentText("当前水表读数为" + userInfo.curdata + ",检测到数据库有对应水表，是否保存读数？")
                .setCancelText("取消")
                .setConfirmText("保存")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismiss();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        saveMeterData(userInfo);
                        sDialog.dismiss();
                        ToastUtil.showShortToast("保存成功");
                    }
                });
        TextView contentView = sweetAlertDialog.getContentTextView();
        if (contentView != null) {
            contentView.setText("读取成功");
        }
        sweetAlertDialog.show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_OPEN_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    connectBlueTooth();
                }
                break;
            default:
                break;
        }
    }
}
