package com.only.engine.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.web")
data class WebProperties(

    /** 是否启用 Web 模块，默认 true */
    var enable: Boolean = true,

    /** 是否启用返回结果审计处理，默认 true */
    var enableAuditHandler: Boolean = true,

    /** 是否启用返回结果包装，默认 true */
    var enableResultWrapper: Boolean = true,

    /** 是否启用全局异常拦截，默认 true */
    var enableExceptionHandler: Boolean = true,

    /** 过滤器相关配置 */
    var filter: FilterProperties = FilterProperties(),

    /** 国际化相关配置 */
    var i18n: I18n = I18n()
) {

    /** 国际化配置 */
    data class I18n(
        /** 是否启用国际化，默认 false */
        var enable: Boolean = false
    )

    /** 过滤器配置 */
    data class FilterProperties(

        /** 是否启用健康检查，默认 true */
        var enableHealthCheck: Boolean = true,

        /** 是否启用线程本地变量，默认 true */
        var enableThreadLocal: Boolean = true,

        /** 是否启用登录拦截，默认 false */
        var enableUserLogin: Boolean = false,

        /** 不走拦截器的路径集合，默认空 */
        var skipPath: MutableSet<String> = mutableSetOf(),

        /** 请求白名单 URL 列表，默认空 */
        var externalUrlList: List<String> = emptyList(),

        /** 请求体包装过滤器配置 */
        var requestBody: RequestBodyFilterProperties = RequestBodyFilterProperties(),

        /** 请求追踪过滤器配置 */
        var track: TrackFilterProperties = TrackFilterProperties()
    )

    /** 请求追踪过滤器配置 */
    data class TrackFilterProperties(

        /** 是否启用请求打印，默认 true */
        var enable: Boolean = true,

        /** 打印类型（1=console，2=log），默认 2 */
        var printType: Int = 2,

        /** 是否打印请求体，默认 true */
        var printRequestBody: Boolean = true,

        /** 是否打印响应体，默认 true */
        var printResponseBody: Boolean = true,

        /** 敏感字段（默认空） */
        var sensitiveKeys: Set<String> = emptySet(),

        /** 过滤 URI 集合（默认空） */
        var filterUris: Set<String> = emptySet()
    )

    /** 请求体包装过滤器配置 */
    data class RequestBodyFilterProperties(

        /** 是否启用请求体包装，默认 true */
        var enable: Boolean = true,

        /** 不需要包装的 URI 路径集合（默认空） */
        var filterUris: Set<String> = emptySet()
    )
}
