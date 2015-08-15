package com.bigzhao.jsexe.engine.net;

import com.alibaba.fastjson.JSONObject;
import com.bigzhao.jsexe.engine.Engine;
import com.bigzhao.jsexe.util.L;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.mozilla.javascript.NativeObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Map;

public class ZHttpPost extends HttpPost {
	
	public ZHttpPost(String s) {
		super(s);
        if (HttpHelper.proxy!=null) {
            RequestConfig cfg=RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(10000).setProxy(HttpHelper.proxy).build();
            setConfig(cfg);
        }
       /* HttpHost hh=new HttpHost("localhost",8888,"http");
        RequestConfig cfg=RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(10000).setProxy(hh).build();
        setConfig(cfg);
        this.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");*/
	}
	
	public ZHttpPost body(Object data){
		try {
			setEntity(new StringEntity(Engine.jsToJava(data).toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public ZHttpPost form(Object data){
		Object o=Engine.jsToJava(data);
		if (o instanceof JSONObject){
			JSONObject json=(JSONObject)o;
			LinkedList<NameValuePair> pairs=new LinkedList<>();			
			for (Map.Entry<String, Object> p:json.entrySet()){
				pairs.add(new BasicNameValuePair(p.getKey(), 
						p.getValue()==null?null:p.getValue().toString()));
			}
			try {
				UrlEncodedFormEntity form=new UrlEncodedFormEntity(pairs);
				setEntity(form);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			body(data);
		}
		return this;
	}
	
	public ZResponse exec(){
		try {
            L.j("POST: " + this.getURI());
            return new ZResponse(HttpHelper.exec(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ZHttpPost header(NativeObject obj){
        if (obj==null) return this;
		Object[] ids=obj.getAllIds();
		for (Object id:ids){
			Object value=obj.get(id);
            if (value==null) removeHeaders(id.toString());
			else addHeader(id.toString(), value.toString());
		}
		return this;
	}
	
	public ZHttpPost header(String name,String value){
		addHeader(name, value);
		return this;
	}

    public ZHttpPost cookie(NativeObject obj){
        if (obj==null) return this;
        CookieStore cs=HttpHelper.getContext().getCookieStore();
        for (NativeObject.Entry<Object,Object> entry:obj.entrySet()){
            cs.addCookie(new BasicClientCookie(entry.getKey().toString(),entry.getValue().toString()));
        }
        return this;
    }

    public ZHttpPost cookie(String name,String value){
        HttpHelper.getContext().getCookieStore().addCookie(new BasicClientCookie(name,value));
        return this;
    }
}
