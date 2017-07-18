package com.bmrobin.zuul

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.zuul.EnableZuulProxy

@SpringBootApplication
@EnableZuulProxy
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}