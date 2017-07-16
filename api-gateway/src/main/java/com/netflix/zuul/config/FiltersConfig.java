package com.netflix.zuul.config;

import com.netflix.zuul.filters.pre.DebugRequestFilter;
import com.netflix.zuul.filters.pre.PreDecorationFilter;
import com.netflix.zuul.filters.route.SimpleHostRoutingFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiltersConfig {

//    @Bean
//    public SimpleHostRoutingFilter simpleHostRoutingFilter() {
//        return new SimpleHostRoutingFilter();
//    }

    @Bean
    public PreDecorationFilter preDecorationFilter() {
        return new PreDecorationFilter();
    }

    @Bean
    public DebugRequestFilter debugRequestFilter() {
        return new DebugRequestFilter();
    }

}