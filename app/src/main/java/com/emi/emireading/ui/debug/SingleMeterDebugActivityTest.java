package com.emi.emireading.ui.debug;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.LoadingDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.core.config.EmiConstants.FIRM_CODE_1001;
import static com.emi.emireading.core.config.EmiConstants.FIRM_CODE_7833;

/**
 * @author :chenhx
 * @description : 单表维护
 * @company :翼迈科技
 * @date: 2017年10月26日上午 11:44
 */

public class SingleMeterDebugActivityTest extends BaseActivity implements View.OnClickListener {
    private Button btn_connect;
    private String mNewMeterAddressStr;
    private static final String TAG = "SingleMeterDebugActivityTest";
    private String mInputThStr;
    private Button btn_read;
    private EditText editTextFirmCode;
    private int receiveCount = 0;
    private final int ERROR_CODE = -1;
    private Button btn_clear;
    private String mFirmCode;
    private final int checkBeginIndex = 4;
    private final int checkEndIndex = 21;
    private final int modifyThEndIndex = 22;
    private final int modifyMeterAddressEndIndex = 24;
    private final int csBeginIndex = 2;
    private final int csEndIndex = 16;
    private final int settingCallBackDataLength = 18;
    private final int MeterAddressMaxLength = 10;
    private CallBackThread mCallBackThread;
    private final int MaxInputThLength = 2;
    private final int MaxInputAddressLength = 10;
    private String editFirmCode;
    private String userId = "";
    /**
     * 数据库是否有该记录
     */
    private boolean isExist = false;
    private boolean isNormal = false;
    /**
     * 特殊市场的使能设置返回的数据正确长度
     */
    private final static int SETTING_CHECK_SPECIAL_LENGTH = 39;
    private final static int SETTING_CHECK_NORMAL_LENGTH = 38;

    /**
     * 修改表地址返回的数据正确长度
     */
    private final static int MODIFY_METER_ADDRESS_CHECK_SPECIAL_LENGTH = 40;
    private final static int MODIFY_METER_ADDRESS_CHECK_NORMAL_LENGTH = 36;
    /**
     * 读取表地址返回数据长度
     */
    private final static int READ_METER_ADDRESS_CHECK_LENGTH_NORMAL = 36;
    private final static int READ_METER_ADDRESS_CHECK_LENGTH_SPECIAL = 37;
    /**
     * 读取水表信息返回数据长度
     */
    private final static int READ_METER_INFO_CHECK_LENGTH_NORMAL = 74;
    private final static int READ_METER_INFO_CHECK_LENGTH_NORMAL_1 = 50;
    private final static int READ_METER_INFO_CHECK_LENGTH_SPECIAL = 78;
    /**
     * 改表地址设置使能回调长度
     */
    private int settingModifyMeterAddressCheckLength;
    private static final int SETTING_MODIFY_METER_ID_CHECK_LENGTH_NORMAL = 40;
    private static final int SETTING_MODIFY_METER_ID_CHECK_LENGTH_SPECIAL = 36;
    private int readMeterAddressCheckLength;
    private int modifyMeterAddressCheckLength;
    private int readMeterInfoCheckLength;
    String strMeterAddr = "";
    String strFirmCode = "";
    String strMeterData = "";
    String strChanleCode = "";
    String strChanleDate = "";
    String locationstring = "";
    String channelNumber = "";
    String editBuffer = "";
    /**
     * 上次用户水量
     */
    int lastData;
    /**
     * 本次用户水量
     */
    int currentData;
    boolean breadaddr = false;
    /**
     * 表地址（不包括厂商代码）
     */
    byte addr[] = {0, 0, 0, 0, 0, 0, 0};
    private CheckBox cb_switch;
    private final int NO_EMI_DEVICE_CODE = 10;
    private final int DEVICE_DISCONNECTED = 11;
    private final int CONNECT_SUCCESS_CODE = 3;
    private final int CONNECT_FAILED_CODE = 5;
    private final int MODIFY_TH_CODE = 1001;
    private final int CLEAR_TH_SUCCESS = 1002;
    private final int CLEAR_TH_FAILED = 1000;
    private final int EDIT_METER_ADDRESS_CODE = 999;
    private final int MODIFY_METER_ADDRESS = 1003;
    private final int IS_READING_CODE = 2;
    private final int READ_SUCCESS = 6;
    public BluetoothDevice device1 = null;
    private final int READ_ADDRESS_FINISH_CODE = 0;
    private final int OpenBlueToothRequestCode = 1;
    private BluetoothSocket socket;
    public BluetoothAdapter mBluetoothAdapter = getDefaultAdapter();
    private LoadingDialog loadingDialog;
    private Context mContext;
    private TextView tv_status;
    private String strhex = "";
    private EditText et_meter_address;
    private Button btnEditMeterAddress;
    private TextView tvChannel;
    private TextView tv_meter_data;
    private TextView tv_loacation;
    private Button btnClearTh;
    private int cmdTag = 0;
    private static final int TAG_READ_METER = 0;
    private static final int TAG_SETTING = 1;
    private static final int TAG_MODIFY_TH = 2;
    private static final int TAG_EDIT_METER_ADDRESS = 3;
    private int currentMode = -1;
    private final static int MODE_CLEAR_TH = 1001;
    private final static int MODE_EDIT_METER_ADDRESS = 1002;
    private TitleView tvTitleView;
    private static final int LENGTH_FIRM_CODE = 4;
    private CheckBox checkBoxFirmCode;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_singlemeter_debug;
    }

    @Override
    protected void initIntent() {
        registerBroadcastReceiver();
    }

    @Override
    protected void initUI() {
        init();
    }

    @Override


    protected void initData() {
        if (EmiConstants.bluetoothSocket != null) {
            try {
                EmiConstants.bluetoothSocket.close();
                EmiConstants.bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (EmiConstants.bluetoothSocket != null) {
            showConnect();
        } else {
            showDisconnect();
        }
    }

    private void showUiByCondition() {
        if (EmiUtils.isMeterTypeDui()) {
            btnClearTh.setVisibility(View.GONE);
        } else {
            btnClearTh.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        mContext = SingleMeterDebugActivityTest.this;
        tvTitleView = findViewById(R.id.tvTitleView);
        btn_connect = (Button) findViewById(R.id.bt_connect);
        btnClearTh = (Button) findViewById(R.id.btnClearTh);
        btn_read = (Button) findViewById(R.id.bt_read);
        btn_clear = (Button) findViewById(R.id.bt_clear);
        cb_switch = (CheckBox) findViewById(R.id.cb_switch);
        editTextFirmCode = (EditText) findViewById(R.id.editTextFirmCode);
        cb_switch.setChecked(true);
        tv_status = (TextView) findViewById(R.id.tv_status);
        et_meter_address = (EditText) findViewById(R.id.et_meter_address);
        tv_meter_data = (TextView) findViewById(R.id.tv_meter_data);
        tv_loacation = (TextView) findViewById(R.id.tv_location);
        tvChannel = findViewById(R.id.tvChannel);
        checkBoxFirmCode = findViewById(R.id.cbFirmCode);
        btnEditMeterAddress = (Button) findViewById(R.id.btnEditMeterAddress);
        btn_connect.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btnClearTh.setOnClickListener(this);
        btnEditMeterAddress.setOnClickListener(this);
        if (EmiUtils.isNormalStandardType()) {
            settingModifyMeterAddressCheckLength = SETTING_MODIFY_METER_ID_CHECK_LENGTH_NORMAL;
            modifyMeterAddressCheckLength = MODIFY_METER_ADDRESS_CHECK_NORMAL_LENGTH;
            readMeterAddressCheckLength = READ_METER_ADDRESS_CHECK_LENGTH_NORMAL;
            readMeterInfoCheckLength = READ_METER_INFO_CHECK_LENGTH_NORMAL;
        } else {
            settingModifyMeterAddressCheckLength = SETTING_MODIFY_METER_ID_CHECK_LENGTH_SPECIAL;
            modifyMeterAddressCheckLength = MODIFY_METER_ADDRESS_CHECK_SPECIAL_LENGTH;
            readMeterAddressCheckLength = READ_METER_ADDRESS_CHECK_LENGTH_SPECIAL;
            readMeterInfoCheckLength = READ_METER_INFO_CHECK_LENGTH_SPECIAL;
        }
        tvTitleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                doSkip();
            }
        });
        showUiByCondition();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_clear:
                clearAllData();
                break;
            case R.id.bt_connect:
                doConnectClickEvent();
                break;
            case R.id.bt_read:
                if (EmiConstants.bluetoothSocket == null) {
                    ToastUtil.showShortToast("请先连接蓝牙设备");
                    return;
                }
                clearData();
                String inputFirmCodeStr = editTextFirmCode.getText().toString();
                if (checkBoxFirmCode.isChecked()) {
                    if (LENGTH_FIRM_CODE == inputFirmCodeStr.length()) {
                        mFirmCode = inputFirmCodeStr;
                    } else {
                        ToastUtil.showShortToast("请输入4位厂商代码");
                        return;
                    }
                } else {
                    if (EmiUtils.isNormalStandardType()) {
                        mFirmCode = FIRM_CODE_7833;
                    } else {
                        mFirmCode = FIRM_CODE_1001;
                    }
                }
                LogUtil.d("已执行------------厂商代码：" + mFirmCode);
                sendReadSingleMeterCmd(mFirmCode);
                break;

            case R.id.btnClearTh:
                cmdTag = TAG_MODIFY_TH;
                showInputThDialog();
                break;
            case R.id.btnEditMeterAddress:
                cmdTag = TAG_EDIT_METER_ADDRESS;
                if (checkBoxFirmCode.isChecked()) {
                    //指定厂商代码（读取和修改都从输入框中获取厂商代码）
                    mFirmCode = editTextFirmCode.getText().toString();
                    if (mFirmCode.length() == LENGTH_FIRM_CODE) {
                        //输入新的厂商代码
                        showInputFirmCodeDialog();
                    } else {
                        ToastUtil.showShortToast("请输入四位厂商代码");
                    }
                } else {
                    //不指定厂商代码（厂商代码从设置中获取：特殊市场：1001，普通市场：7833）
                    if (EmiUtils.isNormalStandardType()) {
                        mFirmCode = FIRM_CODE_7833;
                    } else {
                        mFirmCode = FIRM_CODE_1001;
                    }
                    showInputAddressDialog();
                }
                break;
            default:
                break;
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEAR_TH_SUCCESS:
                    ToastUtil.showShortToast("修改千位成功");
                    break;
                case NO_EMI_DEVICE_CODE:
                    //没有连接设备
                    showDisconnect();
                    closeDialog();
                    break;
                case CONNECT_SUCCESS_CODE:
                    //连接成功
                    showConnect();
                    closeDialog();
                    break;
                case CONNECT_FAILED_CODE:
                    ToastUtil.showShortToast("连接失败");
                    showDisconnect();
                    closeDialog();
                    break;
                case ERROR_CODE:
                    ToastUtil.showShortToast("读取失败，请检查连接线是否稳定或这重新连接蓝牙");
                    break;
                case READ_ADDRESS_FINISH_CODE:
                    //获取到表地址,开始根据表地址读水表读数
                    et_meter_address.setText(strMeterAddr);
                    LogUtil.w("当前取的厂商代码：" + mFirmCode);
                    if (EmiUtils.isNormalStandardType()) {
                        readSingleMeterNormal(mFirmCode);
                    } else {
                        readSingleMeterSpecial(mFirmCode);
                    }
                    break;
                case READ_SUCCESS:
                    doReadSuccess();
                    if (isExist && isNormal) {
                        showIsSaveDialog(currentData);
                    }
                    break;
                case MODIFY_TH_CODE:
                    cmdTag = TAG_MODIFY_TH;
                    sendModifyThousandthCmd();
                    break;
                case MODIFY_METER_ADDRESS:
                    cmdTag = TAG_EDIT_METER_ADDRESS;
                    LogUtil.d(TAG, "开始发送改表地址指令");
                    sendModifyMeterAddressCmd();
                    break;
                case DEVICE_DISCONNECTED:
                    showDisconnect();
                    break;
                case EDIT_METER_ADDRESS_CODE:
                    getMeterInfoByMeterId(EmiStringUtil.clearFirstZero(mNewMeterAddressStr));
                    doModifyMeterIdSuccess();
                    break;
                default:
                    break;
            }
        }
    };


    private void showDisconnect() {
        btn_connect.setText("连接蓝牙设备");
        tv_status.setText("蓝牙未连接");
        tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.ripple_red));
        makeButtonEnable(true);
        btn_connect.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_red_selcetor));
        if (mCallBackThread != null) {
            mCallBackThread.interrupt();
            mCallBackThread = null;
        }
    }

    private void showConnect() {
        btn_connect.setText("断开");
        tv_status.setText("蓝牙已连接");
        makeButtonEnable(true);
        tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        btn_connect.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_bg_green_sel));
        mCallBackThread = new CallBackThread();
        mCallBackThread.start();
    }


    private void doConnectClickEvent() {
        BluetoothAdapter bluetoothAdapter = getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            connectBlueToothDevice();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, OpenBlueToothRequestCode);
        }
    }

    private void closeDialog() {
        if (loadingDialog != null) {
            loadingDialog.close();
        }
    }

    private void connectBlueToothDevice() {
        if (EmiConstants.bluetoothSocket == null) {
        /*    loadingDialog = new LoadingDialog(mContext, "正在连接抄表蓝牙设备");
            loadingDialog.show();*/
            btn_connect.setText("正在连接...");
            makeButtonEnable(false);
            connectToBlueDevice();
        } else {
            disConnect();
            ToastUtil.showShortToast("连接已断开");
        }
    }

    private void disConnect() {
        try {
            if (socket != null || EmiConstants.bluetoothSocket != null) {
                if (socket != null) {
                    socket.close();
                }
                socket = null;
                if (EmiConstants.bluetoothSocket != null) {
                    EmiConstants.bluetoothSocket.close();
                }
                EmiConstants.bluetoothSocket = null;
                showDisconnect();
            }
        } catch (IOException e) {
            LogUtil.e(TAG, e.toString());
        }
    }

    private void makeButtonEnable(boolean b) {
        btn_clear.setEnabled(b);
        btn_read.setEnabled(b);
        btn_connect.setEnabled(b);
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
                //用迭代
                for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
                    final BluetoothDevice device = iterator.next();
                    if (EmiConstants.EMI_DEVICE_NAME.equals(device.getName())) {
                        emiDeviceArrayList.add(device);
                    }
                }
                int emiDeviceCount = checkEmiDeviceCount(emiDeviceArrayList);
                LogUtil.w(TAG, "emiDeviceCount：" + emiDeviceCount);
                if (emiDeviceCount == 0) {
                    ToastUtil.showShortToast("没有配对抄表设备，请先配对");
                    closeDialog();
                    return;
                } else if (emiDeviceCount == Integer.MAX_VALUE) {
                    int emiDeviceIndex = getEmiDeviceIndex(emiDeviceArrayList);
                    connectByMac(emiDeviceArrayList.get(emiDeviceIndex));
                } else {
                    showSelectDialog(emiDeviceArrayList);
                }
            }
        }
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
            LogUtil.e(TAG, e.toString());
        }
    }

    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            receiveCount++;
            if (receiveCount % 2 == 0) {
                LogUtil.w(TAG, "已执行");
                if (ACTION_STATE_CHANGED.equals(action)) {
                    if (isBlueToothEnable()) {
                        ToastUtil.showShortToast("蓝牙已打开");
                        showConnect();
                    } else {
                        if (EmiConstants.bluetoothSocket == null) {
                            ToastUtil.showShortToast("蓝牙已关闭");
                            showDisconnect();
                        } else {
                            showDisconnect();
                        }
                    }
                }
            }
        }
    };

    private boolean isBlueToothEnable() {
        BluetoothAdapter adapter = getDefaultAdapter();
        return adapter.isEnabled();
    }

    /**
     * 是否指定表地址
     *
     * @return
     */
    private boolean isAppointAddress() {
        return cb_switch.isChecked();
    }

    @Override
    protected void onDestroy() {
        disConnect();
        super.onDestroy();
    }

    /**
     * 根据表地址读取水表读数（普通市场）
     *
     * @param
     */
    private void readSingleMeterNormal(String firmCode) {
        // TODO Auto-generated method stub
        byte hexcheck = 0x00;
        LogUtil.d(TAG, "当前为普通市场指定表地址:" + firmCode);
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFE, 0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01, 0x03, 0x1F, (byte) 0x90, 0x00, 0x00,
                0x16};
        byte[] firmCodeByte = stringToBytes(getHexString(firmCode));

        // 反射
        for (int i = 0; i < 5; i++) {
            cmd[6 + i] = addr[i];
        }
        cmd[11] = firmCodeByte[1];
        cmd[12] = firmCodeByte[0];
        for (int k = 4; k <= 17; k++) {
            hexcheck += cmd[k];
        }
        cmd[18] = hexcheck;
        strhex = "";
        sendBTMessage(cmd);
        delay(500);
    }

    private void sendBTMessage(byte[] cmd) {
        if (EmiConstants.bluetoothSocket == null) {
            return;
        }
        try {
            OutputStream os = EmiConstants.bluetoothSocket.getOutputStream();
            os.write(cmd);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delay(int ms) {
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取表地址(普通市场)
     */
    private void readAddressNormal() {
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, 0x68, 0x10, (byte) 0xAA,
                (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
                (byte) 0xAA, (byte) 0xAA, 0x03, 0x03, (byte) 0x0A, (byte) 0x81,
                0x01, (byte) 0xB0, 0x16};
        sendBTMessage(cmd);
        delay(500);
    }

    /**
     * 普通市场读表数
     */
    private void readMeterByAddressNormal(String firmCode) {
        // TODO Auto-generated method stub
        byte hexcheck = 0x00;
        breadaddr = true;
        LogUtil.d(TAG, "发送普通市场指定表地址读表数指令:厂商代码:" + firmCode);
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
                0x03, 0x1F, (byte) 0x90, 0x00, 0x00, 0x16};
        editBuffer = et_meter_address.getText().toString();
        int a = editBuffer.length();
        if (a > 0) {
            for (; a < 10; a++) {
                editBuffer = "0" + editBuffer;
            }
            byte[] srtbyte = {0, 0, 0, 0, 0};
            byte[] firmCodeByte;
            srtbyte = stringToBytes(getHexString(editBuffer));
            firmCodeByte = stringToBytes(getHexString(firmCode));
            String newStr = et_meter_address.getText().toString().replaceAll("^(0+)", "");
            LogUtil.d(TAG, "表地址：" + newStr);
            getMeterInfoByMeterId(newStr);
            cmd[6] = srtbyte[4];
            cmd[7] = srtbyte[3];
            cmd[8] = srtbyte[2];
            cmd[9] = srtbyte[1];
            cmd[10] = srtbyte[0];
            cmd[11] = firmCodeByte[1];
            cmd[12] = firmCodeByte[0];
            for (int k = 4; k <= 17; k++) {
                hexcheck += cmd[k];
            }
            cmd[18] = hexcheck;
            strhex = "";
            sendBTMessage(cmd);
            delay(500);
        }
    }




    private void clearAllData() {
        et_meter_address.setText("");
        tv_loacation.setText("");
        tv_meter_data.setText("");
        editTextFirmCode.setText("");
        tvChannel.setText("");
        strhex = "";
    }


    private void sendReadSingleMeterCmd(String firmCode) {
        cmdTag = TAG_READ_METER;
        //是否是普通市场
        boolean isNormal = EmiUtils.isNormalStandardType();
        //是否指定表地址
        boolean isAppoint = isAppointAddress();
        //指定的表地址
        String appointAddressStr;
        if (isAppoint) {
            //指定表地址
            appointAddressStr = et_meter_address.getText().toString();
            if (TextUtils.isEmpty(appointAddressStr)) {
                ToastUtil.showShortToast("请输入表地址");
            } else {
                if (isNormal) {
                    //指定表地址读取普通市场
                    readMeterByAddressNormal(mFirmCode);
                } else {
                    //指定表地址读取特殊市场
                    readMeterByAddressSpecial(mFirmCode);
                }
            }
        } else {
            //不指定表地址（先读表地址）
            if (isNormal) {
                //发送普通市场的“获取表地址”指令
                readAddressNormal();
            } else {
                //发送特殊市场的“获取表地址”指令
                readAddressSpecial();
            }
        }
    }


    protected void readMeterByAddressSpecial(String firmCode) {
        // TODO Auto-generated method stub
        byte hexcheck = 0x00;
        breadaddr = true;
      /*  byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x10, 0x01, 0x03,
                (byte) 0x90, 0x1F, 0x00, 0x00, 0x16};*/
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03,
                (byte) 0x90, 0x1F, 0x00, 0x00, 0x16};
        editBuffer = et_meter_address.getText().toString();
        int a = editBuffer.length();
        if (a > 0) {
            for (; a < 10; a++) {
                editBuffer = "0" + editBuffer;
            }
            System.out.println(editBuffer);
            byte[] srtbyte = {0, 0, 0, 0, 0};
            byte[] firmCodeByte;
            LogUtil.d("当前的厂商代码：" + mFirmCode);
            firmCodeByte = stringToBytes(getHexString(firmCode));
            srtbyte = stringToBytes(getHexString(editBuffer));
            String meterAddress = et_meter_address.getText().toString();
            String newStr = meterAddress.replaceAll("^(0+)", "");
            LogUtil.d(TAG, "newStr：" + newStr);
            getMeterInfoByMeterId(newStr);
            cmd[6] = srtbyte[4];
            cmd[7] = srtbyte[3];
            cmd[8] = srtbyte[2];
            cmd[9] = srtbyte[1];
            cmd[10] = srtbyte[0];
            cmd[11] = firmCodeByte[1];
            cmd[12] = firmCodeByte[0];
            for (int k = 4; k <= 17; k++) {
                hexcheck += cmd[k];
            }
            cmd[18] = (byte) hexcheck;
            strhex = "";
            sendBTMessage(cmd);
            delay(500);
        }
    }

    /**
     * 根据表地址读取水表读数
     *
     * @param
     */
    private void readSingleMeterSpecial(String firmCode) {
        // TODO Auto-generated method stub
        byte hexcheck = 0x00;
        LogUtil.d("当前为特殊市场指定表地址读表数模式：" + firmCode);
        byte[] firmCodeByte = stringToBytes(getHexString(firmCode));
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x10, 0x01, 0x03, (byte) 0x90, 0x1F, 0x00, 0x00, 0x16};
        //					byte[] cmd = {(byte)0xFE,(byte)0xFE,(byte)0xFE,(byte)0xFE,0x68,0x10,0x00,0x00,0x00,0x00,0x00,0x33,0x78,0x01,0x03,0x1F,(byte) 0x90,0x00,0x00,0x16};//反射
        for (int i = 0; i < 5; i++) {
            cmd[6 + i] = addr[i];
        }
        cmd[11] = firmCodeByte[1];
        cmd[12] = firmCodeByte[0];
        for (int k = 4; k <= 17; k++) {
            hexcheck += cmd[k];
        }
        cmd[18] = hexcheck;
        strhex = "";
        sendBTMessage(cmd);
        delay(500);
    }

    /**
     * 读特殊市场表地址
     */
    private void readAddressSpecial() {
        //		 byte[] cmd = {(byte)0xFE,(byte)0xFE,0x68,0x10,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,0x03,0x03,(byte)0x0A,(byte)0x81,0x01,(byte)0xB0,0x16}; //反射式
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, 0x68, 0x10, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, 0x03, 0x03, (byte) 0x81, (byte) 0x0A, 0x01, (byte) 0xB0, 0x16};
        sendBTMessage(cmd);
        delay(500);
    }


    private void sendSettingCmdNormal() {
        String oldMeterAddress = et_meter_address.getText().toString();
        if (TextUtils.isEmpty(oldMeterAddress)) {
            ToastUtil.showShortToast("请输入原表地址");
            return;
        }
        byte hexCheck = 0x00;
        cmdTag = TAG_SETTING;
        //设置使能指令
       /* byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x04,
                0x07, (byte) 0xAA, (byte) 0xA0, 0x00, (byte) 0x87, 0x56, 0x20, (byte) 0x88, 0x00, 0x16};*/
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04,
                0x07, (byte) 0xAA, (byte) 0xA0, 0x00, (byte) 0x87, 0x56, 0x20, (byte) 0x88, 0x00, 0x16};
        editBuffer = et_meter_address.getText().toString();
        if (!checkBoxFirmCode.isChecked()) {
            //不指定厂商代码
            if (EmiUtils.isNormalStandardType()) {
                mFirmCode = FIRM_CODE_7833;
            } else {
                mFirmCode = FIRM_CODE_1001;
            }
        } else {
            //指定厂商代码
            mFirmCode = editTextFirmCode.getText().toString();
            if (mFirmCode.length() != LENGTH_FIRM_CODE) {
                ToastUtil.showShortToast("请输入正确的厂商代码");
                return;
            }
        }
        LogUtil.w("此时的厂商代码：" + mFirmCode);
        int a = editBuffer.length();
        if (a > 0) {
            for (; a < 10; a++) {
                editBuffer = "0" + editBuffer;
            }
            LogUtil.d(TAG, "测试" + editBuffer);
            byte[] srtByte;
            byte[] firmCodeByte;
            srtByte = stringToBytes(getHexString(editBuffer));
            firmCodeByte = stringToBytes(getHexString(mFirmCode));
            cmd[6] = srtByte[4];
            cmd[7] = srtByte[3];
            cmd[8] = srtByte[2];
            cmd[9] = srtByte[1];
            cmd[10] = srtByte[0];
            cmd[11] = firmCodeByte[1];
            cmd[12] = firmCodeByte[0];
            for (int k = checkBeginIndex; k <= checkEndIndex; k++) {
                hexCheck += cmd[k];
            }
            cmd[checkEndIndex + 1] = hexCheck;
            sendBTMessage(cmd);
            delay(500);
        }
    }

    /**
     * 特殊市场
     */
    private void sendSettingCmdSpecial() {
        String oldMeterAddress = et_meter_address.getText().toString();
        if (TextUtils.isEmpty(oldMeterAddress)) {
            ToastUtil.showShortToast("请输入原表地址");
            return;
        }
        byte hexCheck = 0x00;
        cmdTag = TAG_SETTING;
        //设置使能指令
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04,
                0x07, (byte) 0xAA, (byte) 0xA0, 0x00, (byte) 0x87, 0x56, 0x20, (byte) 0x88, 0x00, 0x16};
        editBuffer = et_meter_address.getText().toString();
        if (!checkBoxFirmCode.isChecked()) {
            //不指定厂商代码
            if (EmiUtils.isNormalStandardType()) {
                mFirmCode = FIRM_CODE_7833;
            } else {
                mFirmCode = FIRM_CODE_1001;
            }
        } else {
            //指定厂商代码
            mFirmCode = editTextFirmCode.getText().toString();
            if (mFirmCode.length() != LENGTH_FIRM_CODE) {
                ToastUtil.showShortToast("请输入正确的厂商代码");
                return;
            }
        }
        int a = editBuffer.length();
        if (a > 0) {
            for (; a < 10; a++) {
                editBuffer = "0" + editBuffer;
            }
            LogUtil.d(TAG, "测试" + editBuffer);
            byte[] srtByte;
            byte[] firmCodeByte;
            srtByte = stringToBytes(getHexString(editBuffer));
            LogUtil.w("当前的厂商代码：" + mFirmCode);
            firmCodeByte = stringToBytes(getHexString(mFirmCode));
            cmd[6] = srtByte[4];
            cmd[7] = srtByte[3];
            cmd[8] = srtByte[2];
            cmd[9] = srtByte[1];
            cmd[10] = srtByte[0];
            cmd[11] = firmCodeByte[1];
            cmd[12] = firmCodeByte[0];
            for (int k = checkBeginIndex; k <= checkEndIndex; k++) {
                hexCheck += cmd[k];
            }
            cmd[checkEndIndex + 1] = hexCheck;
            sendBTMessage(cmd);
            delay(500);
        }
    }


    /**
     * 计算CS校验位
     *
     * @param data
     * @param startIndex
     * @param endIndex
     * @return
     */
    private boolean checkCs(byte[] data, int startIndex, int endIndex, int csIndex) {
        int checkSum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            checkSum += byte2Int(data[i]);
            checkSum = checkSum & 0xFF;
        }
        LogUtil.w(TAG, "校验位：" + byte2Int(data[csIndex]));
        LogUtil.w(TAG, "校验位：" + checkSum);
        if (byte2Int(data[csIndex]) == checkSum) {
            return true;
        }
        return false;
    }
@Override
    public int byte2Int(byte b) {
        return b & 0xFF;
    }

    /**
     * 修改千分位
     */
    private void sendModifyThousandthCmd() {
        String oldMeterAddress = et_meter_address.getText().toString();
        if (TextUtils.isEmpty(oldMeterAddress)) {
            ToastUtil.showShortToast("请输入原表地址");
            return;
        }
        byte hexCheck = 0x00;
        //修改千分位
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x16,
                0x08, (byte) 0x16, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
        editBuffer = et_meter_address.getText().toString();
        int a = editBuffer.length();
        if (a > 0) {
            for (; a < MeterAddressMaxLength; a++) {
                editBuffer = "0" + editBuffer;
            }
            LogUtil.d(TAG, "测试" + editBuffer);
            LogUtil.d(TAG, "测试mInputThStr:" + mInputThStr);
            byte[] srtByte;
            byte[] thByte = stringToBytes(getHexString(mInputThStr));
            srtByte = stringToBytes(getHexString(editBuffer));
            cmd[6] = srtByte[4];
            cmd[7] = srtByte[3];
            cmd[8] = srtByte[2];
            cmd[9] = srtByte[1];
            cmd[10] = srtByte[0];
            cmd[20] = thByte[0];
            for (int k = checkBeginIndex; k <= modifyThEndIndex; k++) {
                hexCheck += cmd[k];
            }
            cmd[modifyThEndIndex + 1] = hexCheck;
            sendBTMessage(cmd);
            delay(1000);
        }
    }

    private class CallBackThread extends Thread {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                int bytes;
                InputStream mmInStream = null;
                ArrayList<Byte> settingDataArrayList = new ArrayList<>();
                ArrayList<Byte> clearThArrayList = new ArrayList<>();
                ArrayList<Byte> editMeterArrayList = new ArrayList<>();
                try {
                    if (EmiConstants.bluetoothSocket != null) {
                        mmInStream = EmiConstants.bluetoothSocket.getInputStream();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                    LogUtil.e(TAG, e1.toString());
                }
                while (true) {
                    LogUtil.i(TAG, "CallBackThread--->" + cmdTag);
                    if ((bytes = mmInStream.read(buffer)) > 0) {
                        LogUtil.w(TAG, "接收到回调");
                        LogUtil.w(TAG, "此时的cmdTag=" + cmdTag);
                        switch (cmdTag) {
                            case TAG_READ_METER:
                                readMeterCallBack(bytes, buffer);
                                break;
                            case TAG_SETTING:
                                settingCallBack(bytes, buffer, settingDataArrayList);
                                break;
                            case TAG_MODIFY_TH:
                                clearThCallBack(bytes, buffer, clearThArrayList);
                                break;
                            case TAG_EDIT_METER_ADDRESS:
                                LogUtil.d(TAG, "修改表地址回调");
                                editMeterAddressCallBack(bytes, buffer, editMeterArrayList);
                                break;
                            default:
                                break;
                        }

                    }
                }
            } catch (IOException e) {
                LogUtil.e(TAG, "错误：" + e.toString());
                mHandler.sendEmptyMessage(DEVICE_DISCONNECTED);
                Thread.interrupted();
                LogUtil.i("interrupted = false");
                stopThread();
                EmiConstants.bluetoothSocket = null;
            }
        }
    }

    private void readMeterCallBack(int bytes, byte[] buffer) {
        LogUtil.w(TAG, "读表指令：回调成功");
        try {
            byte[] buf_data = new byte[bytes];
            for (int i = 0; i < bytes; i++) {
                buf_data[i] = buffer[i];
            }
            strhex = strhex + DigitalTrans.byte2hex(buf_data);
            LogUtil.d(TAG, "strhex长度：" + strhex.length());
            LogUtil.i(TAG, "strhex内容：" + strhex);
            LogUtil.w(TAG, "当前校验长度：" + readMeterAddressCheckLength);
            if (breadaddr == false) {
                if (strhex.length() >= readMeterAddressCheckLength) {
                    byte[] addrinfo = DigitalTrans.hex2byte(strhex);
                    LogUtil.w(TAG, "strhex----------->" + strhex);
                    LogUtil.d(TAG, "strhex----------->长度" + strhex.length());
                    int n = 0;
                    while (addrinfo[n++] != 0x68) {
                        if (n > 18) {
                            break;
                        }
                    }
                    if (n < 22) {
                        strMeterAddr = "";
                        strFirmCode = "";
                        for (int l = 0; l < 5; l++) {
                            String str1 = DigitalTrans
                                    .algorismToHEXString(addrinfo[1
                                            + n + l]);
                            addr[l] = addrinfo[1 + n + l];
                            strMeterAddr = str1 + strMeterAddr;
                        }
                        LogUtil.i(TAG, "水表地址：" + strMeterAddr);
                        String newStr = strMeterAddr.replaceAll("^(0+)", "");
                        LogUtil.d(TAG, "表地址：" + newStr);
                        getMeterInfoByMeterId(newStr);
                        for (int l = 0; l < 2; l++) {
                            String str2 = DigitalTrans
                                    .algorismToHEXString(addrinfo[6
                                            + n + l]);
                            addr[5 + l] = addrinfo[6 + n + l];
                            strFirmCode = str2 + strFirmCode;
                        }
                        LogUtil.d(TAG, "读取到的厂商代码：" + strFirmCode);
                        mFirmCode = strFirmCode;
                        strhex = "";
                        breadaddr = true;
                        Message message = mHandler.obtainMessage();
                        message.what = READ_ADDRESS_FINISH_CODE;
                        mHandler.sendMessage(message);
                    }
                    bytes = 0;
                }
            } else {
                LogUtil.d(TAG, "readMeterInfoCheckLength=" + readMeterInfoCheckLength);
                LogUtil.w(TAG, "strhex=" + strhex.length());
                LogUtil.i(TAG, "strhex内容:" + strhex);
                if (hasReceiveFinish(strhex, readMeterInfoCheckLength, READ_METER_INFO_CHECK_LENGTH_NORMAL_1)) {
                    strFirmCode = "";
                    strMeterData = "";
                    byte[] Meterinfo = DigitalTrans
                            .hex2byte(strhex);
                    int m = 0;
                    while (Meterinfo[m++] != 0x68)
                        ;
                    byte[] addr = new byte[5];
                    for (int j = 0; j < 5; j++) {
                        addr[4 - j] = Meterinfo[1 + m + j];
                    }
                    byte[] ReadNum = new byte[2];

                    for (int k = 0; k < 2; k++) {
                        ReadNum[1 - k] = Meterinfo[m + 14 + k];
                    }
                    LogUtil.w(TAG, "------->" + strhex);
                    strMeterData = DigitalTrans.byte2hex(ReadNum);
                    LogUtil.i(TAG, "水表读数：" + strMeterData);
                    for (int l = 0; l < 2; l++) {
                        String str2 = DigitalTrans
                                .algorismToHEXString(Meterinfo[m
                                        + 6 + l]);
                        strFirmCode = str2 + strFirmCode;
                    }
                    strhex = "";
                    // str2 ="";
                    breadaddr = false;
                    Message message = new Message();
                    //读取成功
                    message.what = READ_SUCCESS;
                    mHandler.sendMessage(message);
                } else {
                    if (strhex.length() > 80) {
                        strhex = "";
                    }
                }
            }
        } catch (Exception e) {
            mHandler.sendEmptyMessage(ERROR_CODE);
        }

    }


    /**
     * 设置使能回调
     *
     * @param bytes
     * @param buffer
     */
    private void settingCallBack(int bytes, byte[] buffer, ArrayList<Byte> byteArrayList) {
        byte[] dataByte;
        LogUtil.w(TAG, "使能回调成功settingCallBack()");
        boolean checkCsResult;
        byte[] bufData = new byte[bytes];
        LogUtil.w(TAG, "bytes==" + bytes);
        for (int i = 0; i < bytes; i++) {
            bufData[i] = buffer[i];
            if (cmdTag == TAG_SETTING) {
                byteArrayList.add(buffer[i]);
            }
        }
        strhex = strhex + DigitalTrans.byte2hex(bufData);
        LogUtil.e(TAG, "strhex ==" + strhex);
        LogUtil.w(TAG, "byteArrayList长度：" + byteArrayList.size());
        LogUtil.d(TAG, "settingModifyMeterAddressCheckLength：" + settingModifyMeterAddressCheckLength);
        if (cmdTag == TAG_SETTING) {
            if (hasReceiveFinish(strhex)) {
                dataByte = new byte[byteArrayList.size()];
                LogUtil.i(TAG, "byteDataList长度：" + byteArrayList.size());
                LogUtil.i(TAG, "strhex===" + strhex);
                int startIndex = getCheckStartIndex(strhex);
                int endIndex = getCheckEndIndex(strhex);
                LogUtil.w(TAG, "getCheckStartIndex =" + startIndex);
                LogUtil.d(TAG, "getCheckStartIndex =" + endIndex);
                for (int i = 0; i < byteArrayList.size(); i++) {
                    dataByte[i] = byteArrayList.get(i);
                }
                checkCsResult = checkCs(dataByte, startIndex, endIndex, endIndex + 1);
                if (checkCsResult) {
                    LogUtil.i(TAG, "cmdTag-------->" + cmdTag);
                    LogUtil.w(TAG, "cmdTag-------->" + currentMode);
                    if (currentMode == MODE_CLEAR_TH) {
                        mHandler.sendEmptyMessage(MODIFY_TH_CODE);
                    } else if (currentMode == MODE_EDIT_METER_ADDRESS) {
                        mHandler.sendEmptyMessage(MODIFY_METER_ADDRESS);
                    }
                }
                strhex = "";
                byteArrayList.clear();
                LogUtil.d(TAG, "已执行清空");
            } else {
                if (strhex.length() > 80) {
                    LogUtil.e(TAG, "数据异常");
                    strhex = "";
                    byteArrayList.clear();
                }
            }
        }
    }

    /**
     * 清空千分位回调
     */
    private void clearThCallBack(int bytes, byte[] buffer, ArrayList<Byte> byteArrayList) {
        byte[] dataByte;
        LogUtil.w(TAG, "回调成功");
        boolean checkCsResult;
        byte[] bufData = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            bufData[i] = buffer[i];
            if (cmdTag == TAG_MODIFY_TH) {
                byteArrayList.add(buffer[i]);
            }
        }
        strhex = strhex + DigitalTrans.byte2hex(bufData);
        if (cmdTag == TAG_MODIFY_TH) {
            LogUtil.d(TAG, "修改千分位回调：strhex内容 = " + strhex);
            LogUtil.d(TAG, "修改千分位回调：strhex = " + strhex.length());
            if (hasReceiveFinish(strhex, 40)) {
                dataByte = new byte[byteArrayList.size()];
                for (int i = 0; i < byteArrayList.size(); i++) {
                    dataByte[i] = byteArrayList.get(i);
                }
                checkCsResult = checkCs(dataByte, csBeginIndex, csEndIndex + 1, 18);
                if (checkCsResult) {
                    mHandler.sendEmptyMessage(CLEAR_TH_SUCCESS);
                } else {
                    mHandler.sendEmptyMessage(CLEAR_TH_FAILED);
                }
                strhex = "";
                byteArrayList.clear();
            }
        }
    }

    /**
     * 修改表地址
     */
    private void sendModifyMeterAddressCmd() {
        String oldMeterAddress = et_meter_address.getText().toString();
        if (TextUtils.isEmpty(oldMeterAddress)) {
            return;
        }
        byte hexCheck = 0x00;

        //普通市场
     /*   byte[] cmdNormal = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x15, 0x0A, 0x18,
                (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x00, 0x16};*/
        byte[] cmdNormal = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x15, 0x0A, 0x18,
                (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
        /*byte[] cmdSpecial = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x10, 0x15, 0x0A,
                (byte) 0xA0, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
                0x10, 0x00, 0x16};*/
        /**
         *  特殊市场
         */
        byte[] cmdSpecial = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x15, 0x0A,
                (byte) 0xA0, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x16};
        byte[] cmd;
        if (EmiUtils.isNormalStandardType()) {
            cmd = cmdNormal;
            LogUtil.d(TAG, "当前是普通市场");
        } else {
            LogUtil.i(TAG, "当前是特殊市场");
            cmd = cmdSpecial;
        }
        //修改表地址
        editBuffer = et_meter_address.getText().toString();
        int a = editBuffer.length();
        int b = mNewMeterAddressStr.length();
        if (a > 0) {
            for (; a < MeterAddressMaxLength; a++) {
                editBuffer = "0" + editBuffer;
            }
            LogUtil.d(TAG, "editbuf=" + editBuffer);
            if (b > 0) {
                for (; b < MeterAddressMaxLength; b++) {
                    mNewMeterAddressStr = "0" + mNewMeterAddressStr;
                }
                LogUtil.d(TAG, "mNewMeterAddressStr=" + mNewMeterAddressStr);
                byte[] srtByte;
                byte[] firmCodeByte;
                LogUtil.d("当前的厂商代码为：" + strFirmCode);
                byte[] newMeterByte;
                byte[] newFirmCodeByte = null;
                srtByte = stringToBytes(getHexString(editBuffer));
                firmCodeByte = stringToBytes(getHexString(strFirmCode));
                newMeterByte = stringToBytes(getHexString(mNewMeterAddressStr));
                if (checkBoxFirmCode.isChecked() && editTextFirmCode != null && editTextFirmCode.length() == LENGTH_FIRM_CODE) {
                    newFirmCodeByte = stringToBytes(getHexString(editFirmCode));
                }
                cmd[6] = srtByte[4];
                cmd[7] = srtByte[3];
                cmd[8] = srtByte[2];
                cmd[9] = srtByte[1];
                cmd[10] = srtByte[0];
                cmd[11] = firmCodeByte[1];
                cmd[12] = firmCodeByte[0];
                cmd[18] = newMeterByte[4];
                cmd[19] = newMeterByte[3];
                cmd[20] = newMeterByte[2];
                cmd[21] = newMeterByte[1];
                cmd[22] = newMeterByte[0];
                if (checkBoxFirmCode.isChecked() && newFirmCodeByte != null) {
                    cmd[23] = newFirmCodeByte[1];
                    cmd[24] = newFirmCodeByte[0];
                } else {
                    cmd[23] = firmCodeByte[1];
                    cmd[24] = firmCodeByte[0];
                }
                for (int k = checkBeginIndex; k <= modifyThEndIndex + 2; k++) {
                    hexCheck += cmd[k];
                }
                cmd[modifyThEndIndex + 3] = hexCheck;
                sendBTMessage(cmd);
                delay(1000);
            }
        }
    }

    private void showInputAddressDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("输入新表地址");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInputThStr = et_meter_address.getText().toString();
                mNewMeterAddressStr = builder.editText.getText().toString();
                if (TextUtils.isEmpty(mNewMeterAddressStr)) {
                    ToastUtil.showShortToast("请输入新表地址");
                    return;
                }
                cmdTag = TAG_EDIT_METER_ADDRESS;
                currentMode = MODE_EDIT_METER_ADDRESS;
                if (EmiUtils.isNormalChannel()) {
                    sendSettingCmdNormal();
                } else {
                    sendSettingCmdSpecial();
                }
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
        builder.setInputMaxLength(MaxInputAddressLength);
        inputDialog.show();
    }

    /**
     * 修改表地址回调
     */
    private void editMeterAddressCallBack(int bytes, byte[] buffer, ArrayList<Byte> byteArrayList) {
        byte[] dataByte;
        LogUtil.w(TAG, "修改表地址回调成功editMeterAddressCallBack");
        byte[] bufData = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            bufData[i] = buffer[i];
            if (cmdTag == TAG_MODIFY_TH) {
                byteArrayList.add(buffer[i]);
            }
        }
        strhex = strhex + DigitalTrans.byte2hex(bufData);
        LogUtil.d(TAG, "修改表地址返回的strhex=" + strhex);
        LogUtil.w(TAG, "修改表地址返回的strhex长度：" + strhex.length());
        LogUtil.w(TAG, "modifyMeterAddressCheckLength：" + modifyMeterAddressCheckLength);
        if (cmdTag == TAG_EDIT_METER_ADDRESS) {
            if (hasReceiveFinish(strhex, modifyMeterAddressCheckLength)) {
                dataByte = new byte[byteArrayList.size()];
                LogUtil.w(TAG, "修改表地址：byteDataList长度：" + byteArrayList.size());
                for (int i = 0; i < byteArrayList.size(); i++) {
                    dataByte[i] = byteArrayList.get(i);
                }
                mHandler.sendEmptyMessage(EDIT_METER_ADDRESS_CODE);
                strhex = "";
                byteArrayList.clear();
            }
        }
    }

    private void connectByMac(BluetoothDevice device) {
        //得到BluetoothDevice对象,也就是说得到配对的蓝牙适配器
        //得到远程蓝牙设备的地址
        String address = device.getAddress();
        String name = device.getName();
        if (EmiConstants.EMI_DEVICE_NAME.equals(name)) {
            LogUtil.i(TAG, "MAC地址:" + address);
            device1 = mBluetoothAdapter.getRemoteDevice(address);
            ThreadPoolManager.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = device1.createRfcommSocketToServiceRecord(
                                UUID.fromString(EmiConstants.UUID));
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
                    }
                }
            });
        } else {
            ToastUtil.showShortToast("该设备不是翼迈抄表设备");
            closeDialog();
            showDisconnect();
        }
    }


    private int checkEmiDeviceCount(ArrayList<BluetoothDevice> deviceArrayList) {
        int count = 0;
        for (int i = 0; i < deviceArrayList.size(); i++) {
            if (EmiConstants.EMI_DEVICE_NAME.equals(deviceArrayList.get(i).getName())) {
                count++;
            }
        }
        return count;
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
        deviceDialog.setCanceledOnTouchOutside(false);
        deviceDialog.setCancelable(false);
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


    private void showInputThDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("请输入千位和百位");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInputThStr = builder.editText.getText().toString();
                currentMode = MODE_CLEAR_TH;
                if (TextUtils.isEmpty(mInputThStr) || mInputThStr.length() != MaxInputThLength) {
                    ToastUtil.showShortToast("请输入千位和百位");
                    return;
                }
                sendSettingCmdNormal();
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


    private void setMaxLength(EditText editText, int maxLength) {
        if (editText != null) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        } else {
            LogUtil.e(TAG, "editLength为null");
        }
    }

    private void stopThread() {
        try {
            LogUtil.w("直接抛出异常");
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private boolean hasReceiveFinish(String cmdStr) {
        boolean isStart = cmdStr.startsWith("FE");
        boolean isEnd = cmdStr.endsWith("16");
        boolean isLengthSame = (SETTING_MODIFY_METER_ID_CHECK_LENGTH_NORMAL == cmdStr.length() || SETTING_MODIFY_METER_ID_CHECK_LENGTH_SPECIAL == cmdStr.length());
        LogUtil.d("cmd长度：" + cmdStr.length());
        LogUtil.w(TAG, "isStart = " + isStart + "----" + "isEnd=" + isEnd + "----" + isLengthSame);
        return isStart && isEnd && isLengthSame;
    }

    /**
     * 判断是否接收完成
     *
     * @param cmdStr
     * @param checkLength
     * @return
     */
    private boolean hasReceiveFinish(String cmdStr, int checkLength) {
        boolean isStart = cmdStr.startsWith("FE");
        boolean isEnd = cmdStr.endsWith("16");
        boolean isLengthSame = (checkLength == cmdStr.length());
        LogUtil.i(TAG, "hasReceiveFinish--->cmdStr=" + cmdStr);
        LogUtil.d("cmd长度：" + cmdStr.length() + "---checkLength长度:" + checkLength);
        LogUtil.w(TAG, "isStart = " + isStart + "----" + "isEnd=" + isEnd + "----" + isLengthSame);
        return isStart && isEnd && isLengthSame;
    }

    private boolean hasReceiveFinish(String cmdStr, int checkLength, int checkLength1) {
        boolean isStart = cmdStr.startsWith("FE");
        boolean isEnd = cmdStr.endsWith("16");
        boolean isLengthSame = (checkLength == cmdStr.length() || checkLength1 == cmdStr.length());
        LogUtil.i(TAG, "hasReceiveFinish--->cmdStr=" + cmdStr);
        LogUtil.d("cmd长度：" + cmdStr.length() + "---checkLength长度:" + checkLength +
                "----checkLength1长度:" + checkLength1);
        LogUtil.w(TAG, "isStart = " + isStart + "----" + "isEnd=" + isEnd + "----" + isLengthSame);
        return isStart && isEnd && isLengthSame;
    }


    private int getCheckStartIndex(String str) {
        int startIndex = str.indexOf("FE68");
        startIndex = (startIndex / 2) + 1;
        return startIndex;
    }

    private int getCheckEndIndex(String str) {
        int endIndex = str.lastIndexOf("16");
        endIndex = (endIndex / 2) - 2;
        return endIndex;
    }

    private void doReadSuccess() {
        et_meter_address.setText(et_meter_address.getText().toString());
        editTextFirmCode.setText(strFirmCode);
        tv_meter_data.setText(strMeterData);
        tv_loacation.setText(locationstring);
        tvChannel.setText(channelNumber);
        try {
            currentData = Integer.parseInt(strMeterData);
            isNormal = true;
        } catch (NumberFormatException e) {
            ToastUtil.showShortToast("读数异常");
            isNormal = false;
        }

    }

    /**
     * 根据表地址查询水表相关信息
     */
    private void getMeterInfoByMeterId(String meterId) {
        UserInfo userInfo = getSqOperator().queryMeterInfoByMeterId(meterId);
        locationstring = userInfo.useraddr;
        channelNumber = userInfo.channelNumber;
        if (!EmiStringUtil.isEmpty(userInfo.accountnum)) {
            userId = userInfo.accountnum;
            lastData = userInfo.lastdata;
            isExist = true;
        } else {
            isExist = false;
        }
    }

    private void doModifyMeterIdSuccess() {
        et_meter_address.setText(EmiStringUtil.appendZero(mNewMeterAddressStr, 10));
        tvChannel.setText(channelNumber);
        tv_loacation.setText(locationstring);
        if (checkBoxFirmCode.isChecked()) {
            editTextFirmCode.setText(editFirmCode);
        } else {
            editTextFirmCode.setText(strFirmCode);
        }
        ToastUtil.showShortToast("表地址修改成功");
    }



    private void showInputFirmCodeDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("输入要修改的厂商代码");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editFirmCode = builder.editText.getText().toString();
                if (TextUtils.isEmpty(editFirmCode) || editFirmCode.length() != LENGTH_FIRM_CODE) {
                    ToastUtil.showShortToast("请输入4位数的厂商代码");
                    return;
                }
                showInputAddressDialog();
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
        builder.setInputMaxLength(MaxInputAddressLength);
        inputDialog.show();
    }


    private void clearData() {
        tv_loacation.setText("");
        tv_meter_data.setText("");
        editTextFirmCode.setText("");
        tvChannel.setText("");
        strhex = "";
    }

    /**
     * 显示是否保存对话框
     */
    private void showIsSaveDialog(final int currentData) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("读取成功！")
                .setContentText("当前水表读数为" + currentData + ",是否将读数保存到数据库?")
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
                        saveReadDataToSq(currentData);
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


    private void saveReadDataToSq(int currentData) {
        int currentYL = currentData - lastData;
        getSqOperator().updateData(userId, currentData,
                currentYL, EmiConstants.STATE_SUCCESS, TimeUtil.getCurrentTime());
    }
}
