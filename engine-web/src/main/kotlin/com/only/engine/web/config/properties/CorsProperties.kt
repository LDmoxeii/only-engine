package com.only.engine.web.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * CORS 跨域配置
 */
@ConfigurationProperties(prefix = "only.engine.web.cors")
data class CorsProperties(
    /** 是否启用 CORS，默认 true */
    var enable: Boolean = true,

    /** 允许的源模式，默认允许所有 */
    var allowedOriginPatterns: Set<String> = setOf("*"),

    /** 允许的请求头，默认允许所有 */
    var allowedHeaders: Set<String> = setOf("*"),

    /** 允许的请求方法，默认允许所有 */
    var allowedMethods: Set<String> = setOf("*"),

    /** 是否允许发送凭证，默认 true */
    var allowCredentials: Boolean = true,

    /** 预检请求缓存时间（秒），默认 1800 秒 */
    var maxAge: Long = 1800L,
)
