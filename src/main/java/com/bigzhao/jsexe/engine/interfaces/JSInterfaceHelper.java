package com.bigzhao.jsexe.engine.interfaces;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;

/**
 * Created by yangzhao.lyz on 2015/9/6.
 */
public class JSInterfaceHelper {
    public static HashMap<String,Object> exts=new HashMap<String, Object>();
    public static void registerJsInterfaceExt(String name,Object ext){
        exts.put(name, ext);
    }

    public static interface Func{Object f(Object[] args);}

    public static void registerJsInterfaceFunction(String name,Func ext){
        exts.put(name, ext);
    }
}
