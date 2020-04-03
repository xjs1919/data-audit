package com.github.xjs.audit.util;

import java.net.URLEncoder;

public class StringUtil {

    public static boolean isEmpty(String src){
        return src == null || src.length() <= 0;
    }

    public static byte[] toBytes(String src){
        return toBytes(src, "UTF-8");
    }

    public static byte[] toBytes(String src, String charset){
        try{
            return src.getBytes(charset);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static String toString(byte[] data){
        return toString(data, "UTF-8");
    }

    public static String toString(byte[] data, String charset){
        try{
            return new String(data, charset);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static int toInt(String src, int defValue){
        if(StringUtil.isEmpty(src)){
            return defValue;
        }
        try{
            return Integer.parseInt(src);
        }catch(Exception e){
            return defValue;
        }
    }

    public static double toDouble(String src, double defValue){
        if(StringUtil.isEmpty(src)){
            return defValue;
        }
        try{
            return Double.parseDouble(src);
        }catch(Exception e){
            return defValue;
        }
    }

    public static String concatUrl(String front, String end){
        if(isEmpty(front)){
            return end;
        }
        if(isEmpty(end)){
            return front;
        }
        if(front.endsWith("/")){
            front = front.substring(0, front.length()-1);
        }
        if(end.startsWith("/")){
            end = end.substring(1);
        }
        return front + "/" + end;
    }

    public static String concatParams(String front, String key, String value){
        if(StringUtil.isEmpty(key) || StringUtil.isEmpty(value)){
            return front;
        }
        return concatParams(front, key+"="+value);
    }

    public static String concatParams(String front, String params){
        if(isEmpty(front)){
            return null;
        }
        if(isEmpty(params)){
            return front;
        }
        if(front.indexOf("?")>0){
            return front + "&" + params;
        }else{
            return front + "?" + params;
        }
    }

    public static String urlEncode(String src){
        if(StringUtil.isEmpty(src)){
            return null;
        }
        try{
            return URLEncoder.encode(src, "UTF-8");
        }catch(Exception e){
            e.printStackTrace();
            return src;
        }
    }
}
