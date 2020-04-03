/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的日志服务
 *
 * @author 605162215@qq.com
 * @date 2019/12/9 14:19
 **/
@Slf4j
public class DefaultAuditLogService implements IAuditLogService{
    @Override
    public <T> void send(T data) {
        if(data == null){
            return;
        }
        String src = JSON.toJSONString(data);
        log.info(src);
    }
}
