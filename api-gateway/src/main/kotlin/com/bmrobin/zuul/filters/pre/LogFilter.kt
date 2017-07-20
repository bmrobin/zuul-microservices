package com.bmrobin.zuul.filters.pre

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.*
import javax.servlet.http.HttpServletRequest

class LogFilter : ZuulFilter() {

    val log: Logger = LoggerFactory.getLogger(LogFilter::class.java)

    override fun filterOrder(): Int {
        return PRE_DECORATION_FILTER_ORDER - 1
    }

    override fun filterType(): String {
        return PRE_TYPE
    }

    override fun shouldFilter(): Boolean {
        return true
    }

    override fun run(): Any? {
        val request: HttpServletRequest = RequestContext.getCurrentContext().request
        log.info("zuul received request :: ${request.method} ${request.requestURL}")
        return null
    }
}
