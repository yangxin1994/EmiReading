package com.emi.emireading.ui;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CollectorCommunicationAdapter;
import com.emi.emireading.adpter.CommonSelectEmiAdapter;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.EmiDividerItemDecoration;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.DisplayUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;
import com.emi.emireading.widget.view.emimenu.EmiMenu;
import com.emi.emireading.widget.view.emimenu.EmiMenuItem;
import com.emi.emireading.widget.view.emimenu.OnMenuItemClickListener;
import com.emi.emireading.widget.view.pickerview.TimePickerDialog;
import com.emi.emireading.widget.view.pickerview.data.Type;
import com.emi.emireading.widget.view.pickerview.listener.OnDateSetListener;
import com.emi.emireading.widget.view.popup.EmiPopupList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.adpter.CollectorCommunicationAdapter.EMPTY_DATA;
import static com.emi.emireading.adpter.CollectorCommunicationAdapter.FAILED_DATA;
import static com.emi.emireading.core.config.EmiConstants.EMPTY;
import static com.emi.emireading.core.config.EmiConstants.ERROR_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.IO_EXCEPTION;
import static com.emi.emireading.core.config.EmiConstants.METER_INFO_LENGTH;
import static com.emi.emireading.core.config.EmiConstants.MSG_BLUETOOTH_CONNECT;
import static com.emi.emireading.core.config.EmiConstants.MSG_ERROR;
import static com.emi.emireading.core.config.EmiConstants.NEW_LINE;
import static com.emi.emireading.core.config.EmiConstants.NULL;
import static com.emi.emireading.core.config.EmiConstants.ONE_SECOND;
import static com.emi.emireading.core.config.EmiConstants.PREF_WATER_WARNING_LINE;
import static com.emi.emireading.core.config.EmiConstants.REQUEST_CODE_OPEN_BLUETOOTH;
import static com.emi.emireading.core.config.EmiConstants.SPACE;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.core.config.EmiConstants.TEN;
import static com.emi.emireading.core.config.EmiConstants.ZERO;
import static com.emi.emireading.ui.TimePickerActivity.EXTRA_SELECT_COUNT;
import static com.emi.emireading.ui.TimePickerActivity.EXTRA_SELECT_DATE;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_READ_ED;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_WRITE;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_NO_WRITE;

/**
 * @author :zhoujian
 * @description : 采集器通讯页面(集中器通讯A)
 * @company :翼迈科技
 * @date 2018年06月14日下午 01:38
 * @Email: 971613168@qq.com
 */

public class CollectorCommunicationActivity extends BaseActivity implements View.OnClickListener, OnDateSetListener {
    public static final String RESULT_OK = "OK";
    public static final String RESULT_TOTAL = "TOTAL";
    public static final String RESULT_END = "END";
    private SimpleDateFormat sf = new SimpleDateFormat("HHmm");
    private boolean isConnect;
    public BluetoothDevice bluetoothDevice = null;
    private EmiMultipleProgressDialog dialog;
    private BluetoothSocket socket;
    private boolean isStop;
    private int receiveCount;
    private Context mContext;
    private UserInfo modifyUserInfo;
    /**
     * 是否是单个写入
     */
    private boolean singleWrite = false;
    /**
     * 水表地址的标准长度
     */
    private static final int METER_ADDRESS_LENGTH = 14;
    public static final int REQUEST_CODE_DATE_PICKER = 666;
    private static final int MSG_TOAST_INFO = 1001;
    private static final int MSG_LOAD_FINISH = 1002;
    private static final int MSG_CLOSE_DIALOG = 1003;
    private static final int MSG_NOTIFY_DIALOG = 1005;
    public static final int SKIP_TAG_COLLECTOR_COMMUNICATION = 3;
    private String mFileName;
    private String channelNumber;
    private ArrayList<UserInfo> uiDataList = new ArrayList<>();
    private ArrayList<UserInfo> tempDataList = new ArrayList<>();
    /**
     * 待写入读数集合
     */
    private ArrayList<UserInfo> pendingWriteList = new ArrayList<>();
    private TimePickerDialog mDialogHourMinute;
    /**
     * 收到回调的次数
     */
    private int tempCount = -1;
    private ArrayList<Byte> callBackByteList = new ArrayList<>();
    private String protocolTag = EMPTY;
    private Handler mHandler = new MyHandler(this);
    private ArrayList<Disposable> dataReceiveDisposableList = new ArrayList<>();
    private ArrayList<Disposable> blueToothConnectDisposableList = new ArrayList<>();
    private ArrayList<Disposable> communicationDisposableList = new ArrayList<>();
    private CollectorCommunicationAdapter communicationAdapter;
    private RecyclerView rvUserInfo;
    private Button btnWriteMeterId;
    private Button btnClearMeterId;
    private Button btnReadAll;
    private Button btnReadMeterData;
    private Button btnConnect;
    private Button btnReadMeterId;
    private TitleView titleView;
    private boolean isFinish;
    private boolean isInterrupt;
    private int successCount;
    private ArrayList<String> stringArrayList;
    private static final String NO_TIMING = "00";

    /**
     * 分包长度
     */
    private final int SIZE_SPLIT_PACKAGE = 40;
    /**
     * 下载表地址flag
     */
    private boolean downloadMeterAddressFlag;
    private int count;
    private int splitCount;
    private int currentCount;
    private List<EmiMultipleProgressDialog> dialogList = new ArrayList<>();
    private static final String TIME = "TIME";
    /**
     * 写表地址协议
     */
    private static final String PROTOCOL_WRITE_ADDRESS = "#WRADDR ";
    /**
     * 读表地址协议
     */
    private static final String PROTOCOL_READ_ADDRESS = "#RDADDR ";
    /**
     * 清空地址协议
     */
    private static final String PROTOCOL_CLEAR_ADDRESS = "#DEADDR ";


    /**
     * 读水表读数协议
     */
    private static final String PROTOCOL_READ_METER_DATA = "#RDDATA ";

    /**
     * 群抄协议
     */
    private static final String PROTOCOL_READ_ALL_METER = "#BATCH ";
    /**
     * 写入水表读数到设备
     */
    private static final String PROTOCOL_WRITE_METER_DATA = "#WRDATA ";
    /**
     * 替换水表地址
     */
    private static final String PROTOCOL_REPLACE_METER_ADDRESS = "#RPADDR ";
    /**
     * 设置当前时间
     */
    private static final String PROTOCOL_SETTING_CURRENT_TIME = "#TIME ";

    /**
     * 读取当前时间
     */
    private static final String PROTOCOL_READ_CURRENT_TIME = "#TIME";

    /**
     * 设置定时时间
     */
    private static final String PROTOCOL_SETTING_TIMING = "#TIMING ";

    /**
     * 读取定时时间
     */
    private static final String PROTOCOL_READ_TIMING = "#TIMING";

    public static final int MAX_WRITE_SIZE = 240;
    private String inputMsg;
    private String secondInputMsg;
    private float mRawX;
    private float mRawY;
    /**
     * 用水警戒线
     */
    private int waterLine;

    private EmiMenu emiMenu;

    private StringBuilder dateSb = new StringBuilder("");
    private List<UserInfo> waitSycUserList = new ArrayList<>();

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                hideCount(false);
                doConnectBlueTooth();
                break;
            //写入表地址(下载)
            case R.id.btnWriteMeterId:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                hideCount(false);
                ArrayList<String> functionNameList = new ArrayList<>();
                functionNameList.add("写入水表地址");
                functionNameList.add("写入水表读数");
                showSelectTxtFileDialog(functionNameList);
                break;
            case R.id.btnClearMeterId:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                hideCount(false);
                downloadMeterAddressFlag = false;
                doClearCollectorData();
                break;
            case R.id.btnReadMeterId:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                protocolTag = PROTOCOL_READ_ADDRESS;
                hideCount(false);
                showDialog("正在读取水表地址...");
                sendBTCmd(makeProtocol(PROTOCOL_READ_ADDRESS).getBytes());
                clearReceiveData();
                listenCommunication();
                uiDataList.clear();
                notifyUI();
                break;
            case R.id.btnReadMeterData:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                protocolTag = PROTOCOL_READ_METER_DATA;
                showDialog("正在获取水表读数...");
                hideCount(false);
                uiDataList.clear();
                notifyUI();
                sendBTCmd(makeProtocol(PROTOCOL_READ_METER_DATA).getBytes());
                clearReceiveData();
                listenCommunication();
                break;
            case R.id.btnReadAll:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                hideCount(false);
                protocolTag = PROTOCOL_READ_ALL_METER;
                showDialog("正在发送群抄指令...");
                sendBTCmd(makeProtocol(PROTOCOL_READ_ALL_METER).getBytes());
                clearReceiveData();
                listenCommunication();
                break;
            default:
                break;
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.emi_activity_collector_communication;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        mFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        channelNumber = getIntent().getStringExtra(EXTRA_CHANNEL_NUMBER);
        LogUtil.d(TAG, "----文件名：" + mFileName + "----通道号：" + channelNumber);
    }

    @Override
    protected void initUI() {
        btnConnect = findViewById(R.id.btnConnect);
        rvUserInfo = findViewById(R.id.rvUserInfo);
        btnClearMeterId = findViewById(R.id.btnClearMeterId);
        btnReadAll = findViewById(R.id.btnReadAll);
        btnWriteMeterId = findViewById(R.id.btnWriteMeterId);
        btnReadMeterId = findViewById(R.id.btnReadMeterId);
        btnReadMeterData = findViewById(R.id.btnReadMeterData);
        titleView = findViewById(R.id.titleView);
        btnConnect.setOnClickListener(this);
        btnReadMeterId.setOnClickListener(this);
        btnClearMeterId.setOnClickListener(this);
        btnWriteMeterId.setOnClickListener(this);
        btnReadAll.setOnClickListener(this);
        btnReadMeterData.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        waterLine = PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50);
        EmiUtils.keepScreenLongLight(this, true);
        showDialog("正在查询...");
        hideCount(true);
        emiMenu = getMenu(mContext, this, new OnMenuItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                emiMenu.dismiss();
                switch (position) {
                    case 0:
                        showCount();
                        break;
                    case 1:
                        if (!isConnect) {
                            tipConnect();
                            return;
                        }
                        doSettingCurrentTime();
                        break;
                    case 2:
                        if (!isConnect) {
                            tipConnect();
                            return;
                        }
                        doReadCurrentTime();
                        break;
                    case 3:
                        if (!isConnect) {
                            tipConnect();
                            return;
                        }
                        clearDate();
                        dateSb.append(PROTOCOL_SETTING_TIMING);
                        showTimePicker();
                        break;
                    case 4:
                        if (!isConnect) {
                            tipConnect();
                            return;
                        }
                        doReadTiming();
                        break;
                    default:
                        break;
                }
            }
        });
        titleView.setRightIcon(R.mipmap.more);
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emiMenu.showAsDropDown(view);
                //                showCount();
            }
        });
        communicationAdapter = new CollectorCommunicationAdapter(uiDataList);
        rvUserInfo.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                mRawX = e.getRawX();
                mRawY = e.getRawY();
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                mRawX = e.getRawX();
                mRawY = e.getRawY();
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        stringArrayList = new ArrayList<>();
        stringArrayList.add("替换表地址");
        stringArrayList.add("写入读数");
        communicationAdapter.setOnItemLongClickListener(new BaseEmiAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseEmiAdapter adapter, View view, int position) {
                showPop(view, position);
                return true;
            }
        });
        View emptyView = getViewByResource(mContext, R.layout.layout_empty_view);
        communicationAdapter.setEmptyView(emptyView);
        communicationAdapter.bindToRecyclerView(rvUserInfo);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvUserInfo.setLayoutManager(layoutManager);
        rvUserInfo.addItemDecoration(new EmiDividerItemDecoration(
                mContext, EmiDividerItemDecoration.VERTICAL_LIST));
        ThreadPoolManager.EXECUTOR.execute(new GetUserInfoRunnable());
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


    /**
     * 监听蓝牙数据接收
     */
    private void listenBlueToothDataReceive() {
        doEventByInterval(200, new Observer<Long>() {
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
                    resolveCallBackResult(callbackResult);
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

    private void stopThread() {
        try {
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
        String dateString = getDateToString(millSeconds);
        LogUtil.d(TAG, "millSeconds=" + millSeconds);
        LogUtil.i(TAG, "millSeconds=" + dateString);
        dateSb.append(dateString);
        dateSb.append(SPACE);
        doSkip();
    }


    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final CollectorCommunicationActivity activity = (CollectorCommunicationActivity) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_BLUETOOTH_CONNECT:
                        activity.doConnectSuccess();
                        break;
                    case MSG_LOAD_FINISH:
                        activity.notifyUI();
                        activity.closeAllDialog();
                        break;
                    case MSG_ERROR:
                        String errorMsg = (String) msg.obj;
                        LogUtil.e("有异常：" + errorMsg);
                        activity.doConnectFailed();
                        ToastUtil.showShortToast(errorMsg);
                        activity.closeAllDialog();
                        break;
                    case MSG_TOAST_INFO:
                        ToastUtil.showShortToast((String) msg.obj);
                        activity.closeAllDialog();
                        break;
                    case MSG_CLOSE_DIALOG:
                        activity.closeAllDialog();
                        break;
                    case MSG_NOTIFY_DIALOG:
                        activity.notifyDialog((String) msg.obj);
                        break;
                    default:
                        break;
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

    private void doConnectFailed() {
        isConnect = false;
        makeButtonEnable(true);
        stopTimer(blueToothConnectDisposableList);
        showDisconnect();
        closeAllDialog();
    }


    private void closeAllDialog() {
        EmiMultipleProgressDialog dialog;
        for (int i = dialogList.size() - 1; i >= 0; i--) {
            dialog = dialogList.get(i);
            dialog.dismiss();
        }
    }

    private void showDisconnect() {
        isConnect = false;
        makeButtonEnable(true);
        btnConnect.setBackgroundResource(R.drawable.btn_bg_red);
        btnConnect.setText("蓝牙未连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
    }


    private void doConnectSuccess() {
        observeBlueToothConnection();
        isConnect = true;
        isStop = false;
        closeAllDialog();
        showConnectSuccess();
        makeButtonEnable(true);
        ThreadPoolManager.EXECUTOR.execute(new ReceiverDataRunnable());
    }

    private class ReceiverDataRunnable implements Runnable {
        @Override
        public void run() {
            blueToothReceiveCallBack();
        }
    }


    private void showConnectSuccess() {
        btnConnect.setText("蓝牙已连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        btnConnect.setBackgroundResource(R.drawable.btn_bg_green_sel);
    }

    /**
     * 监听蓝牙连接情况
     */
    public void observeBlueToothConnection() {
        doEventByInterval(ONE_SECOND, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                blueToothConnectDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                if (socket == null || (!socket.isConnected())) {
                    doConnectFailed();
                    ToastUtil.showShortToast("蓝牙已断开");
                    isConnect = false;
                    stopTimer(blueToothConnectDisposableList);
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(blueToothConnectDisposableList);
            }

            @Override
            public void onComplete() {
                stopTimer(blueToothConnectDisposableList);
            }
        });
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
            isInterrupt = true;
            ToastUtil.showShortToast("蓝牙已断开");
        }
    }

    private void doDisConnect() {
        if (socket != null || EmiConstants.bluetoothSocket != null) {
            isStop = true;
            EmiConstants.bluetoothSocket = socket;
            if (socket != null && socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    socket = null;
                }
            }
            isConnect = false;
            socket = null;
            showDisconnect();
        }
    }

    private void showDialog(String text) {
        closeAllDialog();
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setLabel(text)
                .setCancellable(false)
                .show();
        dialogList.add(dialog);
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
                    closeAllDialog();
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
            closeAllDialog();
            showDisconnect();
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
        closeAllDialog();
        showDisconnect();
        makeButtonEnable(true);
    }


    private void sendToastMsg(String msg) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_TOAST_INFO;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    private void sendEmptyMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }

    private class GetUserInfoRunnable implements Runnable {
        @Override
        public void run() {
            if (TextUtils.isEmpty(mFileName) || TextUtils.isEmpty(channelNumber)) {
                sendToastMsg("未获取到通道号");
                return;
            }
            uiDataList.clear();
            tempDataList.clear();
            tempDataList.addAll(getSqOperator().findByChannel(mFileName, channelNumber));
            uiDataList.addAll(tempDataList);
            for (UserInfo userInfo : tempDataList) {
                if (userInfo.state == STATE_FAILED || userInfo.state == STATE_NO_READ) {
                    userInfo.curdata = FAILED_DATA;
                }
            }
            setUserInfoUploadState(uiDataList, STATE_NO_WRITE);
            for (UserInfo userInfo : uiDataList) {
                if (userInfo == null || TextUtils.isEmpty(userInfo.meteraddr)) {
                    tempDataList.clear();
                    uiDataList.clear();
                    sendToastMsg("检测到空表地址存在");
                    return;
                }
            }
            sendEmptyMsg(MSG_LOAD_FINISH);
            LogUtil.d(TAG, "查询到的数据数量：" + uiDataList.size());
        }
    }

    /**
     * 更新列表
     */
    private void notifyUI() {
        communicationAdapter.notifyDataSetChanged();
    }


    private String convertMeterAddress(UserInfo userInfo) {
        if (userInfo != null && (!TextUtils.isEmpty(userInfo.meteraddr))) {
            if (userInfo.meteraddr.length() == METER_INFO_LENGTH) {
                return userInfo.meteraddr;
            }
            if (TextUtils.isEmpty(userInfo.firmCode)) {
                userInfo.firmCode = EmiConstants.FIRM_CODE_7833;
            }
            return userInfo.firmCode + EmiStringUtil.appendZero(userInfo.meteraddr, 10);
        }
        return "";
    }

    private String convertMeterData(UserInfo userInfo) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (userInfo != null && (!TextUtils.isEmpty(userInfo.meteraddr))) {
            if (userInfo.meteraddr.length() == METER_ADDRESS_LENGTH) {
                stringBuilder.append(userInfo.meteraddr.substring(4, userInfo.meteraddr.length()));
            } else {
                stringBuilder.append(EmiStringUtil.appendZero(userInfo.meteraddr, 10));
            }
            stringBuilder.append(SPACE);
            if (userInfo.curdata > 0 || singleWrite) {
                stringBuilder.append(EmiStringUtil.appendZero(String.valueOf(userInfo.curdata), 4));
                userInfo.state = STATE_PEOPLE_RECORDING;
            } else {
                if (userInfo.state == STATE_NO_READ || userInfo.state == STATE_FAILED || userInfo.state == STATE_ALL) {
                    stringBuilder.append(ERROR_METER_DATA);
                } else {
                    stringBuilder.append(EmiStringUtil.appendZero(String.valueOf(userInfo.curdata), 4));
                }
            }
            return stringBuilder.toString();
        }
        return "";
    }

    /**
     * 生成协议字符串
     *
     * @param protocol
     * @param userInfoList
     * @return
     */
    private String makeProtocol(String protocol, List<UserInfo> userInfoList) {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(protocol);
        boolean isWriteMeterData = PROTOCOL_WRITE_METER_DATA.equals(protocol);
        if (userInfoList != null) {
            if (isWriteMeterData) {
                removeEmptyData(userInfoList);
            }
            for (UserInfo userInfo : userInfoList) {
                if (isWriteMeterData) {
                    //拼接水表读数
                    if (userInfo.state != STATE_FAILED && userInfo.state != STATE_NO_READ && userInfo.state != STATE_ALL) {
                        stringBuilder.append(convertMeterData(userInfo));
                        stringBuilder.append(SPACE);
                    }
                } else {
                    //拼接水表地址
                    stringBuilder.append(convertMeterAddress(userInfo));
                    stringBuilder.append(SPACE);
                }
            }

        }

        return stringBuilder.toString().trim();
    }

    private String makeProtocol(String protocol, String content) {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(protocol);
        stringBuilder.append(content);
        return stringBuilder.toString().trim();
    }

    /**
     * 生成协议字符串
     *
     * @param protocol
     * @return
     */
    private String makeProtocol(String protocol) {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(protocol);
        LogUtil.i(TAG, "发送的协议：" + stringBuilder.toString().trim());
        return stringBuilder.toString().trim();
    }

    private void writeToCollector(List<List<UserInfo>> listList) {
        LogUtil.i(TAG, "分割的数量：" + listList.size());
        splitCount = listList.size();
        clearReceiveData();
        isInterrupt = false;
        listenCommunication();
        successCount = 0;
        downloadingMeterInfo(listList);
    }

    private void clearReceiveData() {
        receiveCount = 0;
        tempCount = -1;
        callBackByteList.clear();
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

    private void tipConnect() {
        ToastUtil.showShortToast("请先连接蓝牙");
    }

    /**
     * 解析返回结果
     *
     * @param value
     */
    private void resolveCallBackResult(String value) {
        switch (protocolTag) {
            case PROTOCOL_CLEAR_ADDRESS:
                if (value.contains(RESULT_OK)) {
                    if (!downloadMeterAddressFlag) {
                        ToastUtil.showShortToast("清空成功");
                        uiDataList.clear();
                        notifyUI();
                        closeAllDialog();
                    } else {
                        closeAllDialog();
                        showDialog("准备写入水表地址到设备...");
                        protocolTag = PROTOCOL_WRITE_ADDRESS;
                        writeMeterInfo(EmiStringUtil.splitList(tempDataList, SIZE_SPLIT_PACKAGE), tempDataList);
                    }
                    LogUtil.d(TAG, "------------->" + tempDataList.size());
                    stopTimer(communicationDisposableList);
                } else {
                    sendToastMsg("数据返回异常");
                    isInterrupt = true;
                }
                break;
            case PROTOCOL_WRITE_ADDRESS:
                //写表地址回调
                if (value.contains(RESULT_TOTAL)) {
                    if (currentCount == splitCount) {
                        setUserInfoUploadState(uiDataList, STATE_HAS_WRITE);
                        notifyUI();
                        closeAllDialog();
                        isInterrupt = true;
                        int success = parseCallbackDataCount(value);
                        int failed = tempDataList.size() - success;
                        StringBuilder result = new StringBuilder("");
                        result.append("写入总数：");
                        result.append(tempDataList.size());
                        result.append(NEW_LINE);
                        result.append("成功总数:");
                        result.append(success);
                        result.append(NEW_LINE);
                        result.append("失败总数:");
                        result.append(failed);
                        showTip(result.toString());

                        hideCount(true);
                    } else {
                        LogUtil.w(TAG, "当前已经写入成功" + currentCount + "次");
                        LogUtil.i(TAG, "当前已经写入成功" + splitCount + "次");
                        isInterrupt = false;
                        isFinish = true;
                    }
                    stopTimer(communicationDisposableList);
                } else {
                    LogUtil.d(TAG, "数据返回异常--" + value);
                    sendToastMsg("数据返回异常");
                }
                break;

            case PROTOCOL_READ_ADDRESS:
                //读表地址回调
                if (value.contains(RESULT_END)) {
                    String[] resultArray = value.split(NEW_LINE);
                    uiDataList.clear();
                    UserInfo resultUserInfo;
                    for (String meterInfo : resultArray) {
                        resultUserInfo = parseMeterInfo(meterInfo);
                        if (resultUserInfo != null) {
                            uiDataList.add(resultUserInfo);
                        }
                    }
                    setUserInfoUploadState(uiDataList, STATE_HAS_READ_ED);
                    notifyUI();
                    ToastUtil.showShortToast("读取成功");
                    stopTimer(communicationDisposableList);
                    hideCount(true);
                } else {
                    sendToastMsg("数据返回异常");
                    LogUtil.i(TAG, "数据返回异常--" + value);
                    stopTimer(communicationDisposableList);
                }
                closeAllDialog();
                break;
            case PROTOCOL_READ_METER_DATA:
                //读水表读数
                if (value.contains(NULL.toUpperCase())) {
                    showTip("当前设备无水表数据");
                } else {
                    if (value.contains(RESULT_END)) {
                        String[] resultArray = value.split(NEW_LINE);
                        uiDataList.clear();
                        UserInfo resultUserInfo;
                        for (String meterInfo : resultArray) {
                            resultUserInfo = parseMeterData(meterInfo);
                            if (resultUserInfo != null) {
                                uiDataList.add(resultUserInfo);
                            }
                        }
                        setUserInfoUploadState(uiDataList, STATE_HAS_READ_ED);
                        notifyUI();
                        waitSycUserList.clear();
                        waitSycUserList.addAll(getExistUserInfo(uiDataList));
                        if (!waitSycUserList.isEmpty()) {
                            showSaveDialog(waitSycUserList);
                        }
                        stopTimer(communicationDisposableList);
                        ToastUtil.showShortToast("读取成功");
                        hideCount(true);
                    }
                }
                closeAllDialog();
                break;
            case PROTOCOL_READ_ALL_METER:
                if (value.contains(RESULT_OK)) {
                    ToastUtil.showShortToast("发送完成");
                    closeAllDialog();
                    stopTimer(communicationDisposableList);
                }
                break;
            case PROTOCOL_WRITE_METER_DATA:
                //写入水表读数回调
                if (value.contains(RESULT_TOTAL)) {
                    successCount += parseCallbackDataCount(value);
                    if (currentCount == splitCount) {
                        isInterrupt = true;
                        closeAllDialog();
                        StringBuilder sb = new StringBuilder("");
                        if (!singleWrite) {
                            int failedCount = pendingWriteList.size() - successCount;
                            sb.append("列表总数:");
                            sb.append(uiDataList.size());
                            sb.append(NEW_LINE);
                            sb.append("写入总数:");
                            sb.append(pendingWriteList.size());
                            sb.append(NEW_LINE);
                            sb.append("成功总数:");
                            sb.append(successCount);
                            sb.append(NEW_LINE);
                            sb.append("失败总数:");
                            sb.append(failedCount);
                        } else {
                            for (UserInfo userInfo : uiDataList) {
                                if (userInfo.equals(modifyUserInfo)) {
                                    userInfo.curdata = modifyUserInfo.data;
                                }
                            }
                            int successCount = parseCallbackDataCount(value);
                            if (successCount > 0) {
                                sb.append("写入成功");
                            } else {
                                sb.append("未找到对应数据,写入失败");
                            }
                            notifyUI();
                        }
                        showTip(sb.toString());
                    } else {
                        LogUtil.w(TAG, "当前已经写入成功" + currentCount + "次");
                        LogUtil.i(TAG, "当前已经写入成功" + splitCount + "次");
                        isInterrupt = false;
                        isFinish = true;
                    }
                    stopTimer(communicationDisposableList);
                }
                break;
            case PROTOCOL_REPLACE_METER_ADDRESS:
                closeAllDialog();
                if (value.contains(RESULT_OK)) {
                    showTip("替换成功");
                } else {
                    showTip("替换失败(表地址可能不存在)");
                }
                stopTimer(communicationDisposableList);
                break;
            case PROTOCOL_SETTING_CURRENT_TIME:
                closeAllDialog();
                stopTimer(communicationDisposableList);
                LogUtil.d(TAG, "--------->" + value);
                if (value.contains(RESULT_OK)) {
                    showTip("同步成功");
                } else {
                    showTip("同步失败");
                }
                break;
            case PROTOCOL_READ_CURRENT_TIME:
                closeAllDialog();
                stopTimer(communicationDisposableList);
                String result = resolveCurrentTime(value);
                if (TextUtils.isEmpty(result)) {
                    showTip("读取失败");
                } else {
                    showTip("当前设备时间", result);
                }
                break;
            case PROTOCOL_SETTING_TIMING:
                closeAllDialog();
                stopTimer(communicationDisposableList);
                if (value.contains(RESULT_OK)) {
                    showTip("定时设置成功");
                } else {
                    showTip("设置失败");
                }
                break;
            case PROTOCOL_READ_TIMING:
                LogUtil.d(TAG, "读取的定时时间：" + value);
                closeAllDialog();
                stopTimer(communicationDisposableList);
                String tip = resolveCallbackTime(value);
                showTip("读取的定时日期", tip);
                break;
            default:
                closeAllDialog();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        stopTimer(dataReceiveDisposableList);
        stopTimer(blueToothConnectDisposableList);
        stopTimer(communicationDisposableList);
        isInterrupt = true;
        isFinish = true;
        EmiUtils.keepScreenLongLight(this, false);
        doDisConnect();
        super.onDestroy();
    }


    private UserInfo parseMeterInfo(String meterInfo) {
        UserInfo userInfo = new UserInfo();
        userInfo.meteraddr = "空";
        userInfo.useraddr = "未知";
        userInfo.curdata = EMPTY_DATA;
        userInfo.accountnum = "";
        userInfo.channelNumber = "";
        userInfo.filename = "";
        int rightLength = 14;
        if (TextUtils.isEmpty(meterInfo) || meterInfo.length() != rightLength) {
            return null;
        } else if (meterInfo.length() == METER_ADDRESS_LENGTH) {
            userInfo.firmCode = meterInfo.substring(0, 4);
            userInfo.meteraddr = meterInfo.substring(4, meterInfo.length());
            userInfo.meteraddr = EmiStringUtil.clearFirstZero(userInfo.meteraddr);
            UserInfo findUserInfo = getSqOperator().findUserByMeterInfo(userInfo.meteraddr, userInfo.firmCode);
            if (findUserInfo == null) {
                findUserInfo = getSqOperator().findUserByMeterInfo(meterInfo);
            }
            if (findUserInfo != null) {
                findUserInfo.curdata = EMPTY_DATA;
            }
            return findUserInfo != null ? findUserInfo : userInfo;
        }
        return userInfo;
    }


    private UserInfo parseMeterData(String meterInfo) {
        UserInfo userInfo = new UserInfo();
        userInfo.meteraddr = "空";
        userInfo.useraddr = "空";
        userInfo.channelNumber = "";
        userInfo.accountnum = "";
        userInfo.filename = "";
        int meterDataLength = 2;
        LogUtil.d(TAG, "获取的信息：" + meterInfo);
        if (meterInfo.length() == METER_ADDRESS_LENGTH + 1 && meterInfo.contains(SPACE)) {
            String[] meterData = meterInfo.split(SPACE);
            if (meterData.length == meterDataLength) {
                userInfo.meteraddr = EmiStringUtil.clearFirstZero(meterData[0]);
                userInfo.curdata = TextUtils.isDigitsOnly(meterData[1]) ? Integer.parseInt(meterData[1]) : -1;
            }
            UserInfo findUserInfo = getSqOperator().findUserByMeterInfo(userInfo.meteraddr);
            if (findUserInfo == null) {
                String test = EmiConstants.FIRM_CODE_7833 + meterData[0];
                findUserInfo = getSqOperator().findUserByMeterInfo(test);
            }
            if (findUserInfo != null) {
                findUserInfo.curdata = userInfo.curdata;
            }
            LogUtil.d(TAG, "解析的水表地址：" + userInfo.meteraddr + "----" + userInfo.curdata);
            return findUserInfo != null ? findUserInfo : userInfo;
        }
        return null;
    }

    private void listenCommunication() {
        doEventCountDown(ONE_SECOND * 60, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                communicationDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                closeAllDialog();
                ToastUtil.showShortToast("通讯超时");
                stopTimer(communicationDisposableList);
                stopSend();
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

    private void showTip(String text) {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("提示")
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

    private void makeButtonEnable(boolean b) {
        btnConnect.setEnabled(b);
        btnClearMeterId.setEnabled(b);
        btnConnect.setEnabled(b);
        btnReadAll.setEnabled(b);
        btnReadMeterData.setEnabled(b);
        btnWriteMeterId.setEnabled(b);
    }

    /**
     * 清空采集器数据
     */
    private void doClearCollectorData() {
        protocolTag = PROTOCOL_CLEAR_ADDRESS;
        showDialog("正在发送清空指令...");
        sendBTCmd(makeProtocol(PROTOCOL_CLEAR_ADDRESS).getBytes());
        clearReceiveData();
        listenCommunication();
    }

    /**
     * 写入水表信息（表地址或读数）到设备
     */
    private void writeMeterInfo(final List<List<UserInfo>> listList, List<UserInfo> writeList) {
        LogUtil.d(TAG, "待写入数量：" + listList.size());
        if (writeList.size() > MAX_WRITE_SIZE) {
            ToastUtil.showShortToast("待写入数量超出" + MAX_WRITE_SIZE + "!");
            closeAllDialog();
            return;
        }
        if (PROTOCOL_WRITE_METER_DATA.equals(protocolTag)) {
            showDialog("准备写入水表读数...");
        }
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                writeToCollector(listList);
            }
        });

    }


    private void downloadingMeterInfo(final List<List<UserInfo>> listList) {
        String protocol;
        if (listList.isEmpty()) {
            sendToastMsg("无数据");
            return;
        }
        for (int i = 0; i < listList.size(); i++) {
            protocol = makeProtocol(protocolTag, listList.get(i));
            isFinish = false;
            currentCount = i + 1;
            count = 0;
            delays(3000);
            listenCommunication();
            if (!isInterrupt) {
                sendBTCmd(protocol.getBytes());
                if (PROTOCOL_WRITE_METER_DATA.equals(protocolTag)) {
                    //写入水表读数（需要移除未抄和失败的数据）
                    if (listList.size() > 1) {
                        sendNotifyDialogMsg("正在写入第" + currentCount + "包水表读数...");
                    } else {
                        sendNotifyDialogMsg("正在写入水表读数...");
                    }
                } else {
                    if (listList.size() > 1) {
                        sendNotifyDialogMsg("正在写入第" + currentCount + "包表地址");
                    } else {
                        sendNotifyDialogMsg("正在写入水表地址...");
                    }
                }
                LogUtil.d(TAG, "发送的协议：" + protocol);
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
            } else {
                break;
            }
        }
    }


    private void sendNotifyDialogMsg(String text) {
        Message message = mHandler.obtainMessage();
        message.obj = text;
        message.what = MSG_NOTIFY_DIALOG;
        mHandler.sendMessage(message);
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


    private void stopSend() {
        isFinish = true;
        isInterrupt = true;
        closeAllDialog();
    }


    private void showSelectTxtFileDialog(final ArrayList<String> functionNameList) {
        CommonSelectEmiAdapter selectFileAdapter = new CommonSelectEmiAdapter(functionNameList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("请选择操作");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final CommonSelectDialog deviceDialog = builder.create();
        selectFileAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                deviceDialog.dismiss();
                switch (position) {
                    case 0:
                        singleWrite = false;
                        doDownloadMeterAddress();
                        break;
                    case 1:
                        singleWrite = false;
                        //批量写入
                        doBatchWriteMeterData();
                        break;
                    default:
                        break;
                }

            }
        });
        selectFileAdapter.bindToRecyclerView(builder.recyclerView);
        deviceDialog.setCancelable(false);
        deviceDialog.setCanceledOnTouchOutside(false);
        builder.setAdapter(selectFileAdapter);
        deviceDialog.show();
    }


    private void doDownloadMeterAddress() {
        downloadMeterAddressFlag = true;
        if (tempDataList.isEmpty()) {
            ToastUtil.showShortToast("无待写入数据");
            return;
        }
        if (tempDataList.size() > MAX_WRITE_SIZE) {
            ToastUtil.showShortToast("待写入数量超出" + MAX_WRITE_SIZE);
            return;
        }
        doClearCollectorData();
    }


    private int parseCallbackDataCount(String value) {
        LogUtil.i(TAG, "解析的内容：" + value);
        value = value.replace(NEW_LINE, EmiConstants.EMPTY);
        String[] valueArray = value.split(SPACE);
        if (valueArray.length > 1) {
            return TextUtils.isDigitsOnly(valueArray[1]) ? Integer.parseInt(valueArray[1]) : 0;
        }
        return 0;
    }


    private void showPop(View view, int position) {
        final EmiPopupList normalViewPopupList = new EmiPopupList(mContext);
        normalViewPopupList.showPopupListWindow(view, position, mRawX, mRawY, stringArrayList, new EmiPopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                String title = "当前表地址：" + uiDataList.get(contextPosition).meteraddr;
                switch (position) {
                    case 0:
                        protocolTag = PROTOCOL_REPLACE_METER_ADDRESS;
                        showInputDialog(contextPosition, title, "请输入新表地址");
                        break;
                    case 1:
                        protocolTag = PROTOCOL_WRITE_METER_DATA;
                        showInputDialog(contextPosition, title, "请输入读数");
                        break;
                    default:
                        break;
                }
            }
        });
    }


    private void showInputDialog(final int position, String title, String hint) {
        final boolean isReplace = PROTOCOL_REPLACE_METER_ADDRESS.equals(protocolTag);
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                inputMsg = builder.editText.getText().toString();
                secondInputMsg = builder.secondEditText.getText().toString();
                if (TextUtils.isEmpty(inputMsg)) {
                    ToastUtil.showShortToast("请输入水表地址");
                    return;
                }
                if (isReplace) {
                    if (TextUtils.isEmpty(secondInputMsg)) {
                        ToastUtil.showShortToast("请输入厂商代码");
                    }
                    doReplaceMeterId(uiDataList.get(position), inputMsg, secondInputMsg);
                } else {
                    singleWriteMeterData(position);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog dialog = builder.create();
        builder.editText.setHint(hint);
        if (isReplace) {
            builder.editText.setMinimumHeight(DisplayUtil.dip2px(mContext, 50));
            builder.secondEditText.setMinimumHeight(DisplayUtil.dip2px(mContext, 50));
            builder.secondEditText.setVisibility(View.VISIBLE);
            builder.secondEditText.setHint("请输入厂商代码");
        }
        dialog.show();
    }

    /**
     * 替换水表地址
     *
     * @param oldUserInfo
     * @param newMeterId
     * @param firmCode
     */
    private void doReplaceMeterId(UserInfo oldUserInfo, String newMeterId, String firmCode) {
        if (!isConnect) {
            tipConnect();
            return;
        }
        protocolTag = PROTOCOL_REPLACE_METER_ADDRESS;
        List<UserInfo> userInfoList = new ArrayList<>();
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.meteraddr = newMeterId;
        newUserInfo.firmCode = firmCode;
        userInfoList.add(oldUserInfo);
        userInfoList.add(newUserInfo);
        showDialog("正在替换表地址...");
        String protocol = makeProtocol(PROTOCOL_REPLACE_METER_ADDRESS, userInfoList);
        LogUtil.i(TAG, "发送的协议：" + protocol);
        sendBTCmd(protocol.getBytes());
        clearReceiveData();
        listenCommunication();
    }

    private void singleWriteMeterData(int position) {
        singleWrite = true;
        ArrayList<UserInfo> userInfoArrayList = new ArrayList<>();
        UserInfo userInfo = uiDataList.get(position);
        userInfo.curdata = Integer.parseInt(inputMsg);
        modifyUserInfo = userInfo;
        userInfo.state = STATE_PEOPLE_RECORDING;
        userInfoArrayList.add(userInfo);
        //单个写入
        writeMeterInfo(EmiStringUtil.splitList(userInfoArrayList, SIZE_SPLIT_PACKAGE), userInfoArrayList);
    }

    private void showCount() {
        StringBuilder sb = new StringBuilder("");
        sb.append("当前");
        switch (protocolTag) {
            case PROTOCOL_READ_ADDRESS:
                sb.append("读取的水表数量:");
                sb.append(uiDataList.size());
                break;
            case PROTOCOL_READ_METER_DATA:
                sb.append("读取的水表数量:");
                sb.append(uiDataList.size());
                break;
            case PROTOCOL_WRITE_ADDRESS:
                sb.append("写入的水表数量:");
                sb.append(uiDataList.size());
                break;
            default:
                sb.append(uiDataList.size());
                break;
        }
        showTip(sb.toString());
    }

    private void hideCount(boolean isShow) {
        titleView.setRightButtonIsShow(true);
    }


    public EmiMenu getMenu(Context context, LifecycleOwner lifecycleOwner, OnMenuItemClickListener onMenuItemClickListener) {
        return new EmiMenu.Builder(context)
                .addItem(new EmiMenuItem("统计", false))
                .addItem(new EmiMenuItem("设置设备时间", false))
                .addItem(new EmiMenuItem("读取设置时间", false))
                .addItem(new EmiMenuItem("设置定时时间", false))
                .addItem(new EmiMenuItem("读取定时时间", false))
                .setLifecycleOwner(lifecycleOwner)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setTextColor(context.getResources().getColor(R.color.md_grey_800))
                .setMenuColor(Color.WHITE)
                .setSelectedEffect(false)
                .setOnMenuItemClickListener(onMenuItemClickListener)
                .build();
    }


    /**
     * 设置设备时间
     */
    private void doSettingCurrentTime() {
        protocolTag = PROTOCOL_SETTING_CURRENT_TIME;
        showDialog("正在同步设备时间...");
        String currentTime = TimeUtil.getCurrentTime();
        String time = TimeUtil.getTimeStringNoSplit(currentTime);
        String protocol = makeProtocol(PROTOCOL_SETTING_CURRENT_TIME, time);
        LogUtil.w(TAG, "测试结果=" + protocol);
        listenCommunication();
        sendBTCmd(protocol.getBytes());
        clearReceiveData();
        listenCommunication();
    }


    /**
     * 设置设备时间
     */
    private void doReadCurrentTime() {
        protocolTag = PROTOCOL_READ_CURRENT_TIME;
        showDialog("正在读取设备时间...");
        String protocol = makeProtocol(PROTOCOL_READ_CURRENT_TIME, "");
        LogUtil.w(TAG, "测试结果=" + protocol);
        listenCommunication();
        sendBTCmd(protocol.getBytes());
        clearReceiveData();
        listenCommunication();
    }


    private String resolveCurrentTime(String callbackValue) {
        if (TextUtils.isEmpty(callbackValue)) {
            return "";
        }
        callbackValue = callbackValue.replace(NEW_LINE, "");
        String[] stringArray = callbackValue.split(SPACE);
        if (callbackValue.contains(TIME) && stringArray.length == 2) {
            String value = stringArray[1];
            return TimeUtil.getTimeChineseCharacter(value);
        }
        return "";
    }


    private void doSkip() {
        Intent intent = new Intent();
        intent.setClass(mContext, TimePickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_DATE_PICKER);
    }

    private void showTimePicker() {
        mDialogHourMinute = new TimePickerDialog.Builder()
                .setType(Type.HOURS_MINUTE)
                .setCallBack(this)
                .setTitle("选择定时时间")
                .build();
        mDialogHourMinute.show(getSupportFragmentManager(), "hour_minute");
    }

    public String getDateToString(long time) {
        Date d = new Date(time);
        return sf.format(d);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_DATE_PICKER:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        int size = data.getIntExtra(EXTRA_SELECT_COUNT, 0);
                        if (size < TEN) {
                            dateSb.append(ZERO);
                        }
                        dateSb.append(size);
                        dateSb.append(data.getStringExtra(EXTRA_SELECT_DATE));
                        LogUtil.d(TAG, "生成的数据：" + dateSb.toString());
                        doSettingTiming(dateSb.toString());
                    }
                }
                break;
            case REQUEST_CODE_OPEN_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    connectBlueTooth();
                }
                break;
            default:
                break;
        }
    }

    private void clearDate() {
        dateSb.setLength(0);
    }

    /**
     * 设置定时时间
     *
     * @param protocol
     */
    private void doSettingTiming(String protocol) {
        protocolTag = PROTOCOL_SETTING_TIMING;
        showDialog("正在设置定时时间...");
        LogUtil.w(TAG, "测试结果=" + protocol);
        listenCommunication();
        sendBTCmd(protocol.getBytes());
        clearReceiveData();
        listenCommunication();
    }

    private void doReadTiming() {
        protocolTag = PROTOCOL_READ_TIMING;
        showDialog("正在读取定时时间...");
        listenCommunication();
        sendBTCmd(PROTOCOL_READ_TIMING.getBytes());
        clearReceiveData();
        listenCommunication();
    }


    private String resolveCallbackTime(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        String failedString = "抄表失败";
        int size = 3;
        int timeSize = 2;
        value = value.replace(NEW_LINE, "");
        String[] arrays = value.split(SPACE);
        if (arrays.length == size) {
            String time;
            String date;
            StringBuilder sb = new StringBuilder("");
            time = arrays[1];
            date = arrays[2];
            LogUtil.w(TAG, "time ===" + time);
            LogUtil.w(TAG, "date ===" + date);
            if (!TextUtils.isDigitsOnly(time) || (!TextUtils.isDigitsOnly(date))) {
                return failedString;
            }
            sb.append("当前定时时间:");
            List<String> timeList = EmiStringUtil.splitStrToList(time);
            if (timeList.size() != timeSize) {
                return failedString;
            }
            sb.append(timeList.get(0));
            sb.append("时");
            sb.append(timeList.get(1));
            sb.append("分");
            sb.append(NEW_LINE);
            List<String> dateList = EmiStringUtil.splitStrToList(date);
            if (dateList.size() == 1 && NO_TIMING.equals(dateList.get(0))) {
                sb.append("抄表天数:0天");
                return sb.toString();
            } else {
                if (dateList.size() < timeSize) {
                    return failedString;
                }
            }
            sb.append("抄表天数:");
            sb.append(EmiStringUtil.clearFirstZero(dateList.get(0)));
            sb.append("天;日期为:");
            for (int i = 1; i < dateList.size(); i++) {
                sb.append(dateList.get(i));
                sb.append("号");
                if (i != dateList.size() - 1) {
                    sb.append("、");
                }
            }
            return sb.toString();
        }
        return failedString;
    }

    /**
     * 移除未抄或失败用户
     */
    private void removeEmptyData(List<UserInfo> userInfoList) {
        UserInfo userInfo;
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            userInfo = userInfoList.get(i);
            if (userInfo.state == STATE_ALL || userInfo.state == STATE_FAILED || userInfo.state == STATE_NO_READ) {
                userInfoList.remove(i);
            }
        }
    }

    /**
     * 筛选待写入水表读数的用户
     */
    private List<UserInfo> selectWriteDataList(List<UserInfo> allUserList) {
        List<UserInfo> userInfoList = new ArrayList<>();
        UserInfo userInfo;
        for (int i = 0; i < allUserList.size(); i++) {
            userInfo = allUserList.get(i);
            boolean isNeedWrite = userInfo.curdata > 0 || userInfo.state == STATE_WARNING ||
                    userInfo.state == STATE_PEOPLE_RECORDING || userInfo.state == STATE_SUCCESS;
            if (isNeedWrite) {
                userInfoList.add(userInfo);
            }
        }
        return userInfoList;
    }


    /**
     * 批量写入水表读数
     */
    private void doBatchWriteMeterData() {
        protocolTag = PROTOCOL_WRITE_METER_DATA;
        pendingWriteList.clear();
        pendingWriteList.addAll(selectWriteDataList(tempDataList));
        LogUtil.w(TAG, "筛选的数据集合长度：" + pendingWriteList.size());
        LogUtil.i(TAG, "总的数据集合长度：" + tempDataList.size());
        if (pendingWriteList.isEmpty()) {
            ToastUtil.showShortToast("没有要写入的数据");
            return;
        }
        writeMeterInfo(EmiStringUtil.splitList(pendingWriteList, SIZE_SPLIT_PACKAGE), pendingWriteList);
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


    /**
     * 显示是否保存对话框
     */
    private void showSaveDialog(final List<UserInfo> userInfoList) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("同步数据")
                .setContentText("检测到数据库有对应水表,是否同步数据?")
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
                        sDialog.dismiss();
                        //todo：执行保存数据库逻辑
                        asyncUpdateUserInfo(userInfoList);
                    }
                });
        TextView contentView = sweetAlertDialog.getContentTextView();
        if (contentView != null) {
            contentView.setText("读取成功");
        }
        sweetAlertDialog.show();
    }

    /**
     * 本地数据存和设备返回的数据对比并赋值
     *
     * @param userInfoList
     * @return
     */
    private List<UserInfo> getExistUserInfo(List<UserInfo> userInfoList) {
        List<UserInfo> savedUserList = new ArrayList<>();
        for (UserInfo info : userInfoList) {
            for (UserInfo sqUserInfo : tempDataList) {
                if (EmiStringUtil.clearFirstZero(info.meteraddr).equals(EmiStringUtil.clearFirstZero(sqUserInfo.meteraddr))) {
                    sqUserInfo.curdata = info.curdata;
                    if (sqUserInfo.curdata == -1) {
                        //抄表失败,需要重置为0
                        sqUserInfo.state = STATE_FAILED;
                        sqUserInfo.data = 0;
                        sqUserInfo.curyl = 0;
                    } else if (sqUserInfo.curdata >= 0) {
                        //读数不为-1表示水表返回了读数,需要判断用量
                        sqUserInfo.curyl = sqUserInfo.curdata - sqUserInfo.lastdata;
                        if (sqUserInfo.curyl < 0 || sqUserInfo.curyl > waterLine) {
                            sqUserInfo.state = STATE_WARNING;
                        } else {
                            sqUserInfo.state = STATE_SUCCESS;
                        }
                    }
                    savedUserList.add(sqUserInfo);
                    break;
                }
            }
        }
        return savedUserList;
    }


    private void asyncUpdateUserInfo(final List<UserInfo> userInfoList) {
        showDialog("保存中...");
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                getSqOperator().updateData(userInfoList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        closeAllDialog();
                    }
                });
            }
        });
    }
}
