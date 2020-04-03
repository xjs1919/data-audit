package com.demo.service;

import com.alibaba.fastjson.JSON;
import com.demo.config.DemoConfiguration;
import com.demo.controller.vo.req.LoginVo;
import com.github.xjs.audit.user.AuditUserInfo;
import com.github.xjs.audit.util.UUIDUtil;
import com.github.xjs.audit.util.WebUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService implements InitializingBean {

    public static final String COOKIE_NAME = "demo_tk";

    private HashMap<String, UserInfo> db = new HashMap<String, UserInfo>();

    @Autowired
    private DemoConfiguration.AuditCacheService cacheService;

    public void afterPropertiesSet() throws Exception{
        db.put("1", new UserInfo("1", "admin" ,"123456", "管理员"));
        db.put("2", new UserInfo("2", "xjs" ,"123456", "JoshuaXu"));
    }

    public AuditUserInfo getByToken(String tk) {
        if(StringUtils.isEmpty(tk)){
            return null;
        }
        String tkValue = cacheService.get(tk);
        if(StringUtils.isEmpty(tkValue)){
            return null;
        }
        return JSON.toJavaObject(JSON.parseObject(tkValue), AuditUserInfo.class);
    }

    public String login(HttpServletResponse response, LoginVo vo) {
        String username = vo.getUsername();
        String password = vo.getPassword();
        for(Map.Entry<String, UserInfo> entry : db.entrySet()){
            String id = entry.getKey();
            UserInfo user = entry.getValue();
            if(user.getUsername().equals(username) && user.getPassword().equals(password)){
                String tk = UUIDUtil.uuid();
                cacheService.set(tk, JSON.toJSONString(new AuditUserInfo(id, user.getUsername(), user.getNickname())), 7200);
                WebUtil.addCookie(response, COOKIE_NAME, tk, Integer.MAX_VALUE);
                return tk;
            }
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo{
        private String id;
        private String username;
        private String password;
        private String nickname;
    }
}
