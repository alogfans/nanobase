package com.alogfans.nanobase.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Use the system built-in hashmap for maintaining key/value data
 * Extremely trivial.
 *
 * Created by Alogfans on 2015/10/10.
 */
public class HashMapEngine extends Engine {
    private Map<String, String> hashMap;

    public HashMapEngine() {
        hashMap = new HashMap<String, String>();
    }

    @Override
    public boolean containsKey(String key) {
        return hashMap.containsKey(key);
    }

    @Override
    public void put(String key, String value) {
        hashMap.put(key, value);
    }

    @Override
    public String get(String key) {
        return hashMap.get(key);
    }

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public void remove(String key) {
        hashMap.remove(key);
    }
}
