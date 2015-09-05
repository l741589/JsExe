package com.bigzhao.jsexe.engine.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bigzhao.jsexe.engine.Engine;
import com.bigzhao.jsexe.util.Util;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.mozilla.javascript.*;
import org.mozilla.javascript.json.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

public class ZResponse {
	
	private HttpResponse res;
    private byte[] data;

    public HttpResponse getResponse() {
        return res;
    }

    public ZResponse(HttpResponse response) {
		res=response;
	}

    public Object body(String encoding){
        return $body(encoding);
    }

    public Object body(){
        return $body();
    }
	public String $body(String encoding){
		try {
			return new String($data(),encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    public String $body(){
        try {
            return new String($data());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] $data(){
        try {
            if (data!=null) return data;
            return data=EntityUtils.toByteArray(res.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream $stream(){
        try {
            return res.getEntity().getContent();
        } catch (IOException e) {
            return null;
        }
    }

    public ZResponse skip(Object obj){
        int start=0;
        if (obj instanceof Number){
            start=((Number)obj).intValue();
        }else if (obj instanceof NativeFunction){
            return skip(((NativeFunction)obj).call(Engine.context(),Engine.scope(),Engine.scope(),null));
        }else if (obj instanceof Function){
            return skip(Engine.call((Scriptable)null, (Function) obj, (Object) $data()));
        }
        data= Arrays.copyOfRange($data(),start,$data().length);
        return this;
    }

	
	public Object json(String encoding){
		//JSONObject obj=JSON.parseObject($body(encoding));
        try {
            return (new JsonParser(Engine.context(), Engine.scope())).parseValue($body(encoding));
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject obj=JSON.parseObject($body());
            return Engine.javaToJs(obj);
        }
	}
	
	public Object json(){
        try {
            return (new JsonParser(Engine.context(), Engine.scope())).parseValue($body());
        }catch (Exception e){
            e.printStackTrace();
            JSONObject obj=JSON.parseObject($body());
            return Engine.javaToJs(obj);
        }
	}
	
	public ZDocument html(String encoding){
		return new ZDocument(Jsoup.parse($body(encoding)));
	}

	public ZDocument html(){
		return new ZDocument(Jsoup.parse($body()));
	}
	
	private ScriptableObject headers;
	public ScriptableObject header(){
		if (headers!=null) return headers;
		headers=Engine.newObject();
		Header[] hs=res.getAllHeaders();
		for (Header h:hs){
			ScriptableObject.putProperty(headers, h.getName(), h.getValue());
		}
		return headers;
	}

	public Object header(String name){
		ScriptableObject hs=header();
		return hs.get(name);
	}



    public byte[] $unzip(){
        try {
            return decompress($data());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ZResponse unzip(){
        data=$unzip();
        return this;
    }

    public static byte[] decompress(InputStream is) throws IOException {
        InflaterInputStream iis = new InflaterInputStream(is);
        return Util.readStream(iis);
    }


    public static byte[] decompress(byte[] data) throws IOException {
        return decompress(new ByteArrayInputStream(data));
    }

    @Override
    public String toString() {
        return $body();
    }
}
