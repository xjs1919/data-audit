/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo;

import com.github.xjs.audit.config.EnableDataAudit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 入口类
 *
 * @author 605162215@qq.com
 * @date 2019/12/4 17:11
 **/
@SpringBootApplication
@EnableDataAudit
public class AuditDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditDemoApplication.class, args);
    }
}
