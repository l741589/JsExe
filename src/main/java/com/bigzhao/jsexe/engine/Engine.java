package com.bigzhao.jsexe.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigzhao.jsexe.engine.interfaces.JSInterface;
import com.bigzhao.jsexe.engine.interfaces.JSInterfaceHelper;
import org.apache.commons.io.FileUtils;
import org.apache.http.util.TextUtils;
import org.mozilla.javascript.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Roy on 15-4-27.
 */
public class Engine {

    public static enum ScopeStrategy{
        GLOBAL_SINGLE,
        THREAD_LOCAL,
        TOKEN_LOCAL,
    }

    public static ScopeStrategy scopeStrategy=ScopeStrategy.TOKEN_LOCAL;

    private static class Scope{
        static Scope instance;
        final Scriptable scope=context().newObject(topScope());
        final LinkedList<String> file=new LinkedList<String>();
        final HashMap<String,Object> loaded=new HashMap<String,Object>();
        final long createTime=System.currentTimeMillis();
        //final HashSet<String> loading=new HashSet<>();
        final JSInterface jsInterface;

        String token;
        Scope(String token){
            instance=this;
            this.token=token;
            //ScriptableObject.putProperty(topScope(),"$__jsexe_scope_token",token+"_"+System.currentTimeMillis());
            Scriptable ji=(Scriptable)Context.javaToJS(jsInterface = new JSInterface(), scope);
            ScriptableObject.putProperty(scope, "$", ji);
            JSInterfaceHelper.apply(jsInterface,ji);
        }

        public static Scope getInstance() {
            if (instance==null) return new Scope("$$$###@@@<<<<Def4u1t-t0keN>>>@@@###$$$");
            return instance;
        }
    }

    private static class JContext{
        Context context;
        Scope currentScope;

        public JContext(){
            try {
                context = Context.enter();
            }catch (Exception e){
                if (context==null){
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            try {
                Context.exit();
            }catch(Exception e){}
        }
    }
    
    private static ConcurrentHashMap<String, Scope> scopes = new ConcurrentHashMap<String, Scope>();
    private static ThreadLocal<JContext> jcontext = new ThreadLocal<JContext>();
    private static Scriptable topScope;

    private static JContext jc(){
        if (jcontext.get()==null) jcontext.set(new JContext());
        return jcontext.get();
    }

    public static String getScopeToken(){
        try {
            return jc().currentScope.token;
        }catch (Exception e){
            return null;
        }
    }

    public static Long getScopeCreateTime(){
        try {
            return jc().currentScope.createTime;
        }catch (Exception e){
            return null;
        }
    }

    private static Scriptable topScope() {
        if (topScope == null) topScope = context().initStandardObjects();
        return topScope;
    }

    public static Context context() {
        return jc().context;
    }
    public static Scriptable scope() {
        switch (scopeStrategy){
            case GLOBAL_SINGLE:return Scope.getInstance().scope;
            default:
                if (jc().currentScope==null) jc().currentScope=new Scope("$$$###@@@<<<<Def4u1t-t0keN>>>@@@###$$$");
                return jc().currentScope.scope;
        }
    }

    public static void deleteScope(String token){
        ScriptableObject.deleteProperty(topScope(),token);
        scopes.remove(token);
        jc().currentScope=null;
    }

    public static Scriptable scope(String token) {
        switch(scopeStrategy){
            case TOKEN_LOCAL: {
                Scope s = scopes.get(token);
                if (s == null) {
                    s = new Scope(token);
                    scopes.put(token, s);
                }
                jc().currentScope = s;
                return s.scope;
            }
            case THREAD_LOCAL: {
                Scriptable s = scope();
                jc().currentScope.token = token;
                return s;
            }
            case GLOBAL_SINGLE:{
                Scope s=Scope.getInstance();
                jc().currentScope=s;
                s.token=token;
                return s.scope;
            }
        }
        return null;
    }

    public static String genFilename(String filename){
        if (filename.contains(":")||filename.startsWith("/")) return filename;
        String f=new File(currentFilename()).getParent()+File.separatorChar+filename;
        if (new File(f).exists()) return f;
        return System.getProperty("user.dir")+File.separatorChar+filename;
    }

    public static Object execute(String scopeToken,String filename,Object...args){
        boolean fileAdded=false;
        try {
            filename=genFilename(filename);
            Scriptable scope=scope(scopeToken);
            //jc().currentScope.loading.add(filename);
            String code= FileUtils.readFileToString(new File(filename));
            jc().currentScope.file.add(filename);
            fileAdded=true;
            ScriptableObject.putProperty(scope, "arguments", newArray(args));
            return context().evaluateString(scope, code, filename, 1, null);
        } catch (IOException e) {
            deleteScope(scopeToken);
            throw new RuntimeException(e);
        }finally {
            if (fileAdded) jc().currentScope.file.removeLast();
            if (TextUtils.isEmpty(scopeToken)) deleteScope(scopeToken);
        }
    }

    public static String loadText(String filename){
        filename=genFilename(filename);
        try {
            return FileUtils.readFileToString(new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object load(String filename,Object...args){
        boolean fileAdded=false;
        try {
            filename=genFilename(filename);
            Scriptable scope=scope();
            if (jc().currentScope.loaded.containsKey(filename)) return jc().currentScope.loaded.get(filename);
            if (jc().currentScope.file.contains(filename)) throw new RuntimeException("cyclic dependency");
            jc().currentScope.file.add(filename);
            fileAdded=true;
            String code= FileUtils.readFileToString(new File(filename));
            ScriptableObject.putProperty(scope, "arguments", newArray(args));
            Object ret=context().evaluateString(scope, code, filename, 1, null);
            //jc().currentScope.loading.remove(filename);
            jc().currentScope.loaded.put(filename, ret);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if(fileAdded) jc().currentScope.file.removeLast();
        }
    }

    public static String currentFilename(){
        return jc().currentScope.file.getLast();
    }

    public static Object eval(String text){
        return context().evaluateString(scope(), text, "EVAL", 1, null);
    }

    public static void exit() {
        scopes=new ConcurrentHashMap<String, Scope>();
        jcontext=new ThreadLocal<JContext>();
        topScope=null;
    }

    public static Object call(String name,Object...args){
        return call(scope(),name,args);
    }


    public static Object call(Scriptable thisobj,String name,Object...args) {
        Object o = thisobj.get(name, thisobj);
        if (!(o instanceof Function)) return null;
        Function f = (Function) o;
        return call(thisobj, f, args);
    }

    public static Object call(Scriptable thisobj,Function f,Object...args){
        if (thisobj==null) thisobj=scope();
        for (int i=0;i<args.length;++i) args[i]=javaToJs(args[i]);
        return jsToJava(f.call(context(), scope(), thisobj, args));
    }



    public static NativeObject newObject(){
        return (NativeObject)context().newObject(scope());
    }

    public static NativeObject newObject(Scriptable scope){
        return (NativeObject)context().newObject(scope);
    }

    public static Function newFunction(String name,Scriptable obj,Method m){
        Function f=new NativeJavaMethod(m,m.getName());
        ScriptableObject.putProperty(obj,name,f);
        return f;
    }

    public static NativeArray newArray(int length){
        return (NativeArray)context().newArray(scope(), length);
    }

    public static NativeArray newArray(Object[] elements){
        Object[] arr=new Object[elements.length];
        for (int i=0;i<elements.length;++i) arr[i]=elements[i] instanceof Scriptable?elements[i]:Context.javaToJS(elements[i],scope());
        return (NativeArray)context().newArray(scope(),arr);
    }

    public static Object javaToJs(Object o){
        if (o==null) return null;
        if (o instanceof Iterable){
            Iterable<Object> json=(Iterable<Object>)o;
            ArrayList<Object> arr=new ArrayList<Object>();
            for(Object e:json) arr.add(Engine.javaToJs(e));
            return Engine.newArray(arr.toArray());
        }else if (o.getClass().isArray()){
            Object[] arr=(Object[])o;
            for (int i=0;i<arr.length;++i) arr[i]=Engine.javaToJs(arr[i]);
            return Engine.newArray(arr);
        }else if (o instanceof Map){
            Map<String,Object> json=(Map<String,Object>)o;
            NativeObject obj=(NativeObject)Engine.newObject();
            for (Map.Entry<String, Object> e:json.entrySet()){
                ScriptableObject.putProperty(obj, e.getKey(), javaToJs(e.getValue()));
            }
            return obj;
        }
        o= JSON.toJSON(o);
        if (o instanceof JSON){
            return javaToJs(o);
        }else{
            return Context.javaToJS(o,scope());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T jsToJava(Object obj,Class<T> cls){
        if (obj==null) return null;
        Object json=jsToJava(obj);
        if (json instanceof JSON) {
            if (cls.isArray()) {
                JSONArray a = (JSONArray) json;
                Class<?> t = cls.getComponentType();
                T out = (T) Array.newInstance(t, a.size());
                for (int i = 0; i < a.size(); ++i) {
                    Object e = jsToJava(a.get(i), t);
                    Array.set(out, i, e);
                }
                return out;
            } else return JSON.toJavaObject((JSON) json, cls);
        }
        return (T)json;
    }

    public static Object jsToJava(Object o){
        if (o==null) return null;
        if (o instanceof NativeObject){
            NativeObject obj=(NativeObject)o;
            Object[] ids=obj.getAllIds();
            JSONObject json=new JSONObject();
            for (Object id:ids){
                Object val=ScriptableObject.getProperty(obj, id.toString());
                json.put(id.toString(), jsToJava(val));
            }
            return json;
        }else if (o instanceof NativeArray){
            NativeArray obj=(NativeArray)o;
            JSONArray json=new JSONArray();
            for (Object e:obj){
                json.add(jsToJava(e));
            }
            return json;
        }else if (o instanceof NativeJavaObject){
            return ((NativeJavaObject)o).unwrap();
        }else if (o instanceof ConsString){
            return ((ConsString)o).toString();
        }
        return o;
    }

    private void debug(){


    }
}
