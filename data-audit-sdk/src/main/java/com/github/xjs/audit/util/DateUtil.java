package com.github.xjs.audit.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateUtil {

    public static final String FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    private static final Locale DEFAULT_LOCALE = Locale.CHINA;

    private static ThreadLocal<Map<String, SimpleDateFormat>> threadLocal = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        protected synchronized Map<String, SimpleDateFormat> initialValue() {
            Map<String, SimpleDateFormat> map = new HashMap<String, SimpleDateFormat>();
            map.put(FORMAT_YMDHMS, new SimpleDateFormat(FORMAT_YMDHMS, DEFAULT_LOCALE));
            return map;
        }
    };

    private DateUtil(){}

    public static SimpleDateFormat getDateFormat(String format) {
        Map<String, SimpleDateFormat> map = (Map<String, SimpleDateFormat>) threadLocal.get();
        SimpleDateFormat sdf = map.get(format);
        if(sdf != null){
            return sdf;
        }
        try{
            sdf = new SimpleDateFormat(format, DEFAULT_LOCALE);
            map.put(format, sdf);
        }catch(Exception e){
            e.printStackTrace();
        }
        return sdf;
    }

    public static Date parse(String textDate, String format) {
        if(textDate == null || textDate.length() <= 0){
            return null;
        }
        try{
            SimpleDateFormat sdf = getDateFormat(format);
            if(sdf == null){
                return null;
            }
            return sdf.parse(textDate);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String format(Date date, String format){
        if(date == null){
            return null;
        }
        SimpleDateFormat sdf = getDateFormat(format);
        if(sdf == null){
            return null;
        }
        return sdf.format(date);
    }


}
