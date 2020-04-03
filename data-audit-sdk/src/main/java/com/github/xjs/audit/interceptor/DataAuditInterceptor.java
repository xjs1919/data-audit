/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.interceptor;

import com.github.xjs.audit.config.DataAudit;
import com.github.xjs.audit.config.DataAuditProperties;
import com.github.xjs.audit.log.IAuditLogService;
import com.github.xjs.audit.mybatis.MybatisDataChangeEventListener;
import com.github.xjs.audit.mybatis.dto.ChangeData;
import com.github.xjs.audit.mybatis.util.MybatisUtils;
import com.github.xjs.audit.threadlocal.AuditDataHolder;
import com.github.xjs.audit.user.AuditUserInfo;
import com.github.xjs.audit.user.IAuditUserService;
import com.github.xjs.audit.util.LogUtil;
import com.github.xjs.audit.util.StringUtil;
import com.github.xjs.audit.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 拦截器，拦截所有的请求
 *
 * @author 605162215@qq.com
 * @date 2019/12/4 16:27
 **/
@Slf4j
public class DataAuditInterceptor implements HandlerInterceptor{

    private static final String COOKIE_EVENT_ID = "audit_event_id";

    private IAuditUserService auditUserService;
    private DataAuditProperties properties;
    private IAuditLogService auditLogService;


    public DataAuditInterceptor(IAuditUserService auditUserService, DataAuditProperties properties, IAuditLogService auditLogService){
        this.auditUserService = auditUserService;
        this.properties = properties;
        this.auditLogService = auditLogService;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LogUtil.debug(log, "[DataAuditInterceptor][preHandle]start");
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        DataAudit dataAudit = handlerMethod.getMethodAnnotation(DataAudit.class);
        if(dataAudit == null){
            return true;
        }
        //获取登录的用户
        AuditUserInfo userInfo = auditUserService.getUserInfo(request);
        if(userInfo == null){
            LogUtil.debug(log,"[DataAuditInterceptor][preHandle] userInfo is null");
            return true;
        }else{
            LogUtil.debug(log,"[DataAuditInterceptor][preHandle] userInfo :{}", userInfo);
        }
        //存入TL
        AuditDataHolder.put(AuditDataHolder.APP_KEY, properties.getAppKey());
        AuditDataHolder.put(AuditDataHolder.USER_ID, userInfo.getUserId());
        AuditDataHolder.put(AuditDataHolder.USER_NAME, userInfo.getUserName());
        AuditDataHolder.put(AuditDataHolder.USER_NICK, userInfo.getUserNick());
        AuditDataHolder.put(AuditDataHolder.CLIENT_IP, WebUtil.getRemoteIP(request));
        AuditDataHolder.put(AuditDataHolder.EVENT_ID, dataAudit.eventId());
        AuditDataHolder.put(AuditDataHolder.EVENT_NAME, dataAudit.eventName());
        AuditDataHolder.put(AuditDataHolder.EVENT_ID_PARENT, WebUtil.getCookieValue(request, COOKIE_EVENT_ID));
        AuditDataHolder.put(AuditDataHolder.URI, request.getRequestURI());
        AuditDataHolder.put(AuditDataHolder.METHOD, request.getMethod().toUpperCase());
        AuditDataHolder.put(AuditDataHolder.CREATE_AT, System.currentTimeMillis());
        //设置event_id到cookie
        WebUtil.addCookie(response, COOKIE_EVENT_ID, ""+dataAudit.eventId(), 2 * 3600);
        LogUtil.debug(log,"[DataAuditInterceptor][preHandle] save data to TL:{}", AuditDataHolder.getAll());
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        LogUtil.debug(log,"[DataAuditInterceptor][afterCompletion] start");
        // 获取TL中的数据
        Map<String, Object> datas = AuditDataHolder.getAll();
        if(datas == null || datas.size() <= 1){
            LogUtil.debug(log, "[DataAuditInterceptor][afterCompletion] datas is null");
            return;
        }else{
            LogUtil.debug(log, "[DataAuditInterceptor][afterCompletion] datas:{}",datas);
        }
        //发送到kafka
        auditLogService.send(datas);
        // 删除所有的数据
        AuditDataHolder.deleteAll();
        LogUtil.debug(log,"[DataAuditInterceptor][afterCompletion] over");
    }
}
