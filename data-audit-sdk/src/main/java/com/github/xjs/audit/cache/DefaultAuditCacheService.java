/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.github.xjs.audit.util.StringUtil;

import java.util.concurrent.TimeUnit;

/**
 * 默认的缓存服务，使用Guava实现的本机内存缓存
 *
 * @author 605162215@qq.com
 * @date 2019/12/17 9:44
 **/
public class DefaultAuditCacheService implements IAuditCacheService {

    /**
     * SDK用到的最大过期时间是24小时
     * */
    private static final int MAX_EXPIRE_SECONDS = 24 * 3600;

    @Override
    public void set(String key, String value, int expireSeconds) {
        if(StringUtil.isEmpty(key) || value == null){
            return;
        }
        if(expireSeconds <= 0 || expireSeconds >= MAX_EXPIRE_SECONDS){
            expireSeconds = MAX_EXPIRE_SECONDS;
        }
        Cache<String,String> cache = CacheFactory.getCache();
        //把过期时间写入到value中
        String valueString = (System.currentTimeMillis() + expireSeconds * 1000)+":"+value;
        cache.put(key, valueString);
    }

    @Override
    public String get(String key) {
        Cache<String,String> cache = CacheFactory.getCache();
        String value = cache.getIfPresent(key);
        if(StringUtil.isEmpty(value)){
            return null;
        }
        //从value中解析出过期时间来
        int colon = value.indexOf(":");
        if(colon <= 0){
            return null;
        }
        long expireAt = Long.parseLong(value.substring(0, colon));
        long now = System.currentTimeMillis();
        if(expireAt > now){
            return value.substring(colon+1);
        }
        //如果已经过期，删除并返回null
        cache.invalidate(key);
        return null;
    }

    /**
     * Guava实现的内存缓存
     * */
    public static class CacheFactory{

        private CacheFactory(){}

        private static volatile Cache<String,String> cache;

        public static Cache<String,String> getCache(){
            if(cache == null){
                synchronized (CacheFactory.class){
                    if(cache == null){
                        cache = CacheBuilder.newBuilder()
                                .expireAfterAccess(MAX_EXPIRE_SECONDS, TimeUnit.SECONDS)
                                .build();
                    }
                }
            }
            return cache;
        }
    }
}
