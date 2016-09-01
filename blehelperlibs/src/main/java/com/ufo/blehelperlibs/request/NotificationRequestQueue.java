package com.ufo.blehelperlibs.request;


import android.util.Log;

import com.ufo.blehelperlibs.callback.OnNotificationCallback;

import java.util.HashMap;

/**
 * Created by sz on 2016/9/1.
 * <p/>
 * 描述:通知设置请求队列
 */
public class NotificationRequestQueue implements IRequestQueue<OnNotificationCallback> {
    private static final String TAG = "NotificationRequest";

    HashMap<String, OnNotificationCallback> map = new HashMap<>();

    @Override
    public void set(String key, OnNotificationCallback onNotificationCallback) {
        map.put(key, onNotificationCallback);
    }

    @Override
    public OnNotificationCallback get(String key) {
        return map.get(key);
    }

    /**
     * 移除一个元素
     *
     * @param key
     */
    public boolean removeRequest(String key) {
        Log.d(TAG, " NotificationRequestQueue before " + map.size());
        OnNotificationCallback onNotificationCallback = map.remove(key);
        Log.d(TAG, " NotificationRequestQueue after " + map.size());
        return null == onNotificationCallback;
    }
}
