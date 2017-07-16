/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package com.netflix.zuul.filters.route

import com.netflix.config.DynamicIntProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.constants.ZuulConstants
import com.netflix.zuul.context.Debug
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.util.HTTPRequestUtils
import org.apache.http.Header
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.SSLContexts
import org.apache.http.entity.InputStreamEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.DefaultRedirectStrategy
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicHttpRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLContext
import javax.servlet.http.HttpServletRequest
import kotlin.concurrent.timerTask

class SimpleHostRoutingFilter: ZuulFilter() {

    val LOG: Logger = LoggerFactory.getLogger(this.javaClass.name)

    val CONTENT_ENCODING: String = "Content-Encoding"
    val CLIENTLOADER: Runnable = Runnable { loadClient() }
    val CONNECTION_MANAGER_TIMER: Timer = Timer(true)
    val CLIENT: AtomicReference<CloseableHttpClient> = AtomicReference<CloseableHttpClient>(newClient())

    val SOCKET_TIMEOUT: DynamicIntProperty = DynamicPropertyFactory.getInstance().getIntProperty(
            ZuulConstants.ZUUL_HOST_SOCKET_TIMEOUT_MILLIS, 10000)

    val CONNECTION_TIMEOUT: DynamicIntProperty = DynamicPropertyFactory.getInstance().getIntProperty(
            ZuulConstants.ZUUL_HOST_CONNECT_TIMEOUT_MILLIS, 2000)

    init {
        // cleans expired connections at an interval
        SOCKET_TIMEOUT.addCallback(CLIENTLOADER)
        CONNECTION_TIMEOUT.addCallback(CLIENTLOADER)
        CONNECTION_MANAGER_TIMER.schedule(timerTask {
            try {
                val hc: HttpClient = CLIENT.get()
                hc.connectionManager.closeExpiredConnections()
            } catch (t: Throwable) {
                LOG.error("error closing expired connections")
            }
        }, 30000, 5000)
    }

    override fun filterType(): String {
        return "route"
    }

    override fun filterOrder(): Int {
        return 100
    }

    override fun shouldFilter(): Boolean {
        return RequestContext.getCurrentContext().routeHost != null &&
                RequestContext.getCurrentContext().sendZuulResponse()
    }

    override fun run(): Any {
        val request: HttpServletRequest = RequestContext.getCurrentContext().request
        val headers: Array<Header> = buildZuulRequestHeaders(request)
        val verb: String = getVerb(request)
        val requestEntity: InputStream = request.inputStream
        val httpclient: CloseableHttpClient = CLIENT.get()

        val uri = RequestContext.getCurrentContext().request.requestURI

        try {
            val response: HttpResponse = forward(httpclient, verb, uri, request, headers, requestEntity)
            Debug.addRequestDebug("ZUUL :: ${uri}")
            Debug.addRequestDebug("ZUUL :: Response statusLine > ${response.statusLine}")
            Debug.addRequestDebug("ZUUL :: Response code > ${response.statusLine.statusCode}")
            setResponse(response)
        } catch (e: Exception) {
            throw e;
        }
        return Any()
    }

    fun newConnectionManager(): HttpClientConnectionManager {
        val sslContext: SSLContext = SSLContexts.createSystemDefault()
        val socketFactoryRegistry: Registry<ConnectionSocketFactory> = RegistryBuilder.create<ConnectionSocketFactory>()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLConnectionSocketFactory(sslContext))
                .build()
        val connectionManager = PoolingHttpClientConnectionManager(socketFactoryRegistry)
        connectionManager.maxTotal = System.getProperty("zuul.max.host.connections", "200").toInt()
        connectionManager.defaultMaxPerRoute = System.getProperty("zuul.max.host.connections", "20").toInt()
        return connectionManager
    }

    fun loadClient() {
        val oldClient: CloseableHttpClient = CLIENT.get()
        CLIENT.set(newClient())
        CONNECTION_MANAGER_TIMER.schedule(timerTask {
            try {
                oldClient.close()
            } catch(t: Throwable) {
                LOG.error("error shutting down old connection manager")
            }
        }, 30000)
    }

    fun newClient(): CloseableHttpClient {
        val builder: HttpClientBuilder = HttpClientBuilder.create()
        builder.setConnectionManager(newConnectionManager())
        builder.setRetryHandler(DefaultHttpRequestRetryHandler(0, false))
        builder.setRedirectStrategy(DefaultRedirectStrategy())
        return builder.build()
    }

    fun debug(verb: String, uri: String, request: HttpServletRequest, headers: Array<Header>, requestEntity: InputStream): InputStream {
        if (Debug.debugRequest()) {
            Debug.addRequestDebug("ZUUL:: host=${RequestContext.getCurrentContext().routeHost}")
            headers.forEach {
                Debug.addRequestDebug("ZUUL:: Header > ${it.name}  ${it.value}")
            }
            val query: String = if (request.queryString != null) "?" + request.queryString else ""

            Debug.addRequestDebug("ZUUL:: > $verb  $uri$query HTTP/1.1")
            return debugRequestEntity(requestEntity)
        }
        return requestEntity
    }

    fun debugRequestEntity(inputStream: InputStream): InputStream {
        if (Debug.debugRequestHeadersOnly()) {
            return inputStream
        }

        val entity: String = inputStream.bufferedReader(Charsets.UTF_8).use { lines -> lines.readText() }
        Debug.addRequestDebug("ZUUL:: Entity > $entity")
        return ByteArrayInputStream(entity.toByteArray())
    }

    fun forward(httpClient: CloseableHttpClient, verb: String, uri: String, request: HttpServletRequest, headers: Array<Header>, requestEntity: InputStream): HttpResponse {
        val newRequestEntity = debug(verb, uri, request, headers, requestEntity)
        val httpHost: HttpHost = getHttpHost()
        val httpRequest: HttpRequest

        when (verb) {
            "POST" -> {
                httpRequest = HttpPost(uri + getQueryString())
                val entity: InputStreamEntity = InputStreamEntity(newRequestEntity)
                httpRequest.setEntity(entity)
            }
            "PUT" -> {
                httpRequest = HttpPut(uri + getQueryString())
                val entity: InputStreamEntity = InputStreamEntity(newRequestEntity, request.contentLengthLong)
                httpRequest.setEntity(entity)
            }
            else -> httpRequest = BasicHttpRequest(verb, uri + getQueryString())
        }

        try {
            httpRequest.setHeaders(headers)
            return forwardRequest(httpClient, httpHost, httpRequest)
        } finally {
            //httpClient.close();
        }
    }

    fun forwardRequest(httpclient: HttpClient, httpHost: HttpHost, httpRequest: HttpRequest): HttpResponse {
        return httpclient.execute(httpHost, httpRequest)
    }

    fun getQueryString(): String {
        val encoding: String = "UTF-8"
        val request: HttpServletRequest = RequestContext.getCurrentContext().request
        val currentQueryString: String = request.queryString

        if (currentQueryString == "") {
            return ""
        }

        var rebuiltQueryString: String = ""
        for (keyPair: String in currentQueryString.split("&")) {
            if (rebuiltQueryString.isNotEmpty()) {
                rebuiltQueryString += "&"
            }

            if (keyPair.contains("=")) {
                val list: List<String> = keyPair.split(Regex("="), 2)
                val name = list[0]
                var value = list[1]
                value = URLDecoder.decode(value, encoding)
                value = URI(null, null, null, value, null).toString().substring(1)
                value = value.replace("&", "%26", false)
                rebuiltQueryString = rebuiltQueryString + name + "=" + value
            } else {
                var value = URLDecoder.decode(keyPair, encoding)
                value = URI(null, null, null, value, null).toString().substring(1)
                rebuiltQueryString += value
            }
        }
        return "?" + rebuiltQueryString
    }

    fun getHttpHost(): HttpHost {
        val httpHost: HttpHost
        val host: URL = RequestContext.getCurrentContext().routeHost
        httpHost = HttpHost(host.host, host.port, host.protocol)
        return httpHost
    }

    fun getRequestBody(request: HttpServletRequest): InputStream? {
        try {
            return request.inputStream
        } catch (e: IOException) {
            LOG.warn(e.message)
            return null
        }
    }

    fun isValidHeader(name: String): Boolean {
        if (name.toLowerCase().contains("content-length")) {
            return false
        }

        if (name.toLowerCase() == "host") {
            return false
        }

        if (!RequestContext.getCurrentContext().responseGZipped) {
            if (name.toLowerCase().contains("accept-encoding")) {
                return false
            }
        }
        return true
    }

    fun buildZuulRequestHeaders(request: HttpServletRequest): Array<Header> {

        val headers: ArrayList<BasicHeader> = ArrayList()
        val headerNames: Enumeration<String> = request.headerNames
        while (headerNames.hasMoreElements()) {
            val name: String = headerNames.nextElement().toLowerCase()
            val value: String = request.getHeader(name)
            if (isValidHeader(name)) headers.add(BasicHeader(name, value))
        }

        val zuulRequestHeaders: Map<String, String> = RequestContext.getCurrentContext().zuulRequestHeaders
        zuulRequestHeaders.keys.forEach { it: String ->
            val name: String = it.toLowerCase()
            val h: BasicHeader? = headers.find { head: BasicHeader -> head.name == name }
            if (h != null) {
                headers.remove(h)
            }
            headers.add(BasicHeader(it, zuulRequestHeaders[it]))
        }

        if (RequestContext.getCurrentContext().responseGZipped) {
            headers.add(BasicHeader("accept-encoding", "deflate, gzip"))
        }

        return headers.toTypedArray()
    }


    fun getVerb(request: HttpServletRequest): String {
        return getVerb(request.method)
    }

    fun getVerb(sMethod: String): String {
        if (sMethod.toLowerCase() == "post") {
            return "POST"
        }
        if (sMethod.toLowerCase() == "put") {
            return "PUT"
        }
        if (sMethod.toLowerCase() == "delete") {
            return "DELETE"
        }
        if (sMethod.toLowerCase() == "options") {
            return "OPTIONS"
        }
        if (sMethod.toLowerCase() == "head") {
            return "HEAD"
        }
        return "GET"
    }

    fun setResponse(response: HttpResponse) {
        val context = RequestContext.getCurrentContext()

        RequestContext.getCurrentContext().set("hostZuulResponse", response)
        RequestContext.getCurrentContext().responseStatusCode = response.statusLine.statusCode
        RequestContext.getCurrentContext().responseDataStream = response.entity?.content

        var isOriginResponseGzipped: Boolean = false
        response.getHeaders(CONTENT_ENCODING).forEach { h: Header ->
            if (HTTPRequestUtils.getInstance().isGzipped(h.value)) {
                isOriginResponseGzipped = true
                return
            }
        }
        context.responseGZipped = isOriginResponseGzipped

        if (Debug.debugRequest()) {
            response.allHeaders?.forEach { header: Header ->
                if (isValidHeader(header)) {
                    RequestContext.getCurrentContext().addZuulResponseHeader(header.name, header.value)
                    Debug.addRequestDebug("ORIGIN_RESPONSE:: < ${header.name}, ${header.value}")
                }
            }
            context.responseDataStream = ByteArray(context.responseDataStream.available()).inputStream()
        } else {
            response.allHeaders?.forEach { header: Header ->
                val ctx: RequestContext = RequestContext.getCurrentContext()
                ctx.addOriginResponseHeader(header.name, header.value)

                if (header.name.toLowerCase() == "content-length") {
                    ctx.originContentLength = header.value.toLong()
                }

                if (isValidHeader(header)) {
                    ctx.addZuulResponseHeader(header.name, header.value)
                }
            }
        }

    }

    fun isValidHeader(header: Header): Boolean {
        when (header.name.toLowerCase()) {
            "connection",
            "content-length",
            "content-encoding",
            "server",
            "transfer-encoding" -> return false
            else -> return true
        }
    }
}

