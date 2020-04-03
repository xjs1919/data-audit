/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.cache;
/**
 * 缓存key
 *
 * @author 605162215@qq.com
 * @date 2019/12/6 10:32
 **/
public interface CacheKey {

    String PREFIX = "techc:audit:";

    String TOKEN = PREFIX + "tk:";
    int TOKEN_EXPIRE = 3600;

    String TABLE_COLUMNS = PREFIX + "tblCols:";
    int TABLE_COLUMNS_EXPIRE  = 24*3600;

}
