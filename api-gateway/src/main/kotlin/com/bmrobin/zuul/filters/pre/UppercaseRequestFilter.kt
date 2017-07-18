package com.bmrobin.zuul.filters.pre

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest

class UppercaseRequestFilter: ZuulFilter() {

    val log: Logger = LoggerFactory.getLogger(UppercaseRequestFilter::class.java)

    override fun filterOrder(): Int {
        return 10
    }

    override fun filterType(): String {
        return "pre"
    }

    override fun shouldFilter(): Boolean {
        return true
    }

    override fun run(): Any {
        val context: RequestContext = RequestContext.getCurrentContext()
        val request: HttpServletRequest = context.request
        var inputStream: InputStream? = context.get("requestEntity") as InputStream?

        if (inputStream == null) {
            inputStream = request.inputStream
        }

        log.info("${request.method} ${request.requestURI}")

        var requestBody: String = StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"))
        requestBody = requestBody.toUpperCase()
        context.set("requestEntity", ByteArrayInputStream(requestBody.toByteArray()))

        return Any()
    }
}