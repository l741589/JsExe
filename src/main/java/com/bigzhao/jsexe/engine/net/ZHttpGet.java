package com.bigzhao.jsexe.engine.net;


import com.bigzhao.jsexe.util.L;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.mozilla.javascript.NativeObject;



public class ZHttpGet extends HttpGet {

	public ZHttpGet(String s) {
		super(s);
        setConfig(HttpHelper.getContext().getRequestConfig());
        if (HttpHelper.proxy!=null) {
            RequestConfig cfg=RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(10000).setProxy(HttpHelper.proxy).build();
            setConfig(cfg);
        }
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
