package com.ufo.blehelperlibs.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sz on 2016/9/1.
 * <p/>
 * 描述:打印工具
 */
public class PrintUtils {

    private static final String TAG = "PrintUtils";

    /**
     * 打印所以的特征
     * @param servicesMap
     */
    public static void printCharacteristic(HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap) {
        for (Map.Entry<String, Map<String, BluetoothGattCharacteristic>> s : servicesMap.entrySet()) {
            Log.d(TAG, "s: " + s.getKey());
            for (Map.Entry<String, BluetoothGattCharacteristic> c : s.getValue().entrySet()) {
                Log.d(TAG, "    c: " + c.getKey() + "   v: " + c.getValue());
            }
        }
    }
}
