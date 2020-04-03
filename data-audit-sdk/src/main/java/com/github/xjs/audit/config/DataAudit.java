package com.github.xjs.audit.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataAudit {

    String eventId() default "";

    String eventName() default "";

}