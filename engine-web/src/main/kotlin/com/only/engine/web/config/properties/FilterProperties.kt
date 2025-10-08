package com.only.engine.web.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 过滤器配置
 */
@ConfigurationProperties(prefix = "only.engine.web.filter")
data class FilterProperties(

    /** 健康检查过滤器配置 */
    var healthCheck: HealthCheckFilterProperties = HealthCheckFilterProperties(),

    /** 线程本地变量过滤器配置 */
    var threadLocal: ThreadLocalFilterProperties = ThreadLocalFilterProperties(),

    /** 用户登录过滤器配置 */
    var userLogin: UserLoginFilterProperties = UserLoginFilterProperties(),

    /** 请求体包装过滤器配置 */
    var requestBody: RequestBodyFilterProperties = RequestBodyFilterProperties(),

    ) {
    /**
     * 健康检查过滤器配置
     */
    data class HealthCheckFilterProperties(
        /** 是否启用健康检查，默认 true */
        var enable: Boolean = true,
        /** 健康检查路径，默认 /health */
        var healthPath: String = "/health",
    )

    /**
     * 线程本地变量过滤器配置
     */
    data class ThreadLocalFilterProperties(
        /** 是否启用线程本地变量，默认 true */
        var enable: Boolean = true,
    )

    /**
     * 用户登录过滤器配置
     */
    data class UserLoginFilterProperties(
        /** 是否启用登录拦截，默认 false */
        var enable: Boolean = false,
        /** 不走拦截器的路径集合，默认空 */
        var skipPaths: MutableSet<String> = mutableSetOf(),
        /** 请求白名单 URL 列表，默认空 */
        var whitelistUrls: List<String> = emptyList(),
    )

    /**
     * 请求体包装过滤器配置
     */
    data class RequestBodyFilterProperties(

        /** 是否启用请求体包装，默认 true */
        var enable: Boolean = true,

        /** 不需要包装的 URI 路径集合（默认空） */
        var filterUris: Set<String> = emptySet(),

        /** 不需要包装的Content-Type集合（默认空） */
        var filterContentTypes: Set<String> = emptySet(),
    )
}
