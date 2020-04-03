/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.log;
/**
 * 日志服务
 *
 * @author 605162215@qq.com
 * @date 2019/12/9 14:10
 **/
public interface IAuditLogService {

    public <T> void send(T data);

}
