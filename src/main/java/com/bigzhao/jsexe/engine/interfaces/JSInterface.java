package com.bigzhao.jsexe.engine.interfaces;

import com.bigzhao.jsexe.engine.Engine;
import com.bigzhao.jsexe.engine.media.MediaHelper;
import com.bigzhao.jsexe.engine.net.HttpHelper;
import com.bigzhao.jsexe.engine.net.ZResponse;
import org.apache.commons.io.FileUtils;


import java.io.*;

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
    @Deprecated
	public void print(String s){io.print(s);}
    @Deprecated
	public void println(String s){io.println(s);}
    @Deprecated
	public void println(){io.println();}
    @Deprecated
	public void log(String s){io.log(s);}
    @Deprecated
	public void log(){io.log();}
    @Deprecated
	public void print(String format,Object...args){	io.print(format, args);}
    @Deprecated
	public void log(String format,Object...args){io.log(format, args);}

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

    @Deprecated
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }


    @Deprecated
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
    public JSInterfaceIO io=new JSInterfaceIO();
    public static Object util=new JSInterfaceUtil();

}
