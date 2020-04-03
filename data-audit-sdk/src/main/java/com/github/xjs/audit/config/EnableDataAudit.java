package com.github.xjs.audit.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DataAuditMarkerConfiguration.class)
public @interface EnableDataAudit {
}