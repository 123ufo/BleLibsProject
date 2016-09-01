package com.ufo.blehelperlibs.callback;

/**
 * Created by sz on 2016/9/1.
 * 描述:接收设备向手机发送的广播数据
 */
public interface OnReceiverCallback {


    /**
     * 蓝牙未开启
     */
    int FAILED_BLUETOOTH_DISABLE = 1;
    /**
     * 服务无效
     */
    int FAILED_INVALID_SERVICE = 2;
    /**
     * 特征无效
     */
    int FAILED_INVALID_CHARACTER = 3;
    /**
     * 操作失败
     */
    int FAILED_OPERATION = 5;


    void onSuccess(byte[] value);

    void onFailed(int state);
}
