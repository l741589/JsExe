package com.bigzhao.jsexe.engine.interfaces;

/**
 * Created by Roy on 15-8-28.
 */
public interface IKeyValuePersistent {
    void put(String key, String value);
    String get(String key);
}
