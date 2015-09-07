package com.bigzhao.jsexe.engine.interfaces;

import com.bigzhao.jsexe.engine.Engine;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangzhao.lyz on 2015/9/6.
 */
public class JSInterfaceHelper {
    private static HashMap<String,Object> exts=new HashMap<String, Object>();
    public static void registerJsInterfaceExt(String name,Object ext){
        exts.put(name, ext);
    }

    public static void registerJsInterfaceFunction(String name,Method ext){
        exts.put(name, ext);
    }

    public static void apply(JSInterface ji,Scriptable jsji){
        ji.ext=Engine.newObject(jsji);

        for (Map.Entry<String,Object> e: JSInterfaceHelper.exts.entrySet()){
            Scriptable jsext=(Scriptable) jsji.get("ext", jsji);
            if (e.getValue() instanceof Method){
                Engine.newFunction(e.getKey(),jsext , (Method) e.getValue());
            }else {
                ScriptableObject.putProperty(ji.ext,e.getKey(),Context.javaToJS(e.getValue(), jsext));
            }
        }
    }
}
