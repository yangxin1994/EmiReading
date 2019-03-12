package com.emi.emireading.ui.debug;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.MeterInfoEmiAdapter;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
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
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;

/**
 * @author :zhoujian
 * @description : 采集器通讯（安庆早期市场设备）（68协议）
 * @company :翼迈科技
 * @date 2018年05月21日上午 09:54
 * @Email: 971613168@qq.com
 */

public class WriteDataToDeviceActivity extends BaseActivity implements View.OnClickListener {
    private static final String CONSTANCE_FLAG = "689A";
    private Button btnConnect;
    private Button btnWrite;
    private Button btnRead;
    private boolean isConnect;
    private boolean clearFlag = false;
    public static final int SKIP_TAG_CONCENTRATOR_COMMUNICATION_68 = 5;
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
    private Disposable countDownDisable;
    private Handler handler = new MyHandler(this);
    /**
     * 时间间隔(毫秒)
     */
    private static final int TIME_INTERVAL = 1000;
    private boolean isStop;
    private String mFileName;
    private ReceiverDataRunnable dataThread;
    private ArrayList<UserInfo> userInfoArrayList = new ArrayList<>();
    private boolean isWrite = false;
    private int receiveCount;
    /**
     * 一次最大的数据长度
     */
    private static final int MAX_SIZE = 254;
    /**
     * 收到回调的次数
     */
    private int tempCount = -1;
    /**
     * 计时
     */
    private static final int MSG_COUNT_TIME = 1002;
    private StringBuffer callBackStringBuffer = new StringBuffer("");
    /**
     * 蓝牙已连接
     */
    private static final int MSG_CONNECT = 100;
    private static final int MSG_LOAD_FINISH = 1001;
    private int radix = 16;
    private RecyclerView rvUserInfo;
    private MeterInfoEmiAdapter meterInfoAdapter;
    private ArrayList<UserInfo> uiDataList = new ArrayList<>();

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
        titleView.setTitle("数据写入" + ":" + mFileName);
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


    private void showDialog(String textMsg) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.spin_animation_dialog);
        AnimationDrawable drawable = (AnimationDrawable) imageView.getBackground();
        drawable.start();
        dialog = EmiMultipleProgressDialog.create(this)
                .setCustomView(imageView)
                .setLabel(textMsg);
        dialog.setCancellable(false);
        dialog.setBackgroundColor(R.color.transparent).
                show();
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
                //                countDownConnect();
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

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
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
                if (userInfoArrayList.size() > MAX_SIZE) {
                    ToastUtil.showShortToast("待写入数据量超出范围");
                    return;
                }
                isWrite = true;
                clearFlag = false;
                showDialog("正在写入数据到设备...");
                clearReceiveData();
                writeToDevice();
                stopTimer(countDownDisable);
                countDownTime();
                setState(userInfoArrayList,STATE_NO_WRITE);
                notifyUI(userInfoArrayList);
                currentState = STATE_NO_WRITE;
                break;
            case R.id.btnRead:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                isWrite = false;
                clearFlag = false;
                showDialog("正在读取设备数据...");
                clearReceiveData();
                clearListView();
                sendBTCmd(cmdReadDevice());
                stopTimer(countDownDisable);
                countDownTime();
                currentState = STATE_HAS_WRITE;
                break;
            case R.id.btnClear:
                if (!isConnect) {
                    ToastUtil.showShortToast("请先连接蓝牙");
                    return;
                }
                showDialog("正在发送清空指令...");
                clearReceiveData();
                sendClearCmd();
                stopTimer(countDownDisable);
                countDownTime();
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
            final WriteDataToDeviceActivity activity = (WriteDataToDeviceActivity) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_CONNECT:
                        activity.doConnectSuccess();
                        break;
                    case MSG_ERROR:
                        String errorMsg = (String) msg.obj;
                        LogUtil.e("异常：" + errorMsg);
                        activity.doConnectFailed();
                        activity.closeDialog();
                        break;
                    case MSG_LOAD_FINISH:
                        LogUtil.d("数据集合长度：" + activity.userInfoArrayList.size());
                        activity.notifyUI(activity.userInfoArrayList);
                        activity.closeDialog();
                        break;
                    case MSG_COUNT_TIME:
                        activity.doReceiveData();
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
            receiveCallBack();
        }
    }


    /**
     * 获取通道板回传回来的指令
     */
    private void receiveCallBack() {
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
                        //收到回调说明，通讯正常，因此需要关闭该计时器
                        stopTimer(countDownDisable);
                        doReceiveData();
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
                sendErrorMsg(e.toString());
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


    private class GetDataRunnable implements Runnable {

        @Override
        public void run() {
            userInfoArrayList.clear();
            userInfoArrayList.addAll(getSqOperator().find(mFileName, STATE_SUCCESS));
            userInfoArrayList.addAll(getSqOperator().find(mFileName, STATE_PEOPLE_RECORDING));
            userInfoArrayList.addAll(getSqOperator().find(mFileName,STATE_WARNING));
            setState(userInfoArrayList, STATE_NO_WRITE);
            currentState = STATE_NO_WRITE;
            dataCount = userInfoArrayList.size();
            sendEmptyMsg(MSG_LOAD_FINISH);
        }
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

    private void doReceiveData() {
        doEventByInterval(500, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                baseDisposable = d;
                LogUtil.i("当前time：" + d);
            }

            @Override
            public void onNext(Long aLong) {
                if (tempCount == receiveCount) {
                    if (isWrite && !clearFlag) {
                        boolean isCorrect = checkWriteCallBackDataCorrect(callBackStringBuffer.toString(), userInfoArrayList.size());
                        if (isCorrect) {
                            ToastUtil.showShortToast("写入完成");
                            dataCount = userInfoArrayList.size();
                            currentState = STATE_HAS_WRITE;
                            //已写入
                            notifyUI(updateDataState(userInfoArrayList, STATE_HAS_WRITE));
                        } else {
                            ToastUtil.showShortToast("写入失败");
                        }
                    } else {
                        //读取或者清除回调
                        if (clearFlag) {
                            ToastUtil.showShortToast("清除设备数据成功");
                            clearListView();
                        } else {
                            //读取指令回调
                            boolean isCorrect = checkReadCallBackDataCorrect(callBackStringBuffer.toString());
                            if (isCorrect) {
                                String meterDataStr = getReadCallBackMeterData(callBackStringBuffer.toString());
                                ToastUtil.showShortToast("读取完成");
                               ArrayList<UserInfo> receiveList = getCallBackDataList(meterDataStr);
                               currentState = STATE_HAS_READ_ED;
                                dataCount = receiveList.size();
                                notifyUI(receiveList);
                            } else {
                                ToastUtil.showShortToast("读取数据有误");
                            }
                        }

                    }
                    LogUtil.i("接收完毕~");
                    closeDialog();
                    stopTimer(countDownDisable);
                    cancelTimers();
                } else {
                    LogUtil.d("正在接收中...");
                }
                tempCount = receiveCount;
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.e("当前time：" + e.toString());
                cancelTimers();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        cancelTimers();
        stopTimer(countDownDisable);
        doDisConnect();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private boolean checkWriteCallBackDataCorrect(String callbackStr, int correctCount) {
        String beginCode = "FEFE68";
        String lastCode = "16";
        String last = EmiStringUtil.getLastStr(callbackStr, 2);
        String callBackSizeStr;
        LogUtil.d(TAG, "-------->" + last);
        if (callbackStr.contains(beginCode) && last.equals(lastCode)) {
            callBackSizeStr = callbackStr.substring(callbackStr.length() - 6, callbackStr.length() - 4);
            LogUtil.d("callBackSizeStr---------->" + callBackSizeStr);
            LogUtil.w("callBackSizeStr---------->" + DigitalTrans.hexToInt(callBackSizeStr));
            return correctCount == DigitalTrans.hexToInt(callBackSizeStr);
        }
        return false;
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

    private void clearReceiveData() {
        receiveCount = 0;
        tempCount = -1;
        callBackStringBuffer.setLength(0);
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
                callBackUserInfo.state = STATE_HAS_READ_ED;
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

    private List<UserInfo> updateDataState(List<UserInfo> userInfoList, int state) {
        ArrayList<UserInfo> userInfoArrayList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            userInfo.state = state;
            userInfoArrayList.add(userInfo);
        }
        return userInfoArrayList;
    }

    private void countDownTime() {
        doEventByInterval(TIME_INTERVAL, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                countDownDisable = d;
            }

            @Override
            public void onNext(Long aLong) {
                LogUtil.d("正在计时：" + aLong);
                if (aLong >= TIME_OUT) {
                    ToastUtil.showShortToast("通讯超时");
                    closeDialog();
                    stopTimer(countDownDisable);
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(countDownDisable);
            }

            @Override
            public void onComplete() {
                stopTimer(countDownDisable);
            }
        });
    }


    private void sendClearCmd() {
        clearFlag = true;
        byte[] cmd = new byte[]{(byte) (0xFE), (byte) (0xFE), (byte) (0x68), (byte) (0x00), (byte) (0x04),
                (byte) (0x68), (byte) (0x19), (byte) (0xFF), (byte) (0xFF), (byte) (0x00), (byte) (0x17), (byte) (0x16)};
        sendBTCmd(cmd);
    }


    private void setState(List<UserInfo> userInfoList, int state) {
        for (UserInfo userInfo : userInfoList) {
            userInfo.state = state;
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


}