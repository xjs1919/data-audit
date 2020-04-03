/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.util;

import lombok.extern.slf4j.Slf4j;

/**
 * DB Util
 *
 * @author 605162215@qq.com
 * @date 2019/12/12 19:22
 **/
@Slf4j
public class DBUtil {

    public static void close(AutoCloseable... closeables){
        if(closeables == null || closeables.length <= 0){
            return;
        }
        for(AutoCloseable closeable : closeables){
            try{
                if(closeable != null){
                    closeable.close();
                }
            }catch(Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 从连接的url中获取数据库的名字
     * jdbc:mysql://192.168.1.26:3306/demo?allowMultiQueries=true
     *
     * @param url 连接字符串
     * */
    public static String getDbNameFromUrl(String url){
        if(StringUtil.isEmpty(url)){
            return null;
        }
        //先去掉末尾的参数
        int ask = url.indexOf("?");
        if(ask > 0){
            url = url.substring(0, ask);
        }
        if(url.endsWith("/")){
            url = url.substring(0, url.length()-1);
        }
        int lastSlash = url.lastIndexOf("/");
        return url.substring(lastSlash+1);

    }

}
