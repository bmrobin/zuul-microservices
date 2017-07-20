package com.bmrobin.zuul.filters.config

import com.bmrobin.zuul.filters.post.AddResponseHeaderFilter
import com.bmrobin.zuul.filters.pre.ServiceRequestFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FiltersConfig {

    @Bean
    fun serviceRequestFilter(): ServiceRequestFilter {
        return ServiceRequestFilter()
    }

    @Bean
    fun addResponseHeaderFilter(): AddResponseHeaderFilter {
        return AddResponseHeaderFilter()
    }

}
