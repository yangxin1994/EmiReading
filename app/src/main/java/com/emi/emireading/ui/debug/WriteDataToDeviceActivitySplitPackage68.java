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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.emi.emireading.R;
import com.emi.emireading.adpter.MeterInfoEmiAdapter;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.EmiDividerItemDecoration;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.IO_EXCEPTION;
import static com.emi.emireading.core.config.EmiConstants.NEW_LINE;
import static com.emi.emireading.core.config.EmiConstants.ONE_SECOND;
import static com.emi.emireading.core.config.EmiConstants.SPACE;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.ui.CollectorCommunicationActivity.MAX_WRITE_SIZE;

/**
 * @author :zhoujian
 * @description : 采集器通讯68协议(分包)
 * @company :翼迈科技
 * @date 2018年05月21日上午 09:54
 * @Email: 971613168@qq.com
 */

public class WriteDataToDeviceActivitySplitPackage68 extends BaseActivity implements View.OnClickListener {
    private static final String CONSTANCE_FLAG = "689A";
    private Button btnConnect;
    private Button btnWrite;
    private Button btnRead;
    private boolean isConnect;
    private ArrayList<Byte> callBackByteList = new ArrayList<>();
    private boolean clearFlag = false;
    public static final int SKIP_TAG_CONCENTRATOR_COMMUNICATION_SPLIT_PACKAGE_68 = 6;
    private List<EmiMultipleProgressDialog> dialogList = new ArrayList<>();
    /**
     * 超时时间(秒)
     */
    private static final int TIME_OUT = 30;
    public BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket socket;
    private EmiMultipleProgressDialog dialog;
    private Context mContext;
    private Disposable mDisposable;
    private static final int MSG_ERROR = -1;

    private static final int MSG_NO_DATA = 2;
    private Handler handler = new MyHandler(this);
    /**
     * 时间间隔(毫秒)
     */
    private static final int TIME_INTERVAL = 1000;
    private boolean isStop;
    private String mFileName;
    private ReceiverDataRunnable dataThread;
    private ArrayList<UserInfo> userInfoArrayList = new ArrayList<>();
    private ArrayList<UserInfo> pendingWriteUserInfoList = new ArrayList<>();
    private boolean isWrite = false;
    private int receiveCount;
    private boolean isFinish;
    private boolean isInterrupt;
    private int count;

    private int currentReceivePackageCount;
    /**
     * 一次最大的数据长度
     */
    private static final int MAX_SIZE = 254;

    private static final int PACKAGE_SIZE = 240;
    /**
     * 收到回调的次数
     */
    private int tempCount = -1;
    private ArrayList<Disposable> communicationDisposableList = new ArrayList<>();
    /**
     * 用于监听是否写入完成
     */
    private ArrayList<Disposable> writeFinishDisposableList = new ArrayList<>();

    /**
     * 用于监听是否接收完成
     */
    private ArrayList<Disposable> receiveFinishDisposableList = new ArrayList<>();
    /**
     * 蓝牙已连接
     */
    private static final int MSG_CONNECT = 100;
    private static final int MSG_LOAD_FINISH = 1001;
    private static final int MSG_NOTIFY_DIALOG = 1005;
    private int radix = 16;
    private RecyclerView rvUserInfo;
    private MeterInfoEmiAdapter meterInfoAdapter;
    private ArrayList<UserInfo> uiDataList = new ArrayList<>();
    private ArrayList<Disposable> dataReceiveDisposableList = new ArrayList<>();

    /**
     * 未写入
     */
    public static final int STATE_NO_WRITE = 9001;
    /**
     * 已写入
     */
    public static final int STATE_HAS_WRITE = 9002;
    /**
     * 已读取
     */
    public static final int STATE_HAS_READ_ED = 9003;
    private TitleView titleView;
    private Button btnClear;
    private int currentState = STATE_NO_WRITE;
    private int dataCount;
    private String mProtocolTag = "";
    private static final String FINISH = "END";

    /**
     * 发送数据到设备
     */
    private static final String PROTOCOL_SEND_DATA_TO_DEVICE = "PROTOCOL_SEND_DATA_TO_DEVICE";

    /**
     * 清空设备数据
     */
    private static final String PROTOCOL_CLEAR_DEVICE_DATA = "PROTOCOL_CLEAR_DEVICE_DATA";

    /**
     * 读取设备数据
     */
    private static final String PROTOCOL_READ_DEVICE_DATA = "PROTOCOL_READ_DEVICE_DATA";

    private static final String OK = "OK";

    @Override
    protected int getContentLayout() {
        return R.layout.activity_write_data_to_device;
    }

    @Override
    protected void initIntent() {
        TAG = "WriteDataToDeviceActivitySplitPackage68";
        mContext = this;
        mFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
    }

    @Override
    protected void initUI() {
        btnConnect = findViewById(R.id.btnConnect);
        btnWrite = findViewById(R.id.btnWrite);
        btnRead = findViewById(R.id.btnRead);
        rvUserInfo = findViewById(R.id.rvUserInfo);
        titleView = findViewById(R.id.titleView);
        btnClear = findViewById(R.id.btnClear);
    }

    @Override
    protected void initData() {
        btnConnect.setOnClickListener(this);
        btnWrite.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        titleView.setOnClickRightListener(this);
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCountAlert();
            }
        });
        showDialog("正在获取数据");
        titleView.setTitle("数据写入");
        meterInfoAdapter = new MeterInfoEmiAdapter(uiDataList);
        View headerView = getViewByResource(mContext, R.layout.head_view_meter_info_layout);
        View emptyView = getViewByResource(mContext, R.layout.layout_empty_view);
        meterInfoAdapter.addHeaderView(headerView);
        meterInfoAdapter.setEmptyView(emptyView);
        meterInfoAdapter.bindToRecyclerView(rvUserInfo);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvUserInfo.setLayoutManager(layoutManager);
        rvUserInfo.addItemDecoration(new EmiDividerItemDecoration(
                mContext, EmiDividerItemDecoration.VERTICAL_LIST));
        ThreadPoolManager.EXECUTOR.execute(new GetDataRunnable());
    }

    private void connectBlueTooth() {
        if (!isConnect) {
            showDialog("正在连接蓝牙...");
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

    private void makeButtonEnable(boolean b) {
        btnConnect.setEnabled(b);
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
                                sendErrorMsg("蓝牙被占用");
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
            //用迭代
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
                    showDisconnect();
                    return;
                } else if (emiDeviceArrayList.size() == Integer.MAX_VALUE) {
                    int emiDeviceIndex = getEmiDeviceIndex(emiDeviceArrayList);
                    LogUtil.w(TAG, "已执行1");
                    if (emiDeviceIndex >= 0) {
                        LogUtil.w(TAG, "已执行2");
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

    private void cancelConnect() {
        closeDialog();
        showDisconnect();
        makeButtonEnable(true);
    }


    private void showDisconnect() {
        isConnect = false;
        cancel();
        makeButtonEnable(true);
        btnConnect.setBackgroundResource(R.drawable.btn_bg_red);
        btnConnect.setText("蓝牙未连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        btnWrite.setText("数据写入");
    }

    /**
     * 取消计时
     */
    public void cancel() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            LogUtil.w("====定时器取消======");
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
                            doConnectFailed();
                            ToastUtil.showShortToast("蓝牙已断开");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        cancel();
                    }

                    @Override
                    public void onComplete() {
                        cancel();
                    }
                });
    }

    private void doConnectFailed() {
        isConnect = false;
        makeButtonEnable(true);
        showDisconnect();
        closeDialog();
    }


    private void doDisConnect() {
        cancel();
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

    private void sendEmptyMsg(int what) {
        handler.sendEmptyMessage(what);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                connectBlueTooth();
                break;
            case R.id.btnWrite:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                clearFlag = false;
                doClearDeviceData();
                break;
            case R.id.btnRead:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                doReadDeviceData();
           /*     isWrite = false;
                clearFlag = false;
                showDialog("正在读取设备数据...");
                clearReceiveData();
                clearListView();
                sendBTCmd(cmdReadDevice());
                stopTimer(countDownDisable);
                countDownTime();
                currentState = STATE_HAS_WRITE;*/
                break;
            case R.id.btnClear:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                clearFlag = true;
                doClearDeviceData();
                break;
            case R.id.titleView:
                showCountAlert();
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
            final WriteDataToDeviceActivitySplitPackage68 activity = (WriteDataToDeviceActivitySplitPackage68) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_CONNECT:
                        activity.doConnectSuccess();
                        break;
                    case MSG_ERROR:
                        String errorMsg = (String) msg.obj;
                        LogUtil.e("有异常：" + errorMsg);
                        activity.doConnectFailed();
                        activity.closeDialog();
                        break;
                    case MSG_LOAD_FINISH:
                        LogUtil.d("数据集合长度：" + activity.userInfoArrayList.size());
                        activity.notifyUI(activity.userInfoArrayList);
                        activity.closeDialog();
                        break;
                    case MSG_NOTIFY_DIALOG:
                        activity.notifyDialog((String) msg.obj);
                        break;
                    case MSG_NO_DATA:
                        activity.cancelListen();
                        activity.cancelListenReceive();
                        activity.closeDialog();
                        activity.showTip("读取数据", "当前设备无数据");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void sendErrorMsg(String errorMsg) {
        Message message = handler.obtainMessage();
        message.what = MSG_ERROR;
        message.obj = errorMsg;
        handler.sendMessage(message);
    }

    private void doConnectSuccess() {
        interval(TIME_INTERVAL);
        isConnect = true;
        isStop = false;
        closeDialog();
        showConnectSuccess();
        makeButtonEnable(true);
        dataThread = new ReceiverDataRunnable();
        ThreadPoolManager.EXECUTOR.execute(dataThread);
    }


    private void showConnectSuccess() {
        btnConnect.setText("蓝牙已连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        btnConnect.setBackgroundResource(R.drawable.btn_bg_green_sel);
    }


    public class ReceiverDataRunnable implements Runnable {
        @Override
        public void run() {
            blueToothReceiveCallBack();
        }
    }

    /**
     * 获取蓝牙回调数据
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
                    //                    Log.w(TAG, "已执行");
                    if (receiveCount == 0) {
                        listenBlueToothDataReceive();
                    }
                    receiveCount++;
                    byte[] bufData = new byte[bytes];
                    for (int i = 0; i < bytes; i++) {
                        bufData[i] = buffer[i];
                        callBackByteList.add(buffer[i]);
                    }

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

    private void clearReceiveData() {
        receiveCount = 0;
        tempCount = -1;
        callBackByteList.clear();
    }

    private class GetDataRunnable implements Runnable {

        @Override
        public void run() {
            userInfoArrayList.clear();
            currentState = STATE_NO_WRITE;
            pendingWriteUserInfoList.clear();
            pendingWriteUserInfoList.addAll(getSqOperator().find(mFileName));
            userInfoArrayList.addAll(pendingWriteUserInfoList);
            setState(userInfoArrayList, STATE_NO_WRITE);
            dataCount = userInfoArrayList.size();
            sendEmptyMsg(MSG_LOAD_FINISH);
        }
    }

    /**
     * 监听蓝牙数据接收
     */
    private void listenBlueToothDataReceive() {
        doEventByInterval(600, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                dataReceiveDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (tempCount == receiveCount) {
                    LogUtil.i(TAG, "接收完毕~");
                    LogUtil.d("当前次数：" + receiveCount);
                    String callbackResult = getCallBackString();
                    LogUtil.i(TAG, "蓝牙返回的字符数据：" + callbackResult);
                    resolveCallback(callbackResult);
                    stopTimer(dataReceiveDisposableList);
                    clearReceiveData();
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
                removeInvalidTimer(dataReceiveDisposableList);
            }
        });
    }

    private String getCallBackString() {
        int size = callBackByteList.size();
        byte[] value = new byte[size];
        for (int i = 0; i < size; i++) {
            if (i < callBackByteList.size()) {
                value[i] = callBackByteList.get(i);
            }
        }
        return new String(value);
    }

    private void writeToDevice() {
        byte[] cmd = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFE, 0x68, 0x00, 0x00, 0x68, 0x19,
                (byte) 0xFF, (byte) 0xFF, (byte) 0x00};
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        for (byte b : cmd) {
            byteArrayList.add(b);
            cmd[11] = (byte) userInfoArrayList.size();
            LogUtil.d("cmd[11] = " + cmd[11]);
        }
        String meterAddress;
        String currentData;
        for (UserInfo userInfo : userInfoArrayList) {
            meterAddress = appendString(userInfo.meteraddr, "0", 10);
            currentData = appendString(String.valueOf(userInfo.curdata), "0", 4);
            LogUtil.d(TAG, "补齐后的数据：表地址" + meterAddress);
            LogUtil.i(TAG, "补齐后的数据：表读数：" + currentData);
            byte[] firmCodeBytes = stringToBytes(meterAddress, radix);
            byte[] currentDataBytes = stringToBytes(currentData, radix);
            for (byte meterAddressByte : firmCodeBytes) {
                LogUtil.i("转换后的数据：表地址：" + meterAddressByte);
                byteArrayList.add(meterAddressByte);
            }
            for (byte currentDataByte : currentDataBytes) {
                LogUtil.i("转换后的数据：表读数：" + currentDataByte);
                byteArrayList.add(currentDataByte);
            }
        }
        byteArrayList.add(getCsNumber(byteArrayList, 8, byteArrayList.size()));
        byteArrayList.add((byte) 0x16);
        byte[] resultByte = new byte[byteArrayList.size()];
        int length = userInfoArrayList.size() * 5 + userInfoArrayList.size() * 2 + 4;
        LogUtil.d("长度：" + length);
        byte[] lengthBytes = DigitalTrans.intToByteArray(length);
        byteArrayList.set(5, lengthBytes[0]);
        byteArrayList.set(6, lengthBytes[1]);
        for (int i = 0; i < byteArrayList.size(); i++) {
            resultByte[i] = byteArrayList.get(i);
        }
        String result = DigitalTrans.byte2hex(resultByte);
        sendBTCmd(resultByte);
        LogUtil.d("最终结果：" + result);
    }

    /**
     * 获取校验位
     *
     * @param data
     * @param startIndex
     * @param endIndex
     * @return
     */
    private byte getCsNumber(ArrayList<Byte> data, int startIndex, int endIndex) {
        int count = 0;
        for (int i = startIndex; i < endIndex; i++) {
            count += byte2Int(data.get(i));
            count = count & 0xFF;
        }
        LogUtil.d("getCs:校验和：" + count);
        return (byte) count;
    }


    private byte[] cmdReadDevice() {
        return new byte[]{
                (byte) 0xFE, (byte) 0xFE, (byte) 0x68,
                0x00, 0x04, 0x68, 0x1A, (byte) 0xFF, (byte) 0xFF, 0x00, 0x18, 0x16
        };
    }


    @Override
    protected void onDestroy() {
        cancelListen();
        cancelListenReceive();
        cancelListenWrite();
        isInterrupt = true;
        isFinish = true;
        doDisConnect();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void cancelListen() {
        stopTimer(communicationDisposableList);
    }

    private void cancelListenWrite() {
        stopTimer(writeFinishDisposableList);
    }

    private void cancelListenReceive() {
        stopTimer(receiveFinishDisposableList);
    }

    private boolean checkReadCallBackDataCorrect(String callbackStr) {
        String beginCode = "FEFE68";
        String lastCode = "16";
        String last = EmiStringUtil.getLastStr(callbackStr, 2);
        LogUtil.d(TAG, "-------->" + last);
        if (callbackStr.contains(beginCode) && callbackStr.contains(CONSTANCE_FLAG) && last.equals(lastCode)) {
            int middleIndex = callbackStr.lastIndexOf("9A");
            int csIndex = callbackStr.lastIndexOf(lastCode) - 2;
            String checkString = callbackStr.substring(middleIndex, csIndex);
            LogUtil.i(TAG, "checkString = " + checkString);
            byte[] checkArray = DigitalTrans.stringConvertBytes(checkString);
            //由于最后四个字符串是校验位和终止码，需要去掉
            String csValue = callbackStr.substring(csIndex, callbackStr.length() - 2);
            int csNumber = DigitalTrans.stringConvertInt(csValue, 16);
            boolean isCsCorrect = checkCs(checkArray, csNumber);
            int first68Index = callbackStr.indexOf("68") + 2;
            String dataLengthValue = callbackStr.substring(first68Index, first68Index + 4);
            LogUtil.d(TAG, "dataLengthValue =" + dataLengthValue);
            LogUtil.i(TAG, "stringConvertInt-->" + DigitalTrans.stringConvertInt(dataLengthValue, 16));
            int dataLength = DigitalTrans.stringConvertInt(dataLengthValue, 16);
            LogUtil.i(TAG, "checkString 长度--->" + checkString.length());
            boolean isLengthCorrect = dataLength == checkString.length() / 2;
            return isCsCorrect && isLengthCorrect;
        }
        return false;
    }


    private String getReadCallBackMeterData(String callbackData) {
        //8表示设备地址长度，需要过滤
        int startIndex = callbackData.lastIndexOf(CONSTANCE_FLAG) + CONSTANCE_FLAG.length() + 8;
        //过滤终止码16和2位校验位
        int endIndex = callbackData.lastIndexOf("16") - 2;
        return callbackData.substring(startIndex, endIndex);
    }

    /**
     * 获取回调的数据集合
     *
     * @param meterDataStr
     * @return
     */
    private ArrayList<UserInfo> getCallBackDataList(String meterDataStr) {
        ArrayList<UserInfo> userInfoArrayList = new ArrayList<>();
        int correctLength = 14;
        boolean isNumber = isNumber(meterDataStr);
        boolean isCorrect = meterDataStr.length() % correctLength == 0 && isNumber;
        int count = 0;
        if (isCorrect) {
            List<String> strList = EmiStringUtil.getStrList(meterDataStr, correctLength);
            int meterLength = 10;
            UserInfo callBackUserInfo;
            for (String s : strList) {
                callBackUserInfo = new UserInfo();
                callBackUserInfo.meteraddr = EmiStringUtil.clearFirstZero(EmiStringUtil.substring(s, 0, meterLength));
                callBackUserInfo.curdata = Integer.parseInt(EmiStringUtil.substring(s, meterLength, s.length()));
                callBackUserInfo.uploadState = STATE_HAS_READ_ED;
                LogUtil.d(TAG, "表地址：" + EmiStringUtil.substring(s, 0, meterLength));
                LogUtil.d(TAG, "表读数：" + EmiStringUtil.substring(s, meterLength, s.length()));
                LogUtil.i(TAG, "表地址：处理过后：" + callBackUserInfo.meteraddr);
                LogUtil.i(TAG, "表读数：处理过后：" + callBackUserInfo.curdata);
                userInfoArrayList.add(callBackUserInfo);
            }
            LogUtil.d(TAG, "数据源长度：" + strList.size());
        }

        LogUtil.i(TAG, "isNumber=" + count);
        LogUtil.d(TAG, "isNumber=" + isNumber);
        return userInfoArrayList;
    }

    public boolean isNumber(String str) {
        String regular = "^[-\\+]?[\\d]*$";
        Pattern pattern = Pattern.compile(regular);
        return pattern.matcher(str).matches();
    }

    /**
     * 更新列表
     *
     * @param userInfoList
     */
    private void notifyUI(List<UserInfo> userInfoList) {
        uiDataList.clear();
        uiDataList.addAll(userInfoList);
        meterInfoAdapter.notifyDataSetChanged();
    }

    private void notifyUI() {
        meterInfoAdapter.notifyDataSetChanged();
    }


    private void sendClearCmd() {
        mProtocolTag = PROTOCOL_CLEAR_DEVICE_DATA;
        byte[] cmd = new byte[]{(byte) (0xFE), (byte) (0xFE), (byte) (0x68), (byte) (0x00), (byte) (0x04),
                (byte) (0x68), (byte) (0x1C), (byte) (0xFF), (byte) (0xFF), (byte) (0x00), (byte) (0x1A), (byte) (0x16)};
        sendBTCmd(cmd);
    }


    private void setState(List<UserInfo> userInfoList, int state) {
        for (UserInfo userInfo : userInfoList) {
            userInfo.uploadState = state;
        }
    }

    private void clearListView() {
        List<UserInfo> userInfoList = new ArrayList<>();
        notifyUI(userInfoList);
    }


    private void showCountAlert() {
        StringBuilder stringBuilder = new StringBuilder("当前");
        switch (currentState) {
            case STATE_NO_WRITE:
                stringBuilder.append("待写入");
                break;
            case STATE_HAS_WRITE:
                stringBuilder.append("已写入");
                break;
            case STATE_HAS_READ_ED:
                stringBuilder.append("已读取");
                break;
            default:
                stringBuilder.append("待写入");
                break;
        }
        stringBuilder.append("的数量：");
        stringBuilder.append(dataCount);
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("数据统计")
                .setContentText(stringBuilder.toString())
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
                        sDialog.dismiss();
                    }
                })
                .show();
    }

    private void listenCommunication(int second) {
        doEventCountDown(ONE_SECOND * second, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                communicationDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                closeDialog();
                ToastUtil.showShortToast("通讯超时");
                stopTimer(communicationDisposableList);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                stopTimer(communicationDisposableList);
            }
        });

    }

    /**
     * 写入水表信息（表地址或读数）到设备
     */
    private void writeMeterInfo(final List<List<UserInfo>> listList, List<UserInfo> writeList) {
        LogUtil.d(TAG, "待写入数量：" + listList.size());
        if (writeList.size() > MAX_WRITE_SIZE) {
            ToastUtil.showShortToast("待写入数量超出" + MAX_WRITE_SIZE + "!");
            closeDialog();
            return;
        }
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                writeToCollector(listList);
            }
        });

    }


    private void writeToCollector(List<List<UserInfo>> listList) {
        LogUtil.i(TAG, "分割的数量：" + listList.size());
      /*  splitCount = listList.size();
        clearReceiveData();
        isInterrupt = false;
        listenCommunication();
        successCount = 0;
        downloadingMeterInfo(listList);*/
    }

    private void closeDialog() {
        EmiMultipleProgressDialog dialog;
        for (int i = dialogList.size() - 1; i >= 0; i--) {
            dialog = dialogList.get(i);
            dialog.dismiss();
        }
    }

    private void showDialog(String text) {
        closeDialog();
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setLabel(text)
                .setCancellable(false)
                .show();
        dialogList.add(dialog);
    }


    private void doWriteDataToDevice() {
        if (pendingWriteUserInfoList.isEmpty()) {
            ToastUtil.showShortToast("数据为空");
            return;
        }
        mProtocolTag = PROTOCOL_SEND_DATA_TO_DEVICE;
        showDialog("正在整合数据包...");
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                List<List<UserInfo>> userLists = EmiStringUtil.splitList(pendingWriteUserInfoList, PACKAGE_SIZE);
                int packageNumber;
                for (int i = 0; i < userLists.size(); i++) {
                    count = 0;
                    isFinish = false;
                    packageNumber = i + 1;
                    sendNotifyDialogMsg("正在向设备发送第" + packageNumber + "包数据");
                    doSendCMDWriteDataToDevice(userLists.get(i));
                    while (!isFinish) {
                        delays(ONE_SECOND);
                        count++;
                        LogUtil.d(TAG, "当前等待次数：" + count);
                        if (isInterrupt) {
                            stopThread();
                            LogUtil.w(TAG, "循环已跳出");
                            break;
                        }
                    }
                }
            }
        });
    }

    private void sendNotifyDialogMsg(String text) {
        Message message = handler.obtainMessage();
        message.obj = text;
        message.what = MSG_NOTIFY_DIALOG;
        handler.sendMessage(message);
    }

    private void doSendCMDWriteDataToDevice(List<UserInfo> userInfoList) {
        mProtocolTag = PROTOCOL_SEND_DATA_TO_DEVICE;
        clearReceiveData();
        byte[] cmd = EmiUtils.getCmdWriteDataToDevice(userInfoList);
        if (cmd.length == 0) {
            LogUtil.e("指令为空！");
            return;
        }
        cancelListen();
        listenCommunication(20);
        sendBTCmd(cmd);
    }


    private void notifyDialog(String text) {
        if (dialog != null) {
            if (!dialog.isShowing()) {
                showDialog(text);
            } else {
                dialog.setLabel(text);
            }
        } else {
            showDialog(text);
        }
    }


    private void handleCallbackWriteDataToDevice(String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        cancelListen();
        cancelListenWrite();
        listenWriteIsFinish(16);
        String info = value.replace(NEW_LINE, "");
        String[] infoArray = info.split(SPACE);
        LogUtil.d(TAG, "数组长度：" + infoArray.length);
        for (int i = 0; i < infoArray.length; i++) {
            LogUtil.d(TAG, "数组长度：" + infoArray[i]);
        }

    }


    private void resolveCallback(String value) {
        if (TextUtils.isEmpty(value)) {
            ToastUtil.showShortToast("返回的数据为空");
            return;
        }
        switch (mProtocolTag) {
            case PROTOCOL_SEND_DATA_TO_DEVICE:
                isFinish = true;
                handleCallbackWriteDataToDevice(value);
                break;
            case PROTOCOL_CLEAR_DEVICE_DATA:
                handleCallbackClearData(value);
                break;
            case PROTOCOL_READ_DEVICE_DATA:
                handleCallbackReadDeviceData(value);
                break;
            default:
                break;
        }
    }

    private void listenWriteIsFinish(int second) {
        doEventCountDown(ONE_SECOND * second, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                writeFinishDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                closeDialog();
                cancelListenWrite();
                cancel();
                setState(userInfoArrayList, STATE_HAS_WRITE);
                currentState = STATE_HAS_WRITE;
                notifyUI(userInfoArrayList);
                showTip("数据写入", "写入完成");
            }
        });
    }

    private void listenReceiveIsFinish(int second) {
        doEventCountDown(ONE_SECOND * second, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                receiveFinishDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                closeDialog();
                cancelListen();
                cancelListenReceive();
                setState(userInfoArrayList, STATE_HAS_READ_ED);
                LogUtil.d("获取的数据长度:" + userInfoArrayList.size());
                currentState = STATE_HAS_READ_ED;
                notifyUI(userInfoArrayList);
                dataCount = userInfoArrayList.size();
                showTip("获取设备数据", "读取完成");
            }
        });
    }


    private void showTip(String title, String text) {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(title)
                .setContentText(text)
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
                        sDialog.dismiss();
                    }
                })
                .show();
    }

    private void handleCallbackClearData(String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if (clearFlag) {
            if (value.contains(OK)) {
                closeDialog();
                cancelListen();
                uiDataList.clear();
                notifyUI();
                showTip("清空数据", "清空完成");
            }
        } else {
            doWriteDataToDevice();
        }

    }

    private void doClearDeviceData() {
        mProtocolTag = PROTOCOL_CLEAR_DEVICE_DATA;
        clearReceiveData();
        cancelListen();
        showDialog("正在发送清空指令...");
        listenCommunication(8);
        sendClearCmd();
    }


    private void doReadDeviceData() {
        mProtocolTag = PROTOCOL_READ_DEVICE_DATA;
        String protocolReadDeviceData = "#RDDATA";
        uiDataList.clear();
        userInfoArrayList.clear();
        notifyUI();
        cancelListen();
        closeDialog();
        showDialog("正在获取设备数据...");
        listenCommunication(15);
        clearReceiveData();
        currentReceivePackageCount = 0;
        sendBTCmd(protocolReadDeviceData.getBytes());
    }

    private void handleCallbackReadDeviceData(String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if (!value.contains(OK) || !value.contains(FINISH)) {
            ToastUtil.showShortToast("返回数据异常");
            closeDialog();
            cancelListen();
            return;
        }
        cancelListen();
        listenCommunication(45);
        parseMeterInfo(value);
        cancelListenReceive();
        listenReceiveIsFinish(35);
    }

    private void parseMeterInfo(final String value) {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                currentReceivePackageCount++;
                String info = value.replace(OK, "");
                info = info.replace(FINISH, "");
                String[] meterDataArray = info.split(NEW_LINE);
                LogUtil.d(TAG, "返回的数据长度：" + meterDataArray.length);
                if (meterDataArray.length == 0) {
                    sendEmptyMsg(MSG_NO_DATA);
                    return;
                }
                sendNotifyDialogMsg("正在解析设备返回的第" + currentReceivePackageCount + "包数据");
                UserInfo userInfo;
                String[] meterInfoArray;
                List<String> meterInfoList;
                meterInfoList = EmiUtils.stringArrayToStringList(meterDataArray);
                for (int i = meterInfoList.size() - 1; i >= 0; i--) {
                    if (TextUtils.isEmpty(meterInfoList.get(i))) {
                        meterInfoList.remove(i);
                    }
                }
                List<UserInfo> callbackUserList = new ArrayList<>();
                LogUtil.e("处理前的数组长度：" + meterInfoList.size());
                for (String meterInfo : meterInfoList) {
                    LogUtil.e("处理前的数组长度：" + meterInfo);
                    if (TextUtils.isEmpty(meterInfo)) {
                        continue;
                    }
                    meterInfo = meterInfo.replace(NEW_LINE, "");
                    meterInfoArray = meterInfo.split(SPACE);
                    if (meterInfoArray.length != 2) {
                        continue;
                    }
                    meterInfoArray[0] = meterInfoArray[0].replace(NEW_LINE, "");
                    meterInfoArray[1] = meterInfoArray[1].replace(NEW_LINE, "");
                    if (!TextUtils.isDigitsOnly(meterInfoArray[0])) {
                        continue;
                    }
                    userInfo = new UserInfo();
                    userInfo.meteraddr = EmiStringUtil.clearFirstZero(meterInfoArray[0]);
                    if (EmiConstants.ERROR_METER_DATA.equalsIgnoreCase(meterDataArray[1])) {
                        userInfo.curdata = -1;
                    } else {
                        try {
                            userInfo.curdata = Integer.parseInt(meterInfoArray[1]);
                        } catch (NumberFormatException e) {
                            userInfo.curdata = -1;
                            userInfo.state = STATE_FAILED;
                        }
                    }
                    userInfo.useraddr = getSqOperator().queryUserAddress(userInfo.meteraddr);
                    if (userInfo.curdata > EmiConstants.METER_MAX_METER_DATA || userInfo.curdata < 0) {
                        userInfo.state = STATE_FAILED;
                    } else {
                        userInfo.state = STATE_SUCCESS;
                    }
                    callbackUserList.add(userInfo);
                    LogUtil.i("解析的数据：内容：" + meterInfo);
                }
                userInfoArrayList.addAll(callbackUserList);
                LogUtil.e("解析的数据：内容：长度：" + userInfoArrayList.size());
                //                sendEmptyMsg(MSG_READ_FINISH);
            }
        });
    }


}