package com.emi.emireading.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.event.ServiceEvent;
import com.emi.emireading.listener.OnReceiveBlueToothDataListener;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static com.emi.emireading.event.ServiceEvent.NOTIFY_TYPE.CONNECT_BLUETOOTH;
import static com.emi.emireading.event.ServiceEvent.NOTIFY_TYPE.SERVICE_ON_DESTROY;

/**
 * @author :zhoujian
 * @description : BlueToothService
 * @company :翼迈科技
 * @date 2018年01月19日上午 10:30
 * @Email: 971613168@qq.com
 */

public class BlueToothService extends Service {
    private static final String TAG = "BlueToothService";
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mAdapter;
    private int mState;
    public boolean isConnected;
    private int mNewState;
    private DataReceiveThread dataReceiveThread;
    private OnReceiveBlueToothDataListener mOnReceiveBlueToothDataListener;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    /**
     * we're doing nothing
     */
    public static final int STATE_NONE = 0;
    /**
     * now listening for incoming connections
     */
    public static final int STATE_LISTEN = 1;
    /**
     * now initiating an outgoing connection
     * 蓝牙正在连接
     */
    public static final int STATE_CONNECTING = 2;
    /**
     * now connected to a remote device
     * 蓝牙已连接
     */
    public static final int STATE_CONNECTED = 3;

    public Set<BluetoothDevice> getBondedDevices() {
        return mAdapter.getBondedDevices();
    }


    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate--->");
        super.onCreate();

        // Get the local Bluetooth adapter
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;

        start();  // 开启所有线程

        EventBus.getDefault().post(new ServiceEvent(ServiceEvent.NOTIFY_TYPE.SERVICE_ON_CREATE));
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.e(TAG, "listen() failed--->" + e.toString());
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        @Override
        public void run() {
            Log.d(TAG, "Socket Type: " + "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + "secure");
            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    if (mmServerSocket != null){
                        socket = mmServerSocket.accept();
                    }
                } catch (IOException e) {
                    break;
                }

                LogUtil.d(TAG, "socket --->" + socket);
                // If a connection was accepted
                if (socket != null) {
                    LogUtil.d(TAG, "mState = " + mState);
                    switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice(), "secure");
                            break;
                        case STATE_NONE:

                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            LogUtil.e(TAG, "END mAcceptThread, socket Type: ");
        }


        public void connected(BluetoothSocket socket, BluetoothDevice device,
                              final String socketType) {
            Log.d(TAG, "connected, Socket Type:" + socketType);

            // Cancel the thread that completed the connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            // Cancel any thread currently running a connection
            if (dataReceiveThread != null) {
                dataReceiveThread.cancel();
                dataReceiveThread = null;
            }

            // Cancel the accept thread because we only want to connect to one device
            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }

            //        if (mInsecureAcceptThread != null) {
            //            mInsecureAcceptThread.cancel();
            //            mInsecureAcceptThread = null;
            //        }

            // Start the thread to manage the connection and perform transmissions

            dataReceiveThread = new DataReceiveThread(socket, socketType);
            dataReceiveThread.start();
            Log.e(TAG, "DEVICE_NAME = " + device.getName());

            // Update UI title
            updateUserInterfaceTitle();
        }

        public void cancel() {
            Log.d(TAG,    "cancel " + this);
            try {
                if(mmServerSocket != null){
                    mmServerSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG,  "close() of server failed", e);
            }
        }
    }

    /**
     * 数据接收线程
     */
    private class DataReceiveThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public DataReceiveThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;

            isConnected = true;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if (mOnReceiveBlueToothDataListener != null) {
                        mOnReceiveBlueToothDataListener.onReceiveData(bytes, buffer);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                //                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                //                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    private void connectionLost() {
        // Send a failure message back to the Activity
        LogUtil.e(TAG, "Device connection was lost");

        mState = STATE_NONE;
        isConnected = false;

        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        LogUtil.e(TAG, "restart to connect device");
        start();
    }

    private void updateUserInterfaceTitle() {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;
        EventBus.getDefault().post(new ServiceEvent(CONNECT_BLUETOOTH,mState));
    }

    private int getState() {
        return mState;
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = "Secure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            EmiConstants.bluetoothSocket = tmp;
            mState = STATE_CONNECTING;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            mConnectThread = null;

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }


    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        LogUtil.e(TAG, "Unable to connect device");
        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
        LogUtil.e(TAG, "restart to connect device");
        start();
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (dataReceiveThread != null) {
            dataReceiveThread.cancel();
            dataReceiveThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }

        //        if (mInsecureAcceptThread == null) {
        //            mInsecureAcceptThread = new AcceptThread(false);
        //            mInsecureAcceptThread.start();
        //        }

        // Update UI title
        updateUserInterfaceTitle();
    }


    public void setReceiveBlueToothDataListener(OnReceiveBlueToothDataListener dataListener) {
        this.mOnReceiveBlueToothDataListener = dataListener;
    }


    public void connected(BluetoothSocket socket, BluetoothDevice device,
                          final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (dataReceiveThread != null) {
            dataReceiveThread.cancel();
            dataReceiveThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        //        if (mInsecureAcceptThread != null) {
        //            mInsecureAcceptThread.cancel();
        //            mInsecureAcceptThread = null;
        //        }

        // Start the thread to manage the connection and perform transmissions
        dataReceiveThread = new DataReceiveThread(socket, socketType);
        dataReceiveThread.start();
        Log.e(TAG, "DEVICE_NAME = " + device.getName());

        // Update UI title
        updateUserInterfaceTitle();
    }



    public class ServiceBinder extends Binder {
        private BlueToothService service;

        protected ServiceBinder(BlueToothService service) {
            this.service = service;
        }

        public BlueToothService getService() {
            return service;
        }
    }
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public void connect(BluetoothDevice device, boolean secure) {
        Log.e(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (dataReceiveThread != null) {
            dataReceiveThread.cancel();
            dataReceiveThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        ThreadPoolManager.EXECUTOR.execute(mConnectThread);
        // Update UI title
        updateUserInterfaceTitle();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().post(new ServiceEvent(SERVICE_ON_DESTROY));
        LogUtil.w(TAG,"服务已销毁");
        super.onDestroy();
    }
}
