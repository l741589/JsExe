package com.bigzhao.jsexe.engine.net;

import com.bigzhao.jsexe.engine.Engine;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.*;
import org.apache.http.protocol.HttpContext;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Roy on 15-4-27.
 */
public class HttpHelper {

    public static HttpHost proxy;

    private static ConcurrentHashMap<String,HttpClientContext> contexts = new ConcurrentHashMap<String,HttpClientContext>();

    private static CloseableHttpClient client;

    public static HttpClient getClient(){
        if (client==null) {
            client=HttpClientBuilder.create().build();
        }
        return client;
    }

    public static void setClient(CloseableHttpClient client) {
        HttpHelper.client = client;
    }

    public static HttpResponse exec(HttpUriRequest request){
        try {
            HttpClient c=getClient();
            Field f=c.getClass().getDeclaredField("cookieStore");
            f.setAccessible(true);
            f.set(c,null);
            return c.execute(request, getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HttpClientContext getContext(){
        String token=Engine.getScopeToken();
        Scriptable scope= Engine.scope(token);
        HttpClientContext cx=contexts.get(token);
        if (cx==null) {
            cx=HttpClientContext.create();
            cx.setCookieStore(new BasicCookieStore());
        }
        contexts.put(token,cx);
        return cx;

    }

    public static void reset(){
        contexts=new ConcurrentHashMap<String, HttpClientContext>();
    }

    public void proxy(String host,int port){
        if (host==null||port==0) HttpHelper.proxy=null;
        else HttpHelper.proxy=new HttpHost(host,port,"http");
    }

    public static void setCookie(String name,String value){
        setCookie(name, value, null, null, null, false);
    }
    public static void setCookie(String name,String value,Date expires,String domain,String path){
        setCookie(name, value, expires, path, domain, false);
    }
    public static void setCookie(String name,String value,Date expires,String path,String domain,boolean secure){
        CookieStore cs=getContext().getCookieStore();
        if (cs==null){
            cs=new BasicCookieStore();
            getContext().setCookieStore(cs);
        }
        BasicClientCookie cookie=new BasicClientCookie(name,value);
        cookie.setSecure(secure);
        cookie.setExpiryDate(expires);
        cookie.setDomain(domain);
        cookie.setPath(path);
        //cookie.setVersion(1);
        cs.addCookie(cookie);
    }
    public ScriptableObject cookie(){
        ScriptableObject cookies=Engine.newObject();
        List<Cookie> cc=HttpHelper.getContext().getCookieStore().getCookies();
        for (Cookie c:cc){
            ScriptableObject.putProperty(cookies, c.getName(), c.getValue());
        }
        return cookies;
    }

    public String cookie(String name){
        ScriptableObject hs=cookie();
        Object o=hs.get(name);
        if (o==null) return null;
        return o.toString();
    }
    public void delCookie(String name){
        List<Cookie> cc=HttpHelper.getContext().getCookieStore().getCookies();
        for (Cookie c:cc){
            if (c.getName().equals("name")){
                setCookie(c.getName(),c.getValue(),new Date(),c.getDomain(),c.getPath(),c.isSecure());
            }
        }
    }
    public static void disableCookie2(){
        Registry<CookieSpecProvider> registry = RegistryBuilder
                .<CookieSpecProvider> create()
                .register(CookieSpecs.BEST_MATCH, new NetscapeDraftSpecFactory()).build();

        getContext().setCookieSpecRegistry(registry);
    }


    public static ZHttpGet get(String url){return new ZHttpGet(url);}
    public static ZHttpGet get(String url,Object data){return new ZHttpGet(url,data);}
    public static ZHttpPost post(String url){	return new ZHttpPost(url);}
    public static ZHttpPost post(String url,Object data){	return new ZHttpPost(url).form(data);}

    public static void ajax(NativeObject obj){
        try {
            ZResponse res = req(obj);
            if (res.getResponse().getStatusLine().getStatusCode()<400){
                Engine.call(obj, "success", res.body());
            }else{
                StatusLine sl=res.getResponse().getStatusLine();
                Engine.call(obj,"error",sl.getStatusCode(),sl.toString());
            }
        }catch (Exception e){
            Engine.call(obj, "error",e);
        }
    }

    public static ZResponse req(NativeObject obj){
        String url=obj.get("url").toString();
        String type=obj.get("type")!=null?obj.get("type").toString():"GET";
        Object _cookie=obj.get("cookie");
        Object _header=obj.get("header");
        NativeObject cookie=_cookie instanceof NativeObject?(NativeObject)_cookie:null;
        NativeObject header=_header instanceof NativeObject?(NativeObject)_header:null;
        Object data= Engine.jsToJava(obj.get("data"));
        ZResponse res=null;
        if ("GET".equalsIgnoreCase(type)){
            res=get(url,data).cookie(cookie).header(header).exec();
        }else{
            res=post(url).cookie(cookie).header(header).body(data.toString()).exec();
        }
        return res;
    }

}
