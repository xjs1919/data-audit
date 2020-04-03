/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.threadlocal;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.github.xjs.audit.mybatis.MybatisDataChangeEventListener;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ThreadLocal,存放单次请求的数据
 *
 * @author 605162215@qq.com
 * @date 2019/12/5 9:10
 **/
public class AuditDataHolder {

    public static final String APP_KEY = "appKey";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String USER_NICK = "userNick";
    public static final String CLIENT_IP = "clientIp";
    public static final String EVENT_ID = "eventId";
    public static final String EVENT_NAME = "eventName";
    public static final String EVENT_ID_PARENT = "eventIdParent";
    public static final String URI = "uri";
    public static final String METHOD = "method";
    public static final String CREATE_AT = "createAt";
    public static final String DETAILS = "details";

    /**
     * 保存ThreadLocal数据
     * */
    private static TransmittableThreadLocal<Map<String, Object>> HOLDER = new TransmittableThreadLocal<Map<String, Object>>(){
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<String, Object>();
        }
    };

    public static void addToDetails(List<MybatisDataChangeEventListener.Detail> changeDetails){
        if(changeDetails == null || changeDetails.size() <= 0){
            return;
        }
        Map<String, Object>  map = HOLDER.get();
        List<MybatisDataChangeEventListener.Detail> oldList = (List<MybatisDataChangeEventListener.Detail>)map.get(DETAILS);
        if(oldList == null){
            map.put(DETAILS, changeDetails);
        }else{
            oldList.addAll(changeDetails);
        }
    }

    /**
     * 向TL设置值
     * @param key
     * @param value
     * */
    public static void put(String key, Object value){
        if(StringUtils.isEmpty(key) || value == null){
            return;
        }
        Map<String, Object>  map = HOLDER.get();
        map.put(key, value);
    }

    /**
     * 从TL取值
     * @param key
     * @return
     * */
    public static Object get(String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        Map<String, Object>  map = HOLDER.get();
        return map.get(key);
    }

    /**
     * 从TL取所有的值
     * @return
     * */
    public static Map<String, Object> getAll(){
        return HOLDER.get();
    }

    /**
     * 从TL删除key
     * @return key对应的值
     * */
    public static Object delete(String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        Map<String, Object>  map = HOLDER.get();
        return map.remove(key);
    }

    /**
     * 从TL删除所有的key
     * */
    public static void deleteAll(){
        Map<String, Object>  map = HOLDER.get();
        map.clear();
    }

    /**
     * 删除TL,防止内存泄漏
     * */
    public static void remove(){
        HOLDER.remove();
    }

}
