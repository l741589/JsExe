package com.bigzhao.jsexe.engine.net;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bigzhao.jsexe.util.L;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.mozilla.javascript.NativeObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


public class ZHttpGet extends HttpGet {

    private static String genUrl(String s,Object data){
        if (data==null) return s;
        Map<String,Object> map;
        if (data instanceof Map) map=(Map<String,Object>)data;
        else map= (JSONObject)JSON.toJSON(data);
        boolean start=true;
        StringBuilder sb=new StringBuilder();
        for (Map.Entry<String,Object> e:map.entrySet()){
            if (start){
                start=false;
                sb.append("?");
            }else sb.append("&");
            sb.append(e.getKey());
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(e.getValue().toString(),"utf-8"));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
        return s+sb.toString();
    }

    public ZHttpGet(String s,Object data) {
        super(genUrl(s,data));
        setConfig(HttpHelper.getContext().getRequestConfig());
        if (HttpHelper.proxy!=null) {
            RequestConfig cfg=RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(10000).setProxy(HttpHelper.proxy).build();
            setConfig(cfg);
        }
    }

	public ZHttpGet(String s) {
		this(s, null);
	}

	public ZResponse exec(){
		try {
            L.j("GET: " + this.getURI());
            //setConfig(HttpHelper.getContext().getRequestConfig());
            ZResponse res=new ZResponse(HttpHelper.exec(this));
            return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ZHttpGet header(NativeObject obj){
        if (obj==null) return this;
		Object[] ids=obj.getAllIds();
		for (Object id:ids){
			Object value=obj.get(id);
            if (value==null) removeHeaders(id.toString());
            else addHeader(id.toString(), value.toString());
		}
		return this;
	}

    public ZHttpGet cookie(NativeObject obj){
        if (obj==null) return this;
        CookieStore cs=HttpHelper.getContext().getCookieStore();
        for (NativeObject.Entry<Object,Object> entry:obj.entrySet()){
            cs.addCookie(new BasicClientCookie(entry.getKey().toString(),entry.getValue().toString()));
        }
        return this;
    }
    public ZHttpGet cookie(String name,String value){
        HttpHelper.getContext().getCookieStore().addCookie(new BasicClientCookie(name,value));
        return this;
    }
}
