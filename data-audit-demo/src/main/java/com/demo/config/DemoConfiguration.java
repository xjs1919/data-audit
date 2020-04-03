package com.demo.config;

import com.demo.controller.LoginController;
import com.demo.service.UserService;
import com.github.xjs.audit.cache.IAuditCacheService;
import com.github.xjs.audit.db.IAuditDataSourceService;
import com.github.xjs.audit.user.AuditUserInfo;
import com.github.xjs.audit.user.IAuditUserService;
import com.github.xjs.audit.util.StringUtil;
import com.github.xjs.audit.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class DemoConfiguration {

    @Bean
    public AuditDataSource auditDataSource(DataSource ds){
        return new AuditDataSource(ds);
    }

    @Bean
    public AuditCacheService cacheService(StringRedisTemplate stringRedisTemplate){
        return new AuditCacheService(stringRedisTemplate);
    }

    @Bean
    public IAuditUserService auditUserService(UserService userService){
        return new AuditUserService(userService);
    }

    public static class AuditUserService implements IAuditUserService{
        private UserService userService;
        public AuditUserService(UserService userService){
            this.userService = userService;
        }
        @Override
        public AuditUserInfo getUserInfo(HttpServletRequest request) {
            String tk = WebUtil.getCookieValue(request, UserService.COOKIE_NAME);
            if(StringUtils.isEmpty(tk)){
                tk = request.getParameter(UserService.COOKIE_NAME);
            }
            if(StringUtils.isEmpty(tk)){
                throw new RuntimeException("无法获取用户的token");
            }
            AuditUserInfo userInfo = userService.getByToken(tk);
            if(userInfo==null){
                throw new RuntimeException("token已经过期或已失效");
            }
            return userInfo;
        }
    }

    public static class AuditDataSource implements IAuditDataSourceService {
        private DataSource ds;
        public AuditDataSource(DataSource ds){
            this.ds = ds;
        }
        @Override
        public List<DataSource> getDataSources() {
            List<DataSource> list = new ArrayList<>();
            list.add(ds);
            return list;
        }
    }

    public static class AuditCacheService implements IAuditCacheService {
        private StringRedisTemplate stringRedisTemplate;
        public AuditCacheService(StringRedisTemplate stringRedisTemplate){
            this.stringRedisTemplate = stringRedisTemplate;
        }
        @Override
        public void set(String key, String value, int expireSeconds) {
            if(StringUtil.isEmpty(key) || StringUtil.isEmpty(value)){
                return;
            }
            stringRedisTemplate.boundValueOps(key).set(value, expireSeconds, TimeUnit.SECONDS);
        }
        @Override
        public String get(String key) {
            if(StringUtil.isEmpty(key)){
                return null;
            }
            return stringRedisTemplate.boundValueOps(key).get();
        }

    }
}
