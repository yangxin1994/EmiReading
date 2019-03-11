package com.emi.emireading.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.RxTimerUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.core.config.EmiConfig.READING_DELAY;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_POSITION;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_USER_LIST;
import static com.emi.emireading.core.config.EmiConstants.MSG_ERROR;
import static com.emi.emireading.core.config.EmiConstants.PREF_READING_DELAY;
import static com.emi.emireading.core.config.EmiConstants.PREF_WATER_WARNING_LINE;
import static com.emi.emireading.core.config.EmiConstants.STANDARD_TYPE_NORMAL;
import static com.emi.emireading.core.config.EmiConstants.STANDARD_TYPE_SPECIAL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.core.config.EmiConstants.userInfoArrayList;
import static com.emi.emireading.ui.ChannelListActivity.EXTRA_CHANNEL;
import static com.emi.emireading.ui.MeterQueryActivityNew.REQUEST_CODE_DETAIL_INFO;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_BUNDLE;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_EDIT_DATA_LIST;
import static com.emi.emireading.ui.UserInfoListActivity.RESULT_CODE_CHANNEL_DATA;

/**
 * @author :zhoujian
 * @description :自动抄表
 * @company :翼迈科技
 * @date 2018年03月19日下午 04:11
 * @Email: 971613168@qq.com
 */
@Deprecated
public class AutoReadMeterActivity extends BaseActivity implements View.OnClickListener {
    private Button btnConnect;
    private Button btnRead;
    private boolean isNormal;
    private static final int TIME_INTERVAL = 1000;
    private Button btnReRead;
    private TitleView titleView;
    boolean receiveFinish = false;
    private ArrayList<UserInfo> backUpList = new ArrayList<>();
    private ArrayList<UserInfo> currentReadList = new ArrayList<>();
    /**
     * 是否成功读取到通道板
     */
    private boolean isReadChannelSuccess;
    private static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    private static final String EXTRA_METER_ADDRESS = "EXTRA_METER_ADDRESS";
    private static final String EXTRA_STATE = "EXTRA_STATE";
    private static final String EXTRA_USER_NAME = "EXTRA_USER_NAME";
    private static final String EXTRA_CURRENT_USAGE = "EXTRA_CURRENT_USAGE";
    private static final int REQUEST_CODE_USER_LIST = 10;
    /**
     * 蓝牙已连接
     */
    private static final int MSG_CONNECT = 100;
    /**
     * 读取通道号成功
     */
    private static final int MSG_READ_SUCCESS_CHANNEL = 101;

    /**
     * 循环抄表中
     */
    private static final int MSG_READING_METER = 102;
    /**
     * 通道板不匹配
     */
    private static final int MSG_CHANNEL_NOT_MATCHING = -110;
    public boolean isStop = false;
    private Disposable mDisposable;
    private static final int MSG_AUTO_READ_FINISH = 103;
    private static final int MSG_AUTO_REREAD = 104;
    /**
     * 人工选择的通道板号
     */
    private String inputChannel = "";
    private String strHex = "";
    private String strChannel = "";
    /**
     * 读通道板的flag
     */
    private boolean readChannelFlag = false;
    /**
     * 加载数据完成
     */
    private static final int MSG_LOAD_FINISH = 1;
    private static final int MSG_DIS_CONNECT = -100;
    private Context mContext;
    private ListView dataListView;
    private boolean hasDevice;
    public boolean isConnect = false;
    private ReceiverDataRunnable dataThread;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothDevice bluetoothDevice = null;
    private EmiMultipleProgressDialog dialog;
    private BluetoothSocket socket;
    private boolean isNeedReadChannel;
    public static final int ACTION_OPEN_BLUETOOTH = 1;
    public ReadMeterRunnable mReadMeterThread = null;
    private byte[] cmd = {};
    private UserInfo currentUserInfo;
    private int limitUsage;
    List<HashMap<String, Object>> dataList = new ArrayList<>();
    private SimpleAdapter listItemAdapter;
    /**
     * 补读标记位
     */
    private boolean reReadFlag = false;
    private boolean findFlag = false;
    private ArrayList<UserInfo> adapterUserList = new ArrayList<>();

    /**
     * 自动补抄标识位
     */
    private boolean autoReReadFlag = false;
    /**
     * 抄表需要的数据源
     */
    private List<UserInfo> userInfoList;
    private List<UserInfo> failedList = new ArrayList<>();
    private String mFileName;
    private Disposable readDisposable;

    private Handler handler = new MyHandler(this);

    @Override
    protected int getContentLayout() {
        return R.layout.activity_auto_read_meter;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        mFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        isNeedReadChannel = EmiUtils.isNeedChannel();
        if (!isNeedReadChannel) {
            inputChannel = getIntent().getStringExtra(EXTRA_CHANNEL);
            LogUtil.d("传递过来的通道板：" + inputChannel);
        }
        isNormal = EmiUtils.isNormalChannel();
    }

    @Override
    protected void initUI() {
        btnConnect = findViewById(R.id.btnConnect);
        btnRead = findViewById(R.id.btnRead);
        btnReRead = findViewById(R.id.btnReRead);
        dataListView = findViewById(R.id.lv);
        titleView = findViewById(R.id.titleView);
    }

    @Override
    protected void initData() {
        btnConnect.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnReRead.setOnClickListener(this);
        READING_DELAY = PreferenceUtils.getInt(PREF_READING_DELAY, 1000);
        LogUtil.w(TAG, "延时时间：" + READING_DELAY);
        initAdapter();
        loadUserData();
        limitUsage = PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50);
        titleView.setRightIconText("统计");
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UserInfoListActivity.class);
                LogUtil.d(TAG, "传递过去的通道号：" + inputChannel);
                intent.putExtra(EXTRA_CHANNEL, inputChannel);
                intent.putExtra(EXTRA_FILE_NAME, mFileName);
                startActivityForResult(intent, REQUEST_CODE_USER_LIST);
            }
        });
        registerBroadcastReceiver();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    connectBlueTooth();
                } else {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, ACTION_OPEN_BLUETOOTH);
                }
                break;
            case R.id.btnRead:
                autoReReadFlag = false;
                doRead();
                break;
            case R.id.btnReRead:
                //补抄
                autoReReadFlag = false;
                reReadFlag = true;
                doReRead();
                break;
            default:
                break;
        }
    }


    private class GetUserInfoRunnable implements Runnable {
        @Override
        public void run() {
            if (StringUtils.isNotEmpty(mFileName)) {
                if (isNeedReadChannel) {
                    userInfoList = getSqOperator().find(mFileName);
                } else {
                    LogUtil.d(TAG, "不需要读通道板，此时传递过来的channel=" + inputChannel);
                    userInfoList = getSqOperator().findByChannel(mFileName, inputChannel);
                    LogUtil.d(TAG, "查询到的数据长度：" + userInfoList.size());
                }
                removeEmptyUserInfo(userInfoList);
                removeUserByState(STATE_PEOPLE_RECORDING);
                failedList.clear();
                for (UserInfo userInfo : userInfoList) {
                    if (EmiConstants.STATE_FAILED == userInfo.state) {
                        failedList.add(userInfo);
                    }
                }
                LogUtil.e(TAG, "failedList长度：" + failedList.size());
                sendEmptyMsg(MSG_LOAD_FINISH);
                LogUtil.w("数据长度：" + userInfoList.size());
            } else {
                //先读取通道号

            }
        }
    }


    private void sendErrorMsg(String errorMsg) {
        Message message = handler.obtainMessage();
        message.what = MSG_ERROR;
        message.obj = errorMsg;
        handler.sendMessage(message);
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
                    case MSG_READING_METER:
                        LogUtil.i("接收到消息");
                        activity.notifyUI(msg);
                        break;
                    case MSG_ERROR:
                        String errorMsg = (String) msg.obj;
                        LogUtil.e("有异常：" + errorMsg);
                        activity.doConnectFailed();
                        activity.closeDialog();
                        break;
                    case MSG_LOAD_FINISH:
                        activity.closeDialog();
                        break;
                    case MSG_CONNECT:
                        activity.doConnectSuccess();
                        break;
                    case MSG_READ_SUCCESS_CHANNEL:
                        LogUtil.i(TAG, "读到的通道板号：" + activity.inputChannel);
                        activity.removeDifferentChannel(activity.inputChannel);
                        activity.executeReadMeterThread(activity.inputChannel);
                        break;
                    case MSG_AUTO_READ_FINISH:
                        activity.doReadFinish();
                        break;
                    case MSG_AUTO_REREAD:
                        //自动补抄
                        if (activity.failedList.size() > 0) {
                            activity.autoReReadFlag = true;
                            activity.showAutoReRead();
                            activity.doReRead();
                        }
                        break;
                    case MSG_CHANNEL_NOT_MATCHING:
                        ToastUtil.showShortToast("通道板不匹配");
                        break;
                    default:
                        break;
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


    private void connectByMac(BluetoothDevice device) {
        //得到BluetoothDevice对象,也就是说得到配对的蓝牙适配器
        //得到远程蓝牙设备的地址
        String address = device.getAddress();
        String name = device.getName();
        if (EmiConstants.EMI_DEVICE_NAME.equals(name)) {
            LogUtil.i(TAG, "MAC地址:" + address);
            bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
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
            hasDevice = true;
        } else {
            ToastUtil.showShortToast("该设备不是翼迈抄表设备");
            closeDialog();
            showDisconnect();
        }
    }


    private void makeButtonEnable(boolean b) {
        btnConnect.setEnabled(b);
        btnRead.setEnabled(b);
        btnReRead.setEnabled(b);
        dataListView.setEnabled(b);
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
        btnRead.setText("自动抄表");
        btnReRead.setText("补抄");
    }


    private void showDialog(String textMsg) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.spin_animation_dialog);
        AnimationDrawable drawable = (AnimationDrawable) imageView.getBackground();
        drawable.start();
        dialog = EmiMultipleProgressDialog.create(this)
                .setCustomView(imageView)
                .setLabel(textMsg);
        dialog.setBackgroundColor(R.color.transparent).
                show();
    }


    private void loadUserData() {
        showDialog("正在获取抄表数据...");
        ThreadPoolManager.EXECUTOR.execute(new GetUserInfoRunnable());
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


    private void doRead() {
        if (isConnect) {
            strHex = "";
            readChannelFlag = false;
            reReadFlag = false;
            dataList.clear();
            listItemAdapter.notifyDataSetChanged();
            showStartReading();
            if (isNeedReadChannel) {
                readChannel();
            } else {
                //直接读表
                strChannel = inputChannel;
                executeReadMeterThread(strChannel);
            }
        } else {
            Toast.makeText(AutoReadMeterActivity.this, "请先连接蓝牙设备!!!", Toast.LENGTH_SHORT).show();
        }
    }


    private void showConnectSuccess() {
        btnConnect.setText("蓝牙已连接");
        btnConnect.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        btnConnect.setBackgroundResource(R.drawable.btn_bg_green_sel);
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
        cancel();
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


    public class ReceiverDataRunnable implements Runnable {
        @Override
        public void run() {
            currentReadList.clear();
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
                    byte[] bufData = new byte[bytes];
                    for (int i = 0; i < bytes; i++) {
                        bufData[i] = buffer[i];
                    }
                    strHex = strHex + DigitalTrans.byte2hex(bufData);
                    if (readChannelFlag) {
                        //通道板指令回调
                        LogUtil.w(TAG, "读通道板的回调");
                        if (isNormal) {
                            receiveChannelNormal();
                        } else {
                            receiveChannelSpecial();
                        }
                    } else {
                        //水表读数回调
                        receiveMeterReadingCallBack();
                    }
                } else {
                    Log.e(TAG, "已执行");
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

    private void stopThread() {
        try {
            LogUtil.w("直接抛出异常");
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
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


    /**
     * 普通通道板回调
     */
    private void receiveChannelNormal() {
        if (strHex.length() >= 30) {
            byte[] channelInfo = DigitalTrans.hex2byte(strHex);
            int n = 0;
            while (channelInfo[n++] != 0x68) {
                if (n == channelInfo.length) {
                    break;
                }
            }
            if (n < channelInfo.length) {
                int channelNum = 0;
                for (int l = 0; l < 6; l++) {
                    channelNum += (int) ((channelInfo[4 + n + l] - 0x30) * (Math.pow(10, l)));
                }
                strChannel = String.valueOf(channelNum);
                inputChannel = strChannel;
                LogUtil.i(TAG, "通道板号(普通)--->strChannel:" + strChannel);
                isReadChannelSuccess = true;
                strHex = "";
                readChannelFlag = false;
                //此时说明已经成功读到通道板号
                sendEmptyMsg(MSG_READ_SUCCESS_CHANNEL);
                selectSameChannelUser(userInfoList, inputChannel);
            }
        }
    }


    /**
     * 特殊通道板回调处理
     */
    private void receiveChannelSpecial() {
        if (strHex.length() >= 48) {
            byte[] channelInfo = DigitalTrans.hex2byte(strHex);
            int n = 0;
            while (channelInfo[n++] != 0x68) {
                if (n > 30) {
                    break;
                }
            }
            if (n < 30) {
                String strChannelNum = "";
                for (int l = 0; l < 4; l++) {
                    String str = DigitalTrans.algorismToHEXString(channelInfo[13 + n + l]);
                    strChannelNum = str + strChannelNum;
                }
                strChannel = String.valueOf(Integer.parseInt(strChannelNum));
                LogUtil.d(TAG, "通道板号(特殊)--->strChannel:" + strChannel);
                inputChannel = strChannel;
                strHex = "";
                readChannelFlag = false;
                //此时说明已经成功读到通道板
                isReadChannelSuccess = true;
                sendEmptyMsg(MSG_READ_SUCCESS_CHANNEL);
                selectSameChannelUser(userInfoList, inputChannel);
            }
        }
    }


    private void selectSameChannelUser(List<UserInfo> userInfoList, String channel) {
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            if (!channel.equals(userInfoList.get(i).channelNumber)) {
                userInfoList.remove(userInfoList.get(i));
            }
        }
    }

    /**
     * 读表回调
     */
    private void receiveMeterReadingCallBack() {
        if (strHex.length() >= 70) {
            byte[] MeterInfo = DigitalTrans.hex2byte(strHex);
            int m = 0;
            while (MeterInfo[m++] != 0x68)
                ;
            byte[] addr = new byte[5];
            for (int j = 0; j < 5; j++) {
                addr[4 - j] = MeterInfo[1 + m + j];
            }
            byte[] ReadNum = new byte[2];

            for (int k = 0; k < 2; k++) {
                ReadNum[1 - k] = MeterInfo[m + 14 + k];
            }
            String strReadNum = DigitalTrans.byte2hex(ReadNum);
            int readData = 0;
            //读出的水表示数
            try {
                readData = Integer.parseInt(strReadNum);
            } catch (NumberFormatException e) {
                sendErrorMsg(e.toString());
            }
            currentUserInfo.setCurdata(readData);
            currentUserInfo.channel = currentUserInfo.channelNumber;
            //本次用水量
            int currentUsage = readData - currentUserInfo.getLastdata();
            if ((currentUsage > limitUsage) || currentUsage < 0) {
                currentUserInfo.state = STATE_WARNING;
            } else {
                currentUserInfo.state = STATE_SUCCESS;
            }
            currentUserInfo.channel = currentUserInfo.channelNumber;
            currentUserInfo.curyl = currentUsage;
            getSqOperator().updateData(mFileName, currentUserInfo.accountnum, readData, currentUsage, currentUserInfo.state, TimeUtil.getCurrentTime(), currentUserInfo.channel);
            strHex = "";
            receiveFinish = true;
            //读取水表成功
            transportReadResult(currentUserInfo);
            currentReadList.add(currentUserInfo);
            //如果是补抄,则需要移除失败数据(移除前，需要判断是否存在)
            for (UserInfo info : failedList) {
                if (info.equals(currentUserInfo)) {
                    //将failedList中补抄到的状态更新掉
                    info.state = currentUserInfo.state;
                    break;
                }
            }
        }
    }


    private void sendEmptyMsg(int what) {
        handler.sendEmptyMessage(what);
    }


    private void sendMsgReadFinish() {
        handler.sendEmptyMessage(MSG_AUTO_READ_FINISH);
    }

    private void registerBroadcastReceiver() {
        IntentFilter stateChangeFilter = new IntentFilter(
                ACTION_STATE_CHANGED);
        IntentFilter connectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter disConnectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED);
        try {
            registerReceiver(stateChangeReceiver, stateChangeFilter);
            registerReceiver(stateChangeReceiver, connectedFilter);
            registerReceiver(stateChangeReceiver, disConnectedFilter);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.w(TAG, "已执行");
            if (ACTION_STATE_CHANGED.equals(action)) {
                if (isBlueToothEnable()) {
                    ToastUtil.showShortToast("蓝牙已打开");
                } else {
                    ToastUtil.showShortToast("蓝牙已关闭");
                    showDisconnect();
                }
            }
        }
    };


    private boolean isBlueToothEnable() {
        BluetoothAdapter adapter = getDefaultAdapter();
        return adapter.isEnabled();
    }


    @Override
    protected void onDestroy() {
        doDisConnect();
        isStop = true;
        cancel();
        RxTimerUtil.cancel();
        super.onDestroy();
    }


    private void executeReadMeterThread(String channel) {
        mReadMeterThread = new ReadMeterRunnable(channel);
        ThreadPoolManager.EXECUTOR.execute(mReadMeterThread);
    }


    public class ReadMeterRunnable implements Runnable {
        private String channelStr;

        public ReadMeterRunnable(String channelStr) {
            this.channelStr = channelStr;
        }

        @Override
        public void run() {
            //清除通道号前面的0
            channelStr = EmiStringUtil.clearFirstZero(channelStr);
            LogUtil.w(TAG, "inputChannel =" + channelStr);
            readingMeter(channelStr);
        }
    }

    /**
     * 自动抄表
     *
     * @param channelNumber
     */
    private void readingMeter(String channelNumber) {
        String inputChannel = EmiStringUtil.clearFirstZero(channelNumber);
        String localChannel;
        String strSendAddress;
        String strFirmCode;
        int currentState = STATE_NO_READ;
        currentUserInfo = null;
        currentReadList.clear();
        LogUtil.d("处理过后的通道板号：" + inputChannel);
        for (int i = 0; i < userInfoList.size(); i++) {
            currentUserInfo = userInfoList.get(i);
            localChannel = EmiStringUtil.clearFirstZero(currentUserInfo.channelNumber);
            LogUtil.w("localChannel = " + localChannel + "---" + "inputChannel=" + inputChannel);
            if (inputChannel.equals(localChannel)) {
                strSendAddress = DigitalTrans.patchHexString(currentUserInfo.meteraddr, 10);
                strFirmCode = EmiUtils.formatFirmCode(currentUserInfo.firmCode);
                LogUtil.d(TAG, "处理后的表地址：" + strSendAddress);
                LogUtil.d(TAG, "处理后的厂商代码：" + strFirmCode);
                adapterUserList.add(currentUserInfo);
                if (reReadFlag) {
                    //补抄先清补抄标志位
                    failedList = removeSameUser(failedList);
                    for (UserInfo failedInfo : failedList) {
                        if (failedInfo.equals(currentUserInfo) && failedInfo.state == STATE_FAILED) {
                            //当前是补抄模式
                            LogUtil.e(TAG, "当前是补抄模式");
                            failedInfo.state = currentUserInfo.state;
                            findFlag = true;
                            break;
                        }
                    }
                }
                boolean isNeedRead = (!reReadFlag) || (reReadFlag && findFlag);
                if (isNeedRead) {
                    findFlag = false;
                    switch (EmiUtils.getStandardType()) {
                        case STANDARD_TYPE_SPECIAL:
                            LogUtil.w(TAG, "特殊市场");
                            cmd = EmiUtils.getReadingCmdSpecial(strSendAddress, strFirmCode);
                            break;
                        case STANDARD_TYPE_NORMAL:
                            LogUtil.i(TAG, "普通市场");
                            cmd = EmiUtils.getReadingCmdNormal(strSendAddress, strFirmCode);
                            break;
                        default:
                            cmd = EmiUtils.getReadingCmdNormal(strSendAddress, strFirmCode);
                            break;
                    }
                    receiveFinish = false;
                    strHex = "";
                    sendBTCmd(cmd);
                    //抄表失败
                    delays(READING_DELAY + 200);
                    int count = 0;
                    while (!receiveFinish) {
                        if (++count > 1) {
                            LogUtil.w(TAG, "执行transportReadResult");
                            //抄表失败
                            currentUserInfo.state = EmiConstants.STATE_FAILED;
                            currentUserInfo.data = 0;
                            currentUserInfo.curyl = 0;
                            currentUserInfo.channel = currentUserInfo.channelNumber;
                            failedList.add(currentUserInfo);
                            currentState = getSqOperator().findStateWithUserNO(mFileName, currentUserInfo.accountnum);
                            if (currentState == STATE_FAILED || currentState == STATE_NO_READ) {
                                getSqOperator().updateData(mFileName, currentUserInfo.accountnum, currentUserInfo.data, currentUserInfo.curyl, currentUserInfo.state, TimeUtil.getCurrentTime(), currentUserInfo.channel);
                            }
                            transportReadResult(currentUserInfo);
                            currentReadList.add(currentUserInfo);
                            break;
                        } else {
                            //抄表成功
                            delays(READING_DELAY);
                        }
                    }
                } else {
                    LogUtil.e("不符合抄表条件");
                }
            } else {
                sendMsgReadFinish();
            }
        }
        //自动抄表结束
        delays(1000);
        if (currentUserInfo == null) {
            LogUtil.e(TAG, "通道板不匹配");
            sendEmptyMsg(MSG_CHANNEL_NOT_MATCHING);
        }

        if (autoReReadFlag) {
            //自动补抄结束
            sendMsgReadFinish();
        } else {
            if (!reReadFlag) {
                if (failedList.isEmpty()) {
                    //此时说明自动抄表全部抄到，因此无需自动补抄，直接显示抄表结束
                    sendMsgReadFinish();
                } else {
                    //此时说明自动抄表有抄表失败数据，因此需要自动补抄一次
                    sendEmptyMsg(MSG_AUTO_REREAD);
                }
            } else {
                //点击补抄按钮触发的补抄
                sendMsgReadFinish();
            }
        }
    }


    /**
     * 普通通道板
     */
    private void readChannelNormal() {
        readChannelFlag = true;
        byte[] cmd = {0x6A, 0x10, 0x02, (byte) 0xAA, 0x01, 0x27, 0x16};
        sendBTCmd(cmd);
        delays(600);
    }


    private void showStartReading() {
        btnRead.setText("正在抄表...");
        titleView.setTitle("开始抄表...");
        makeButtonEnable(false);
    }


    /**
     * 合肥通道板
     *
     * @param firmCode
     */
    private void readChannelSpecial(String firmCode) {
        readChannelFlag = true;
        byte[] hexCode = DigitalTrans.hexStringToByte(firmCode);
        byte hexCheck = 0x00;
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, 0x68, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x42, 0x00, 0x31, 0x03, 0x01, (byte) 0x89, 0x01,
                0x69, 0x16};
        for (int j = 0; j < hexCode.length; j++) {
            cmd[9 + j] = hexCode[j];
        }
        for (int k = 2; k <= 15; k++) {
            hexCheck += cmd[k];
        }
        cmd[16] = hexCheck;
        sendBTCmd(cmd);
        delays(700);
    }

    /**
     * 传递抄表数据
     */
    private void transportReadResult(UserInfo userInfo) {
        Message message = handler.obtainMessage();
        message.what = MSG_READING_METER;
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_USER_ID, userInfo.accountnum);
        bundle.putString(EXTRA_METER_ADDRESS, userInfo.meteraddr);
        bundle.putString(EXTRA_USER_NAME, userInfo.username);
        bundle.putInt(EXTRA_STATE, userInfo.state);
        bundle.putInt(EXTRA_CURRENT_USAGE, userInfo.curyl);
        message.setData(bundle);
        handler.sendMessage(message);
    }


    private void notifyUI(Message msg) {
        int state = msg.getData().getInt(EXTRA_STATE);
        String meterAddress = msg.getData().getString(EXTRA_METER_ADDRESS);
        String userId = msg.getData().getString(EXTRA_USER_ID);
        String userName = msg.getData().getString(EXTRA_USER_NAME);
        int currentUsage = msg.getData().getInt(EXTRA_CURRENT_USAGE);
        HashMap<String, Object> item = new HashMap<>(3);
        item.put("title", userId);
        item.put("info", userName);
        item.put("yl", currentUsage);
        LogUtil.w(TAG, "获取的状态：" + state);
        if (state == EmiConstants.STATE_SUCCESS) {
            item.put("image", R.mipmap.star);
        } else if (state == EmiConstants.STATE_WARNING) {
            item.put("image", R.mipmap.know);
        } else if (state == EmiConstants.STATE_FAILED) {
            item.put("image", R.mipmap.red);
        }
        if (autoReReadFlag) {
            titleView.setTitle("正在自动补抄 ...");
        } else {
            titleView.setTitle("正在读" + meterAddress + "  ...");
        }
        dataList.add(item);
        listItemAdapter.notifyDataSetChanged();
    }


    private void initAdapter() {
        //数据源
        listItemAdapter = new SimpleAdapter(this, dataList,
                //ListItem的XML实现
                R.layout.vlistview,
                //动态数组与ImageItem对应的子项
                new String[]{"title", "info", "yl", "image"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.title, R.id.info, R.id.yl, R.id.imageView2}
        );
        dataListView.setAdapter(listItemAdapter);
        //滚动底部
        dataListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, UserInfoDetailActivity.class);
                EmiConstants.userInfoArrayList = new ArrayList<>();
                userInfoArrayList.addAll(backUpList);
                intent.putExtra(EXTRA_CHANNEL_NUMBER, userInfoArrayList.get(position).channelNumber);
                intent.putExtra(EXTRA_POSITION, position);
                startActivityForResult(intent, REQUEST_CODE_DETAIL_INFO);
            }
        });
    }


    private void doReadFinish() {
        backUpList.clear();
        showFinish();
        int failedCount = 0;
        removeSameUser(failedList);
        removeSameUser(adapterUserList);
        for (UserInfo userInfo : failedList) {
            if (userInfo.state == STATE_FAILED) {
                failedCount++;
            }
            LogUtil.e("失败的数目：状态：" + userInfo.state);
        }
        toastReadResult(failedCount);
        if (autoReReadFlag) {
            notifyUIAll(adapterUserList);
            backUpList.addAll(adapterUserList);
        } else {
            backUpList.addAll(currentReadList);
        }
    }

    private void

    showFinish() {
        if (reReadFlag && !autoReReadFlag) {
            titleView.setTitle("补抄结束");
        } else {
            titleView.setTitle("抄表结束");
        }
        btnRead.setText("开始抄表");
        btnReRead.setText("补抄");
        makeButtonEnable(true);
    }

    /**
     * 补抄
     */
    private void doReRead() {
        //补抄标志置为true
        if (failedList.isEmpty()) {
            makeButtonEnable(true);
            ToastUtil.showLongToast("无需补抄");
            showFinish();
            return;
        }
        for (UserInfo failedList : failedList) {
            if (failedList.state == STATE_FAILED) {
                break;
            }
            ToastUtil.showLongToast("无需补抄");
            showFinish();
            return;
        }
        if (!isConnect) {
            ToastUtil.showShortToast("请先连接蓝牙");
            return;
        }
        reReadFlag = true;
        dataList.clear();
        makeButtonEnable(false);
        if (!autoReReadFlag) {
            btnReRead.setText("正在补抄...");
        } else {
            btnReRead.setText("补抄");
        }
        strChannel = "";
        strHex = "";
        LogUtil.d("通道板号：" + inputChannel);
        if (isNeedReadChannel) {
            if (isReadChannelSuccess) {
                //读取通道板成功，或者通道板号已经获取到才允许补抄
                mReadMeterThread = new ReadMeterRunnable(inputChannel);
                ThreadPoolManager.EXECUTOR.execute(mReadMeterThread);
            } else {
                readChannel();
            }
        } else {
            //不需要读通道板
            mReadMeterThread = new ReadMeterRunnable(inputChannel);
            ThreadPoolManager.EXECUTOR.execute(mReadMeterThread);
        }

    }


    /**
     * 显示自动补抄
     */
    private void showAutoReRead() {
        btnRead.setText("自动补抄...");
        makeButtonEnable(false);
    }

    /**
     * 显示自动补抄后的列表
     */
    private void showAutoReReadFinish(ArrayList<UserInfo> userList) {
        dataList.clear();
        for (UserInfo userInfo : userList) {
            HashMap<String, Object> item = new HashMap<>(3);
            item.put("title", userInfo.accountnum);
            item.put("info", userInfo.username);
            item.put("yl", userInfo.curyl);
            LogUtil.w(TAG, "获取的状态：" + userInfo.state);
            if (userInfo.state == EmiConstants.STATE_SUCCESS) {
                item.put("image", R.mipmap.star);
            } else if (userInfo.state == EmiConstants.STATE_WARNING) {
                item.put("image", R.mipmap.know);
            } else if (userInfo.state == EmiConstants.STATE_FAILED) {
                item.put("image", R.mipmap.red);
            }
            dataList.add(item);
        }
        listItemAdapter.notifyDataSetChanged();
    }

    /**
     * 读取通道号
     */
    private void readChannel() {
        isReadChannelSuccess = false;
        if (isNormal) {
            readChannelNormal();
        } else {
            readChannelSpecial(EmiConstants.FIRM_CODE_1001);
        }
        doEventCountDown(1000, new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                readDisposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                if (!isReadChannelSuccess) {
                    ToastUtil.showShortToast("通道板不匹配或读取失败");
                    showFinish();
                    stopTimer(readDisposable);
                }
            }

            @Override
            public void onError(Throwable e) {
                stopTimer(readDisposable);
            }

            @Override
            public void onComplete() {
                stopTimer(readDisposable);
            }
        });

    }

    /**
     * 移除不同通道号的
     *
     * @param inputChannel
     */
    private void removeDifferentChannel(String inputChannel) {
        for (int i = failedList.size() - 1; i >= 0; i--) {
            if (!inputChannel.equals(failedList.get(i).channelNumber)) {
                failedList.remove(failedList.get(i));
            }
        }
    }

    /**
     * 吐司抄表结果
     *
     * @param failedCount
     */
    private void toastReadResult(int failedCount) {
        if (reReadFlag && !autoReReadFlag) {
            if (failedCount > 0) {
                ToastUtil.showShortToast("当前是补抄模式,共有" + failedCount + "只水表未抄到");
            } else {
                ToastUtil.showShortToast("补抄结束");
            }
        } else {
            if (!reReadFlag) {
                if (failedCount > 0) {
                    ToastUtil.showShortToast("共有" + failedCount + "只水表未抄到");
                } else {
                    ToastUtil.showShortToast("抄表结束");
                }
            } else {
                if (failedCount > 0) {
                    ToastUtil.showShortToast("共有" + failedCount + "只水表未抄到");
                } else {
                    ToastUtil.showShortToast("抄表完成");
                }
            }
        }
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


    private void notifyUIAll(ArrayList<UserInfo> userInfoArrayList) {
        HashMap<String, Object> item;
        dataList.clear();
        if (userInfoArrayList != null) {
            for (UserInfo userInfo : userInfoArrayList) {
                item = new HashMap<>(3);
                item.put("title", userInfo.accountnum);
                item.put("info", userInfo.username);
                item.put("yl", userInfo.curyl);
                switch (userInfo.state) {
                    case EmiConstants.STATE_SUCCESS:
                        item.put("image", R.mipmap.star);
                        break;
                    case EmiConstants.STATE_WARNING:
                        item.put("image", R.mipmap.know);
                        break;
                    case EmiConstants.STATE_FAILED:
                        item.put("image", R.mipmap.red);
                        break;
                    case EmiConstants.STATE_PEOPLE_RECORDING:
                        item.put("image", R.mipmap.star);
                        break;
                    default:
                        break;
                }
                dataList.add(item);
            }
            listItemAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showShortToast("无数据源");
        }
    }


    private void doFinishCallBack(ArrayList<UserInfo> callBackList) {
        UserInfo currentUserInfo;
        if (callBackList != null) {
            for (UserInfo editUserInfo : callBackList) {
                LogUtil.i(TAG, "当前用户：" + editUserInfo.accountnum + "当前读数:" + editUserInfo.curdata);
                for (int i = 0; i < backUpList.size(); i++) {
                    currentUserInfo = backUpList.get(i);
                    if (currentUserInfo.accountnum.equals(editUserInfo.accountnum) && currentUserInfo.meteraddr.equals(editUserInfo.meteraddr)) {
                        currentUserInfo.state = editUserInfo.state;
                        currentUserInfo.curdata = editUserInfo.curdata;
                        currentUserInfo.curyl = editUserInfo.curyl;
                        currentUserInfo.curreaddate = editUserInfo.curreaddate;
                    }
                }
            }
            notifyUIAll(backUpList);
        } else {
            LogUtil.i(TAG, "集合为空");
        }
    }


    private void removeUserByState(int state) {
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            if (state == userInfoList.get(i).state) {
                userInfoList.remove(i);
            }
        }
    }
}
