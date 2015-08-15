package com.bigzhao.jsexe;

import com.bigzhao.jsexe.engine.Engine;
import com.bigzhao.jsexe.nashorn.NashornEngine;

import java.util.Arrays;

/**
 * Created by Roy on 15-4-27.
 */
public class Main {
    public static void main(String[] args){
        try {
            if (args.length < 1) {
                System.out.println("no enough parameters!");
                return;
            }
            String filename = args[0];
            String token = args.length > 1 ? args[1] : "";
            if (args.length <= 2) {
                Engine.execute(token, filename);
            } else {
                args = Arrays.copyOfRange(args, 2, args.length);
                Engine.execute(token, filename, (Object[]) args);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("ERROR");
            System.exit(1);
        }
    }
}
