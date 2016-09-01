package com.ufo.blehelperlibs.callback;

import android.bluetooth.BluetoothGatt;

/**
 * Created by sz on 2016/8/31.
 * 描述:蓝牙设备连接回调接口
 */
public interface OnConnectCallback {

    /**
     * 蓝牙未开启
     */
    int FAILED_BLUETOOTH_DISABLE = 1;
    /**
     * 无效的远程设备
     */
    int INVALID_REMOTE_DEVICES = 2;
    /**
     * 连接超时
     */
    int CONNECTION_TIME_OUT = 3;
    /**
     * 未知异常,一般是蓝牙适配器为空
     */
    int UNKNOWN_ERROR = 4;

    /**
     * 连接成功
     *
     * @param gatt
     */
    void onSuccess(BluetoothGatt gatt);

    /**
     * 连接失败
     */
    void onFailed(int state);
}
