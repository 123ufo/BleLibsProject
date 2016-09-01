package com.ufo.blehelperlibs.callback;


import android.bluetooth.BluetoothDevice;

/**
 * Created by sz on 2016/8/31.
 *
 * 描述:蓝牙扫描回调接口
 */
public interface OnScanCallback {
    /**
     * 蓝牙未开启
     */
    public static int FAILED_BLUETOOTH_DISABLE = 1;

    /**
     * 扫描完成,成功回调
     */
    void onSuccess();

    /**
     * 扫描失败回调
     *  {@link #FAILED_BLUETOOTH_DISABLE}
     */
    void onFailed(int fail);

    /**
     * 扫描过程中,每扫描到一个设备回调一次
     *
     * @param device     扫描到的设备
     * @param rssi       设备的信息强度
     * @param scanRecord
     */
    void onScanning(final BluetoothDevice device, int rssi, byte[] scanRecord);
}
