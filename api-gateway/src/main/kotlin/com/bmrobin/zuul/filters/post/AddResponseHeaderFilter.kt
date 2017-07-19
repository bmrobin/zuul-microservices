package com.bmrobin.zuul.filters.post

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import java.util.*
import javax.servlet.http.HttpServletResponse

class AddResponseHeaderFilter: ZuulFilter() {

    override fun filterType(): String {
        return "post"
    }

    override fun filterOrder(): Int {
        return 100
    }

    override fun shouldFilter(): Boolean {
        return true
    }

    override fun run(): Any? {
        val context: RequestContext = RequestContext.getCurrentContext()
        val response: HttpServletResponse = context.response
        val rand: Random = Random()
        response.addHeader("X-BMROBIN", rand.nextInt().toString())
        return null
    }
}