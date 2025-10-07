package com.only.engine.web.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Web模块配置属性
 * 提供类型安全的配置选项，支持JVM兼容性
 */
@ConfigurationProperties(prefix = "only.web")
data class WebProperties(

    /** 是否启用 Web 模块，默认 true */
    var enable: Boolean = true,

    /** 返回结果包装器配置 */
    var resultWrapper: ResultWrapperProperties = ResultWrapperProperties(),

    /** 全局异常处理器配置 */
    var exceptionHandler: ExceptionHandlerProperties = ExceptionHandlerProperties(),

    /** 审计处理器配置 */
    var audit: AuditProperties = AuditProperties(),

    /** 过滤器相关配置 */
    var filter: FilterProperties = FilterProperties(),

    /** 国际化相关配置 */
    var i18n: I18nProperties = I18nProperties(),

    /** CORS 跨域配置 */
    var cors: CorsProperties = CorsProperties(),

    /** 性能拦截器配置 */
    var performanceInterceptor: PerformanceInterceptorProperties = PerformanceInterceptorProperties(),
) {

    /**
     * 返回结果包装器配置
     */
    data class ResultWrapperProperties(
        /** 是否启用返回结果包装，默认 true */
        var enable: Boolean = true,
        /** 忽略包装的路径集合，默认空 */
        var ignorePaths: Set<String> = emptySet(),
        /** 忽略包装的Controller类名集合，默认空 */
        var ignoreControllers: Set<String> = emptySet()
    )

    /**
     * 全局异常处理器配置
     */
    data class ExceptionHandlerProperties(
        /** 是否启用全局异常拦截，默认 true */
        var enable: Boolean = true,
        /** 异常日志级别，默认 INFO */
        var logLevel: LogLevel = LogLevel.INFO,
        /** 是否打印异常堆栈，默认 true */
        var printStackTrace: Boolean = true,
        /** 敏感异常信息替换文本，默认为"系统繁忙，请稍后重试" */
        var sensitiveMessageReplacement: String = "系统繁忙，请稍后重试"
    )

    /**
     * 审计处理器配置
     */
    data class AuditProperties(
        /** 是否启用返回结果审计处理，默认 true */
        var enable: Boolean = true,
        /** 审计日志级别，默认 DEBUG */
        var logLevel: LogLevel = LogLevel.DEBUG
    )

    /**
     * 国际化配置
     */
    data class I18nProperties(
        /** 是否启用国际化，默认 false */
        var enable: Boolean = false,
        /** 默认语言，默认 zh_CN */
        var defaultLocale: String = "zh_CN",
        /** 消息文件基础名，默认 messages */
        var basename: String = "messages",
        /** 缓存时间（秒），默认 3600 */
        var cacheSeconds: Int = 3600
    )

    /**
     * 过滤器配置
     */
    data class FilterProperties(

        /** 健康检查过滤器配置 */
        var healthCheck: HealthCheckFilterProperties = HealthCheckFilterProperties(),

        /** 线程本地变量过滤器配置 */
        var threadLocal: ThreadLocalFilterProperties = ThreadLocalFilterProperties(),

        /** 用户登录过滤器配置 */
        var userLogin: UserLoginFilterProperties = UserLoginFilterProperties(),

        /** 请求体包装过滤器配置 */
        var requestBody: RequestBodyFilterProperties = RequestBodyFilterProperties(),

        /** XSS 过滤器配置 */
        var xss: XssFilterProperties = XssFilterProperties(),
    )

    /**
     * 健康检查过滤器配置
     */
    data class HealthCheckFilterProperties(
        /** 是否启用健康检查，默认 true */
        var enable: Boolean = true,
        /** 健康检查路径，默认 /health */
        var healthPath: String = "/health"
    )

    /**
     * 线程本地变量过滤器配置
     */
    data class ThreadLocalFilterProperties(
        /** 是否启用线程本地变量，默认 true */
        var enable: Boolean = true
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
        var whitelistUrls: List<String> = emptyList()
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

    /**
     * XSS 过滤器配置
     */
    data class XssFilterProperties(
        /** 是否启用 XSS 过滤，默认 false */
        var enable: Boolean = false,

        /** 排除路径，不进行 XSS 过滤 */
        var excludeUrls: MutableSet<String> = mutableSetOf(),
    )

    /**
     * CORS 跨域配置
     */
    data class CorsProperties(
        /** 是否启用 CORS，默认 true */
        var enable: Boolean = true,

        /** 允许的源模式，默认允许所有 */
        var allowedOriginPatterns: MutableSet<String> = mutableSetOf("*"),

        /** 允许的请求头，默认允许所有 */
        var allowedHeaders: MutableSet<String> = mutableSetOf("*"),

        /** 允许的请求方法，默认允许所有 */
        var allowedMethods: MutableSet<String> = mutableSetOf("*"),

        /** 是否允许发送凭证，默认 true */
        var allowCredentials: Boolean = true,

        /** 预检请求缓存时间（秒），默认 1800 秒 */
        var maxAge: Long = 1800L,
    )

    /**
     * 性能拦截器配置
     */
    data class PerformanceInterceptorProperties(
        /** 是否启用性能拦截器，默认 true */
        var enable: Boolean = true,

        /** 日志级别，默认 INFO */
        var logLevel: String = "INFO",

        /** 慢请求阈值（毫秒），默认 3000 毫秒 */
        var slowRequestThreshold: Long = 3000L,
    )

    /**
     * 日志级别枚举
     */
    enum class LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR;

        companion object {
            @JvmStatic
            fun fromString(level: String): LogLevel =
                valueOf(level.uppercase())
        }
    }
}
