package com.emi.emireading.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
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
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.LoadingDialog;

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
import static java.lang.Integer.parseInt;

/**
 * @author :chenhx
 * @description : 单表维护
 * @company :翼迈科技
 * @date: 2017年10月26日上午 11:44
 * @Email: 971613168@qq.com
 * @modify by:zhoujian
 */

public class SingleMeterDebugActivityOld extends BaseActivity implements View.OnClickListener{
    private Button btn_connect;
    private String mNewMeterAddressStr;
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
    String strMeterAddr = "";
    String strFirmCode = "";
    String strMeterData = "";
    String strChanleCode = "";
    String strChanleDate = "";
    String locationstring = "";
    String editbuf = "";
    boolean breadaddr = false;
    byte addr[] = { 0, 0, 0, 0, 0, 0, 0 };
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
    private TextView tv_code;
    public BluetoothDevice device1 = null;
    private final int READ_ADDRESS_FINISH_CODE = 0;
    private final int OpenBlueToothRequestCode = 1;
    private BluetoothSocket socket;
    //是否有翼迈抄表设备
    private boolean hasDevice = false;
    public BluetoothAdapter mBluetoothAdapter = getDefaultAdapter();
    private LoadingDialog loadingDialog;
    private Context mContext;
    private TextView tv_status;
    private String strHex = "";
    private EditText et_meter_address;
    private Button btnEditMeterAddress;
    private MyOperator mytab3 = null;
    private SQLiteOpenHelper helper = null;
    private TextView tv_meter_data;
    private TextView tv_loacation;
    private Button btnClearTh;
    private int cmdTag = 0;
    private static final int TAG_READ_METER = 0;
    private static final int TAG_SETTING = 1;
    private static final int TAG_MODIFY_TH = 2;
    private int currentMode = -1;
    private final static int MODE_CLEAR_TH = 1001;
    private final static int MODE_EDIT_METER_ADDRESS = 1002;
    private static final int TAG_EDIT_METER_ADDRESS = 3;
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

    private void showUiByCondition(){
        if (!EmiUtils.isMeterTypeDui()){
            btnClearTh.setVisibility(View.VISIBLE);
            btnEditMeterAddress.setVisibility(View.VISIBLE);
        }else {
            btnClearTh.setVisibility(View.GONE);
        }
    }
    private void init() {
        mContext = SingleMeterDebugActivityOld.this;
//        linEditMeterAddress  = (LinearLayout) findViewById(R.id.linEditMeterAddress);
        btn_connect = (Button) findViewById(R.id.bt_connect);
        btnClearTh = (Button) findViewById(R.id.btnClearTh);
        btn_read = (Button) findViewById(R.id.bt_read);
        btn_clear = (Button) findViewById(R.id.bt_clear);
        cb_switch = (CheckBox) findViewById(R.id.cb_switch);
        editTextFirmCode = (EditText) findViewById(R.id.editTextFirmCode);
        cb_switch.setChecked(true);
        tv_code = (TextView) findViewById(R.id.tv_code);
        tv_status = (TextView) findViewById(R.id.tv_status);
        et_meter_address = (EditText) findViewById(R.id.et_meter_address);
        tv_meter_data = (TextView) findViewById(R.id.tv_meter_data);
        tv_loacation = (TextView) findViewById(R.id.tv_location);
        btnEditMeterAddress = (Button) findViewById(R.id.btnEditMeterAddress);
        btn_connect.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btnClearTh.setOnClickListener(this);
        btnEditMeterAddress.setOnClickListener(this);
        showUiByCondition();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_clear:
                clearData();
                break;
            case R.id.bt_connect:
                doConnectClickEvent();
                break;
            case R.id.bt_read:
                if (EmiConstants.bluetoothSocket==null){
                    ToastUtil.showShortToast("请先连接蓝牙设备");
                    return;
                }
                mFirmCode = editTextFirmCode.getText().toString();
                if (TextUtils.isEmpty(mFirmCode)){
                    if (!EmiUtils.isMeterTypeDui()){
                        mFirmCode = FIRM_CODE_7833;
                    }else {
                        mFirmCode = FIRM_CODE_1001;
                    }
                }
                sendReadSingleMeterCmd();
                break;
            case R.id.btnClearTh:
                cmdTag = TAG_MODIFY_TH;
                showInputThDialog();
                break;
            case R.id.btnEditMeterAddress:
                cmdTag = TAG_EDIT_METER_ADDRESS;
                showInputAddressDialog();
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
                    ToastUtil.showShortToast("请检查连接线是否稳定或这重新连接");
                    break;
                case READ_ADDRESS_FINISH_CODE:
                    //获取到表地址
                    et_meter_address.setText(strMeterAddr);
                    if (!EmiUtils.isMeterTypeDui()){
                        readSingleMeterFan(addr);
                    }else {
                        readSingleMeterDui(addr);
                    }
                    break;
                case READ_SUCCESS:
                    et_meter_address.setText(et_meter_address.getText().toString());
                    tv_code.setText(strFirmCode);
                    editTextFirmCode.setText(strFirmCode);
                    tv_meter_data.setText(strMeterData);
                    tv_loacation.setText(locationstring);
                    break;
                case MODIFY_TH_CODE:
                    cmdTag = TAG_MODIFY_TH ;
                    sendModifyThousandthCmd();
                    break;
                case MODIFY_METER_ADDRESS:
//                    ToastUtils.showShortToast("收到回调");
                    cmdTag = TAG_EDIT_METER_ADDRESS;
                    sendEditMeterAddressCmd();
                    break;
                case DEVICE_DISCONNECTED:
                    showDisconnect();
                    break;
                case EDIT_METER_ADDRESS_CODE:
                    ToastUtil.showShortToast("表地址修改成功");
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
        if (  mCallBackThread != null){
            mCallBackThread.interrupt();
            mCallBackThread = null;
        }
    }

    private void showConnect() {
        btn_connect.setText("断开");
        tv_status.setText("蓝牙已连接");
        makeButtonEnable(true);
        tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        btn_connect.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bt_select_bg));
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
            loadingDialog = new LoadingDialog(mContext, "正在连接抄表蓝牙设备");
            loadingDialog.show();
            btn_connect.setText("正在连接...");
            makeButtonEnable(false);
            connectToBlueDevice();
        } else {
            disConnect();
            ToastUtil.showShortToast("连接已断开");
        }
    }

    private void disConnect(){
        try {
            if (socket != null || EmiConstants.bluetoothSocket != null) {
                if (socket != null) {
                    socket.close();
                }
                socket = null;
                if (EmiConstants.bluetoothSocket  != null){
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
                    if (EmiConstants.EMI_DEVICE_NAME.equals(device.getName())){
                        emiDeviceArrayList.add(device);
                    }
                }
                int emiDeviceCount = checkEmiDeviceCount(emiDeviceArrayList);
                LogUtil.w(TAG,"emiDeviceCount："+emiDeviceCount);
                if (emiDeviceCount == 0){
                    ToastUtil.showShortToast("没有配对抄表设备，请先配对");
                    closeDialog();
                    return;
                }else if(emiDeviceCount == Integer.MAX_VALUE){
                    int emiDeviceIndex = getEmiDeviceIndex(emiDeviceArrayList);
                    connectByMac(emiDeviceArrayList.get(emiDeviceIndex));
                }else {
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
            Log.e(TAG, e.toString());
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
    private boolean isAppointMeterAddress() {
        return cb_switch.isChecked();
    }

    @Override
    protected void onDestroy() {
        disConnect();
        super.onDestroy();
    }

    /**
     *
     * @param address
     */
    private void readSingleMeterFan(byte[] address) {
        // TODO Auto-generated method stub
        byte hexcheck = 0x00;
        // byte[] cmd =
        // {(byte)0xFE,(byte)0xFE,(byte)0xFE,(byte)0xFE,0x68,0x10,0x00,0x00,0x00,0x00,0x00,0x33,0x78,0x01,0x03,(byte)0x90,0x1F,0x00,0x00,0x16};
        byte[] cmd = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFE, 0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x33, 0x78, 0x01, 0x03, 0x1F, (byte) 0x90, 0x00, 0x00,
                0x16};
            // 反射
        for (int i = 0; i < 5; i++) {
            cmd[6 + i] = addr[i];
        }
        for (int j = 0; j < 2; j++) {
            cmd[11 + j] = addr[j + 5];
        }
        for (int k = 4; k <= 17; k++) {
            hexcheck += cmd[k];
        }
        cmd[18] = (byte) hexcheck;
        strHex = "";
        sendBTMessage(cmd);
        delay(500);
    }

    private void sendBTMessage(byte[] cmd) {
        if (EmiConstants.bluetoothSocket == null) {
            return;
        }
        try {
            OutputStream os = EmiConstants.bluetoothSocket .getOutputStream();
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
     * 获取表地址
     */
    private void readMeterAddrFan() {
        byte[] cmd = { (byte) 0xFE, (byte) 0xFE, 0x68, 0x10, (byte) 0xAA,
                (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
                (byte) 0xAA, (byte) 0xAA, 0x03, 0x03, (byte) 0x0A, (byte) 0x81,
                0x01, (byte) 0xB0, 0x16 };
        // 反射式
        // byte[] cmd =
        // {(byte)0xFE,(byte)0xFE,0x68,0x10,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,0x03,0x03,(byte)0x81,(byte)0x0A,0x01,(byte)0xB0,0x16};
        sendBTMessage(cmd);
        delay(500);
    }



    private void readMeterByAddressFan() {
        // TODO Auto-generated method stub
        byte hexcheck = 0x00;
        breadaddr = true;
        // byte[] cmd =
        // {(byte)0xFE,(byte)0xFE,(byte)0xFE,(byte)0xFE,0x68,0x10,0x00,0x00,0x00,0x00,0x00,0x01,0x10,0x01,0x03,(byte)0x90,0x1F,0x00,0x00,0x16};
        // 反射式水表指令
        byte[] cmd = { (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x01,
                0x03, 0x1F, (byte) 0x90, 0x00, 0x00, 0x16 };
        editbuf = et_meter_address.getText().toString();
        int a = editbuf.length();
        if (a > 0) {
            for (; a < 10; a++) {
                editbuf = "0" + editbuf;
            }
            byte[] srtbyte = { 0, 0, 0, 0, 0 };
            byte[] firmCodeByte;
            srtbyte = stringToBytes(getHexString(editbuf));
            firmCodeByte = stringToBytes(getHexString(mFirmCode));
            this.helper = new SQLiteHelper(this);
            mytab3 = new MyOperator(helper.getWritableDatabase());
            String newStr = et_meter_address.getText().toString().replaceAll("^(0+)", "");
            LogUtil.d(TAG,"表地址："+newStr);
            locationstring = mytab3.getlocationbyaddr(newStr);
            Message message = new Message();
            message.what = 4;
            mHandler.sendMessage(message);
            cmd[6] = srtbyte[4];
            cmd[7] = srtbyte[3];
            cmd[8] = srtbyte[2];
            cmd[9] = srtbyte[1];
            cmd[10] = srtbyte[0];
            cmd[11] =firmCodeByte[1];
            cmd[12] = firmCodeByte[0];
            for (int k = 4; k <= 17; k++) {
                hexcheck += cmd[k];
            }
            cmd[18] = hexcheck;
            strHex = "";
            sendBTMessage(cmd);
            delay(500);
        }
    }
    @Override
    public byte[] stringToBytes(String s) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) parseInt(s.substring(i * 2, i * 2 + 2), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return buf;
    }


    private void clearData(){
        et_meter_address.setText("");
        tv_loacation.setText("");
        tv_meter_data.setText("");
        tv_code.setText("");
        editTextFirmCode.setText("");
    }

    private void sendReadSingleMeterCmd(){
        cmdTag = TAG_READ_METER;
        if (!EmiUtils.isMeterTypeDui()){
            //反射式表
            if (isAppointMeterAddress()){
                if (!(et_meter_address.getText().toString().length() > 0)){
                    ToastUtil.showShortToast("请输入表地址");
                    return;
                }
                readMeterByAddressFan();
            }else {
                readMeterAddrFan();
            }
        }else {
            //对射逻辑
            if (isAppointMeterAddress()){
                if (!(et_meter_address.getText().toString().length() > 0)){
                    ToastUtil.showShortToast("请输入表地址");
                    return;
                }
                readMeterByAddressDui();
            }else {
                readMeterAddressDui();
            }
        }
    }



    protected void readMeterByAddressDui() {
        // TODO Auto-generated method stub
        byte  hexcheck=0x00;
        breadaddr = true;
        byte[] cmd = {(byte)0xFE,(byte)0xFE,(byte)0xFE,(byte)0xFE,0x68,0x10,0x00,0x00,0x00,0x00,0x00,0x01,0x10,0x01,0x03,(byte)0x90,0x1F,0x00,0x00,0x16};
        //			byte[] cmd = {(byte)0xFE,(byte)0xFE,(byte)0xFE,(byte)0xFE,0x68,0x10,0x00,0x00,0x00,0x00,0x00,0x33,0x78,0x01,0x03,0x1F,(byte)0x90,0x00,0x00,0x16};//反射
        editbuf = et_meter_address.getText().toString();
        int a = editbuf.length();
        if(a>0)
        {
            for(;a<10;a++){
                editbuf = "0"+editbuf;
            }
            System.out.println(editbuf);
            byte[] srtbyte ={0,0,0,0,0};
            srtbyte =stringToBytes(getHexString(editbuf));
            this.helper=new SQLiteHelper(this);
            mytab3 = new MyOperator(helper.getWritableDatabase());
            String meterAddress = et_meter_address.getText().toString();
            String newStr = meterAddress.replaceAll("^(0+)", "");
            LogUtil.d(TAG,"newStr："+newStr);
            locationstring = mytab3.getlocationbyaddr(newStr);
            Message message = new Message();
            message.what = 4;
            mHandler.sendMessage(message);
            cmd[6] = srtbyte[4];
            cmd[7] = srtbyte[3];
            cmd[8] = srtbyte[2];
            cmd[9] = srtbyte[1];
            cmd[10] = srtbyte[0];
            for(int k=4;k<=17;k++)
            {
                hexcheck+=cmd[k];
            }
            cmd[18]=(byte)hexcheck;
            strHex="";
            sendBTMessage(cmd);
            delay(500);
        }
    }
    private void readSingleMeterDui(byte[] addr) {
        // TODO Auto-generated method stub
        byte  hexcheck=0x00;
        byte[] cmd = {(byte)0xFE,(byte)0xFE,(byte)0xFE,(byte)0xFE,0x68,0x10,0x00,0x00,0x00,0x00,0x00,0x01,0x10,0x01,0x03,(byte)0x90,0x1F,0x00,0x00,0x16};
        //					byte[] cmd = {(byte)0xFE,(byte)0xFE,(byte)0xFE,(byte)0xFE,0x68,0x10,0x00,0x00,0x00,0x00,0x00,0x33,0x78,0x01,0x03,0x1F,(byte) 0x90,0x00,0x00,0x16};//反射
        for(int i=0;i<5;i++)
        {
            cmd[6+i]=addr[i];
        }
        for(int j=0;j<2;j++)
        {
            cmd[11+j]=addr[j+5];
        }
        for(int k=4;k<=17;k++)
        {
            hexcheck+=cmd[k];
        }
        cmd[18]=(byte)hexcheck;
        strHex="";
        sendBTMessage(cmd);
        delay(500);
    }


    private void readMeterAddressDui() {
        //		 byte[] cmd = {(byte)0xFE,(byte)0xFE,0x68,0x10,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,0x03,0x03,(byte)0x0A,(byte)0x81,0x01,(byte)0xB0,0x16}; //反射式
        byte[] cmd = {(byte)0xFE,(byte)0xFE,0x68,0x10,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,0x03,0x03,(byte)0x81,(byte)0x0A,0x01,(byte)0xB0,0x16};
        sendBTMessage(cmd);
        delay(500);
    }


    private void sendSettingCmd() {
        String oldMeterAddress = et_meter_address.getText().toString();
        if (TextUtils.isEmpty(oldMeterAddress)) {
            ToastUtil.showShortToast("请输入原表地址");
            return;
        }
        byte hexCheck = 0x00;
        cmdTag = TAG_SETTING;
        //设置使能指令
        byte[] cmd = { (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x04,
                0x07, (byte) 0xAA, (byte) 0xA0, 0x00,(byte)0x87,0x56, 0x20, (byte)0x88,0x00,0x16 };
        editbuf = et_meter_address.getText().toString();
        int a = editbuf.length();
        if (a > 0) {
            for (; a < 10; a++) {
                editbuf = "0" + editbuf;
            }
            LogUtil.d(TAG, "测试" + editbuf);
            byte[] srtByte;
            srtByte = stringToBytes(getHexString(editbuf));
            cmd[6] = srtByte[4];
            cmd[7] = srtByte[3];
            cmd[8] = srtByte[2];
            cmd[9] = srtByte[1];
            cmd[10] = srtByte[0];
            for (int k = checkBeginIndex; k <= checkEndIndex; k++) {
                hexCheck += cmd[k];
            }
            cmd[checkEndIndex+1] = hexCheck;
            sendBTMessage(cmd);
            delay(500);
        }
    }



    public  byte[] strToHexByte(String hexString)
    {
        hexString = hexString.replace(" ", "");
        if ((hexString.length() % 2) != 0){
            hexString += " ";
        }
        byte[] returnBytes = new byte[hexString.length() / 2];
        for (int i = 0; i < returnBytes.length; i++){
            try {
                returnBytes[i] = Byte.parseByte(hexString.substring(i * 2, i*2+1),16);
            }catch (NumberFormatException e){
                LogUtil.e(TAG,e.toString());
            }
        }
        return returnBytes;
    }

    /**
     * 计算CS校验位
     * @param data
     * @param startIndex
     * @param endIndex
     * @return
     */
    private boolean checkCs(byte[] data,int startIndex,int endIndex,int byteArraySize,int csIndex){
        int invaliteSum_freezeTime = 0;
        if (data.length==byteArraySize){
            for (int i = startIndex; i < endIndex; i++) {
                invaliteSum_freezeTime += byte2Int(data[i]);
                invaliteSum_freezeTime = invaliteSum_freezeTime & 0xFF;
            }
            if (byte2Int(data[csIndex]) == invaliteSum_freezeTime){
                return true;
            }
        }
        return false;
    }


    /**
     * 修改千分位
     */
    private void sendModifyThousandthCmd(){
        String oldMeterAddress = et_meter_address.getText().toString();
        if (TextUtils.isEmpty(oldMeterAddress)) {
            ToastUtil.showShortToast("请输入原表地址");
            return;
        }
        byte hexCheck = 0x00;
        //修改千分位
        byte[] cmd = { (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                0x68, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x78, 0x16,
                0x08, (byte) 0x16, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16};
        editbuf = et_meter_address.getText().toString();
        int a = editbuf.length();
        if (a > 0) {
            for (; a < MeterAddressMaxLength; a++) {
                editbuf = "0" + editbuf;
            }
            LogUtil.d(TAG, "测试" + editbuf);
            LogUtil.d(TAG, "测试mInputThStr:" + mInputThStr);
            byte[] srtByte;
            byte[] thByte = stringToBytes(getHexString(mInputThStr));
            srtByte = stringToBytes(getHexString(editbuf));
            cmd[6] = srtByte[4];
            cmd[7] = srtByte[3];
            cmd[8] = srtByte[2];
            cmd[9] = srtByte[1];
            cmd[10] =srtByte[0];
            cmd[20] = thByte[0];
            for (int k = checkBeginIndex; k <= modifyThEndIndex; k++) {
                hexCheck += cmd[k];
            }
            cmd[modifyThEndIndex+1] = hexCheck;
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
                    mmInStream = EmiConstants.bluetoothSocket.getInputStream();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    LogUtil.e(TAG, e1.toString());
                }
                while (true) {
                    LogUtil.i(TAG,"CallBackThread--->"+cmdTag);
                    if( (bytes = mmInStream.read(buffer)) > 0 ){
                        LogUtil.w(TAG,"接收到回调");
                        LogUtil.w(TAG,"此时的cmdTag="+cmdTag);
                        switch (cmdTag){
                            case TAG_READ_METER:
                                readMeterCallBack(bytes,buffer);
                                break;
                            case TAG_SETTING:
                                settingCallBack(bytes,buffer,settingDataArrayList);
                                break;
                            case TAG_MODIFY_TH:
                                clearThCallBack(bytes,buffer,clearThArrayList);
                                break;
                            case TAG_EDIT_METER_ADDRESS:
                                editMeterAddressCallBack(bytes,buffer,editMeterArrayList);
                                break;
                            default:
                                break;
                        }

                    }
                }
            }catch (Exception e){
                LogUtil.e(TAG,"错误："+e.toString());
                mHandler.sendEmptyMessage(DEVICE_DISCONNECTED);
                Thread.interrupted();
                LogUtil.i("interrupted = false");
                stopThread();
                EmiConstants.bluetoothSocket = null;
            }
            }
        }










    private void readMeterCallBack(int bytes,byte[] buffer){
        LogUtil.w(TAG,"反射式指令：回调成功");
        byte[] buf_data = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            buf_data[i] = buffer[i];
        }
        strHex = strHex + DigitalTrans.byte2hex(buf_data);
        LogUtil.d(TAG,"strHex长度："+strHex.length());
        LogUtil.i(TAG,"strHex内容："+strHex);
        if (breadaddr == false) {
            if (strHex.length() >= 36) {
                byte[] addrinfo = DigitalTrans.hex2byte(strHex);
                LogUtil.w(TAG,"strhex----------->"+strHex);
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
                    Log.i(TAG,"水表地址："+strMeterAddr);
                    this.helper = new SQLiteHelper(this);
                    mytab3 = new MyOperator(helper.getWritableDatabase());
                    String newStr = strMeterAddr.replaceAll("^(0+)", "");
                    LogUtil.d(TAG,"表地址："+newStr);
                    locationstring = mytab3.getlocationbyaddr(newStr);
                    Message message1 = new Message();
                    message1.what = 4;
                    mHandler.sendMessage(message1);
                    for (int l = 0; l < 2; l++) {
                        String str2 = DigitalTrans
                                .algorismToHEXString(addrinfo[6
                                        + n + l]);
                        addr[5 + l] = addrinfo[6 + n + l];
                        strFirmCode = str2 + strFirmCode;
                    }
                    strHex = "";
                    breadaddr = true;
                    Message message = new Message();
                    message.what = 0;
                    mHandler.sendMessage(message);
                }
                bytes = 0;
            }
        } else {
            if (strHex.length() >= 74) {
                strFirmCode = "";
                strMeterData = "";
                byte[] Meterinfo = DigitalTrans
                        .hex2byte(strHex);
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
                LogUtil.w(TAG,"------->"+strHex);
                strMeterData = DigitalTrans.byte2hex(ReadNum);
                Log.d(TAG,"水表读数："+strMeterData);
                for (int l = 0; l < 2; l++) {
                    String str2 = DigitalTrans
                            .algorismToHEXString(Meterinfo[m
                                    + 6 + l]);
                    strFirmCode = str2 + strFirmCode;
                }
                strHex = "";
                // str2 ="";
                breadaddr = false;
                Message message = new Message();
                message.what = READ_SUCCESS;//读取成功
                mHandler.sendMessage(message);
            }
        }
    }

    /**
     * 设置使能回调
     * @param bytes
     * @param buffer
     */
    private void settingCallBack(int bytes,byte[] buffer,ArrayList<Byte> byteArrayList){
        byte[] dataByte;
        LogUtil.w(TAG,"回调成功");
        boolean checkCsResult;
        byte[] bufData = new byte[bytes];
        LogUtil.w(TAG,"bytes=="+bytes);
        for (int i = 0; i < bytes; i++) {
            bufData[i] = buffer[i];
            if (cmdTag == TAG_SETTING){
                byteArrayList.add(buffer[i]);
            }
        }
        strHex = strHex + DigitalTrans.byte2hex(bufData);
        LogUtil.e(TAG,"strhex =="+strHex);
       LogUtil.w(TAG,"byteArrayList长度："+byteArrayList.size());
        if (cmdTag == TAG_SETTING ){
            if (strHex.length() >= 36){
                dataByte = new byte[byteArrayList.size()];
                LogUtil.i(TAG,"byteDataList长度："+byteArrayList.size());
                LogUtil.i(TAG,"strhex==="+strHex);
                for (int i = 0; i < byteArrayList.size(); i++) {
                    dataByte[i] = byteArrayList.get(i);
                }
                checkCsResult = checkCs(dataByte,csBeginIndex,csEndIndex,settingCallBackDataLength,16);
                if (checkCsResult){
                    LogUtil.i(TAG,"cmdTag-------->"+cmdTag);
                    LogUtil.w(TAG,"cmdTag-------->"+currentMode);
                    if (currentMode == MODE_CLEAR_TH){
                        mHandler.sendEmptyMessage(MODIFY_TH_CODE);
                    }else if(currentMode == MODE_EDIT_METER_ADDRESS){
                        mHandler.sendEmptyMessage(MODIFY_METER_ADDRESS);
                    }
                }
                strHex = "";
                byteArrayList.clear();
                LogUtil.e(TAG,"已执行清空");
            }
        }
    }

    /**
     * 清空千分位回调
     */
    private void clearThCallBack(int bytes,byte[] buffer,ArrayList<Byte> byteArrayList ){
        byte[] dataByte;
        LogUtil.w(TAG,"回调成功");
        boolean checkCsResult;
        byte[] bufData = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            bufData[i] = buffer[i];
            if (cmdTag == TAG_MODIFY_TH){
                byteArrayList.add(buffer[i]);
            }
        }
        strHex = strHex + DigitalTrans.byte2hex(bufData);
        LogUtil.w(TAG,"清空千分位：byteArrayList长度："+byteArrayList.size());
        if (cmdTag == TAG_MODIFY_TH){
            if (strHex.length() >= 40){
                dataByte = new byte[byteArrayList.size()];
                LogUtil.w(TAG,"清空千分位：byteDataList长度："+byteArrayList.size());
                for (int i = 0; i < byteArrayList.size(); i++) {
                    dataByte[i] = byteArrayList.get(i);
                }
                checkCsResult = checkCs(dataByte,csBeginIndex,csEndIndex+1,settingCallBackDataLength+2,18);
                if (checkCsResult){
                    mHandler.sendEmptyMessage(CLEAR_TH_SUCCESS);
                }else {
                    mHandler.sendEmptyMessage(CLEAR_TH_FAILED);
                }
                strHex= "";
                byteArrayList.clear();
            }
        }
    }

    /**
     * 修改表地址
     */
    private void sendEditMeterAddressCmd() {
        String oldMeterAddress = et_meter_address.getText().toString();
        if (TextUtils.isEmpty(oldMeterAddress)) {
            return;
        }
        byte hexCheck = 0x00;
        //修改表地址
        byte[] cmd = {(byte) 0xFE,(byte) 0xFE,(byte) 0xFE,(byte) 0xFE,
                0x68,0x10,0x00, 0x00, 0x00, 0x00,0x00,0x33,0x78,0x15,
                0x0A,0x18,(byte)0xA0,0x00,0x00,0x00,0x00,0x00, 0x00, 0x33,
                0x78,0x00,0x16};
        //修改表地址
        editbuf = et_meter_address.getText().toString();
        int a = editbuf.length();
        int b = mNewMeterAddressStr.length();
        if (a > 0) {
            for (; a < MeterAddressMaxLength; a++) {
                editbuf = "0" + editbuf;
            }
            LogUtil.d(TAG, "editbuf=" + editbuf);
            if (b > 0) {
                for (; b < MeterAddressMaxLength; b++) {
                    mNewMeterAddressStr = "0" + mNewMeterAddressStr;
                }
                LogUtil.d(TAG, "mNewMeterAddressStr=" + mNewMeterAddressStr);
                byte[] srtByte;
                byte[] newMeterByte;
                srtByte = stringToBytes(getHexString(editbuf));
                newMeterByte = stringToBytes(getHexString(mNewMeterAddressStr));
                cmd[6] = srtByte[4];
                cmd[7] = srtByte[3];
                cmd[8] = srtByte[2];
                cmd[9] = srtByte[1];
                cmd[10] = srtByte[0];
                cmd[18] = newMeterByte[4];
                cmd[19] = newMeterByte[3];
                cmd[20] = newMeterByte[2];
                cmd[21] = newMeterByte[1];
                cmd[22] = newMeterByte[0];
                for (int k = checkBeginIndex; k <= modifyThEndIndex+2; k++) {
                    hexCheck += cmd[k];
                }
                cmd[modifyThEndIndex + 3] = hexCheck;
                sendBTMessage(cmd);
                delay(1000);
            }
        }
    }

    private void showInputAddressDialog(){
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("输入新表地址");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInputThStr =et_meter_address.getText().toString();
                mNewMeterAddressStr = builder.editText.getText().toString();
                if (TextUtils.isEmpty(mNewMeterAddressStr)){
                    ToastUtil.showShortToast("请输入新表地址");
                    return;
                }
                cmdTag = TAG_EDIT_METER_ADDRESS;
                currentMode = MODE_EDIT_METER_ADDRESS;
                sendSettingCmd();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog inputDialog =  builder.create();
        builder.setInputMaxLength(MaxInputAddressLength);
        inputDialog.show();
    }

    /**
     * 修改表地址回调
     */
    private void editMeterAddressCallBack(int bytes,byte[] buffer,ArrayList<Byte> byteArrayList ){
        byte[] dataByte;
        LogUtil.w(TAG,"回调成功");
        boolean checkCsResult;
        byte[] bufData = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            bufData[i] = buffer[i];
            if (cmdTag == TAG_MODIFY_TH){
                byteArrayList.add(buffer[i]);
            }
        }
        strHex = strHex + DigitalTrans.byte2hex(bufData);
        if (cmdTag == TAG_EDIT_METER_ADDRESS){
            if (strHex.length() >= 19){
                dataByte = new byte[byteArrayList.size()];
                LogUtil.w(TAG,"修改表地址：byteDataList长度："+byteArrayList.size());
                for (int i = 0; i < byteArrayList.size(); i++) {
                    dataByte[i] = byteArrayList.get(i);
                }
               mHandler.sendEmptyMessage(EDIT_METER_ADDRESS_CODE);
                strHex= "";
                byteArrayList.clear();
            }
        }
    }

    private void connectByMac(BluetoothDevice device){
        //得到BluetoothDevice对象,也就是说得到配对的蓝牙适配器
        //得到远程蓝牙设备的地址
        String address = device.getAddress();
        String name = device.getName();
        if ("EMI0001".equals(name)) { //EMI0001
            LogUtil.i(TAG,"MAC地址:"+address);
            device1 = mBluetoothAdapter.getRemoteDevice(address);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = device1.createRfcommSocketToServiceRecord(
                                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        socket.connect();
                        EmiConstants.bluetoothSocket = socket;
                        LogUtil.w(TAG, "socket.isConnected() = " + socket.isConnected());
                        Message message = new Message();
                        message.what = CONNECT_SUCCESS_CODE;//蓝牙连接成功
                        mHandler.sendMessage(message);
                    } catch (IOException e) {
                        LogUtil.e(TAG, "connectToBlueDevice()--->" + e.toString());
                        Message message = new Message();
                        message.what = CONNECT_FAILED_CODE;
                        mHandler.sendMessage(message);
                        e.printStackTrace();
                    }
                }
            }).start();
            hasDevice = true;
        }else {
            ToastUtil.showShortToast("该设备不是翼迈抄表设备");
            closeDialog();
            showDisconnect();
        }
    }


    private int checkEmiDeviceCount(ArrayList<BluetoothDevice> deviceArrayList){
        int count = 0;
        for (int i = 0; i < deviceArrayList.size(); i++) {
            if (EmiConstants.EMI_DEVICE_NAME.equals(deviceArrayList.get(i).getName())){
                count++;
            }
        }
        return  count;
    }




    private void showSelectDialog(final ArrayList<BluetoothDevice> emiDeviceArrayList ){
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

    private void cancelConnect(){
        closeDialog();
        showDisconnect();
        makeButtonEnable(true);
    }


    private void showInputThDialog(){
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("请输入千位和百位");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInputThStr = builder.editText.getText().toString();
                currentMode = MODE_CLEAR_TH;
                if (TextUtils.isEmpty(mInputThStr) || mInputThStr.length() != MaxInputThLength ){
                    ToastUtil.showShortToast("请输入千位和百位");
                    return;
                }
                sendSettingCmd();
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


    private void setMaxLength(EditText editText, int maxLength){
        if (editText != null){
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }else {
            LogUtil.e(TAG,"editLength为null");
        }
    }


    private void stopThread(){
        try {
            LogUtil.w("直接抛出异常");
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }





}
