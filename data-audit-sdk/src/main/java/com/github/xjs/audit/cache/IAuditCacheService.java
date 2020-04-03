/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.cache;
/**
 * 缓存服务
 * @author 605162215@qq.com
 * @date 2019/12/6 10:27
 **/
public interface IAuditCacheService {

    /**
     * 向缓存设置值<br>
     * @param key    key
     * @param value  value,如果是复杂类型可以转化成json再存
     * @param expireSeconds 过期时间的秒数
     * */
    public void set(String key, String value, int expireSeconds);

    /**
     * 从缓存取值<br>
     * @param key
     * @return
     * */
    public String get(String key);


}
