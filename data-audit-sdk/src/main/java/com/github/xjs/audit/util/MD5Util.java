/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.util;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * MD5 帮助类
 *
 * @author 605162215@qq.com
 * @date 2019/12/5 15:16
 **/
public class MD5Util {

    private MD5Util(){}

    public static String encode(String src){
        if(StringUtils.isEmpty(src)){
            return null;
        }
       return DigestUtils.md5DigestAsHex(StringUtil.toBytes(src));
    }



    public static void main(String[] args) {
        System.out.println(encode("123"));
    }

}
