package com.bigzhao.jsexe.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Roy on 15-8-15.
 */
public class Util {
    public static byte[] readStream(InputStream is){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] bs = new byte[2048];
            while (true) {
                int l = is.read(bs);
                if (l==-1) break;
                os.write(bs,0,l);
            }
            return os.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
