package com.bigzhao.jsexe.engine.interfaces;

import org.apache.commons.io.FileUtils;

import java.io.*;

/**
 * Created by Roy on 15-8-28.
 */
public class JSInterfaceIO {

    public static class Config{
        public static InputStream in=System.in;
        public static PrintStream out=System.out;
        public static PrintStream err=System.err;
        public static IKeyValuePersistent kvp=new IKeyValuePersistent() {
            public void put(String key, String value) {
                try {
                    FileUtils.writeStringToFile(new File(key + ".txt"), value);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            public String get(String key) {
                try {
                    return FileUtils.readFileToString(new File(key+".txt"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return null;
            }
        };

    }

    public void print(String s){Config.out.print(s);}
    public void println(String s){Config.out.println(s);}
    public void println(){Config.out.println();}
    public void log(String s){println(s);}
    public void log(){println();}
    public void print(String format,Object...args){	Config.out.format(format.replace("%d", "%.0f"), args);}
    public void log(String format,Object...args){print(format, args);}

    public void save(String filename, Object text) {
        try {
            if (!filename.contains(":") && !filename.startsWith("/"))
                filename = System.getProperty("user.dir") + File.separatorChar + filename;
            File f = new File(filename);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            if (f.exists()) {
                FileUtils.writeStringToFile(f, text.toString());
                //L.j("Save to file:"+filename);
            } else throw new RuntimeException("Save to file \"" + filename + "\" failed.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key,String value){
        Config.kvp.put(key, value.toString());
    }

    public String get(String key){
        return Config.kvp.get(key);
    }
}
