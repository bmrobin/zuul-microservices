package com.bmrobin.zuul.filters.pre

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.*
import javax.servlet.http.HttpServletRequest

class ServiceRequestFilter: ZuulFilter() {

    val log: Logger = LoggerFactory.getLogger(ServiceRequestFilter::class.java)

    override fun filterOrder(): Int {
        return PRE_DECORATION_FILTER_ORDER - 1
    }

    override fun filterType(): String {
        return PRE_TYPE
    }

    override fun shouldFilter(): Boolean {
        return RequestContext.getCurrentContext().request.getParameter("service") != null
    }

    override fun run(): Any? {
        val request: HttpServletRequest = RequestContext.getCurrentContext().request
        val serviceId: String = request.getParameter("service")
        RequestContext.getCurrentContext().put(SERVICE_ID_KEY, serviceId)
        log.info("request received :: ${request.method} ${request.requestURL}")
        return null
    }
}
