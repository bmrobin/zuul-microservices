package com.netflix.zuul.filters.pre

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

class PreDecorationFilter: ZuulFilter() {

    val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun filterOrder(): Int {
        return 1
    }

    override fun filterType(): String {
        return "pre"
    }

    override fun shouldFilter(): Boolean {
        return true
    }

    override fun run(): Any {
        // sets origin
//        RequestContext.getCurrentContext().routeHost = URL("http://httpbin.org")
        // set a custom header request to send to the origin
//        RequestContext.getCurrentContext().addOriginResponseHeader("cache-control", "max-age=3600")

        val request: HttpServletRequest = RequestContext.getCurrentContext().request


        log.info("${request.method} ${request.requestURI}")
        return Any()
    }
}