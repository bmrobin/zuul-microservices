package com.bmrobin.zuul.filters.config

import com.bmrobin.zuul.filters.post.AddResponseHeaderFilter
import com.bmrobin.zuul.filters.pre.UppercaseRequestFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FiltersConfig{

    @Bean
    fun uppercaseRequestFilter(): UppercaseRequestFilter {
        return UppercaseRequestFilter()
    }

    @Bean
    fun addResponseHeaderFilter(): AddResponseHeaderFilter {
        return AddResponseHeaderFilter()
    }

}