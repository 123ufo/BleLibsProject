package com.ufo.blehelperlibs.callback;

/**
 * Created by sz on 2016/9/1.
 * 描述:通知设置回调
 */
public interface OnNotificationCallback {
    /**
     * 蓝牙未开启
     */
    int FAILED_BLUETOOTH_DISABLE = 1;
    /**
     * 服务无效
     */
    int FAILED_INVALID_SERVICE = 2;
    /**
     * 特片无效
     */
    int FAILED_INVALID_CHARACTER = 3;
    /**
     * 描述符无效
     */
    int FAILED_INVALID_DESCRIPTOR = 4;
    /**
     * 操作失败
     */
    int FAILED_OPERATION = 5;
    /**
     * 设置通知成功
     */
    void onSuccess();

    /**
     * 设置通知失败
     */
    void onFailed(int state);
}
