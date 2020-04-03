/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.util;

import java.util.UUID;

/**
 * UUID帮助类
 *
 * @author 605162215@qq.com
 * @date 2019/12/6 9:36
 **/
public class UUIDUtil {

    /**
     * 生成UUID
     * */
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
