package com.netflix.zuul.filters.pre

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.Debug
import com.netflix.zuul.context.RequestContext
import javax.servlet.http.HttpServletRequest

class DebugRequestFilter: ZuulFilter() {

    init {
        this.init()
    }

    fun init() {
        Debug.setDebugRequest(true)
    }

    override fun run(): Any {
        val req: HttpServletRequest = RequestContext.getCurrentContext().request
        Debug.addRequestDebug("REQUEST:: " + req.scheme + " " + req.remoteAddr + ":" + req.remotePort)
        Debug.addRequestDebug("REQUEST:: > " + req.method + " " + req.requestURI + " " + req.protocol)
        return Any()
    }

    override fun shouldFilter(): Boolean {
        return Debug.debugRequest()
    }

    override fun filterType(): String {
        return "pre"
    }

    override fun filterOrder(): Int {
        return 2
    }

}