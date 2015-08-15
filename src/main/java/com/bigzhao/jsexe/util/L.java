package com.bigzhao.jsexe.util;

import com.alibaba.fastjson.JSON;

/**
 * Created by Roy on 15-4-27.
 */
public class L {
    private static boolean canLog=true;
    public static void j(Object entity){
        if (!canLog) return;
        if (entity==null) System.out.println("null");
        else if (entity instanceof String){
            System.out.println(entity);
        }else {
            System.out.println(JSON.toJSONString(entity));
        }
    }
}
