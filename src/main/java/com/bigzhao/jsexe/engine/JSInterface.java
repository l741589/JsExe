package com.bigzhao.jsexe.engine;

import com.bigzhao.jsexe.engine.media.MediaHelper;
import com.bigzhao.jsexe.engine.net.HttpHelper;
import com.bigzhao.jsexe.engine.net.ZResponse;
import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.ast.Scope;


import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;

public class JSInterface {


    public Object load(String filename,Object...args){
        return Engine.load(filename, args);
    }
    public String loadText(String filename){
      return Engine.loadText(filename);
    }
    public Object loadJson(String filename){
        return Engine.eval("("+loadText(filename)+")");
    }
	public void print(String s){System.out.print(s);}
	public void println(String s){System.out.println(s);}
	public void println(){System.out.println();}
	public void log(String s){println(s);}
	public void log(){println();}	
	public void print(String format,Object...args){	System.out.format(format.replace("%d", "%.0f"), args);}	
	public void log(String format,Object...args){print(format,args);}
    public String readKey(){
        String s=cmd("readkey pr");
        return s;
    }
	
	public String format(String format,Object...args){
		return String.format(format.replace("%d", "%.0f"), args);
	}

    public void printZipData(String filename,String ouf){
        try {
            new FileOutputStream(ouf).write(ZResponse.decompress(new FileInputStream(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printZipData(String filename){
        try {
            System.out.println(new String(ZResponse.decompress(new FileInputStream(filename))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String env(String name){
        return System.getenv(name);
    }

    public String sysprop(String name){
        return System.getProperty(name);
    }

    public String sysprop(String name,String value){
        return System.setProperty(name,value);
    }

    public static String cmd(String commandStr) {
        try {
            Process p = Runtime.getRuntime().exec(commandStr);
            int val=p.waitFor();
            BufferedReader is=new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb=new StringBuilder();
            while (true){
                String s=is.readLine();
                if (s==null) break;
                System.out.println(s);
                sb.append(s).append("\r\n");
            }
            return sb.toString();
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(String filename,Object text){
        try {
            if (!filename.contains(":")&&!filename.startsWith("/")) filename=System.getProperty("user.dir")+ File.separatorChar+filename;
            File f=new File(filename);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            if (f.exists()) {
                FileUtils.writeStringToFile(f, text.toString());
                //L.j("Save to file:"+filename);
            }
            else throw new RuntimeException("Save to file \""+filename+"\" failed.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpHelper http=new HttpHelper();
    public MediaHelper media=new MediaHelper();

    public static Object util=new Util();

    public static class Util{
        public static String md5(String s) {
            char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
            try {
                byte[] btInput = s.getBytes(Charset.forName("utf-8"));
                MessageDigest mdInst = MessageDigest.getInstance("MD5");
                mdInst.update(btInput);
                byte[] md = mdInst.digest();
                int j = md.length;
                char str[] = new char[j * 2];
                int k = 0;
                for (int i = 0; i < j; i++) {
                    byte byte0 = md[i];
                    str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                    str[k++] = hexDigits[byte0 & 0xf];
                }
                return new String(str);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
