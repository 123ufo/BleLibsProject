package com.ufo.blelibsproject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ufo.blehelperlibs.BleManager;
import com.ufo.blehelperlibs.callback.OnConnectCallback;
import com.ufo.blehelperlibs.callback.OnNotificationCallback;
import com.ufo.blehelperlibs.callback.OnReceiverCallback;
import com.ufo.blehelperlibs.callback.OnScanCallback;
import com.ufo.blehelperlibs.callback.OnWriteCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private BleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBleManager = BleManager.getInstance(this);
        findViewById(R.id.btn_scan).setOnClickListener(this);
        findViewById(R.id.btn_connect).setOnClickListener(this);
        findViewById(R.id.btn_disconnect).setOnClickListener(this);
        findViewById(R.id.btn_notification).setOnClickListener(this);
        findViewById(R.id.btn_write).setOnClickListener(this);
        findViewById(R.id.btn_unreceiver).setOnClickListener(this);

        //监听设备以通知方式发过来的数据
        mBleManager.receiver("00001950-0000-1000-8000-00805f9b34fb",
                "00002a6c-0000-1000-8000-00805f9b34fb", new OnReceiverCallback() {
                    @Override
                    public void onSuccess(byte[] value) {
                        System.out.println(TAG + " 接收到的数据:" + new String(value));
                    }

                    @Override
                    public void onFailed(int state) {
                        System.out.println(TAG + " 接收数据失败" + state);
                    }
                });
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_scan) {
            //扫描设备
            mBleManager.scanBleDevices(0, new OnScanCallback() {
                @Override
                public void onSuccess() {
                    System.out.println(TAG + " 扫描完成");
                }

                @Override
                public void onFailed(int fail) {
                    System.out.println(TAG + "扫描失败" + fail);
                }

                @Override
                public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //每扫描一个设备回调一次此方法
                    System.out.println(TAG + " 设备:" + device.getName() + "地址: " + device.getAddress());
                }
            });
        } else if (v.getId() == R.id.btn_connect) {
            //连接设备
            mBleManager.connection(0, "CB:A5:AF:B8:AD:58", new OnConnectCallback() {
                @Override
                public void onSuccess(BluetoothGatt gatt) {
                    System.out.println(TAG + " 连接成功");
                }

                @Override
                public void onFailed(int state) {
                    System.out.println(TAG + " 连接断开: " + state);
                }
            });
        } else if (v.getId() == R.id.btn_disconnect) {
            mBleManager.disConnection();

        } else if (v.getId() == R.id.btn_notification) {
            //设置通知,true为打开通知,false为关
            mBleManager.notification("00001950-0000-1000-8000-00805f9b34fb",
                    "00002a6c-0000-1000-8000-00805f9b34fb",
                    "00002902-0000-1000-8000-00805f9b34fb", true, new OnNotificationCallback() {
                        @Override
                        public void onSuccess() {
                            System.out.println(TAG + " 通知设置成功");
                        }

                        @Override
                        public void onFailed(int state) {
                            System.out.println(TAG + " 通知设置失败代码为:" + state);
                        }
                    });
        } else if (v.getId() == R.id.btn_write) {
            //写入数据
            mBleManager.writer("00001950-0000-1000-8000-00805f9b34fb",
                    "00002a6d-0000-1000-8000-00805f9b34fb",
                    "{S5:1}", new OnWriteCallback() {
                        @Override
                        public void onSuccess() {
                            System.out.println(TAG + " 写成功");
                        }

                        @Override
                        public void onFailed(int state) {
                            System.out.println(TAG + " 写失败:" + state);
                        }
                    });
        } else if (v.getId() == R.id.btn_unreceiver) {
            //移除接收数据
            mBleManager.unReceiver("00001950-0000-1000-8000-00805f9b34fb", "00002a6c-0000-1000-8000-00805f9b34fb");
        }

    }
}
