package com.only.engine.spi.interceptor

import org.springframework.web.servlet.HandlerInterceptor

/**
 * 标记接口，用于标识需要被 SecurityAutoConfiguration 自动注册的拦截器
 * 实现此接口的拦截器会被自动收集并注册到 Spring MVC 中
 */
interface SecurityInterceptor : HandlerInterceptor
