package com.only.engine.web.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 性能拦截器配置
 */
@ConfigurationProperties(prefix = "only.engine.web.performance-interceptor")
data class PerformanceInterceptorProperties(
    /** 是否启用性能拦截器，默认 true */
    var enable: Boolean = true,

    /** 日志级别，默认 INFO */
    var logLevel: String = "INFO",

    /** 慢请求阈值（毫秒），默认 3000 毫秒 */
    var slowRequestThreshold: Long = 3000L,
)
