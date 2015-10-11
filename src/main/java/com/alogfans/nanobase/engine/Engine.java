package com.alogfans.nanobase.engine;

/**
 * The base Engine class, exposed a lot of common functions
 * Created by Alogfans on 2015/10/10.
 */
public abstract class Engine {

    public abstract boolean containsKey(String key);

    public abstract void put(String key, String value);

    public abstract String get(String key);

    public abstract int size();

    public abstract void remove(String key);
}
