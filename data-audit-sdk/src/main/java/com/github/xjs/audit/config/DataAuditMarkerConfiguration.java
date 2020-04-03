package com.github.xjs.audit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataAuditMarkerConfiguration {

    @Bean
    public Marker actionAuditMarker(){
        return new Marker();
    }

    public class Marker{
    }

}
