/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.mybatis;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SkipAudit {

    /**
     * 是否跳过审计
     * */
    @AliasFor("skip")
    boolean value() default true;

    /**
     * 是否跳过审计
     * */
    @AliasFor("value")
    boolean skip() default true;

}
