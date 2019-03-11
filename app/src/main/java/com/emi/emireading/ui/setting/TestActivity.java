package com.emi.emireading.ui.setting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.SelectDeviceEmiAdapter;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.event.ServiceEvent;
import com.emi.emireading.service.BlueToothService;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static com.emi.emireading.service.BlueToothService.STATE_CONNECTED;
import static com.emi.emireading.service.BlueToothService.STATE_CONNECTING;
import static com.emi.emireading.service.BlueToothService.STATE_LISTEN;
import static com.emi.emireading.service.BlueToothService.STATE_NONE;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年01月22日上午 09:15
 * @Email: 971613168@qq.com
 */

public class TestActivity extends BaseActivity implements View.OnClickListener {
    private BlueToothService mBlueToothService;
    private TextView tvConnectStatus;
    private Button btnConnect;
    private Button btnRead;
    private Button btnClear;
    private Context mContext;
    private Handler mHandler = new Handler();
    private boolean isBind= false;
    @Override
    protected int getContentLayout() {
        return R.layout.activity_singlemeter_debug;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        btnConnect = findViewById(R.id.bt_connect);
        tvConnectStatus = findViewById(R.id.tv_status);
        btnRead = findViewById(R.id.bt_read);
        btnClear = findViewById(R.id.bt_clear);
        btnConnect.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        EventBus.getDefault().register(this);
        startService(new Intent(this, BlueToothService.class));
    }

    @Override
    protected void initData() {
        initBlueTooth();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_connect:
                if (mBlueToothService != null){
                    if (EmiConstants.bluetoothSocket == null || (!EmiConstants.bluetoothSocket.isConnected())){
                        doConnectDevice();
                    }else {
                        doDIsConnectDevice();
                    }

                }else {
                    ToastUtil.showShortToast("蓝牙服务未启动！");
                }
                break;
            default:
                break;
        }
    }

    private void showConnectedSuccess() {
        tvConnectStatus.setText(getResources().getString(R.string.connectSuccess));
        btnConnect.setText(getResources().getString(R.string.disConnected));
        tvConnectStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        btnConnect.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_bg_green_sel));
    }

    private void showConnectedFailed() {
        tvConnectStatus.setText(getResources().getString(R.string.connectFailed));
        btnConnect.setText(getResources().getString(R.string.connectBlueTooth));
        tvConnectStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
        btnConnect.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_red_selcetor));
    }

    private void showConnecting() {
        tvConnectStatus.setText(getResources().getString(R.string.isConnecting));
        btnConnect.setText(getResources().getString(R.string.isConnecting));
        tvConnectStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        btnConnect.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bt_select_bg));
    }

    @Override
    protected void onDestroy() {
        if (isBind){
            unbindService(mBandServiceConnection);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    ServiceConnection mBandServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtil.i(TAG, "onServiceConnected--->" + componentName.getClassName());
            BlueToothService.ServiceBinder binder = (BlueToothService.ServiceBinder) iBinder;
            mBlueToothService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.w(TAG, "onServiceDisconnected--->" + componentName.getClassName());
        }
    };


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceEvent event) {
        LogUtil.i(TAG, "onMessageEvent--->"+event.getNotifyType().name());
        switch (event.getNotifyType()) {
            case SERVICE_ON_CREATE:
            case SERVICE_ON_START:
                // 绑定service
                Intent intentService = new Intent(this, BlueToothService.class);
                isBind = bindService(intentService, mBandServiceConnection, Context.BIND_AUTO_CREATE);
                break;
            case CONNECT_BLUETOOTH:
                LogUtil.i(TAG,"当前状态："+event.getConnectState());
                switch (event.getConnectState()) {
                    case STATE_LISTEN:
                        showConnectedFailed();
                        break;
                    case STATE_CONNECTED:
                        showConnectedSuccess();
                        break;
                    case STATE_CONNECTING:
                        showConnecting();
                        break;
                    case STATE_NONE:
                        showConnectedFailed();
                        break;
                    default:
                        break;
                }
                break;
            case SERVICE_ON_DESTROY:
                LogUtil.w(TAG,"服务已销毁");
                break;
            default:
                break;
        }




    }




    private void showSelectDialog(final ArrayList<BluetoothDevice> emiDeviceArrayList) {
        SelectDeviceEmiAdapter deviceAdapter = new SelectDeviceEmiAdapter(emiDeviceArrayList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("请选择蓝牙设备");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //                cancelConnect();
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
                if (mBlueToothService != null){
                    mBlueToothService.connect(emiDeviceArrayList.get(position),true);
                }else {
                    ToastUtil.showShortToast("蓝牙服务未启动");
                }
                deviceDialog.dismiss();
            }
        });
        builder.setAdapter(deviceAdapter);
        deviceDialog.show();
    }



    private void doConnectDevice() {
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
                    return;
                } else {
                    showSelectDialog(emiDeviceArrayList);
                }
            } else {
                ToastUtil.showShortToast("没有已配对设备");
            }
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


    private void initBlueTooth(){
        if (EmiConstants.bluetoothSocket  != null && EmiConstants.bluetoothSocket.isConnected()){
            showConnectedSuccess();
        }else {
            showConnectedFailed();
        }
    }


    private void doDIsConnectDevice(){
        if (mBlueToothService != null){
            mBlueToothService.isConnected = false;
            mBlueToothService.stopSelf();
            EmiConstants.bluetoothSocket = null;
            showConnectedFailed();
        }
    }
}
