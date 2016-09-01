package com.ufo.blehelperlibs.request;

/**
 * Created by sz on 2016/9/1.
 * 描述:请求队列
 */
public interface IRequestQueue<T> {

    void set(String key, T t);

    T get(String key);
}
