/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WebUtil
 * @author 605162215@qq.com
 * @date 2019/12/6 11:14
 **/
@Slf4j
public class WebUtil {

    public static String getRemoteIP(HttpServletRequest request) {
        String formProxy = getFromProxy(request);
        if(!StringUtil.isEmpty(formProxy)){
            String[] ips = formProxy.split(",");
            for (int index = 0; index < ips.length; index++) {
                String ip = ips[index];
                if (!StringUtil.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                    return ip;
                }
            }
        }
        String fromRealIp = request.getHeader("X-Real-IP");
        if(!StringUtil.isEmpty(fromRealIp) && !"unknown".equalsIgnoreCase(fromRealIp)){
            return fromRealIp;
        }
        return request.getRemoteAddr();
    }

    private static String getFromProxy(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
        }
        return ip;
    }

    public static void addCookie(HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath("/");
        if (cookieMaxAge <= 0) {
            cookie.setMaxAge(0);
        } else {
            cookie.setMaxAge(cookieMaxAge);
        }
        //cookie.setHttpOnly(true);
        //cookie.setSecure(true);
        response.addCookie(cookie);
    }

    public static String getCookieValue(HttpServletRequest request, String cookieName){
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if(cookie != null){
            return cookie.getValue();
        }
        return null;
    }
}
