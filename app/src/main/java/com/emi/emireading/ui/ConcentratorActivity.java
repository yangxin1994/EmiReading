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
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CollectorCommunicationAdapter;
import com.emi.emireading.adpter.CommonSelectDeviceEmiAdapter;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.EmiDividerItemDecoration;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.CommonSelect;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.log.EmiLog;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;
import com.emi.emireading.widget.view.emimenu.CustomEmiMenu;
import com.emi.emireading.widget.view.emimenu.IconMenuAdapter;
import com.emi.emireading.widget.view.emimenu.IconPowerMenuItem;
import com.emi.emireading.widget.view.emimenu.MenuAnimation;
import com.emi.emireading.widget.view.emimenu.OnMenuItemClickListener;
import com.emi.emireading.widget.view.pickerview.TimePickerDialog;
import com.emi.emireading.widget.view.pickerview.data.Type;
import com.emi.emireading.widget.view.pickerview.listener.OnDateSetListener;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.adpter.CollectorCommunicationAdapter.FAILED_DATA;
import static com.emi.emireading.core.config.EmiConstants.EMI_CALLBACK_CODE_END;
import static com.emi.emireading.core.config.EmiConstants.EMPTY;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.IO_EXCEPTION;
import static com.emi.emireading.core.config.EmiConstants.METER_MAX_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.MSG_BLUETOOTH_CONNECT;
import static com.emi.emireading.core.config.EmiConstants.MSG_ERROR;
import static com.emi.emireading.core.config.EmiConstants.NEW_LINE;
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
import static com.emi.emireading.core.config.EmiConstants.VALUE_ZERO;
import static com.emi.emireading.ui.ChannelListActivity.EXTRA_CHANNEL_BUNDLE;
import static com.emi.emireading.ui.ChannelListActivity.EXTRA_CHANNEL_LIST_ALL;
import static com.emi.emireading.ui.TimePickerActivity.EXTRA_BUNDLE_DATE;
import static com.emi.emireading.ui.TimePickerActivity.EXTRA_SELECT_DATE_LIST;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_READ_ED;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_HAS_WRITE;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.STATE_NO_WRITE;

/**
 * @author :zhoujian
 * @description : 集中器通讯B（68协议）
 * @company :翼迈科技
 * @date 2018年05月21日上午 09:54
 * @Email: 971613168@qq.com
 */

public class ConcentratorActivity extends BaseActivity implements View.OnClickListener, OnMenuItemClickListener<IconPowerMenuItem>, OnDateSetListener {
    public static final String RESULT_OK = "OK";
    private static final String TITLE = "集中器通讯";
    private CustomEmiMenu emiPopupMenu;
    private TimePickerDialog mDialogHourMinute;
    private SimpleDateFormat sf = new SimpleDateFormat("HHmm");
    private StringBuilder dateSb = new StringBuilder("");
    public static final int SKIP_TAG_CONCENTRATOR_COMMUNICATION = 4;
    private static final int MAX_COUNT = 30;
    /**
     * 点击对话框中item的位置
     */
    private int mPosition;
    private String mConcentratorId;
    private boolean isInterrupt;
    private boolean isConnect;
    public BluetoothDevice bluetoothDevice = null;
    private EmiMultipleProgressDialog dialog;
    private BluetoothSocket socket;
    private boolean isStop;
    private int receiveCount;
    private Context mContext;
    private UserInfo modifyUserInfo;
    /**
     * 是否是删除操作
     */
    private boolean isDeleteFlag = false;
    /**
     * 水表地址的标准长度
     */
    private static final int METER_ADDRESS_LENGTH = 14;
    public static final int REQUEST_CODE_DATE_PICKER = 666;
    private static final int MSG_TOAST_INFO = 1001;
    private static final int MSG_LOAD_FINISH = 1002;
    private static final int MSG_CLOSE_DIALOG = 1003;
    private static final int MSG_NOTIFY_DIALOG = 1004;
    private static final int MSG_READ_DEVICE_DATA_FINISH = 1005;
    private String mFileName;
    private String channelNumber;
    private ArrayList<UserInfo> uiDataList = new ArrayList<>();
    private ArrayList<UserInfo> tempDataList = new ArrayList<>();
    private static final String CONCENTRATE_NUMBER = "00";
    /**
     * 通道ID：相当于采集器ID（采集器编号）集合
     */
    private ArrayList<String> allChannelNumberList = new ArrayList<>();
    /**
     * 待写入读数集合
     */
    private ArrayList<UserInfo> pendingWriteList = new ArrayList<>();
    /**
     * 收到回调的次数
     */
    private int tempCount = -1;
    private StringBuffer callBackStringBuffer = new StringBuffer("");
    private String protocolTag = EMPTY;
    private Handler mHandler = new MyHandler(this);
    private ArrayList<Disposable> dataReceiveDisposableList = new ArrayList<>();
    private ArrayList<Disposable> blueToothConnectDisposableList = new ArrayList<>();
    private ArrayList<Disposable> communicationDisposableList = new ArrayList<>();
    private ArrayList<Disposable> receiveDisposableList = new ArrayList<>();
    private CollectorCommunicationAdapter communicationAdapter;
    private RecyclerView rvUserInfo;
    private Button btnDeleteDeviceData;
    private Button btnWriteToConcentrator;
    private Button btnConnect;
    private TitleView titleView;
    private ArrayList<String> stringArrayList;
    private static final String NO_TIMING = "00";

    /**
     * 分包长度
     */
    private final int SIZE_SPLIT_PACKAGE = 40;
    private List<EmiMultipleProgressDialog> dialogList = new ArrayList<>();
    /**
     * 写表地址协议
     */
    private static final String PROTOCOL_WRITE_ADDRESS = "#WRADDR ";
    /**
     * 读表地址协议
     */
    private static final String PROTOCOL_READ_ADDRESS = "#RDADDR ";
    /**
     * 删除某采集器的地址与读数
     */
    private static final String PROTOCOL_DELETE_DEVICE_DATA = "PROTOCOL_DELETE_DEVICE_DATA";

    /**
     * 读取采集器设备信息
     */
    private static final String PROTOCOL_READ_DEVICE_INFO = "PROTOCOL_READ_DEVICE_INFO";

    /**
     * 写入采集器设备编号
     */
    private static final String PROTOCOL_WRITE_DEVICE_NUMBER = "PROTOCOL_WRITE_DEVICE_NUMBER";

    /**
     * 读取某采集器的数据
     */
    private static final String PROTOCOL_READ_DEVICE_DATA = "PROTOCOL_READ_DEVICE_DATA";

    /**
     * 读取某采集器的数据
     */
    private static final String PROTOCOL_READ_DEVICE_DATA_ALL = "PROTOCOL_READ_DEVICE_DATA_ALL";

    /**
     * 获取集中器管辖的采集器编号
     */
    private static final String PROTOCOL_GET_CHANNEL_ID = "PROTOCOL_GET_CHANNEL_ID";

    /**
     * 修改集中器ID
     */
    private static final String PROTOCOL_EDIT_CONCENTRATOR_ID = "PROTOCOL_EDIT_CONCENTRATOR_ID";

    /**
     * 集中器主动获取下辖设备数据
     */
    private static final String PROTOCOL_START_READ_DEVICE_DATA = "PROTOCOL_START_READ_DEVICE_DATA";

    /**
     * 集中器获取下辖设备冻结数据(freeze)
     */
    private static final String PROTOCOL_START_READ_DEVICE_FREEZE_DATA = "PROTOCOL_START_READ_DEVICE_FREEZE_DATA";


    /**
     * 设置定时时间
     */
    private static final String PROTOCOL_SETTING_TIMING = "#TIMING ";

    /**
     * 读取定时时间
     */
    private static final String PROTOCOL_READ_TIMING = "#TIMING";

    /**
     * 获取集中器ID
     */
    private static final String PROTOCOL_READ_CONCENTRATOR_ID = "PROTOCOL_READ_CONSENTRATOR_ID";

    /**
     *
     */
    private static final String PROTOCOL_WRITE_TO_CONCENTRATOR = "PROTOCOL_WRITE_TO_CONCENTRATOR";

    /**
     * 写入冻结参数freeze
     */
    private static final String PROTOCOL_WRITE_DEVICE_FREEZE = "PROTOCOL_WRITE_DEVICE_FREEZE";
    /**
     * 同步时间
     */
    private static final String PROTOCOL_WRITE_SYC_TIME = "PROTOCOL_WRITE_SYC_TIME";
    /**
     * 组网
     */
    private static final String PROTOCOL_READ_ACTUAL_DATA = "PROTOCOL_READ_ACTUAL_DATA";


    private static final int MAX_WRITE_SIZE = 240;
    private String inputMsg;
    private String secondInputMsg;
    private float mRawX;
    private float mRawY;
    private Button btnGetConcentratorId;
    private Button btnGetChannel;


    /**
     * 当前选择的采集器编号
     */
    private String mSelectId;

    private void clearDate() {
        dateSb.setLength(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDeleteDeviceData:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                if (TextUtils.isEmpty(mConcentratorId)) {
                    ToastUtil.showShortToast("请先获取集中器ID");
                    return;
                }
                showDeleteConfirmAlertDialog();
                break;
            case R.id.btnGetChannel:
                showChangeCollector();
                break;
            case R.id.btnConnect:
                hideCount(false);
                doConnectBlueTooth();
                break;
            //获取集中器ID
            case R.id.btnGetConcentratorId:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                doGetConcentratorId();
                break;
            case R.id.btnClearMeterId:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                break;
            case R.id.btnWriteToConcentrator:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                isDeleteFlag = false;
                doSendCMDDeleteDeviceData(mSelectId);
                break;
            case R.id.btnReadMeterData:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                break;
            case R.id.btnReadAll:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                break;
            case R.id.btnReadDeviceData:
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                if (CONCENTRATE_NUMBER.equals(mSelectId)) {
                    showGetAllDeviceDataConfirmAlertDialog();
                } else {
                    doSendCMDGetDeviceData();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.emi_activity_concentrator;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initIntent() {
        mContext = this;
        mFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        channelNumber = getIntent().getStringExtra(EXTRA_CHANNEL_NUMBER);
        Bundle bundle = getIntent().getBundleExtra(EXTRA_CHANNEL_BUNDLE);
        allChannelNumberList.clear();
        allChannelNumberList.addAll((ArrayList<String>) bundle.getSerializable(EXTRA_CHANNEL_LIST_ALL));
        LogUtil.d(TAG, "----文件名：" + mFileName + "----通道号：" + channelNumber);
        LogUtil.d(TAG, "总通道号数量：" + allChannelNumberList.size());
        mSelectId = channelNumber;
    }

    @Override
    protected void initUI() {
        btnConnect = findViewById(R.id.btnConnect);
        btnGetChannel = findViewById(R.id.btnGetChannel);
        rvUserInfo = findViewById(R.id.rvUserInfo);
        btnDeleteDeviceData = findViewById(R.id.btnDeleteDeviceData);
        btnGetConcentratorId = findViewById(R.id.btnGetConcentratorId);
        btnWriteToConcentrator = findViewById(R.id.btnWriteToConcentrator);
        findViewById(R.id.btnReadDeviceData).setOnClickListener(this);
        titleView = findViewById(R.id.titleView);
        btnConnect.setOnClickListener(this);
        btnWriteToConcentrator.setOnClickListener(this);
        btnDeleteDeviceData.setOnClickListener(this);
        btnGetConcentratorId.setOnClickListener(this);
        btnGetChannel.setOnClickListener(this);
        initPopMenu();
        notifyTitle("采集器编号：" + mSelectId);
        showConcentratorId();
    }

    @Override
    protected void initData() {
        EmiUtils.keepScreenLongLight(this, true);
        showDialog("正在查询...");
        hideCount(true);
        titleView.setRightIcon(R.mipmap.ic_menu_abs_more);
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreFunctionMenu();
            }
        });
        communicationAdapter = new CollectorCommunicationAdapter(uiDataList);
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
                    if (receiveCount == 0) {
                        listenBlueToothDataReceive();
                    }
                    receiveCount++;
                    byte[] bufData = new byte[bytes];
                    for (int i = 0; i < bytes; i++) {
                        bufData[i] = buffer[i];
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
                    String callbackResult = callBackStringBuffer.toString();
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
    public void onItemClick(int position, IconPowerMenuItem item) {
        //右上角菜单栏点击事件
        switch (item.getTitle()) {
            case "统计":
                closeMoreFunctionMenu();
                showTip("当前列表中数据数量:" + uiDataList.size());
                break;
            case "读取设备信息":
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                doSendCMDReadDeviceInfo();
                break;
            case "写入采集器编号":
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                closeMoreFunctionMenu();
                showInputDialog();
                break;
            case "获取采集器编号":
                //读取设备管辖的采集器地址
                closeMoreFunctionMenu();
                doReadDeviceNumber();
                break;
            case "修改集中器ID":
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                closeMoreFunctionMenu();
                showEditConcentratorIdDialog();
                break;
            case "抄下辖数据":
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                closeMoreFunctionMenu();
                doSendCMDStartReadDeviceData();
                break;
            case "抄冻结数据":
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                closeMoreFunctionMenu();
                doSendCMDStartReadDeviceFreezeData();
                break;
            case "同步时间":
                closeMoreFunctionMenu();
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                doSendCMDWriteSycTime();
                break;
            case "设置冻结时间":
                closeMoreFunctionMenu();
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                clearDate();
                showTimePicker();
                break;
            case "组网":
                closeMoreFunctionMenu();
                if (!isConnect) {
                    tipConnect();
                    return;
                }
                doSendCMDReadActualData();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
        String dateString = getDateToString(millSeconds);
        dateSb.append(dateString);
        doSkip();
    }

    public String getDateToString(long time) {
        Date d = new Date(time);
        return sf.format(d);
    }

    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ConcentratorActivity activity = (ConcentratorActivity) mWeakReference.get();
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
                    case MSG_READ_DEVICE_DATA_FINISH:
                        activity.notifyUI();
                        activity.closeAllDialog();
                        String tip = activity.createReadResult(activity.uiDataList);
                        activity.showTip(tip);
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

    private void notifyDialogText(String text) {
        if (dialog != null && dialog.isShowing()) {
            dialog.setLabel(text);
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
            tempDataList.addAll(getSqOperator().find(mFileName));
            uiDataList.addAll(selectUserByChannel(tempDataList, channelNumber, STATE_NO_WRITE));
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
        listenCommunication(21);
        //        downloadingMeterInfo(listList);
    }

    private void clearReceiveData() {
        receiveCount = 0;
        tempCount = -1;
        callBackStringBuffer.setLength(0);
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
        if (!checkCallbackDeviceDataCorrect(value)) {
            ToastUtil.showShortToast("返回的数据异常");
            return;
        }
        switch (protocolTag) {
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
            case PROTOCOL_READ_CONCENTRATOR_ID:
                handleCallBckConcentratorId(value);
                cancelListen();
                closeAllDialog();
                break;
            //写入数据到集中器下的某一个采集器回调
            case PROTOCOL_WRITE_TO_CONCENTRATOR:
                closeAllDialog();
                handleCallBackWriteToConcentrator(value);
                break;
            case PROTOCOL_GET_CHANNEL_ID:
                handleCallbackChannelNumber(value);
                break;
            case PROTOCOL_DELETE_DEVICE_DATA:
                closeAllDialog();
                if (isDeleteFlag) {
                    showTip("清除成功");
                    cancelListen();
                    return;
                }
                if (checkCallbackDeviceDataCorrect(value)) {
                    doWriteMeterDataToConcentrator(uiDataList, mSelectId);
                }
                break;
            case PROTOCOL_READ_DEVICE_DATA:
                handleCallbackReadDeviceData(value);
                break;
            case PROTOCOL_READ_DEVICE_INFO:
                handleCallbackReadDeviceInfo(value);
                break;
            case PROTOCOL_WRITE_DEVICE_NUMBER:
                handleCallbackWriteDeviceNumber(value);
                break;
            case PROTOCOL_EDIT_CONCENTRATOR_ID:
                closeAllDialog();
                cancelListen();
                handleCallbackEditConcentratorId(value);
                break;
            case PROTOCOL_START_READ_DEVICE_DATA:
                closeAllDialog();
                cancelListen();
                showTip("操作成功");
                break;
            case PROTOCOL_START_READ_DEVICE_FREEZE_DATA:
                closeAllDialog();
                cancelListen();
                showTip("操作成功");
                break;
            case PROTOCOL_WRITE_SYC_TIME:
                closeAllDialog();
                cancelListen();
                showTip("同步时间", "同步成功");
                break;
            case PROTOCOL_WRITE_DEVICE_FREEZE:
                closeAllDialog();
                cancelListen();
                showTip("设置冻结参数", "设置成功");
                break;
            case PROTOCOL_READ_ACTUAL_DATA:
                closeAllDialog();
                cancelListen();
                showTip("组网指令", "发送成功");
                break;
            case PROTOCOL_READ_DEVICE_DATA_ALL:
                handleCallbackReadDeviceDataAll(value);
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
        EmiUtils.keepScreenLongLight(this, false);
        doDisConnect();
        super.onDestroy();
    }


    private void listenCommunication(int second) {
        doEventCountDown(ONE_SECOND * second, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                communicationDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                closeAllDialog();
                ToastUtil.showShortToast("通讯超时");
                stopTimer(communicationDisposableList);
                //                stopSend();
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


    private void listenReceiveIsFinish(int second) {
        doEventCountDown(ONE_SECOND * second, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                receiveDisposableList.add(d);
            }

            @Override
            public void onNext(Long aLong) {
                closeAllDialog();
                cancelListen();
                stopTimer(receiveDisposableList);
                List<UserInfo> tempList = new ArrayList<>();
                tempList.addAll(uiDataList);
                doUpdateSQLiteUserInfo(uiDataList, getUserListByCondition(tempList));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                stopTimer(receiveDisposableList);
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


    private void showCount() {
        StringBuilder sb = new StringBuilder("");
        sb.append("当前");
        switch (protocolTag) {
            case PROTOCOL_READ_ADDRESS:
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


    private void doSkip() {
        Intent intent = new Intent();
        intent.setClass(mContext, TimePickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_DATE_PICKER);
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


    private void doConnectBlueTooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            connectBlueTooth();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_OPEN_BLUETOOTH);
        }
    }

    private void doGetConcentratorId() {
        showDialog("正在获取集中器ID...");
        protocolTag = PROTOCOL_READ_CONCENTRATOR_ID;
        listenCommunication(5);
        clearReceiveData();
        sendBTCmd(EmiUtils.getConcentratorIdCmd());
    }

    /**
     * 处理集中器返回的ID数据
     *
     * @param callbackString
     */
    private void handleCallBckConcentratorId(String callbackString) {
        closeAllDialog();
        stopTimer(communicationDisposableList);
        String dataBeginCode = "6800046882";
        if (TextUtils.isEmpty(callbackString) || !checkCallbackDeviceDataCorrect(callbackString) || !callbackString.contains(dataBeginCode)) {
            ToastUtil.showShortToast("数据返回异常");
            return;
        }
        int dataIndex = callbackString.indexOf(dataBeginCode) + dataBeginCode.length();
        mConcentratorId = DigitalTrans.hexStringToAlgorism(callbackString.substring(dataIndex, dataIndex + 2)) + "";
        mSelectId = "00";
        notifyTitle("采集器编号：" + mSelectId);
        showConcentratorId();
    }


    private void doWriteMeterDataToConcentrator(List<UserInfo> userInfoList, String collectorId) {
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取集中器ID编号");
            return;
        }
        if (TextUtils.isEmpty(collectorId)) {
            ToastUtil.showShortToast("请先输入采集器ID");
            return;
        }
        String strHex = Integer.toHexString(Integer.parseInt(mConcentratorId));
        LogUtil.i("tag", "strHex=十进制： " + mConcentratorId);
        LogUtil.i("tag", "strHex=十六进制 " + strHex);
        String strHex1 = Integer.toHexString(Integer.parseInt(collectorId));
        LogUtil.e("tag", "strHex=十进制： " + collectorId);
        LogUtil.e("tag", "strHex=十六进制 " + strHex1);
        byte[] cmd = EmiUtils.getWriteToConcentratorCmd(userInfoList, mConcentratorId, collectorId);
        if (cmd.length == 0) {
            ToastUtil.showShortToast("指令错误");
            return;
        }
        protocolTag = PROTOCOL_WRITE_TO_CONCENTRATOR;
        showDialog("正在发送数据到设备...");
        cancelListen();
        listenCommunication(15);
        EmiLog.w(TAG, "筛选的数据集合长度:" + userInfoList.size());
        sendBTCmd(cmd);
    }

    /**
     * 写入数据回调
     *
     * @param callbackString
     */
    private void handleCallBackWriteToConcentrator(String callbackString) {
        closeAllDialog();
        stopTimer(communicationDisposableList);
        if (TextUtils.isEmpty(callbackString) || !checkCallbackDeviceDataCorrect(callbackString)) {
            ToastUtil.showShortToast("数据返回异常");
            return;
        }
        String correctCode = "6800066899";
        int correctIndex = callbackString.indexOf(correctCode);
        String concentratorIdHex = callbackString.substring(correctIndex + correctCode.length(), correctIndex + correctCode.length() + 2);
        int concentratorId = DigitalTrans.hexStringToAlgorism(concentratorIdHex);
        String collectorIdHex = callbackString.substring(correctIndex + correctCode.length() + 2, correctIndex + correctCode.length() + 2 + 2);
        int collectorId = DigitalTrans.hexStringToAlgorism(collectorIdHex);
        if (correctIndex == -1 && !callbackString.endsWith(EMI_CALLBACK_CODE_END)) {
            ToastUtil.showShortToast("写入失败");
            return;
        }
        for (UserInfo userInfo : uiDataList) {
            userInfo.uploadState = STATE_HAS_WRITE;
        }
        int count;
        correctIndex = correctIndex + correctCode.length();
        count = DigitalTrans.hexStringToAlgorism(callbackString.substring(correctIndex + 8, correctIndex + 8 + 2));
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("当前操作小区ID:");
        stringBuilder.append(concentratorId);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("当前操作的采集器编号:");
        stringBuilder.append(collectorId);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("写入成功数量:");
        stringBuilder.append(count);
        notifyUI();
        showTip(stringBuilder.toString());
    }


    /**
     * 自动抄表设置对话框
     */
    private void showChangeCollector() {
        closeMoreFunctionMenu();
        if (allChannelNumberList.isEmpty()) {
            ToastUtil.showShortToast("没有找到相关采集器信息");
            return;
        }
        final List<CommonSelect> commonSelectList = new ArrayList<>();
        for (String channel : allChannelNumberList) {
            if (channel.equals(channelNumber)) {
                commonSelectList.add(new CommonSelect("采集器编号:" + channel, true));
            } else {
                commonSelectList.add(new CommonSelect("采集器编号:" + channel, false));
            }
        }
        final CommonSelectDeviceEmiAdapter commonSelectAdapter = new CommonSelectDeviceEmiAdapter(commonSelectList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("选择数据源(总数:" + allChannelNumberList.size() + ")");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                channelNumber = allChannelNumberList.get(mPosition);
                mSelectId = channelNumber;
                notifyTitle("采集器编号：" + channelNumber);
                List<UserInfo> userInfoList = selectUserByChannel(tempDataList, channelNumber, STATE_NO_WRITE);
                pendingWriteList.clear();
                pendingWriteList.addAll(userInfoList);
                notifyDataList(userInfoList);
                dialog.dismiss();
                closeMoreFunctionMenu();
            }
        });
        final CommonSelectDialog commonSelectDialog = builder.create();
        commonSelectDialog.setCancelable(true);
        commonSelectDialog.setCanceledOnTouchOutside(true);
        commonSelectAdapter.bindToRecyclerView(builder.recyclerView);
        commonSelectAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                mPosition = position;
                commonSelectAdapter.select(commonSelectList, mPosition);
            }
        });
        builder.setAdapter(commonSelectAdapter);
        commonSelectDialog.show();
    }


    private void notifyDataList(List<UserInfo> dataList) {
        uiDataList.clear();
        uiDataList.addAll(dataList);
        notifyUI();
    }

    private void showMoreFunctionMenu() {
        if (emiPopupMenu != null && !emiPopupMenu.isShowing()) {
            emiPopupMenu.showAsDropDown(titleView.getRightView(), -80, 0);
        }
    }

    private void closeMoreFunctionMenu() {
        if (emiPopupMenu != null && emiPopupMenu.isShowing()) {
            emiPopupMenu.dismiss();
        }
    }

    private void initPopMenu() {
        emiPopupMenu = new CustomEmiMenu.Builder<>(mContext, new IconMenuAdapter())
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_record), "统计"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_data_count), "读取设备信息"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "写入采集器编号"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "获取采集器编号"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "修改集中器ID"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "抄下辖数据"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "抄冻结数据"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "同步时间"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "设置冻结时间"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.mipmap.icon_select_ducupile), "组网"))
                .setLifecycleOwner(this)
                .setOnMenuItemClickListener(this)
                .setAnimation(MenuAnimation.FADE)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .build();
    }

    private void notifyTitle(String titleText) {
        titleView.setTitle(titleText);
    }

    /**
     * 显示集中器ID
     */
    private void showConcentratorId() {
        if (TextUtils.isEmpty(mConcentratorId)) {
            titleView.setRightIconText("ID:空");
        } else {
            if (PROTOCOL_READ_CONCENTRATOR_ID.equals(protocolTag)) {
                ToastUtil.showShortToast("读取成功");
            }
            titleView.setRightIconText("ID:" + mConcentratorId);
        }
    }

    /**
     * 获取当前集中器管辖的采集器编号
     */
    private void doReadDeviceNumber() {
        if (!isConnect) {
            tipConnect();
            return;
        }
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取集中器ID");
            return;
        }
        protocolTag = PROTOCOL_GET_CHANNEL_ID;
        showDialog("正在获取当前设备管辖的采集器编号...");
        clearReceiveData();
        cancelListen();
        listenCommunication(3);
        sendBTCmd(EmiUtils.getCmdDeviceId(mConcentratorId));
    }


    private void handleCallbackChannelNumber(String value) {
        closeAllDialog();
        LogUtil.e("原始数据：" + value);
        stopTimer(communicationDisposableList);
        if (!checkCallbackDeviceDataCorrect(value)) {
            ToastUtil.showShortToast("返回的数据异常");
            return;
        }
        String code = "68A2";
        if (!value.contains(code)) {
            ToastUtil.showShortToast("解析失败");
            return;
        }
        int codeIndex = value.indexOf(code);
        int endIndex = value.lastIndexOf(EmiConstants.EMI_CALLBACK_CODE_END);
        codeIndex = codeIndex + code.length();
        mConcentratorId = DigitalTrans.hexStringToAlgorism(value.substring(codeIndex, codeIndex + 2)) + "";
        //带载的设备数量
        LogUtil.w("截取的内容：" + value.substring(codeIndex + 4 + 2, endIndex - 2));
        String deviceCountStr = DigitalTrans.numberToHexByte(value.substring(codeIndex + 4, codeIndex + 4 + 2)) + "";
        int deviceCount = DigitalTrans.hexStringToAlgorism(deviceCountStr);
        List<String> deviceNumberList = EmiStringUtil.splitStrToList(value.substring(codeIndex + 4 + 2, endIndex - 2));
        if (deviceNumberList.size() != deviceCount) {
            ToastUtil.showShortToast("数据返回异常");
            return;
        }
        for (String s : deviceNumberList) {
            LogUtil.e("当前编号：" + s);
        }
        StringBuilder tip = new StringBuilder("");
        tip.append("当前的设备ID:" + mConcentratorId);
        tip.append(NEW_LINE + "当前设备带载的编号分别为:");
        for (int i = 0; i < deviceNumberList.size(); i++) {
            tip.append(DigitalTrans.hexStringToAlgorism(deviceNumberList.get(i)));
            if (i != deviceNumberList.size() - 1) {
                tip.append("、");
            }
            deviceNumberList.set(i, DigitalTrans.hexStringToAlgorism(deviceNumberList.get(i)) + "");
        }
        LogUtil.d("获取的数量：" + deviceCount);
        LogUtil.d("获取的编号：" + value.substring(codeIndex + 4 + 2, endIndex - 2));
        showReadCollectorList(deviceNumberList);
        //        showTip(tip.toString());
    }

    /**
     * 自动抄表设置对话框
     */
    private void showReadCollectorList(final List<String> channelList) {
        if (channelList.isEmpty()) {
            ToastUtil.showShortToast("没有找到相关采集器信息");
            return;
        }
        final List<CommonSelect> commonSelectList = new ArrayList<>();
        for (String channel : channelList) {
            commonSelectList.add(new CommonSelect("采集器编号:" + channel, false));
        }
        final CommonSelectDeviceEmiAdapter commonSelectAdapter = new CommonSelectDeviceEmiAdapter(commonSelectList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("选择采集器编号(总数:" + channelList.size() + ")");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                closeMoreFunctionMenu();
            }
        });
        final CommonSelectDialog commonSelectDialog = builder.create();
        commonSelectDialog.setCancelable(true);
        commonSelectDialog.setCanceledOnTouchOutside(true);
        commonSelectAdapter.bindToRecyclerView(builder.recyclerView);
        commonSelectAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                closeMoreFunctionMenu();
                commonSelectAdapter.select(commonSelectList, position);
                mSelectId = channelList.get(position);
                notifyTitle("当前采集器编号：" + mSelectId);
            }
        });
        builder.setAdapter(commonSelectAdapter);
        commonSelectDialog.show();
    }


    private void doSendCMDDeleteDeviceData(String deviceId) {
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先输入采集器编号");
            return;
        }
        if (TextUtils.isEmpty(deviceId) || !TextUtils.isDigitsOnly(deviceId)) {
            ToastUtil.showShortToast("设备编号不正确");
            return;
        }
        protocolTag = PROTOCOL_DELETE_DEVICE_DATA;
        showDialog("正在清空当前采集器数据...");
        clearReceiveData();
        cancelListen();
        sendBTCmd(EmiUtils.getCmdDeleteDeviceData(mConcentratorId, deviceId));
        listenCommunication(3);
    }


    private void doSendCMDGetDeviceData() {
        byte[] cmd = EmiUtils.getCmdReadDeviceData(mConcentratorId, mSelectId);
        if (cmd.length == 0) {
            ToastUtil.showShortToast("指令错误");
            return;
        }
        protocolTag = PROTOCOL_READ_DEVICE_DATA;
        clearReceiveData();
        uiDataList.clear();
        notifyUI();
        showDialog("正在获取采集器编号:" + mSelectId + "的数据");
        uiDataList.clear();
        listenCommunication(5);
        sendBTCmd(cmd);
    }


    private void handleCallbackReadDeviceData(String value) {
        String beginCode = "68";
        int index = StringUtils.ordinalIndexOf(value, beginCode, 2) + beginCode.length();
        int endIndex = value.lastIndexOf(EmiConstants.EMI_CALLBACK_CODE_END);
        String allCount = value.substring(index + 6, index + 6 + 2);
        String currentCount = value.substring(index + 6 + 2, index + 6 + 2 + 2);
        int total;
        int current;
        try {
            total = Integer.parseInt(allCount);
            current = Integer.parseInt(currentCount);
        } catch (NumberFormatException e) {
            total = -1;
            current = -1;
        }
        String meterInfo;
        int deviceNumber;
        List<UserInfo> callbackUserList;
        deviceNumber = DigitalTrans.hexStringToAlgorism(value.substring(index + 4, index + 4 + 2));
        if (total == -1 || current == -1) {
            showTip("数据返回异常");
            stopTimer(communicationDisposableList);
            closeAllDialog();
        } else if (total == 0 && current == 0) {
            showTip("当前设备无数据");
            cancelListen();
            closeAllDialog();
        } else if (current < total) {
            cancelListen();
            listenCommunication(10);
            meterInfo = value.substring(index + 10, endIndex - 2);
            callbackUserList = parseStringToUserList(meterInfo);
            uiDataList.addAll(callbackUserList);
            notifyUI();
            notifyDialogText("正在获取" + deviceNumber + "号设备的" + "第" + current + "包数据");
        } else if (current == total && total > 0) {
            cancelListen();
            meterInfo = value.substring(index + 10, endIndex - 2);
            callbackUserList = parseStringToUserList(meterInfo);
            uiDataList.addAll(callbackUserList);
            doUpdateSQLiteUserInfo(uiDataList, getUserListByCondition(uiDataList));
        }

    }

    private void cancelListen() {
        stopTimer(communicationDisposableList);
    }


    private List<UserInfo> parseStringToUserList(String meterInfoStr) {
        int dataSize = 12;
        int minSize = 20;
        List<UserInfo> userInfoList = new ArrayList<>();
        if (meterInfoStr.length() < minSize) {
            return userInfoList;
        }
        int timeSize = 7;
        String meterInfoValue = meterInfoStr.substring(14, meterInfoStr.length());
        String timeString = meterInfoStr.substring(0, 14);
        List<String> timeStringList = EmiStringUtil.getSplitStrListByLength(timeString, 2);
        if (timeStringList.size() != timeSize) {
            return userInfoList;
        }
        int callbackMonth = DigitalTrans.hexStringToAlgorism(timeStringList.get(2));
        Calendar calendar = Calendar.getInstance();
        //获取系统的当前月份
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        boolean isNeedSave = currentMonth == callbackMonth;
        String timeMillis = TimeUtil.dateListParseTimeMillis(timeStringList);
        List<String> meterInfoList = EmiStringUtil.getSplitStrListByLength(meterInfoValue, dataSize);
        String meterInfo;
        UserInfo userInfo;
        for (int i = meterInfoList.size() - 1; i >= 0; i--) {
            meterInfo = meterInfoList.get(i);
            if (!TextUtils.isEmpty(meterInfo) && meterInfo.length() == dataSize) {
                userInfo = new UserInfo();
                userInfo.meteraddr = DigitalTrans.hexStringToAlgorism(meterInfo.substring(0, 8)) + "";
                userInfo.uploadState = STATE_HAS_READ_ED;
                userInfo.curreaddate = timeMillis;
                if (isNeedSave) {
                    userInfo.rereadflag = 1;
                } else {
                    userInfo.rereadflag = -1;
                }
                userInfo.channel = userInfo.channelNumber;
                userInfo.curdata = DigitalTrans.hexStringToAlgorism(meterInfo.substring(8, meterInfo.length()));
                if (userInfo.curdata > EmiConstants.METER_MAX_METER_DATA) {
                    userInfo.curdata = -1;
                    userInfo.state = STATE_FAILED;
                } else {
                    userInfo.state = STATE_SUCCESS;
                }
                userInfoList.add(userInfo);
            }
        }
        Collections.reverse(userInfoList);
        return userInfoList;
    }

    /**
     * 发送读取设备信指令
     */
    private void doSendCMDReadDeviceInfo() {
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取集中器ID编号");
            return;
        }
        byte[] cmd = EmiUtils.getCmdReadDeviceInfo(mConcentratorId);
        if (cmd.length == 0) {
            ToastUtil.showShortToast("设备ID不正确");
            return;
        }
        protocolTag = PROTOCOL_READ_DEVICE_INFO;
        closeAllDialog();
        clearReceiveData();
        showDialog("正在获取设备信息...");
        listenCommunication(3);
        sendBTCmd(cmd);
        closeMoreFunctionMenu();
    }

    /**
     * 解析返回的设备信息
     *
     * @param callbackString
     */
    private void handleCallbackReadDeviceInfo(String callbackString) {
        cancelListen();
        closeAllDialog();
        LogUtil.d("返回的数据：" + callbackString);
        String beginCode = "68";
        int dateLength = 7;
        int dateEndLength = 14;
        int startIndex = StringUtils.ordinalIndexOf(callbackString, beginCode, 2) + beginCode.length() + 6;
        String info = callbackString.substring(startIndex, callbackString.lastIndexOf(EmiConstants.EMI_CALLBACK_CODE_END) - 2);
        if (info.length() < dateEndLength) {
            ToastUtil.showShortToast("解析失败");
            return;
        }
        String dateString = info.substring(0, 14);
        String freezingTime = info.substring(14, 14 + 2);
        freezingTime = DigitalTrans.hexStringToAlgorism(freezingTime) + "时";
        freezingTime = freezingTime + DigitalTrans.hexStringToAlgorism(info.substring(16, 18)) + "秒";
        StringBuilder sbDate = new StringBuilder("");
        List<String> dateList = EmiStringUtil.getSplitStrListByLength(dateString, 2);
        if (dateList.size() != dateLength) {
            ToastUtil.showShortToast("解析失败");
            return;
        }
        sbDate.append(DigitalTrans.hexStringToAlgorism(dateList.get(0)));
        sbDate.append(DigitalTrans.hexStringToAlgorism(dateList.get(1)));
        sbDate.append("年");
        int month = DigitalTrans.hexStringToAlgorism(dateList.get(2));
        sbDate.append(month);
        sbDate.append("月");
        int day = DigitalTrans.hexStringToAlgorism(dateList.get(3));
        sbDate.append(day);
        sbDate.append("日");
        sbDate.append(DigitalTrans.hexStringToAlgorism(dateList.get(4)));
        sbDate.append("时");
        sbDate.append(DigitalTrans.hexStringToAlgorism(dateList.get(5)));
        sbDate.append("分");
        sbDate.append(DigitalTrans.hexStringToAlgorism(dateList.get(6)));
        sbDate.append("秒");
        String freezingCount = DigitalTrans.hexStringToAlgorism(info.substring(18, 20)) + "";
        LogUtil.d("返回的数据：日期：" + sbDate.toString());
        LogUtil.i("返回的数据：" + freezingCount);
        String freezingDateStr = info.substring(20, info.length());
        //冻结天数
        StringBuilder stringBuilderFreezingDate = new StringBuilder("");
        stringBuilderFreezingDate.append("分别是:");
        List<String> freezingDateList = EmiStringUtil.getStrList(freezingDateStr, 2);
        for (int i = 0; i < freezingDateList.size(); i++) {
            stringBuilderFreezingDate.append(DigitalTrans.hexStringToAlgorism(freezingDateList.get(i)));
            stringBuilderFreezingDate.append("号");
            if (i != freezingDateList.size() - 1) {
                stringBuilderFreezingDate.append("、");
            }
        }
        StringBuilder sbTip = new StringBuilder("");
        sbTip.append("设备的时间：");
        sbTip.append(sbDate.toString());
        sbTip.append(NEW_LINE);
        sbTip.append("冻结时间:");
        sbTip.append(freezingTime);
        sbTip.append(NEW_LINE);
        sbTip.append("冻结日期数量:");
        sbTip.append(freezingCount);
        sbTip.append(NEW_LINE);
        sbTip.append("冻日日期:");
        if (VALUE_ZERO.equals(freezingCount)) {
            sbTip.append("无");
        } else {
            sbTip.append(stringBuilderFreezingDate.toString());
        }
        showTip("设备信息", sbTip.toString());
    }


    private void doSendCMDWriteDeviceNumber(int count) {
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取设备ID");
            return;
        }
        byte[] cmd = EmiUtils.getCmdWriteDeviceNumber(mConcentratorId, count);
        if (cmd.length == 0) {
            ToastUtil.showShortToast("设备ID不正确");
            return;
        }
        protocolTag = PROTOCOL_WRITE_DEVICE_NUMBER;
        closeAllDialog();
        listenCommunication(5);
        showDialog("正在设置设备编号...");
        sendBTCmd(cmd);
    }


    private void handleCallbackWriteDeviceNumber(String value) {
        closeAllDialog();
        cancelListen();
        String beginCode = "68";
        int startIndex = StringUtils.ordinalIndexOf(value, beginCode, 2) + beginCode.length() + 6;
        String count = value.substring(startIndex, startIndex + 2);
        String numbers = value.substring(startIndex + 2, value.indexOf(EmiConstants.EMI_CALLBACK_CODE_END) - 2);
        List<String> numberList = EmiStringUtil.getStrList(numbers, 2);
        if (DigitalTrans.hexStringToAlgorism(count) != numberList.size()) {
            ToastUtil.showShortToast("解析失败");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("写入的设备数量:");
        stringBuilder.append(count);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("编号分别为:");
        for (int i = 0; i < numberList.size(); i++) {
            stringBuilder.append(DigitalTrans.hexStringToAlgorism(numberList.get(i)));
            if (i != numberList.size() - 1) {
                stringBuilder.append("、");
            }
        }
        showTip("写入成功", stringBuilder.toString());
    }

    private void showInputDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("请输入当前集中器管辖的数量");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(builder.editText.getText().toString())) {
                    return;
                }
                try {
                    if (Integer.parseInt(builder.editText.getText().toString()) > MAX_COUNT) {
                        ToastUtil.showShortToast("设备编号不能超过" + MAX_COUNT);
                        return;
                    }
                    doSendCMDWriteDeviceNumber(Integer.parseInt(builder.editText.getText().toString()));
                } catch (NumberFormatException e) {
                    ToastUtil.showShortToast("只能输入数字");
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog inputDialog = builder.create();
        builder.setInputMaxLength(2);
        builder.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputDialog.show();
    }


    private void showEditConcentratorIdDialog() {
        final int maxSize = 128;
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("请输入新集中器ID");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(builder.editText.getText().toString())) {
                    return;
                }
                try {
                    if (Integer.parseInt(builder.editText.getText().toString()) > maxSize) {
                        ToastUtil.showShortToast("设备编号不能超过" + maxSize);
                        return;
                    }
                    doSendCMDWriteConcentratorId(builder.editText.getText().toString());
                } catch (NumberFormatException e) {
                    ToastUtil.showShortToast("只能输入数字");
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog inputDialog = builder.create();
        builder.setInputMaxLength(3);
        builder.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputDialog.show();
    }

    private void doSendCMDWriteConcentratorId(String newConcentratorId) {
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取当前设备ID");
            return;
        }
        if (TextUtils.isEmpty(newConcentratorId)) {
            ToastUtil.showShortToast("请输入新设备ID");
            return;
        }
        protocolTag = PROTOCOL_EDIT_CONCENTRATOR_ID;
        closeAllDialog();
        clearReceiveData();
        listenCommunication(3);
        showDialog("正在修改设备ID...");
        byte[] cmd = EmiUtils.getCmdEditConcentratorId(mConcentratorId, newConcentratorId);
        sendBTCmd(cmd);
    }

    private void handleCallbackEditConcentratorId(String value) {
        String beginCode = "68";
        int startIndex = StringUtils.ordinalIndexOf(value, beginCode, 2) + beginCode.length() + 2;
        String newID = value.substring(startIndex, startIndex + 2);
        mConcentratorId = DigitalTrans.hexStringToAlgorism(newID) + "";
        showConcentratorId();
        showTip("集中器ID修改成功", "当前设备修改后的ID为:" + mConcentratorId);
    }

    /**
     * 集中器开始集抄下辖采集器冻结数据  27H
     *
     * @return
     */

    private void doSendCMDStartReadDeviceData() {
        if (TextUtils.isEmpty(mConcentratorId)) {
            tipGetConcentratorId();
            return;
        }
        byte[] cmd = EmiUtils.getCmdStartReadDeviceData(mConcentratorId);
        if (cmd.length == 0) {
            ToastUtil.showShortToast("设备ID不正确");
            return;
        }
        protocolTag = PROTOCOL_START_READ_DEVICE_DATA;
        closeAllDialog();
        showDialog("正在发送”抄下辖采集器数据“指令...");
        listenCommunication(5);
        sendBTCmd(cmd);
    }

    private void doSendCMDStartReadDeviceFreezeData() {
        if (TextUtils.isEmpty(mConcentratorId)) {
            tipGetConcentratorId();
            return;
        }
        byte[] cmd = EmiUtils.getCmdStartReadDeviceFreezeData(mConcentratorId);
        if (cmd.length == 0) {
            ToastUtil.showShortToast("设备ID不正确");
            return;
        }
        protocolTag = PROTOCOL_START_READ_DEVICE_FREEZE_DATA;
        closeAllDialog();
        showDialog("正在发送”抄下辖采集器冻结数据“指令...");
        listenCommunication(5);
        sendBTCmd(cmd);
    }

    private void tipGetConcentratorId() {
        ToastUtil.showShortToast("请先获取设备ID");
    }

    /**
     * 写同步时间
     */
    private void doSendCMDWriteSycTime() {
        protocolTag = PROTOCOL_WRITE_SYC_TIME;
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取设备ID");
            return;
        }
        showDialog("正在同步时间...");
        listenCommunication(5);
        clearReceiveData();
        byte[] cmd = EmiUtils.getCmdWriteSycTime(mConcentratorId, "00");
        sendBTCmd(cmd);
    }

    /**
     * 写冻结参数（日期）
     *
     * @param date
     * @param dateList
     */

    private void doSendCMDSetDeviceRunInfo(String date, List<Integer> dateList) {
        protocolTag = PROTOCOL_WRITE_DEVICE_FREEZE;
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取设备ID");
            return;
        }
        List<Integer> dateLists = new ArrayList<>();
        if (dateList != null && !dateList.isEmpty()) {
            dateLists.addAll(dateList);
        }
        showDialog("正在设置参数...");
        byte[] cmd = EmiUtils.getCmdSetDeviceRunInfo(mConcentratorId, "00", date, dateLists);
        clearReceiveData();
        listenCommunication(5);
        sendBTCmd(cmd);
    }


    private void showTimePicker() {
        mDialogHourMinute = new TimePickerDialog.Builder()
                .setType(Type.HOURS_MINUTE)
                .setCallBack(this)
                .setThemeColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setTitle("选择定时时间")
                .build();
        mDialogHourMinute.show(getSupportFragmentManager(), "hour_minute");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_DATE_PICKER:
                if (Activity.RESULT_OK == resultCode && data != null) {
                    Bundle bundle = data.getBundleExtra(EXTRA_BUNDLE_DATE);
                    if (bundle == null) {
                        ToastUtil.showShortToast("未获取到日期");
                        return;
                    }
                    ArrayList<Integer> dates = (ArrayList<Integer>) bundle.getSerializable(EXTRA_SELECT_DATE_LIST);
                    doSendCMDSetDeviceRunInfo(dateSb.toString(), dates);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 发组网指令
     */

    private void doSendCMDReadActualData() {
        protocolTag = PROTOCOL_READ_ACTUAL_DATA;
        if (TextUtils.isEmpty(mConcentratorId)) {
            ToastUtil.showShortToast("请先获取设备ID");
            return;
        }
        showDialog("正在发送组网指令...");
        byte[] cmd = EmiUtils.getCmdReadActualData(mConcentratorId, mSelectId);
        clearReceiveData();
        listenCommunication(5);
        sendBTCmd(cmd);
    }

    /**
     * 检测数据是否存在
     *
     * @param meterId
     * @return
     */
    private UserInfo getSQLiteUserInfo(String meterId) {
        if (TextUtils.isEmpty(meterId)) {
            return null;
        }
        return getSqOperator().findUserByMeterInfo(meterId);
    }


    private void doUpdateSQLiteUserInfo(final List<UserInfo> allUserInfoList, final List<UserInfo> failedList) {
        if (allUserInfoList == null || allUserInfoList.isEmpty()) {
            return;
        }
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                List<UserInfo> tempList = new ArrayList<>();
                //UI中显示的用户信息
                UserInfo uiUserInfo;
                for (int i = 0; i < allUserInfoList.size(); i++) {
                    uiUserInfo = allUserInfoList.get(i);
                    UserInfo sqLiteUserInfo = getSQLiteUserInfo(uiUserInfo.meteraddr);
                    if (sqLiteUserInfo == null) {
                        loadUserState(uiUserInfo);
                        continue;
                    }
                    sqLiteUserInfo.curdata = uiUserInfo.curdata;
                    sqLiteUserInfo.curyl = sqLiteUserInfo.curdata - sqLiteUserInfo.lastdata;
                    uiUserInfo.channel = sqLiteUserInfo.channelNumber;
                    sqLiteUserInfo.curreaddate = uiUserInfo.curreaddate;
                    uiUserInfo.useraddr = sqLiteUserInfo.useraddr;
                    sqLiteUserInfo.curreaddate = uiUserInfo.curreaddate;
                    sqLiteUserInfo.channel = sqLiteUserInfo.channelNumber;
                    loadUserState(sqLiteUserInfo);
                    if (uiUserInfo.curdata >= 0) {
                        tempList.add(sqLiteUserInfo);
                    }
                }
                removeUserListByCondition(tempList);
                getSqOperator().updateData(tempList);
                LogUtil.w("需要存储的数据长度：" + failedList.size());
                removeNonExistentData(failedList);
                getSqOperator().updateState(failedList, STATE_FAILED);
                LogUtil.i("需要存储的数据长度：" + tempList.size());
                LogUtil.e("需要存储的数据长度：" + failedList.size());
                sendEmptyMsg(MSG_READ_DEVICE_DATA_FINISH);
            }
        });
    }


    private void loadUserState(UserInfo userInfo) {
        int limitUsage = PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50);
        if (userInfo == null) {
            return;
        }
        int waterUsage = userInfo.curdata - userInfo.lastdata;
        if (userInfo.curdata < 0 || userInfo.curdata > METER_MAX_METER_DATA) {
            userInfo.state = STATE_FAILED;
        } else if (waterUsage < 0 || waterUsage > limitUsage) {
            userInfo.state = STATE_WARNING;
        } else {
            userInfo.state = STATE_SUCCESS;
        }
    }

    /**
     * 生成读取结果
     */
    private String createReadResult(List<UserInfo> userInfoList) {
        StringBuilder stringBuilder = new StringBuilder("");
        int failedCount = 0;
        int successCount = 0;
        int warningCount = 0;
        stringBuilder.append("水表总数:");
        stringBuilder.append(userInfoList.size());
        stringBuilder.append(NEW_LINE);
        String readDate = "未知";
        if (!userInfoList.isEmpty()) {
            readDate = userInfoList.get(0).curreaddate;
        }
        for (int i = 0; i < userInfoList.size(); i++) {
            switch (userInfoList.get(i).state) {
                case STATE_FAILED:
                    failedCount++;
                    break;
                case STATE_SUCCESS:
                    successCount++;
                    break;
                case STATE_WARNING:
                    warningCount++;
                    break;
                default:
                    failedCount++;
                    break;
            }
        }
        stringBuilder.append("抄表日期:");
        stringBuilder.append(TimeUtil.getTimeString(readDate));
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("抄表正常:");
        stringBuilder.append(successCount);
        stringBuilder.append("只");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("抄表异常:");
        stringBuilder.append(warningCount);
        stringBuilder.append("只");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("抄表失败:");
        stringBuilder.append(failedCount);
        stringBuilder.append("只");
        return stringBuilder.toString();
    }


    private void showDeleteConfirmAlertDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("确定删除?")
                .setContentText("将要删除设备编号为:" + mSelectId + "的设备数据")
                .setCancelText("取消")
                .setConfirmText("删除")
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
                        isDeleteFlag = true;
                        doSendCMDDeleteDeviceData(mSelectId);
                    }
                })
                .show();
    }


    private void doSendCMDGetDeviceDataAll() {
        byte[] cmd = EmiUtils.getCmdReadDeviceData(mConcentratorId, "00");
        if (cmd.length == 0) {
            ToastUtil.showShortToast("指令错误");
            return;
        }
        protocolTag = PROTOCOL_READ_DEVICE_DATA_ALL;
        clearReceiveData();
        uiDataList.clear();
        notifyUI();
        showDialog("正在获取集中器ID为" + mConcentratorId + "的全部数据");
        uiDataList.clear();
        listenCommunication(5);
        sendBTCmd(cmd);
    }

    private void handleCallbackReadDeviceDataAll(String value) {
        String beginCode = "68";
        int index = StringUtils.ordinalIndexOf(value, beginCode, 2) + beginCode.length();
        int endIndex = value.lastIndexOf(EmiConstants.EMI_CALLBACK_CODE_END);
        String allCount = value.substring(index + 6, index + 6 + 2);
        String currentCount = value.substring(index + 6 + 2, index + 6 + 2 + 2);
        int total;
        int current;
        try {
            total = Integer.parseInt(allCount);
            current = Integer.parseInt(currentCount);
        } catch (NumberFormatException e) {
            total = -1;
            current = -1;
        }
        String meterInfo;
        List<UserInfo> callbackUserList;
        stopTimer(receiveDisposableList);
        listenReceiveIsFinish(8);
        if (total == -1 || current == -1) {
            showTip("数据返回异常");
            stopTimer(communicationDisposableList);
            closeAllDialog();
            return;
        }
        int deviceNumber;
        cancelListen();
        if (total == 0 && current == 0) {
            notifyDialogText("当前设备无数据");
        } else {
            listenCommunication(20);
            meterInfo = value.substring(index + 10, endIndex - 2);
            callbackUserList = parseStringToUserList(meterInfo);
            deviceNumber = DigitalTrans.hexStringToAlgorism(value.substring(index + 4 + 2, index + 4 + 2 + 2));
            uiDataList.addAll(callbackUserList);
            notifyUI();
            notifyDialogText("正在获取" + deviceNumber + "号设备的第" + current + "包数据");
        }
    }


    private void showGetAllDeviceDataConfirmAlertDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("获取全部数据")
                .setContentText("即将获取该集中器带载的全部数据")
                .setCancelText("取消")
                .setConfirmText("确定")
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
                        doSendCMDGetDeviceDataAll();
                    }
                })
                .show();
    }

    private void removeUserListByCondition(List<UserInfo> userInfoList) {
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            if (userInfoList.get(i).rereadflag == -1 || userInfoList.get(i).curdata < 0) {
                userInfoList.remove(i);
            }
        }
    }

    /**
     * 筛选抄表失败的用户
     *
     * @param userInfoList
     * @return
     */
    private List<UserInfo> getUserListByCondition(List<UserInfo> userInfoList) {
        List<UserInfo> list = new ArrayList<>();
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            if (userInfoList.get(i).curdata < 0) {
                list.add(userInfoList.get(i));
            }
        }
        return list;
    }

    /**
     * 移除数据库中不存在的数据（只能在子线程中调用否则会导致ANR）
     *
     * @param userInfoList
     */
    private void removeNonExistentData(List<UserInfo> userInfoList) {
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            if (!getSqOperator().checkMeterIdExist(userInfoList.get(i).meteraddr)) {
                userInfoList.remove(i);
            }
        }
    }
}