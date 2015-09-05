package com.bigzhao.jsexe.engine.interfaces;

import com.bigzhao.jsexe.engine.Engine;
import com.bigzhao.jsexe.engine.net.HttpHelper;

import java.nio.charset.Charset;

/**
 * Created by Roy on 15-9-4.
 */
public class JsExeUtil {
    public String getScopeId(){
        return Engine.getScopeToken();
    }
    public Long getThreadId(){
        return Thread.currentThread().getId();
    }
    public Long getScopeCreateTime(){
        return Engine.getScopeCreateTime();
    }
}
