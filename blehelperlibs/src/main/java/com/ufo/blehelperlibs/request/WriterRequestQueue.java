package com.ufo.blehelperlibs.request;

import android.util.Log;

import com.ufo.blehelperlibs.callback.OnWriteCallback;

import java.util.HashMap;

/**
 * Created by sz on 2016/9/1.
 * 描述:写数据请求队列
 */
public class WriterRequestQueue implements IRequestQueue<OnWriteCallback> {

    private static final String TAG = "WriterRequestQueue";
    HashMap<String, OnWriteCallback> map = new HashMap<>();

    @Override
    public void set(String key, OnWriteCallback callback) {
        map.put(key, callback);
    }

    @Override
    public OnWriteCallback get(String key) {
        return map.get(key);
    }

    /**
     * 移除一个元素
     *
     * @param key
     */
    public boolean removeRequest(String key) {
        Log.d(TAG, " WriterRequestQueue before:" + map.size());
        OnWriteCallback onWriteCallback = map.remove(key);
        Log.d(TAG, " WriterRequestQueue after:" + map.size());
        return null == onWriteCallback;
    }
}
