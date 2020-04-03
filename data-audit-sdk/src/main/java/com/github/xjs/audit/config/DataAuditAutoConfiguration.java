/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.config;

import com.github.xjs.audit.cache.DefaultAuditCacheService;
import com.github.xjs.audit.cache.IAuditCacheService;
import com.github.xjs.audit.db.IAuditDataSourceService;
import com.github.xjs.audit.interceptor.DataAuditInterceptor;
import com.github.xjs.audit.log.DefaultAuditLogService;
import com.github.xjs.audit.log.IAuditLogService;
import com.github.xjs.audit.log.KafkaAuditLog4j2Service;
import com.github.xjs.audit.log.KafkaAuditLogbackService;
import com.github.xjs.audit.mybatis.MybatisDataChangeEventListener;
import com.github.xjs.audit.mybatis.MybatisDataChangeInterceptor;
import com.github.xjs.audit.mybatis.OnDataChangeEventListener;
import com.github.xjs.audit.user.IAuditUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Properties;

/**
 *  配置类
 *
 * @author 605162215@qq.com
 * @date 2019/12/4 16:19
 **/
@Slf4j
@Configuration
@ConditionalOnBean(DataAuditMarkerConfiguration.Marker.class)
@EnableConfigurationProperties(DataAuditProperties.class)
public class DataAuditAutoConfiguration implements WebMvcConfigurer {

    /**配置*/
    @Autowired
    private DataAuditProperties properties;

    /**数据源*/
    @Autowired
    private IAuditDataSourceService dataSourceService;

    /**缓存服务*/
    @Autowired
    private IAuditCacheService auditCacheService;

    /**用户服务*/
    @Autowired
    private IAuditUserService auditUserService;

    /**日志服务*/
    @Autowired
    private IAuditLogService auditLogService;

    /**
     * 拦截器
     * */
    @Bean
    public DataAuditInterceptor actionAuditInterceptor(){
        return new DataAuditInterceptor(auditUserService, properties, auditLogService);
    }

    /**
     * 注册拦截器,拦截所有的请求
     *
     * @param registry registry interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(actionAuditInterceptor()).addPathPatterns("/**");
    }

    /**
     * 缓存服务
     * */
    @Bean
    @ConditionalOnMissingBean(IAuditCacheService.class)
    public IAuditCacheService auditCacheService(){
        return new DefaultAuditCacheService();
    }

    /**
     * 把日志输出到kafka,使用logback
     * */
    @Bean
    @ConditionalOnProperty("audit.kafka.bootstrap-servers")
    @ConditionalOnClass(name = "ch.qos.logback.classic.LoggerContext")
    public IAuditLogService auditLogbackService(){
        return new KafkaAuditLogbackService(properties);
    }

    /**
     * 把日志输出到kafka，使用log4j2
     * */
    @Bean
    @ConditionalOnProperty("audit.kafka.bootstrap-servers")
    @ConditionalOnClass(name = "org.apache.logging.log4j.core.LoggerContext")
    public IAuditLogService auditLog4j2Service(){
        return new KafkaAuditLog4j2Service(properties);
    }

    /**
     * 默认的日志服务
     * */
    @Bean
    @ConditionalOnMissingBean(IAuditLogService.class)
    public IAuditLogService defaultAuditLogService(){
        return new DefaultAuditLogService();
    }

    /**
     * mybatis拦截器
     * */
    @Bean
    public MybatisDataChangeInterceptor mybatisDataChangeInterceptor(){
        MybatisDataChangeInterceptor interceptor = new MybatisDataChangeInterceptor(onMybatisDataChangeEventListener());
        Properties properties = new Properties();
        interceptor.setProperties(properties);
        return interceptor;
    }
    /**
     * mybatis数据变化监听
     * */
    @Bean
    public OnDataChangeEventListener onMybatisDataChangeEventListener(){
        return new MybatisDataChangeEventListener();
    }

}
